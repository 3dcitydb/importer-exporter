/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.Matrix;
import org.citygml4j.impl.citygml.appearance.AppearanceImpl;
import org.citygml4j.impl.citygml.appearance.ColorImpl;
import org.citygml4j.impl.citygml.appearance.ColorPlusOpacityImpl;
import org.citygml4j.impl.citygml.appearance.GeoreferencedTextureImpl;
import org.citygml4j.impl.citygml.appearance.ParameterizedTextureImpl;
import org.citygml4j.impl.citygml.appearance.SurfaceDataPropertyImpl;
import org.citygml4j.impl.citygml.appearance.TexCoordGenImpl;
import org.citygml4j.impl.citygml.appearance.TexCoordListImpl;
import org.citygml4j.impl.citygml.appearance.TextureAssociationImpl;
import org.citygml4j.impl.citygml.appearance.TextureCoordinatesImpl;
import org.citygml4j.impl.citygml.appearance.WorldToTextureImpl;
import org.citygml4j.impl.citygml.appearance.X3DMaterialImpl;
import org.citygml4j.impl.citygml.core.TransformationMatrix2x2Impl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.primitives.DirectPositionImpl;
import org.citygml4j.impl.gml.geometry.primitives.PointImpl;
import org.citygml4j.impl.gml.geometry.primitives.PointPropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
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
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.config.Config;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFileEnum;
import de.tub.citydb.util.Util;

public class DBGlobalAppearance implements DBExporter {
	private final Logger LOG = Logger.getInstance();
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final TemporaryCacheTable drainTable;

	private PreparedStatement psAppearanceCityModel;
	private boolean exportTextureImage;
	private boolean uniqueFileNames;
	private String texturePath;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean transformCoords;
	private String gmlIdPrefix;
	private String pathSeparator;

	private HashSet<String> textureNameCache;

	public DBGlobalAppearance(TemporaryCacheTable drainTable, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.drainTable = drainTable;

		init();
	}

	private void init() throws SQLException {
		textureNameCache = new HashSet<String>();
		exportTextureImage = config.getProject().getExporter().getAppearances().isSetExportTextureFiles();
		uniqueFileNames = config.getProject().getExporter().getAppearances().isSetUniqueTextureFileNames();

		texturePath = config.getInternal().getExportTextureFilePath();
		pathSeparator = config.getProject().getExporter().getAppearances().isTexturePathAbsolute() ?
				File.separator : "/";

		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
		}		

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {		
			psAppearanceCityModel = drainTable.getConnection().prepareStatement("select app.ID as APP_ID, app.GMLID as APP_GMLID, app.NAME as APP_NAME, app.NAME_CODESPACE as APP_NAME_CODESPACE, app.DESCRIPTION as APP_DESCRIPTION, app.THEME, " +
					"sd.ID as SD_ID, sd.GMLID as SD_GMLID, sd.NAME as SD_NAME, sd.NAME_CODESPACE as SD_NAME_CODESPACE, sd.DESCRIPTION as SD_DESCRIPTION, sd.IS_FRONT, upper(sd.TYPE) as TYPE, " +
					"sd.X3D_SHININESS, sd.X3D_TRANSPARENCY, sd.X3D_AMBIENT_INTENSITY, sd.X3D_SPECULAR_COLOR, sd.X3D_DIFFUSE_COLOR, sd.X3D_EMISSIVE_COLOR, sd.X3D_IS_SMOOTH, " +
					"sd.TEX_IMAGE_URI, nvl(sd.TEX_IMAGE.getContentLength(), 0) as DB_TEX_IMAGE_SIZE, sd.TEX_IMAGE.getMimeType() as DB_TEX_IMAGE_MIME_TYPE, sd.TEX_MIME_TYPE, lower(sd.TEX_TEXTURE_TYPE) as TEX_TEXTURE_TYPE, lower(sd.TEX_WRAP_MODE) as TEX_WRAP_MODE, sd.TEX_BORDER_COLOR, " +
					"sd.GT_PREFER_WORLDFILE, sd.GT_ORIENTATION, sd.GT_REFERENCE_POINT, " +
					"tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, tp.SURFACE_GEOMETRY_ID, " +
					"drain.GMLID as SG_GMLID " +
					"from APPEARANCE app inner join APPEAR_TO_SURFACE_DATA a2s on app.ID = a2s.APPEARANCE_ID inner join SURFACE_DATA sd on sd.ID=a2s.SURFACE_DATA_ID inner join TEXTUREPARAM tp on tp.SURFACE_DATA_ID=sd.ID inner join " + drainTable.getTableName() + " drain on drain.id=tp.SURFACE_GEOMETRY_ID where app.ID=? order by sd.ID");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();

			psAppearanceCityModel = drainTable.getConnection().prepareStatement("select app.ID as APP_ID, app.GMLID as APP_GMLID, app.NAME as APP_NAME, app.NAME_CODESPACE as APP_NAME_CODESPACE, app.DESCRIPTION as APP_DESCRIPTION, app.THEME, " +
					"sd.ID as SD_ID, sd.GMLID as SD_GMLID, sd.NAME as SD_NAME, sd.NAME_CODESPACE as SD_NAME_CODESPACE, sd.DESCRIPTION as SD_DESCRIPTION, sd.IS_FRONT, upper(sd.TYPE) as TYPE, " +
					"sd.X3D_SHININESS, sd.X3D_TRANSPARENCY, sd.X3D_AMBIENT_INTENSITY, sd.X3D_SPECULAR_COLOR, sd.X3D_DIFFUSE_COLOR, sd.X3D_EMISSIVE_COLOR, sd.X3D_IS_SMOOTH, " +
					"sd.TEX_IMAGE_URI, nvl(sd.TEX_IMAGE.getContentLength(), 0) as DB_TEX_IMAGE_SIZE, sd.TEX_IMAGE.getMimeType() as DB_TEX_IMAGE_MIME_TYPE, sd.TEX_MIME_TYPE, lower(sd.TEX_TEXTURE_TYPE) as TEX_TEXTURE_TYPE, lower(sd.TEX_WRAP_MODE) as TEX_WRAP_MODE, sd.TEX_BORDER_COLOR, " +
					"sd.GT_PREFER_WORLDFILE, sd.GT_ORIENTATION, " +
					"geodb_util.transform_or_null(sd.GT_REFERENCE_POINT, " + srid + ") AS GT_REFERENCE_POINT, " +
					"tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, tp.SURFACE_GEOMETRY_ID, " +
					"drain.GMLID as SG_GMLID " +
					"from APPEARANCE app inner join APPEAR_TO_SURFACE_DATA a2s on app.ID = a2s.APPEARANCE_ID inner join SURFACE_DATA sd on sd.ID=a2s.SURFACE_DATA_ID inner join TEXTUREPARAM tp on tp.SURFACE_DATA_ID=sd.ID inner join " + drainTable.getTableName() + " drain on drain.id=tp.SURFACE_GEOMETRY_ID where app.ID=? order by sd.ID");
		}
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		ResultSet rs = null;

		try {
			Appearance appearance = new AppearanceImpl();
			AbstractSurfaceData surfaceData = null;

			long appearanceId = splitter.getPrimaryKey();
			psAppearanceCityModel.setLong(1, appearanceId);
			rs = psAppearanceCityModel.executeQuery();

			boolean isInited = false;
			long currentSurfaceDataId = 0;

			while (rs.next()) {
				if (!isInited) {
					// appearance object
					// just handle once
					String gmlId = rs.getString("APP_GMLID");
					if (gmlId != null)
						appearance.setId(gmlId);

					String gmlName = rs.getString("APP_NAME");
					String gmlNameCodespace = rs.getString("APP_NAME_CODESPACE");

					Util.dbGmlName2featureName(appearance, gmlName, gmlNameCodespace);

					String description = rs.getString("APP_DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRefImpl();
						stringOrRef.setValue(description);
						appearance.setDescription(stringOrRef);
					}

					String theme = rs.getString("THEME");
					if (theme != null)
						appearance.setTheme(theme);

					textureNameCache.clear();
					isInited = true;
				}

				// add surface data members to appearance object
				String geometryTarget = rs.getString("SG_GMLID");
				if (rs.wasNull())
					continue;

				geometryTarget = '#' + geometryTarget;

				// get surface data member content
				long surfaceDataId = rs.getLong("SD_ID");
				if (rs.wasNull())
					continue;

				if (currentSurfaceDataId != surfaceDataId) {
					// create a surface data object
					surfaceData = null;
					currentSurfaceDataId = surfaceDataId;

					String surfaceDataType = rs.getString("TYPE");
					if (rs.wasNull() || surfaceDataType == null || surfaceDataType.length() == 0)
						continue;

					if (surfaceDataType.equals(TypeAttributeValueEnum.X3D_MATERIAL.toString().toUpperCase()))
						surfaceData = new X3DMaterialImpl();
					else if (surfaceDataType.equals(TypeAttributeValueEnum.PARAMETERIZED_TEXTURE.toString().toUpperCase()))
						surfaceData = new ParameterizedTextureImpl();
					else if (surfaceDataType.equals(TypeAttributeValueEnum.GEOREFERENCED_TEXTURE.toString().toUpperCase()))
						surfaceData = new GeoreferencedTextureImpl();

					if (surfaceData == null)
						continue;

					String gmlId = rs.getString("SD_GMLID");
					if (gmlId != null) {
						// process xlink
						if (dbExporterManager.lookupAndPutGmlId(gmlId, surfaceDataId, CityGMLClass.ABSTRACT_SURFACE_DATA)) {
							if (useXLink) {
								SurfaceDataProperty surfaceDataProperty = new SurfaceDataPropertyImpl();
								surfaceDataProperty.setHref("#" + gmlId);

								appearance.addSurfaceDataMember(surfaceDataProperty);
								continue;
							} else {
								String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
								if (appendOldGmlId)
									newGmlId += '-' + gmlId;

								gmlId = newGmlId;
							}
						}

						surfaceData.setId(gmlId);
					}

					String gmlName = rs.getString("SD_NAME");
					String gmlNameCodespace = rs.getString("SD_NAME_CODESPACE");

					Util.dbGmlName2featureName(surfaceData, gmlName, gmlNameCodespace);

					String description = rs.getString("SD_DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRefImpl();
						stringOrRef.setValue(description);
						surfaceData.setDescription(stringOrRef);
					}

					int isFront = rs.getInt("IS_FRONT");
					if (!rs.wasNull() && isFront == 0)
						surfaceData.setIsFront(false);

					if (surfaceData.getCityGMLClass() == CityGMLClass.X3D_MATERIAL) {
						X3DMaterial material = (X3DMaterial)surfaceData;

						double shininess = rs.getDouble("X3D_SHININESS");
						if (!rs.wasNull())
							material.setShininess(shininess);

						double transparency = rs.getDouble("X3D_TRANSPARENCY");
						if (!rs.wasNull())
							material.setTransparency(transparency);

						double ambientIntensity = rs.getDouble("X3D_AMBIENT_INTENSITY");
						if (!rs.wasNull())
							material.setAmbientIntensity(ambientIntensity);

						for (int i = 0; i < 3; i++) {
							String columnName = null;

							switch (i) {
							case 0:
								columnName = "X3D_SPECULAR_COLOR";
								break;
							case 1:
								columnName = "X3D_DIFFUSE_COLOR";
								break;
							case 2:
								columnName = "X3D_EMISSIVE_COLOR";
								break;
							}

							String colorString = rs.getString(columnName);
							if (colorString != null) {
								List<Double> colorList = Util.string2double(colorString, "\\s+");

								if (colorList != null && colorList.size() >= 3) {
									Color color = new ColorImpl(colorList.get(0), colorList.get(1), colorList.get(2));

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

						int isSmooth = rs.getInt("X3D_IS_SMOOTH");
						if (!rs.wasNull() && isSmooth == 1)
							material.setIsSmooth(true);

					}

					else if (surfaceData.getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE ||
							surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE) {
						AbstractTexture absTex = (AbstractTexture)surfaceData;

						long dbImageSize = rs.getLong("DB_TEX_IMAGE_SIZE");
						String imageURI = rs.getString("TEX_IMAGE_URI");
						if (imageURI != null) {
							if (uniqueFileNames) {
								String extension = Util.getFileExtension(imageURI);
								imageURI = "tex" + surfaceDataId + (extension != null ? "." + extension : "");
							}

							File file = new File(imageURI);
							String fileName = file.getName();
							if (texturePath != null)
								fileName = texturePath + pathSeparator + fileName;

							absTex.setImageURI(fileName);

							// export texture image from database
							if (exportTextureImage && (uniqueFileNames || !textureNameCache.contains(imageURI))) {
								if (dbImageSize > 0) {
									DBXlinkTextureFile xlink = new DBXlinkTextureFile(
											surfaceDataId,
											file.getName(),
											DBXlinkTextureFileEnum.TEXTURE_IMAGE);
									dbExporterManager.propagateXlink(xlink);
								} else {
									StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
											absTex.getCityGMLClass(), 
											absTex.getId()));
									msg.append(": Skipping 0 byte texture file ' ");
									msg.append(imageURI);
									msg.append("'.");

									LOG.warn(msg.toString());
								}
								
								if (!uniqueFileNames)
									textureNameCache.add(imageURI);
							}
						}

						String dbImageMimeType = rs.getString("DB_TEX_IMAGE_MIME_TYPE");
						if (dbImageMimeType != null) {
							absTex.setMimeType(dbImageMimeType);
						} else {
							String mimeType = rs.getString("TEX_MIME_TYPE");
							if (mimeType != null)
								absTex.setMimeType(mimeType);
						}

						String textureType = rs.getString("TEX_TEXTURE_TYPE");
						if (textureType != null) {
							TextureType type = TextureType.fromValue(textureType);
							absTex.setTextureType(type);
						}

						String wrapMode = rs.getString("TEX_WRAP_MODE");
						if (wrapMode != null) {
							WrapMode mode = WrapMode.fromValue(wrapMode);
							absTex.setWrapMode(mode);
						}

						String borderColorString = rs.getString("TEX_BORDER_COLOR");
						if (borderColorString != null) {
							List<Double> colorList = Util.string2double(borderColorString, "\\s+");

							if (colorList != null && colorList.size() >= 4) {
								ColorPlusOpacity borderColor = new ColorPlusOpacityImpl(
										colorList.get(0), colorList.get(1), colorList.get(2), colorList.get(3)
										);

								absTex.setBorderColor(borderColor);
							} else {
								// database entry incorrect
							}
						}
					}

					if (surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE) {
						GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;

						int preferWorldFile = rs.getInt("GT_PREFER_WORLDFILE");
						if (!rs.wasNull() && preferWorldFile == 0)
							geoTex.setPreferWorldFile(false);

						String orientationString = rs.getString("GT_ORIENTATION");
						if (orientationString != null) {
							List<Double> m = Util.string2double(orientationString, "\\s+");

							if (m != null && m.size() >= 4) {
								Matrix matrix = new Matrix(2, 2);
								matrix.setMatrix(m.subList(0, 4));

								geoTex.setOrientation(new TransformationMatrix2x2Impl(matrix));
							}
						}

						STRUCT struct = (STRUCT)rs.getObject("GT_REFERENCE_POINT");
						if (!rs.wasNull() && struct != null) {
							JGeometry jGeom = JGeometry.load(struct);
							double[] point = jGeom.getPoint();

							if (point != null && point.length >= 2) {
								Point referencePoint = new PointImpl();

								List<Double> value = new ArrayList<Double>();
								value.add(point[0]);
								value.add(point[1]);

								DirectPosition pos = new DirectPositionImpl();
								pos.setValue(value);
								pos.setSrsDimension(2);
								referencePoint.setPos(pos);

								PointProperty pointProperty = new PointPropertyImpl();
								pointProperty.setPoint(referencePoint);

								geoTex.setReferencePoint(pointProperty);
							}
						}
					}

					// add surface data to appearance
					SurfaceDataProperty surfaceDataProperty = new SurfaceDataPropertyImpl();
					surfaceDataProperty.setSurfaceData(surfaceData);
					appearance.addSurfaceDataMember(surfaceDataProperty);
				}

				if (surfaceData == null)
					continue;

				// add targets to surface data
				if (surfaceData.getCityGMLClass() == CityGMLClass.X3D_MATERIAL) {
					X3DMaterial material = (X3DMaterial)surfaceData;
					material.addTarget(geometryTarget);
				}

				else if (surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE) {
					GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;
					geoTex.addTarget(geometryTarget);
				}

				else if (surfaceData.getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE) {
					ParameterizedTexture paraTex = (ParameterizedTexture) surfaceData;

					String worldToTexture = rs.getString("WORLD_TO_TEXTURE");
					String textureCoordinates = rs.getString("TEXTURE_COORDINATES");

					if (textureCoordinates != null) {
						TextureAssociation textureAssociation = new TextureAssociationImpl();
						textureAssociation.setUri(geometryTarget);

						String[] rings = textureCoordinates.trim().split("\\s*;\\s*");
						if (rings != null && rings.length != 0) {
							TexCoordList texCoordList = new TexCoordListImpl();

							for (int i = 0; i < rings.length; i++) {
								String split = rings[i];

								if (split == null || split.length() == 0)
									continue;

								List<Double> coordsList = Util.string2double(split, "\\s+");
								if (coordsList != null && coordsList.size() != 0) {
									TextureCoordinates texureCoordinates = new TextureCoordinatesImpl();
									texureCoordinates.setValue(coordsList);
									texureCoordinates.setRing(geometryTarget + '_' + i + '_');

									texCoordList.addTextureCoordinates(texureCoordinates);
								} else {
									// database entry incorrect
								}
							}

							textureAssociation.setTextureParameterization(texCoordList);
							paraTex.addTarget(textureAssociation);
						}
					}

					else if (worldToTexture != null) {
						TextureAssociation textureAssociation = new TextureAssociationImpl();
						textureAssociation.setUri(geometryTarget);

						List<Double> m = Util.string2double(worldToTexture, "\\s+");
						if (m != null && m.size() >= 12) {
							Matrix matrix = new Matrix(3, 4);
							matrix.setMatrix(m.subList(0, 12));

							WorldToTexture worldToTextureMatrix = new WorldToTextureImpl();
							worldToTextureMatrix.setMatrix(matrix);

							TexCoordGen texCoordGen = new TexCoordGenImpl();
							texCoordGen.setWorldToTexture(worldToTextureMatrix);

							textureAssociation.setTextureParameterization(texCoordGen);
							paraTex.addTarget(textureAssociation);
						} else {
							// database entry incorrect
						}
					}
				}
			}

			if (appearance.isSetSurfaceDataMember()) {
				dbExporterManager.print(appearance);
				dbExporterManager.updateFeatureCounter(CityGMLClass.APPEARANCE);
				return true;
			} 

			return false;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psAppearanceCityModel.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.GLOBAL_APPEARANCE;
	}

}
