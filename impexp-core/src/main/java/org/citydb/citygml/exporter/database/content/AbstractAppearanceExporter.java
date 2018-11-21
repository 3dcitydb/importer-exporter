/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.AppearanceFilterBuilder;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.ColorPlusOpacity;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TextureType;
import org.citygml4j.model.citygml.appearance.WrapMode;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.core.TransformationMatrix2x2;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractAppearanceExporter extends AbstractTypeExporter {
	private final Logger log = Logger.getInstance();

	protected PreparedStatement ps;
	private DBTextureParam textureParamExporter;
	private AttributeValueSplitter valueSplitter;

	private boolean exportTextureImage;
	private boolean uniqueFileNames;
	private String textureURI;
	private boolean useBuckets;
	private int noOfBuckets;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private String gmlIdPrefix;
	private String separator;

	private HashSet<Long> texImageIds;
	private List<PlaceHolder<?>> themes;
	private Set<String> appearanceADEHookTables;
	private Set<String> surfaceDataADEHookTables;

	protected AbstractAppearanceExporter(boolean isGlobal, Connection connection, Query query, CacheTable cacheTable, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		super(exporter);

		texImageIds = new HashSet<>();
		themes = Collections.emptyList();

		exportTextureImage = config.getProject().getExporter().getAppearances().isSetExportTextureFiles();
		uniqueFileNames = config.getProject().getExporter().getAppearances().isSetUniqueTextureFileNames();
		noOfBuckets = config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets(); 
		useBuckets = config.getProject().getExporter().getAppearances().getTexturePath().isUseBuckets() && noOfBuckets > 0;

		textureURI = config.getInternal().getExportTextureURI();
		separator = new File(textureURI).isAbsolute() ? File.separator : "/";
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		String getLength = exporter.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("blob.get_length");

		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
		}

		table = new Table(TableEnum.APPEARANCE.getName(), schema);
		Table appearToSurfaceData = new Table(TableEnum.APPEAR_TO_SURFACE_DATA.getName(), schema);
		Table surfaceData = new Table(TableEnum.SURFACE_DATA.getName(), schema);
		Table texImage = new Table(TableEnum.TEX_IMAGE.getName(), schema);

		select = new Select().addProjection(table.getColumn("id"), table.getColumn("gmlid"), table.getColumn("name"),table.getColumn("name_codespace"), table.getColumn("description"), table.getColumn("theme"),
				surfaceData.getColumn("id"), surfaceData.getColumn("objectclass_id"), surfaceData.getColumn("gmlid"),surfaceData.getColumn("name"), surfaceData.getColumn("name_codespace"), surfaceData.getColumn("description"),
				surfaceData.getColumn("is_front"), surfaceData.getColumn("x3d_shininess"), surfaceData.getColumn("x3d_transparency"), surfaceData.getColumn("x3d_ambient_intensity"),
				surfaceData.getColumn("x3d_specular_color"), surfaceData.getColumn("x3d_diffuse_color"), surfaceData.getColumn("x3d_emissive_color"), surfaceData.getColumn("x3d_is_smooth"),
				surfaceData.getColumn("tex_image_id"), new Function("coalesce", new Function(getLength, texImage.getColumn("tex_image_data")), new IntegerLiteral(0)),
				texImage.getColumn("tex_image_uri"), texImage.getColumn("tex_mime_type"), texImage.getColumn("tex_mime_type_codespace"),
				new Function("lower", surfaceData.getColumn("tex_texture_type")), new Function("lower", surfaceData.getColumn("tex_wrap_mode")),
				surfaceData.getColumn("tex_border_color"), surfaceData.getColumn("gt_prefer_worldfile"), surfaceData.getColumn("gt_orientation"),
				exporter.getGeometryColumn(surfaceData.getColumn("gt_reference_point")))
				.addJoin(JoinFactory.inner(appearToSurfaceData, "appearance_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
				.addJoin(JoinFactory.inner(surfaceData, "id", ComparisonName.EQUAL_TO, appearToSurfaceData.getColumn("surface_data_id")))
				.addJoin(JoinFactory.left(texImage, "id", ComparisonName.EQUAL_TO, surfaceData.getColumn("tex_image_id")));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			appearanceADEHookTables = exporter.getADEHookTables(TableEnum.APPEARANCE);
			surfaceDataADEHookTables = exporter.getADEHookTables(TableEnum.SURFACE_DATA);			
			if (appearanceADEHookTables != null) addJoinsToADEHookTables(appearanceADEHookTables, table);
			if (surfaceDataADEHookTables != null) addJoinsToADEHookTables(surfaceDataADEHookTables, surfaceData);
		}

		if (isGlobal)
			select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
		else {
			select.addSelection(ComparisonFactory.equalTo(table.getColumn("cityobject_id"), new PlaceHolder<>()));
			if (query.isSetAppearanceFilter()) {
				try {
					PredicateToken predicate = new AppearanceFilterBuilder(exporter.getDatabaseAdapter()).buildAppearanceFilter(query.getAppearanceFilter(), table.getColumn("theme"));
					select.addSelection(predicate);

					themes = new ArrayList<>();
					predicate.getInvolvedPlaceHolders(themes);
				} catch (QueryBuildException e) {
					throw new CityGMLExportException("Failed to build appearance filter.", e); 
				}
			}
		}			

		ps = connection.prepareStatement(select.toString());

		textureParamExporter = new DBTextureParam(isGlobal, connection, cacheTable, config, exporter);
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected void clearTextureImageCache() {
		texImageIds.clear();
	}

	protected List<PlaceHolder<?>> getThemeTokens() {
		return themes;
	}

	protected void getAppearanceProperties(Appearance appearance, long appearanceId, ResultSet rs) throws CityGMLExportException, SQLException {
		appearance.setId(rs.getString(2));

		for (SplitValue splitValue : valueSplitter.split(rs.getString(3), rs.getString(4))) {
			Code name = new Code(splitValue.result(0));
			name.setCodeSpace(splitValue.result(1));
			appearance.addName(name);
		}

		String description = rs.getString(5);
		if (!rs.wasNull())
			appearance.setDescription(new StringOrRef(description));

		appearance.setTheme(rs.getString(6));

		// delegate export of generic ADE properties
		if (appearanceADEHookTables != null) {
			List<String> adeHookTables = retrieveADEHookTables(appearanceADEHookTables, rs);
			if (adeHookTables != null) {
				FeatureType featureType = exporter.getFeatureType(appearance);
				exporter.delegateToADEExporter(adeHookTables, appearance, appearanceId, featureType, exporter.getProjectionFilter(featureType));
			}
		}
	}

	protected void addSurfaceData(Appearance appearance, ResultSet rs, boolean lazyExport) throws CityGMLExportException, SQLException {
		long surfaceDataId = rs.getLong(7);
		if (rs.wasNull())
			return;

		int objectClassId = rs.getInt(8);
		AbstractSurfaceData surfaceData = exporter.createObject(objectClassId, AbstractSurfaceData.class);
		if (surfaceData == null) {
			exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, surfaceDataId) + " as surface data object.");
			return;
		}

		String gmlId = rs.getString(9);
		if (gmlId != null) {
			// process xlink
			if (exporter.lookupAndPutObjectUID(gmlId, surfaceDataId, objectClassId)) {
				if (useXLink) {
					SurfaceDataProperty surfaceDataProperty = new SurfaceDataProperty();
					surfaceDataProperty.setHref("#" + gmlId);
					appearance.addSurfaceDataMember(surfaceDataProperty);
					return;
				} else {
					String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
					if (appendOldGmlId)
						newGmlId = newGmlId + "-" + gmlId;

					gmlId = newGmlId;
				}
			}

			surfaceData.setId(gmlId);
		}

		// retrieve targets
		boolean hasTargets = textureParamExporter.doExport(surfaceData, surfaceDataId);
		if (!hasTargets)
			return;

		for (SplitValue splitValue : valueSplitter.split(rs.getString(10), rs.getString(11))) {
			Code name = new Code(splitValue.result(0));
			name.setCodeSpace(splitValue.result(1));
			surfaceData.addName(name);
		}

		String description = rs.getString(12);
		if (!rs.wasNull())
			surfaceData.setDescription(new StringOrRef(description));

		int isFront = rs.getInt(13);
		if (!rs.wasNull() && isFront == 0)
			surfaceData.setIsFront(false);

		if (surfaceData instanceof X3DMaterial) {
			X3DMaterial material = (X3DMaterial)surfaceData;

			double shininess = rs.getDouble(14);
			if (!rs.wasNull())
				material.setShininess(shininess);

			double transparency = rs.getDouble(15);
			if (!rs.wasNull())
				material.setTransparency(transparency);

			double ambientIntensity = rs.getDouble(16);
			if (!rs.wasNull())
				material.setAmbientIntensity(ambientIntensity);

			for (int i = 0; i < 3; i++) {
				String colorString = rs.getString(17 + i);
				if (colorString != null) {
					List<Double> colorList = valueSplitter.splitDoubleList(colorString);
					if (colorList.size() >= 3) {
						Color color = new Color(colorList.get(0), colorList.get(1), colorList.get(2));

						switch (i) {
						case 0:
							material.setSpecularColor(color);
							break;
						case 1:
							material.setDiffuseColor(color);
							break;
						case 2:
							material.setEmissiveColor(color);
							break;
						}
					}
				}
			}

			int isSmooth = rs.getInt(20);
			if (!rs.wasNull() && isSmooth == 1)
				material.setIsSmooth(true);
		}

		else if (surfaceData instanceof AbstractTexture) {
			AbstractTexture abstractTexture = (AbstractTexture)surfaceData;

			long texImageId = rs.getLong(21);			
			if (texImageId != 0) {
				long dbImageSize = rs.getLong(22);

				String imageURI = rs.getString(23);
				if (uniqueFileNames) {
					String extension = Util.getFileExtension(imageURI);
					imageURI = CoreConstants.UNIQUE_TEXTURE_FILENAME_PREFIX + texImageId + (!extension.isEmpty() ? "." + extension : "");
				}

				String fileName = new File(imageURI).getName();
				if (useBuckets)
					fileName = String.valueOf(Math.abs(texImageId % noOfBuckets + 1)) + separator + fileName;

				abstractTexture.setImageURI(textureURI != null ? textureURI + separator + fileName : fileName);

				// export texture image from database
				if (exportTextureImage && (uniqueFileNames || !texImageIds.contains(texImageId))) {
					if (dbImageSize > 0) {
						DBXlinkTextureFile xlink = new DBXlinkTextureFile(
								texImageId,
								fileName);

						if (!lazyExport)
							exporter.propagateXlink(xlink);
						else
							abstractTexture.setLocalProperty(CoreConstants.TEXTURE_IMAGE_XLINK, xlink);

					} else {
						log.warn(exporter.getObjectSignature(exporter.getFeatureType(objectClassId), surfaceDataId) +
								": Skipping 0 byte texture file '" + imageURI + "'.");
					}

					if (!uniqueFileNames)
						texImageIds.add(texImageId);
				}
			}

			String mimeType = rs.getString(24);
			if (!rs.wasNull()) {
				Code code = new Code(mimeType);
				code.setCodeSpace(rs.getString(25));
				abstractTexture.setMimeType(code);
			}

			String textureType = rs.getString(26);
			if (textureType != null)
				abstractTexture.setTextureType(TextureType.fromValue(textureType));

			String wrapMode = rs.getString(27);
			if (wrapMode != null)
				abstractTexture.setWrapMode(WrapMode.fromValue(wrapMode));

			String borderColorString = rs.getString(28);
			if (borderColorString != null) {
				List<Double> colorList = valueSplitter.splitDoubleList(borderColorString);
				if (colorList.size() >= 4) {
					ColorPlusOpacity borderColor = new ColorPlusOpacity(colorList.get(0), colorList.get(1), colorList.get(2), colorList.get(3));
					abstractTexture.setBorderColor(borderColor);
				}
			}
		}

		if (surfaceData instanceof GeoreferencedTexture) {
			GeoreferencedTexture georeferencedTexture = (GeoreferencedTexture)surfaceData;

			int preferWorldFile = rs.getInt(29);
			if (!rs.wasNull() && preferWorldFile == 0)
				georeferencedTexture.setPreferWorldFile(false);

			String orientationString = rs.getString(30);
			if (orientationString != null) {
				List<Double> m = valueSplitter.splitDoubleList(orientationString);
				if (m.size() >= 4) {
					Matrix matrix = new Matrix(2, 2);
					matrix.setMatrix(m.subList(0, 4));
					georeferencedTexture.setOrientation(new TransformationMatrix2x2(matrix));
				}
			}

			Object referencePointObj = rs.getObject(31);
			if (!rs.wasNull()) {
				GeometryObject pointObj = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);
				if (pointObj != null) {
					double[] point = pointObj.getCoordinates(0);
					Point referencePoint = new Point();

					List<Double> value = new ArrayList<>();
					value.add(point[0]);
					value.add(point[1]);

					DirectPosition pos = new DirectPosition();
					pos.setValue(value);
					pos.setSrsDimension(2);
					referencePoint.setPos(pos);

					PointProperty pointProperty = new PointProperty(referencePoint);
					georeferencedTexture.setReferencePoint(pointProperty);
				}
			}
		}

		// delegate export of generic ADE properties
		if (surfaceDataADEHookTables != null) {
			List<String> adeHookTables = retrieveADEHookTables(surfaceDataADEHookTables, rs);
			if (adeHookTables != null) {
				FeatureType featureType = exporter.getFeatureType(surfaceData);
				exporter.delegateToADEExporter(adeHookTables, surfaceData, surfaceDataId, featureType, exporter.getProjectionFilter(featureType));
			}
		}

		// finally add surface data to appearance
		SurfaceDataProperty surfaceDataProperty = new SurfaceDataProperty(surfaceData);
		appearance.addSurfaceDataMember(surfaceDataProperty);
	}

	@Override
	public void close() throws SQLException {
		ps.close();
		textureParamExporter.close();
	}
}
