package de.tub.citydb.db.exporter;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.db.xlink.DBXlinkExternalFileEnum;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.geometry.Matrix4;
import de.tub.citygml4j.implementation.gml._3_1_1.DirectPositionImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.PointImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.PointPropertyImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.StringOrRefImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import de.tub.citygml4j.model.citygml.appearance.AbstractTexture;
import de.tub.citygml4j.model.citygml.appearance.Appearance;
import de.tub.citygml4j.model.citygml.appearance.AppearanceModule;
import de.tub.citygml4j.model.citygml.appearance.AppearanceProperty;
import de.tub.citygml4j.model.citygml.appearance.Color;
import de.tub.citygml4j.model.citygml.appearance.ColorPlusOpacity;
import de.tub.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import de.tub.citygml4j.model.citygml.appearance.ParameterizedTexture;
import de.tub.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import de.tub.citygml4j.model.citygml.appearance.TexCoordGen;
import de.tub.citygml4j.model.citygml.appearance.TexCoordList;
import de.tub.citygml4j.model.citygml.appearance.TextureAssociation;
import de.tub.citygml4j.model.citygml.appearance.TextureCoordinates;
import de.tub.citygml4j.model.citygml.appearance.TextureType;
import de.tub.citygml4j.model.citygml.appearance.WorldToTexture;
import de.tub.citygml4j.model.citygml.appearance.WrapMode;
import de.tub.citygml4j.model.citygml.appearance.X3DMaterial;
import de.tub.citygml4j.model.citygml.core.CityObject;
import de.tub.citygml4j.model.citygml.core.TransformationMatrix2x2;
import de.tub.citygml4j.model.gml.DirectPosition;
import de.tub.citygml4j.model.gml.Point;
import de.tub.citygml4j.model.gml.PointProperty;
import de.tub.citygml4j.model.gml.StringOrRef;

public class DBAppearance implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;
	
	private PreparedStatement psAppearanceCityObject;
	private PreparedStatement psAppearanceCityModel;
	private ResultSet rs;

	private DBTextureParam textureParamExporter;
	private boolean exportTextureImage;
	private String texturePath;
	private String gmlNameDelimiter;
	private AppearanceModule globalAppFactory;

	public DBAppearance(Connection connection, CityGMLFactory cityGMLFactory, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		exportTextureImage = config.getProject().getExporter().getAppearances().isSetExportTextureFiles();
		texturePath = config.getInternal().getExportTextureFilePath();
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		globalAppFactory = config.getProject().getExporter().getModuleVersion().getAppearance().getModule();
		
		psAppearanceCityObject = connection.prepareStatement("select app.ID as APP_ID, app.GMLID as APP_GMLID, app.NAME as APP_NAME, app.NAME_CODESPACE as APP_NAME_CODESPACE, app.DESCRIPTION as APP_DESCRIPTION, app.THEME, " +
				"sd.ID as SD_ID, sd.GMLID as SD_GMLID, sd.NAME as SD_NAME, sd.NAME_CODESPACE as SD_NAME_CODESPACE, sd.DESCRIPTION as SD_DESCRIPTION, sd.IS_FRONT, upper(sd.TYPE) as TYPE, " +
				"sd.X3D_SHININESS, sd.X3D_TRANSPARENCY, sd.X3D_AMBIENT_INTENSITY, sd.X3D_SPECULAR_COLOR, sd.X3D_DIFFUSE_COLOR, sd.X3D_EMISSIVE_COLOR, sd.X3D_IS_SMOOTH, " +
				"sd.TEX_IMAGE_URI, nvl(sd.TEX_IMAGE.getContentLength(), 0) as DB_TEX_IMAGE_SIZE, sd.TEX_IMAGE.getMimeType() as DB_TEX_IMAGE_MIME_TYPE, sd.TEX_MIME_TYPE, lower(sd.TEX_TEXTURE_TYPE) as TEX_TEXTURE_TYPE, lower(sd.TEX_WRAP_MODE) as TEX_WRAP_MODE, sd.TEX_BORDER_COLOR, " +
				"sd.GT_PREFER_WORLDFILE, sd.GT_ORIENTATION, sd.GT_REFERENCE_POINT " +
				"from APPEARANCE app inner join APPEAR_TO_SURFACE_DATA a2s on app.ID = a2s.APPEARANCE_ID inner join SURFACE_DATA sd on sd.ID=a2s.SURFACE_DATA_ID where app.CITYOBJECT_ID=?");

		psAppearanceCityModel = connection.prepareStatement("select app.ID as APP_ID, app.GMLID as APP_GMLID, app.NAME as APP_NAME, app.NAME_CODESPACE as APP_NAME_CODESPACE, app.DESCRIPTION as APP_DESCRIPTION, app.THEME, " +
				"sd.ID as SD_ID, sd.GMLID as SD_GMLID, sd.NAME as SD_NAME, sd.NAME_CODESPACE as SD_NAME_CODESPACE, sd.DESCRIPTION as SD_DESCRIPTION, sd.IS_FRONT, upper(sd.TYPE) as TYPE, " +
				"sd.X3D_SHININESS, sd.X3D_TRANSPARENCY, sd.X3D_AMBIENT_INTENSITY, sd.X3D_SPECULAR_COLOR, sd.X3D_DIFFUSE_COLOR, sd.X3D_EMISSIVE_COLOR, sd.X3D_IS_SMOOTH, " +
				"sd.TEX_IMAGE_URI, nvl(sd.TEX_IMAGE.getContentLength(), 0) as DB_TEX_IMAGE_SIZE, sd.TEX_IMAGE.getMimeType() as DB_TEX_IMAGE_MIME_TYPE, sd.TEX_MIME_TYPE, lower(sd.TEX_TEXTURE_TYPE) as TEX_TEXTURE_TYPE, lower(sd.TEX_WRAP_MODE) as TEX_WRAP_MODE, sd.TEX_BORDER_COLOR, " +
				"sd.GT_PREFER_WORLDFILE, sd.GT_ORIENTATION, sd.GT_REFERENCE_POINT, " +
				"tp.WORLD_TO_TEXTURE, tp.TEXTURE_COORDINATES, tp.SURFACE_GEOMETRY_ID " +
				"from APPEARANCE app inner join APPEAR_TO_SURFACE_DATA a2s on app.ID = a2s.APPEARANCE_ID inner join SURFACE_DATA sd on sd.ID=a2s.SURFACE_DATA_ID inner join TEXTUREPARAM tp on tp.SURFACE_DATA_ID=sd.ID where app.ID=?");

		textureParamExporter = (DBTextureParam)dbExporterManager.getDBExporter(DBExporterEnum.TEXTUREPARAM);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
		Appearance appearance = cityGMLFactory.createAppearance(globalAppFactory);
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

				Util.dbGmlName2featureName(appearance, gmlName, gmlNameCodespace, gmlNameDelimiter);

				String description = rs.getString("APP_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					appearance.setDescription(stringOrRef);
				}

				String theme = rs.getString("THEME");
				if (theme != null)
					appearance.setTheme(theme);

				isInited = true;
			}

			// add surface data members to appearance object
			// we only want those surface data pointing to geometry objects we already
			// exported. so we check if the target of the surface data is held within our
			// gmlId cache.
			long surfaceGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");
			if (rs.wasNull())
				continue;

			String geometryTarget = dbExporterManager.getGmlId(surfaceGeometryId, CityGMLClass.GMLGEOMETRY);
			if (geometryTarget == null)
				continue;

			// get surface data member content
			long surfaceDataId = rs.getLong("SD_ID");
			if (rs.wasNull())
				continue;

			if (currentSurfaceDataId != surfaceDataId) {
				// create a surface data object
				surfaceData = null;
				currentSurfaceDataId = surfaceDataId;

				String gmlId = rs.getString("SD_GMLID");
				if (gmlId != null) {
					// set xlink
					if (dbExporterManager.lookupAndPutGmlId(gmlId, surfaceDataId, CityGMLClass.APPEARANCE)) {
						SurfaceDataProperty surfaceDataProperty = cityGMLFactory.createSurfaceDataProperty(globalAppFactory);
						surfaceDataProperty.setHref("#" + gmlId);

						appearance.addSurfaceDataMember(surfaceDataProperty);
						continue;
					}
				}

				String surfaceDataType = rs.getString("TYPE");
				if (rs.wasNull() || surfaceDataType == null || surfaceDataType.length() == 0)
					continue;

				if (surfaceDataType.equals(CityGMLClass.X3DMATERIAL.toString().toUpperCase()))
					surfaceData = cityGMLFactory.createX3DMaterial(globalAppFactory);
				else if (surfaceDataType.equals(CityGMLClass.PARAMETERIZEDTEXTURE.toString().toUpperCase()))
					surfaceData = cityGMLFactory.createParameterizedTexture(globalAppFactory);
				else if (surfaceDataType.equals(CityGMLClass.GEOREFERENCEDTEXTURE.toString().toUpperCase()))
					surfaceData = cityGMLFactory.createGeoreferencedTexture(globalAppFactory);

				if (surfaceData == null)
					continue;

				if (gmlId != null)
					surfaceData.setId(gmlId);

				String gmlName = rs.getString("SD_NAME");
				String gmlNameCodespace = rs.getString("SD_NAME_CODESPACE");

				Util.dbGmlName2featureName(surfaceData, gmlName, gmlNameCodespace, gmlNameDelimiter);

				String description = rs.getString("SD_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					surfaceData.setDescription(stringOrRef);
				}

				int isFront = rs.getInt("IS_FRONT");
				if (!rs.wasNull() && isFront == 0)
					surfaceData.setIsFront(false);

				if (surfaceData.getCityGMLClass() == CityGMLClass.X3DMATERIAL) {
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
								Color color = cityGMLFactory.createColor(colorList.get(0), colorList.get(1), colorList.get(2), globalAppFactory);

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

				else if (surfaceData.getCityGMLClass() == CityGMLClass.PARAMETERIZEDTEXTURE ||
						surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
					AbstractTexture absTex = (AbstractTexture)surfaceData;

					long dbImageSize = rs.getLong("DB_TEX_IMAGE_SIZE");
					String imageURI = rs.getString("TEX_IMAGE_URI");
					if (imageURI != null) {
						// export texture image from database
						if (dbImageSize > 0) {
							File file = new File(imageURI);
							String fileName = file.getName();
							if (texturePath != null)
								fileName = texturePath + File.separator + fileName;

							absTex.setImageURI(fileName);

							if (exportTextureImage) {
								DBXlinkExternalFile xlink = new DBXlinkExternalFile(
										surfaceDataId,
										file.getName(),
										DBXlinkExternalFileEnum.TEXTURE_IMAGE);
								dbExporterManager.propagateXlink(xlink);
							}
						} else
							absTex.setImageURI(imageURI);
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
						TextureType type = cityGMLFactory.createTextureType(textureType, globalAppFactory);
						absTex.setTextureType(type);
					}

					String wrapMode = rs.getString("TEX_WRAP_MODE");
					if (wrapMode != null) {
						WrapMode mode = cityGMLFactory.createWrapMode(wrapMode, globalAppFactory);
						absTex.setWrapMode(mode);
					}

					String borderColorString = rs.getString("TEX_BORDER_COLOR");
					if (borderColorString != null) {
						List<Double> colorList = Util.string2double(borderColorString, "\\s+");

						if (colorList != null && colorList.size() >= 4) {
							ColorPlusOpacity borderColor = cityGMLFactory.createColorPlusOpacity(
									colorList.get(0), colorList.get(1), colorList.get(2), colorList.get(3),
									globalAppFactory
							);

							absTex.setBorderColor(borderColor);
						} else {
							// database entry incorrect
						}
					}
				}

				if (surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
					GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;

					int preferWorldFile = rs.getInt("GT_PREFER_WORLDFILE");
					if (!rs.wasNull() && preferWorldFile == 0)
						geoTex.setPreferWorldFile(false);

					String orientationString = rs.getString("GT_ORIENTATION");
					if (orientationString != null) {
						String[] splitted = orientationString.trim().split("\\s+");

						if (splitted != null && splitted.length != 0) {
							List<Double> orientationList = new ArrayList<Double>();

							for (String split : splitted) {
								if (split == null)
									continue;

								Double orientationPart = null;
								try {
									orientationPart = Double.parseDouble(split);
								} catch (NumberFormatException nfe) {
									//
								}

								if (orientationPart != null)
									orientationList.add(orientationPart);
							}

							TransformationMatrix2x2 orientation = cityGMLFactory.createTransformationMatrix2x2(orientationList, globalAppFactory.getCoreDependency());
							geoTex.setOrientation(orientation);
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
				SurfaceDataProperty surfaceDataProperty = cityGMLFactory.createSurfaceDataProperty(globalAppFactory);
				surfaceDataProperty.setSurfaceData(surfaceData);
				appearance.addSurfaceDataMember(surfaceDataProperty);
			}

			if (surfaceData == null)
				continue;

			// add targets to surface data
			if (surfaceData.getCityGMLClass() == CityGMLClass.X3DMATERIAL) {
				X3DMaterial material = (X3DMaterial)surfaceData;
				material.addTarget(geometryTarget);
			}

			else if (surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
				GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;
				geoTex.addTarget(geometryTarget);
			}

			else if (surfaceData.getCityGMLClass() == CityGMLClass.PARAMETERIZEDTEXTURE) {
				ParameterizedTexture paraTex = (ParameterizedTexture) surfaceData;

				String worldToTexture = rs.getString("WORLD_TO_TEXTURE");
				String textureCoordinates = rs.getString("TEXTURE_COORDINATES");

				if (textureCoordinates != null) {
					TextureAssociation textureAssociation = cityGMLFactory.createTextureAssociation(globalAppFactory);
					textureAssociation.setUri(geometryTarget);

					String[] rings = textureCoordinates.trim().split("\\s*;\\s*");
					if (rings != null && rings.length != 0) {
						TexCoordList texCoordList = cityGMLFactory.createTexCoordList(globalAppFactory);

						for (int i = 0; i < rings.length; i++) {
							String split = rings[i];

							if (split == null || split.length() == 0)
								continue;

							List<Double> coordsList = Util.string2double(split, "\\s+");
							if (coordsList != null && coordsList.size() != 0) {
								TextureCoordinates texureCoordinates = cityGMLFactory.createTextureCoordinates(globalAppFactory);
								texureCoordinates.setValue(coordsList);
								texureCoordinates.setRing(geometryTarget + "_" + i);

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
					TextureAssociation textureAssociation = cityGMLFactory.createTextureAssociation(globalAppFactory);
					textureAssociation.setUri(geometryTarget);

					List<Double> m = Util.string2double(worldToTexture, "\\s+");
					if (m != null && m.size() >= 12) {
						Matrix4 matrix = new Matrix4(
								m.get(0), m.get(1), m.get(2), m.get(3),
								m.get(4), m.get(5), m.get(6), m.get(7),
								m.get(8), m.get(9), m.get(10), m.get(11)
						);

						WorldToTexture worldToTextureMatrix = cityGMLFactory.createWorldToTexture(globalAppFactory);
						worldToTextureMatrix.setTransformationMatrix3x4(matrix);

						TexCoordGen texCoordGen = cityGMLFactory.createTexCoordGen(globalAppFactory);
						texCoordGen.setWorldToTexture(worldToTextureMatrix);

						textureAssociation.setTextureParameterization(texCoordGen);
						paraTex.addTarget(textureAssociation);
					} else {
						// database entry incorrect
					}
				}
			}
		}

		if (appearance.getSurfaceDataMember() != null && appearance.getSurfaceDataMember().size() != 0) {
			dbExporterManager.print(appearance);
			dbExporterManager.updateFeatureCounter(CityGMLClass.APPEARANCE);
			return true;
		} else
			return false;
	}

	public void read(CityObject cityObject, long cityObjectId) throws SQLException {
		psAppearanceCityObject.setLong(1, cityObjectId);
		rs = psAppearanceCityObject.executeQuery();

		long currentAppearanceId = 0;
		Appearance appearance = null;
	
		AppearanceModule factory = cityObject.getCityGMLModule().getAppearanceDependency();
		
		while (rs.next()) {
			long appearanceId = rs.getLong("APP_ID");

			if (appearanceId != currentAppearanceId) {
				currentAppearanceId = appearanceId;

				appearance = cityGMLFactory.createAppearance(factory);

				String gmlId = rs.getString("APP_GMLID");
				if (gmlId != null)
					appearance.setId(gmlId);

				String gmlName = rs.getString("APP_NAME");
				String gmlNameCodespace = rs.getString("APP_NAME_CODESPACE");

				Util.dbGmlName2featureName(appearance, gmlName, gmlNameCodespace, gmlNameDelimiter);

				String description = rs.getString("APP_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					appearance.setDescription(stringOrRef);
				}

				String theme = rs.getString("THEME");
				if (theme != null)
					appearance.setTheme(theme);

				// add appearance to cityobject
				AppearanceProperty appearanceProperty = cityGMLFactory.createAppearanceProperty(factory);
				appearanceProperty.setAppearance(appearance);

				cityObject.addAppearance(appearanceProperty);
				dbExporterManager.updateFeatureCounter(CityGMLClass.APPEARANCE);
			}

			long surfaceDataId = rs.getLong("SD_ID");
			if (rs.wasNull())
				continue;

			// create a surface data object
			AbstractSurfaceData surfaceData = null;

			String surfaceDataType = rs.getString("TYPE");
			if (rs.wasNull() || surfaceDataType == null || surfaceDataType.length() == 0)
				continue;

			if (surfaceDataType.equals(CityGMLClass.X3DMATERIAL.toString().toUpperCase()))
				surfaceData = cityGMLFactory.createX3DMaterial(factory);
			else if (surfaceDataType.equals(CityGMLClass.PARAMETERIZEDTEXTURE.toString().toUpperCase()))
				surfaceData = cityGMLFactory.createParameterizedTexture(factory);
			else if (surfaceDataType.equals(CityGMLClass.GEOREFERENCEDTEXTURE.toString().toUpperCase()))
				surfaceData = cityGMLFactory.createGeoreferencedTexture(factory);

			if (surfaceData == null)
				continue;

			String gmlId = rs.getString("SD_GMLID");
			if (gmlId != null) {
				// set xlink
				if (dbExporterManager.lookupAndPutGmlId(gmlId, surfaceDataId, CityGMLClass.APPEARANCE)) {
					SurfaceDataProperty surfaceDataProperty = cityGMLFactory.createSurfaceDataProperty(factory);
					surfaceDataProperty.setHref("#" + gmlId);

					appearance.addSurfaceDataMember(surfaceDataProperty);
					continue;
				}

				surfaceData.setId(gmlId);
			}

			String gmlName = rs.getString("SD_NAME");
			String gmlNameCodespace = rs.getString("SD_NAME_CODESPACE");

			Util.dbGmlName2featureName(surfaceData, gmlName, gmlNameCodespace, gmlNameDelimiter);

			String description = rs.getString("SD_DESCRIPTION");
			if (description != null) {
				StringOrRef stringOrRef = new StringOrRefImpl();
				stringOrRef.setValue(description);
				surfaceData.setDescription(stringOrRef);
			}

			int isFront = rs.getInt("IS_FRONT");
			if (!rs.wasNull() && isFront == 0)
				surfaceData.setIsFront(false);

			if (surfaceData.getCityGMLClass() == CityGMLClass.X3DMATERIAL) {
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
							Color color = cityGMLFactory.createColor(colorList.get(0), colorList.get(1), colorList.get(2), factory);

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

			else if (surfaceData.getCityGMLClass() == CityGMLClass.PARAMETERIZEDTEXTURE ||
					surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
				AbstractTexture absTex = (AbstractTexture)surfaceData;

				long dbImageSize = rs.getLong("DB_TEX_IMAGE_SIZE");
				String imageURI = rs.getString("TEX_IMAGE_URI");
				if (imageURI != null) {
					// export texture image from database
					if (dbImageSize > 0) {
						File file = new File(imageURI);
						String fileName = file.getName();
						if (texturePath != null)
							fileName = texturePath + File.separator + fileName;

						absTex.setImageURI(fileName);

						if (exportTextureImage) {
							DBXlinkExternalFile xlink = new DBXlinkExternalFile(
									surfaceDataId,
									file.getName(),
									DBXlinkExternalFileEnum.TEXTURE_IMAGE);
							dbExporterManager.propagateXlink(xlink);
						}
					} else
						absTex.setImageURI(imageURI);
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
					TextureType type = cityGMLFactory.createTextureType(textureType, factory);
					absTex.setTextureType(type);
				}

				String wrapMode = rs.getString("TEX_WRAP_MODE");
				if (wrapMode != null) {
					WrapMode mode = cityGMLFactory.createWrapMode(wrapMode, factory);
					absTex.setWrapMode(mode);
				}

				String borderColorString = rs.getString("TEX_BORDER_COLOR");
				if (borderColorString != null) {
					List<Double> colorList = Util.string2double(borderColorString, "\\s+");

					if (colorList != null && colorList.size() >= 4) {
						ColorPlusOpacity borderColor = cityGMLFactory.createColorPlusOpacity(
								colorList.get(0), colorList.get(1), colorList.get(2), colorList.get(3),
								factory
						);

						absTex.setBorderColor(borderColor);
					} else {
						// database entry incorrect
					}
				}
			}

			if (surfaceData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
				GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;

				int preferWorldFile = rs.getInt("GT_PREFER_WORLDFILE");
				if (!rs.wasNull() && preferWorldFile == 0)
					geoTex.setPreferWorldFile(false);

				String orientationString = rs.getString("GT_ORIENTATION");
				if (orientationString != null) {
					String[] splitted = orientationString.trim().split("\\s+");

					if (splitted != null && splitted.length >= 4) {
						List<Double> orientationList = new ArrayList<Double>();

						for (int i = 0; i < 4; i++) {
							if (splitted[i] == null)
								continue;

							Double orientationPart = null;
							try {
								orientationPart = Double.parseDouble(splitted[i]);
							} catch (NumberFormatException nfe) {
								//
							}

							if (orientationPart != null)
								orientationList.add(orientationPart);
						}

						TransformationMatrix2x2 orientation = cityGMLFactory.createTransformationMatrix2x2(orientationList, factory.getCoreDependency());
						geoTex.setOrientation(orientation);
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

			// get targets for surface data
			textureParamExporter.read(surfaceData, surfaceDataId, factory);

			// finally add surface data to appearance
			SurfaceDataProperty surfaceDataProperty = cityGMLFactory.createSurfaceDataProperty(factory);
			surfaceDataProperty.setSurfaceData(surfaceData);
			appearance.addSurfaceDataMember(surfaceDataProperty);
		}
		
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.APPEARANCE;
	}

}
