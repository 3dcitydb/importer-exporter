/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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

import org.citydb.citygml.common.cache.CacheTable;
import org.citydb.citygml.common.xlink.DBXlink;
import org.citydb.citygml.common.xlink.DBXlinkTextureFile;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.log.Logger;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
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
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.TextureType;
import org.citygml4j.model.citygml.appearance.WorldToTexture;
import org.citygml4j.model.citygml.appearance.WrapMode;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.core.TransformationMatrix2x2;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.util.walker.FeatureWalker;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractAppearanceExporter extends AbstractTypeExporter {
	private final Logger log = Logger.getInstance();
	private final AttributeValueSplitter valueSplitter;
	private final boolean lazyTextureImageExport;
	private final boolean exportTextureImage;
	private final boolean uniqueFileNames;
	private final String textureURI;
	private final boolean useBuckets;
	private final int noOfBuckets;
	private final boolean useXLink;
	private final String separator;
	private final HashSet<Long> texImageIds;

	private final List<Table> appearanceADEHookTables;
	private final List<Table> surfaceDataADEHookTables;

	protected AbstractAppearanceExporter(boolean isGlobal, CacheTable cacheTable, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		super(exporter);

		texImageIds = new HashSet<>();
		lazyTextureImageExport = !isGlobal && exporter.isLazyTextureExport();
		exportTextureImage = exporter.getExportConfig().getAppearances().isSetExportTextureFiles();
		uniqueFileNames = exporter.getExportConfig().getAppearances().isSetUniqueTextureFileNames();
		noOfBuckets = exporter.getExportConfig().getAppearances().getTexturePath().getNoOfBuckets();
		useBuckets = exporter.getExportConfig().getAppearances().getTexturePath().isUseBuckets() && noOfBuckets > 0;

		textureURI = exporter.getInternalConfig().getExportTextureURI();
		separator = new File(textureURI).isAbsolute() ? File.separator : "/";
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		String getLength = exporter.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("blob.get_length");
		useXLink = exporter.getInternalConfig().isExportFeatureReferences();

		table = new Table(TableEnum.APPEARANCE.getName(), schema);
		Table appearToSurfaceData = new Table(TableEnum.APPEAR_TO_SURFACE_DATA.getName(), schema);
		Table surfaceData = new Table(TableEnum.SURFACE_DATA.getName(), schema);
		Table texImage = new Table(TableEnum.TEX_IMAGE.getName(), schema);
		Table textureParam = new Table(TableEnum.TEXTUREPARAM.getName(), schema);
		Table surfaceGeometry = new Table(TableEnum.SURFACE_GEOMETRY.getName(), schema);

		select = new Select().addProjection(table.getColumn("id"), table.getColumn("gmlid"), table.getColumn("name"), table.getColumn("name_codespace"), table.getColumn("description"), table.getColumn("theme"),
				surfaceData.getColumn("id"), surfaceData.getColumn("objectclass_id"), surfaceData.getColumn("gmlid"), surfaceData.getColumn("name"), surfaceData.getColumn("name_codespace"), surfaceData.getColumn("description"),
				surfaceData.getColumn("is_front"), surfaceData.getColumn("x3d_shininess"), surfaceData.getColumn("x3d_transparency"), surfaceData.getColumn("x3d_ambient_intensity"),
				surfaceData.getColumn("x3d_specular_color"), surfaceData.getColumn("x3d_diffuse_color"), surfaceData.getColumn("x3d_emissive_color"), surfaceData.getColumn("x3d_is_smooth"),
				surfaceData.getColumn("tex_image_id"), new Function("coalesce", new Function(getLength, texImage.getColumn("tex_image_data")), new IntegerLiteral(-1)),
				texImage.getColumn("tex_image_uri"), texImage.getColumn("tex_mime_type"), texImage.getColumn("tex_mime_type_codespace"),
				new Function("lower", surfaceData.getColumn("tex_texture_type")), new Function("lower", surfaceData.getColumn("tex_wrap_mode")),
				surfaceData.getColumn("tex_border_color"), surfaceData.getColumn("gt_prefer_worldfile"), surfaceData.getColumn("gt_orientation"),
				exporter.getGeometryColumn(surfaceData.getColumn("gt_reference_point")),
				textureParam.getColumn("world_to_texture"), textureParam.getColumn("texture_coordinates"),
				surfaceGeometry.getColumn("gmlid"), surfaceGeometry.getColumn("is_reverse"))
				.addJoin(JoinFactory.inner(appearToSurfaceData, "appearance_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
				.addJoin(JoinFactory.inner(surfaceData, "id", ComparisonName.EQUAL_TO, appearToSurfaceData.getColumn("surface_data_id")))
				.addJoin(JoinFactory.left(texImage, "id", ComparisonName.EQUAL_TO, surfaceData.getColumn("tex_image_id")))
				.addJoin(JoinFactory.left(textureParam, "surface_data_id", ComparisonName.EQUAL_TO, surfaceData.getColumn("id")));
		appearanceADEHookTables = addJoinsToADEHookTables(TableEnum.APPEARANCE, table);
		surfaceDataADEHookTables = addJoinsToADEHookTables(TableEnum.SURFACE_DATA, surfaceData);

		if (isGlobal) {
			Table tmp = new Table(cacheTable.getTableName());
			select.addJoin(JoinFactory.inner(tmp, "id", ComparisonName.EQUAL_TO, textureParam.getColumn("surface_geometry_id")))
					.addJoin(JoinFactory.inner(surfaceGeometry, "id", ComparisonName.EQUAL_TO, tmp.getColumn("id")))
					.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
		} else
			select.addJoin(JoinFactory.inner(surfaceGeometry, "id", ComparisonName.EQUAL_TO, textureParam.getColumn("surface_geometry_id")));

		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected void triggerLazyTextureExport(AbstractFeature feature) {
		if (exporter.isLazyTextureExport()) {
			feature.accept(new FeatureWalker() {
				@Override
				public void visit(AbstractTexture texture) {
					if (texture.hasLocalProperty(CoreConstants.TEXTURE_IMAGE_XLINK))
						exporter.propagateXlink((DBXlink) texture.getLocalProperty(CoreConstants.TEXTURE_IMAGE_XLINK));
				}
			});
		}
	}

	protected Map<Long, Appearance> doExport(ResultSet rs) throws CityGMLExportException, SQLException {
		// clear texture image cache
		texImageIds.clear();

		long currentAppearanceId = 0;
		Appearance appearance = null;
		Map<Long, Appearance> appearances = new HashMap<>();

		long currentSurfaceDataId = 0;
		SurfaceDataProperty surfaceDataProperty = null;
		SurfaceDataProperty empty = new SurfaceDataProperty();
		Map<String, SurfaceDataProperty> surfaceDataProperties = new HashMap<>();

		while (rs.next()) {
			long appearanceId = rs.getLong(1);

			if (appearanceId != currentAppearanceId || appearance == null) {
				currentAppearanceId = appearanceId;
				currentSurfaceDataId = 0;

				appearance = appearances.get(appearanceId);
				if (appearance == null) {
					appearance = getAppearance(appearanceId, rs);
					appearances.put(appearanceId, appearance);
				}
			}

			// get surface data
			long surfaceDataId = rs.getLong(7);
			if (rs.wasNull())
				continue;

			if (surfaceDataId != currentSurfaceDataId || surfaceDataProperty == null) {
				currentSurfaceDataId = surfaceDataId;
				String key = currentAppearanceId + "_" + surfaceDataId;

				surfaceDataProperty = surfaceDataProperties.get(key);
				if (surfaceDataProperty == null) {
					surfaceDataProperty = getSurfaceData(surfaceDataId, rs, empty);
					surfaceDataProperties.put(key, surfaceDataProperty);

					// add surface data to appearance
					if (surfaceDataProperty != empty)
						appearance.getSurfaceDataMember().add(surfaceDataProperty);
				}
			}

			if (surfaceDataProperty.isSetSurfaceData())
				addTarget(surfaceDataProperty.getSurfaceData(), rs);
		}

		appearances.values().removeIf(v -> v.getSurfaceDataMember().isEmpty());
		return appearances;
	}

	private Appearance getAppearance(long appearanceId, ResultSet rs) throws CityGMLExportException, SQLException {
		Appearance appearance = new Appearance();
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

		return appearance;
	}

	private SurfaceDataProperty getSurfaceData(long surfaceDataId, ResultSet rs, SurfaceDataProperty empty) throws CityGMLExportException, SQLException {
		int objectClassId = rs.getInt(8);
		String gmlId = rs.getString(9);

		boolean generateNewGmlId = false;
		if (gmlId != null) {
			// process xlink
			if (exporter.lookupAndPutObjectId(gmlId, surfaceDataId, objectClassId)) {
				if (useXLink)
					return new SurfaceDataProperty("#" + gmlId);
				else {
					generateNewGmlId = true;
				}
			}
		}

		AbstractSurfaceData surfaceData = exporter.createObject(objectClassId, AbstractSurfaceData.class);
		if (surfaceData == null) {
			exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, surfaceDataId) + " as surface data object.");
			return empty;
		}

		if (generateNewGmlId) {
			gmlId = exporter.generateFeatureGmlId(surfaceData, gmlId);
		}

		surfaceData.setId(gmlId);

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
				long imageSize = rs.getLong(22);

				String imageURI = rs.getString(23);
				if (uniqueFileNames) {
					String extension = Util.getFileExtension(imageURI);
					imageURI = CoreConstants.UNIQUE_TEXTURE_FILENAME_PREFIX + texImageId + (!extension.isEmpty() ? "." + extension : "");
				}

				String fileName = new File(imageURI).getName();
				if (useBuckets)
					fileName = Math.abs(texImageId % noOfBuckets + 1) + separator + fileName;

				abstractTexture.setImageURI(textureURI != null ? textureURI + separator + fileName : fileName);

				// export texture image from database
				if (exportTextureImage && (uniqueFileNames || !texImageIds.contains(texImageId))) {
					if (imageSize > 0) {
						DBXlinkTextureFile xlink = new DBXlinkTextureFile(
								texImageId,
								fileName);

						if (!lazyTextureImageExport)
							exporter.propagateXlink(xlink);
						else
							abstractTexture.setLocalProperty(CoreConstants.TEXTURE_IMAGE_XLINK, xlink);

					} else if (imageSize == 0) {
						log.warn(exporter.getObjectSignature(objectClassId, surfaceDataId) +
								": Skipping 0 byte texture file '" + imageURI + "'.");
					}

					if (!uniqueFileNames)
						texImageIds.add(texImageId);
				}
			} else {
				// skip texture elements lacking a texture image
				return empty;
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

		return new SurfaceDataProperty(surfaceData);
	}

	private void addTarget(AbstractSurfaceData surfaceData, ResultSet rs) throws SQLException {
		String target = rs.getString(34);
		if (target == null || target.length() == 0)
			return;

		target = "#" + target;

		if (surfaceData instanceof X3DMaterial) {
			((X3DMaterial) surfaceData).addTarget(target);
		} else if (surfaceData instanceof GeoreferencedTexture) {
			((GeoreferencedTexture) surfaceData).addTarget(target);
		} else if (surfaceData instanceof ParameterizedTexture) {
			ParameterizedTexture parameterizedTexture = (ParameterizedTexture) surfaceData;

			String worldToTexture = rs.getString(32);
			Object texCoordsObj = rs.getObject(33);

			if (texCoordsObj != null) {
				GeometryObject texCoords = exporter.getDatabaseAdapter().getGeometryConverter().getPolygon(texCoordsObj);
				if (texCoords != null && texCoords.getDimension() == 2) {
					TextureAssociation textureAssociation = new TextureAssociation();
					textureAssociation.setUri(target);
					TexCoordList texCoordList = new TexCoordList();

					for (int i = 0; i < texCoords.getNumElements(); i++) {
						double[] coordinates = texCoords.getCoordinates(i);

						// reverse order of texture coordinates if necessary
						boolean isReverse = rs.getBoolean(35);
						if (isReverse) {
							for (int lower = 0, upper = coordinates.length - 2; lower < upper; lower += 2, upper -= 2) {
								double x = coordinates[lower];
								double y = coordinates[lower + 1];
								coordinates[lower] = coordinates[upper];
								coordinates[lower + 1] = coordinates[upper + 1];
								coordinates[upper] = x;
								coordinates[upper + 1] = y;
							}
						}

						List<Double> value = new ArrayList<>(coordinates.length);
						for (double coordinate : coordinates)
							value.add(coordinate);

						TextureCoordinates textureCoordinates = new TextureCoordinates();
						textureCoordinates.setValue(value);
						textureCoordinates.setRing(target + '_' + i + '_');

						texCoordList.addTextureCoordinates(textureCoordinates);
					}

					textureAssociation.setTextureParameterization(texCoordList);
					parameterizedTexture.addTarget(textureAssociation);
				}
			} else if (worldToTexture != null) {
				TextureAssociation textureAssociation = new TextureAssociation();
				textureAssociation.setUri(target);

				List<Double> m = valueSplitter.splitDoubleList(worldToTexture);
				if (m.size() >= 12) {
					Matrix matrix = new Matrix(3, 4);
					matrix.setMatrix(m.subList(0, 12));

					WorldToTexture worldToTextureMatrix = new WorldToTexture();
					worldToTextureMatrix.setMatrix(matrix);

					TexCoordGen texCoordGen = new TexCoordGen();
					texCoordGen.setWorldToTexture(worldToTextureMatrix);

					textureAssociation.setTextureParameterization(texCoordGen);
					parameterizedTexture.addTarget(textureAssociation);
				}
			}
		}
	}
}
