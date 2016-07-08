/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.exporter.database.content;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.internal.Internal;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.ColorPlusOpacity;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TextureType;
import org.citygml4j.model.citygml.appearance.WrapMode;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.TransformationMatrix2x2;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBAppearance implements DBExporter {
	private final Logger LOG = Logger.getInstance();
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;
	private final DBExporterEnum type;

	private PreparedStatement psAppearance;

	private DBTextureParam textureParamExporter;
	private boolean exportTextureImage;
	private boolean uniqueFileNames;
	private String texturePath;
	private boolean useBuckets;
	private int noOfBuckets;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private String gmlIdPrefix;
	private String pathSeparator;
	private HashSet<Long> texImageIds;

	public DBAppearance(DBExporterEnum type, Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		if (type != DBExporterEnum.LOCAL_APPEARANCE && type != DBExporterEnum.GLOBAL_APPEARANCE)
			throw new IllegalArgumentException("Invalid type " + type + " for appearance exporter.");

		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		this.type = type;

		init();
	}

	private void init() throws SQLException {
		texImageIds = new HashSet<Long>();
		exportTextureImage = config.getProject().getExporter().getAppearances().isSetExportTextureFiles();
		uniqueFileNames = config.getProject().getExporter().getAppearances().isSetUniqueTextureFileNames();
		noOfBuckets = config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets(); 
		useBuckets = config.getProject().getExporter().getAppearances().getTexturePath().isUseBuckets() && noOfBuckets > 0;

		texturePath = config.getInternal().getExportTextureFilePath();
		pathSeparator = config.getProject().getExporter().getAppearances().getTexturePath().isAbsolute() ? File.separator : "/";

		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
		}

		String getLength = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("blob.get_length");

		StringBuilder query = new StringBuilder();
		if (!config.getInternal().isTransformCoordinates()) {		
			query.append("select app.ID as APP_ID, app.GMLID as APP_GMLID, app.NAME as APP_NAME, app.NAME_CODESPACE as APP_NAME_CODESPACE, app.DESCRIPTION as APP_DESCRIPTION, app.THEME, ")
			.append("sd.ID as SD_ID, sd.OBJECTCLASS_ID as SD_OBJECTCLASS_ID, sd.GMLID as SD_GMLID, sd.NAME as SD_NAME, sd.NAME_CODESPACE as SD_NAME_CODESPACE, sd.DESCRIPTION as SD_DESCRIPTION, sd.IS_FRONT, ")
			.append("sd.X3D_SHININESS, sd.X3D_TRANSPARENCY, sd.X3D_AMBIENT_INTENSITY, sd.X3D_SPECULAR_COLOR, sd.X3D_DIFFUSE_COLOR, sd.X3D_EMISSIVE_COLOR, sd.X3D_IS_SMOOTH, ")
			.append("sd.TEX_IMAGE_ID, COALESCE(").append(getLength).append("(ti.TEX_IMAGE_DATA)").append(", 0) as DB_TEX_IMAGE_SIZE, ti.TEX_IMAGE_URI, ti.TEX_MIME_TYPE, ti.TEX_MIME_TYPE_CODESPACE, ")
			.append("lower(sd.TEX_TEXTURE_TYPE) as TEX_TEXTURE_TYPE, lower(sd.TEX_WRAP_MODE) as TEX_WRAP_MODE, sd.TEX_BORDER_COLOR, ")	
			.append("sd.GT_PREFER_WORLDFILE, sd.GT_ORIENTATION, sd.GT_REFERENCE_POINT ")
			.append("from APPEARANCE app inner join APPEAR_TO_SURFACE_DATA a2s on app.ID = a2s.APPEARANCE_ID inner join SURFACE_DATA sd on sd.ID=a2s.SURFACE_DATA_ID left join TEX_IMAGE ti on sd.TEX_IMAGE_ID=ti.ID where ");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			query.append("select app.ID as APP_ID, app.GMLID as APP_GMLID, app.NAME as APP_NAME, app.NAME_CODESPACE as APP_NAME_CODESPACE, app.DESCRIPTION as APP_DESCRIPTION, app.THEME, ")
			.append("sd.ID as SD_ID, sd.OBJECTCLASS_ID as SD_OBJECTCLASS_ID, sd.GMLID as SD_GMLID, sd.NAME as SD_NAME, sd.NAME_CODESPACE as SD_NAME_CODESPACE, sd.DESCRIPTION as SD_DESCRIPTION, sd.IS_FRONT, ")
			.append("sd.X3D_SHININESS, sd.X3D_TRANSPARENCY, sd.X3D_AMBIENT_INTENSITY, sd.X3D_SPECULAR_COLOR, sd.X3D_DIFFUSE_COLOR, sd.X3D_EMISSIVE_COLOR, sd.X3D_IS_SMOOTH, ")
			.append("sd.TEX_IMAGE_ID, COALESCE(").append(getLength).append("(ti.TEX_IMAGE_DATA)").append(", 0) as DB_TEX_IMAGE_SIZE, ti.TEX_IMAGE_URI, ti.TEX_MIME_TYPE, ti.TEX_MIME_TYPE_CODESPACE, ")
			.append("lower(sd.TEX_TEXTURE_TYPE) as TEX_TEXTURE_TYPE, lower(sd.TEX_WRAP_MODE) as TEX_WRAP_MODE, sd.TEX_BORDER_COLOR, ")
			.append("sd.GT_PREFER_WORLDFILE, sd.GT_ORIENTATION, ")
			.append(transformOrNull).append("(sd.GT_REFERENCE_POINT, ").append(srid).append(") AS GT_REFERENCE_POINT ")
			.append("from APPEARANCE app inner join APPEAR_TO_SURFACE_DATA a2s on app.ID = a2s.APPEARANCE_ID inner join SURFACE_DATA sd on sd.ID=a2s.SURFACE_DATA_ID left join TEX_IMAGE ti on sd.TEX_IMAGE_ID=ti.ID where ");
		}

		if (type == DBExporterEnum.LOCAL_APPEARANCE)
			query.append("app.CITYOBJECT_ID=?");
		else
			query.append("app.ID=?");

		psAppearance = connection.prepareStatement(query.toString());
		
		textureParamExporter = (DBTextureParam)dbExporterManager.getDBExporter(
				type == DBExporterEnum.LOCAL_APPEARANCE ? DBExporterEnum.LOCAL_APPEARANCE_TEXTUREPARAM : DBExporterEnum.GLOBAL_APPEARANCE_TEXTUREPARAM);
	}

	public void read(AbstractCityObject cityObject, long cityObjectId) throws SQLException {
		final List<Long> appearanceIds = new ArrayList<Long>();
		ResultSet rs = null;

		try {
			psAppearance.setLong(1, cityObjectId);
			rs = psAppearance.executeQuery();

			long currentAppearanceId = 0;
			Appearance appearance = null;

			while (rs.next()) {
				long appearanceId = rs.getLong(1);

				if (appearanceId != currentAppearanceId) {
					currentAppearanceId = appearanceId;

					int index = appearanceIds.indexOf(appearanceId);
					if (index == -1) {
						appearance = new Appearance();
						getAppearanceProperties(appearance, rs);

						// add appearance to cityobject
						cityObject.addAppearance(new AppearanceProperty(appearance));
						dbExporterManager.updateFeatureCounter(CityGMLClass.APPEARANCE);

						appearanceIds.add(appearanceId);
					} else
						appearance = cityObject.getAppearance().get(index).getAppearance();
				}

				// add surface data to appearance
				addSurfaceData(appearance, rs);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		ResultSet rs = null;

		try {
			Appearance appearance = new Appearance();
			boolean isInited = false;

			long appearanceId = splitter.getPrimaryKey();
			psAppearance.setLong(1, appearanceId);
			rs = psAppearance.executeQuery();

			while (rs.next()) {
				if (!isInited) {
					getAppearanceProperties(appearance, rs);
					texImageIds.clear();
					isInited = true;
				}

				// add surface data to appearance
				addSurfaceData(appearance, rs);
			}

			if (appearance.isSetSurfaceDataMember()) {
				dbExporterManager.processFeature(appearance);
				dbExporterManager.updateFeatureCounter(CityGMLClass.APPEARANCE);
				return true;
			}

			return false;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}

	private void getAppearanceProperties(Appearance appearance, ResultSet rs) throws SQLException {
		String gmlId = rs.getString(2);
		if (gmlId != null)
			appearance.setId(gmlId);

		String gmlName = rs.getString(3);
		String gmlNameCodespace = rs.getString(4);
		if (gmlName != null)
			appearance.setName(Util.string2codeList(gmlName, gmlNameCodespace));

		String description = rs.getString(5);
		if (description != null) {
			StringOrRef stringOrRef = new StringOrRef();
			stringOrRef.setValue(description);
			appearance.setDescription(stringOrRef);
		}

		String theme = rs.getString(6);
		if (theme != null)
			appearance.setTheme(theme);
	}

	private void addSurfaceData(Appearance appearance, ResultSet rs) throws SQLException {
		long surfaceDataId = rs.getLong(7);
		if (rs.wasNull())
			return;

		int classId = rs.getInt(8);
		if (rs.wasNull() || classId == 0)
			return;

		AbstractSurfaceData surfaceData = null;		
		CityGMLClass type = Util.classId2cityObject(classId);
		switch (type) {
		case X3D_MATERIAL:
			surfaceData = new X3DMaterial();
			break;
		case PARAMETERIZED_TEXTURE:
			surfaceData = new ParameterizedTexture();
			break;
		case GEOREFERENCED_TEXTURE:
			surfaceData = new GeoreferencedTexture();
			break;
		default:
			return;
		}

		String gmlId = rs.getString(9);
		if (gmlId != null) {
			// process xlink
			if (dbExporterManager.lookupAndPutGmlId(gmlId, surfaceDataId, CityGMLClass.ABSTRACT_SURFACE_DATA)) {
				if (useXLink) {
					SurfaceDataProperty surfaceDataProperty = new SurfaceDataProperty();
					surfaceDataProperty.setHref("#" + gmlId);

					appearance.addSurfaceDataMember(surfaceDataProperty);
					return;
				} else {
					String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
					if (appendOldGmlId)
						newGmlId += '-' + gmlId;

					gmlId = newGmlId;
				}
			}

			surfaceData.setId(gmlId);
		}

		// retrieve targets
		boolean hasTargets = textureParamExporter.read(surfaceData, surfaceDataId);
		if (!hasTargets)
			return;
		
		String gmlName = rs.getString(10);
		String gmlNameCodespace = rs.getString(11);
		if (gmlName != null)
			surfaceData.setName(Util.string2codeList(gmlName, gmlNameCodespace));

		String description = rs.getString(12);
		if (description != null) {
			StringOrRef stringOrRef = new StringOrRef();
			stringOrRef.setValue(description);
			surfaceData.setDescription(stringOrRef);
		}

		int isFront = rs.getInt(13);
		if (!rs.wasNull() && isFront == 0)
			surfaceData.setIsFront(false);

		if (type == CityGMLClass.X3D_MATERIAL) {
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
					List<Double> colorList = Util.string2double(colorString, "\\s+");

					if (colorList != null && colorList.size() >= 3) {
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
					} else {
						// database entry is incorrect
					}
				}
			}

			int isSmooth = rs.getInt(20);
			if (!rs.wasNull() && isSmooth == 1)
				material.setIsSmooth(true);
		}

		else if (type == CityGMLClass.PARAMETERIZED_TEXTURE ||
				type == CityGMLClass.GEOREFERENCED_TEXTURE) {
			AbstractTexture absTex = (AbstractTexture)surfaceData;

			long texImageId = rs.getLong(21);			
			if (texImageId != 0) {
				long dbImageSize = rs.getLong(22);

				String imageURI = rs.getString(23);
				if (uniqueFileNames) {
					String extension = Util.getFileExtension(imageURI);
					imageURI = Internal.UNIQUE_TEXTURE_FILENAME_PREFIX + texImageId + (extension != null ? "." + extension : "");
				}

				File file = new File(imageURI);
				String fileName = file.getName();
				if (useBuckets)
					fileName = String.valueOf(Math.abs(texImageId % noOfBuckets + 1)) + pathSeparator + fileName;

				absTex.setImageURI(texturePath != null ? texturePath + pathSeparator + fileName : fileName);

				// export texture image from database
				if (exportTextureImage && (uniqueFileNames || !texImageIds.contains(texImageId))) {
					if (dbImageSize > 0) {
						dbExporterManager.propagateXlink(new DBXlinkTextureFile(
								texImageId,
								fileName,
								false));
					} else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								absTex.getCityGMLClass(), 
								absTex.getId()));
						msg.append(": Skipping 0 byte texture file '");
						msg.append(imageURI);
						msg.append("'.");

						LOG.warn(msg.toString());
					}

					if (!uniqueFileNames)
						texImageIds.add(texImageId);
				}
			}

			String mimeType = rs.getString(24);
			if (mimeType != null) {
				Code code = new Code(mimeType);
				code.setCodeSpace(rs.getString(25));
				absTex.setMimeType(code);
			}

			String textureType = rs.getString(26);
			if (textureType != null)
				absTex.setTextureType(TextureType.fromValue(textureType));

			String wrapMode = rs.getString(27);
			if (wrapMode != null) {
				WrapMode mode = WrapMode.fromValue(wrapMode);
				absTex.setWrapMode(mode);
			}

			String borderColorString = rs.getString(28);
			if (borderColorString != null) {
				List<Double> colorList = Util.string2double(borderColorString, "\\s+");

				if (colorList != null && colorList.size() >= 4) {
					ColorPlusOpacity borderColor = new ColorPlusOpacity(
							colorList.get(0), colorList.get(1), colorList.get(2), colorList.get(3)						
							);

					absTex.setBorderColor(borderColor);
				} else {
					// database entry incorrect
				}
			}
		}

		if (type == CityGMLClass.GEOREFERENCED_TEXTURE) {
			GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;

			int preferWorldFile = rs.getInt(29);
			if (!rs.wasNull() && preferWorldFile == 0)
				geoTex.setPreferWorldFile(false);

			String orientationString = rs.getString(30);
			if (orientationString != null) {
				List<Double> m = Util.string2double(orientationString, "\\s+");

				if (m != null && m.size() >= 4) {
					Matrix matrix = new Matrix(2, 2);
					matrix.setMatrix(m.subList(0, 4));

					geoTex.setOrientation(new TransformationMatrix2x2(matrix));
				}
			}

			Object referencePointObj = rs.getObject(31);
			if (!rs.wasNull() && referencePointObj != null) {
				GeometryObject pointObj = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);				

				if (pointObj != null) {
					double[] point = pointObj.getCoordinates(0);
					Point referencePoint = new Point();

					List<Double> value = new ArrayList<Double>();
					value.add(point[0]);
					value.add(point[1]);

					DirectPosition pos = new DirectPosition();
					pos.setValue(value);
					pos.setSrsDimension(2);
					referencePoint.setPos(pos);

					PointProperty pointProperty = new PointProperty(referencePoint);
					geoTex.setReferencePoint(pointProperty);
				}
			}
		}

		// finally add surface data to appearance
		SurfaceDataProperty surfaceDataProperty = new SurfaceDataProperty();
		surfaceDataProperty.setSurfaceData(surfaceData);
		appearance.addSurfaceDataMember(surfaceDataProperty);

		return;
	}

	public void clearLocalCache() {
		texImageIds.clear();
	}

	@Override
	public void close() throws SQLException {
		if (psAppearance != null)
			psAppearance.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return type;
	}

}
