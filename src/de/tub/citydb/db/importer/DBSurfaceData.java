package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.db.xlink.DBXlinkExternalFileEnum;
import de.tub.citydb.db.xlink.DBXlinkTextureParam;
import de.tub.citydb.db.xlink.DBXlinkTextureParamEnum;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.util.UUIDManager;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import de.tub.citygml4j.model.citygml.appearance.AbstractTexture;
import de.tub.citygml4j.model.citygml.appearance.Color;
import de.tub.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import de.tub.citygml4j.model.citygml.appearance.ParameterizedTexture;
import de.tub.citygml4j.model.citygml.appearance.TexCoordGen;
import de.tub.citygml4j.model.citygml.appearance.TexCoordList;
import de.tub.citygml4j.model.citygml.appearance.TextureAssociation;
import de.tub.citygml4j.model.citygml.appearance.TextureCoordinates;
import de.tub.citygml4j.model.citygml.appearance.TextureParameterization;
import de.tub.citygml4j.model.citygml.appearance.X3DMaterial;
import de.tub.citygml4j.model.gml.Point;
import de.tub.citygml4j.model.gml.PointProperty;

public class DBSurfaceData implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psX3DMaterial;
	private PreparedStatement psParaTex;
	private PreparedStatement psGeoTex;
	private DBAppearToSurfaceData dbAppearToSurfaceDataImporter;

	private String dbSrid;
	private boolean replaceGmlId;
	private boolean importTextureImage;
	private String gmlNameDelimiter;

	public DBSurfaceData(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		dbSrid = config.getInternal().getDbSrid();
		importTextureImage = config.getProject().getImporter().getAppearances().isSetImportTextureFiles();
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();

		if (gmlIdCodespace != null && gmlIdCodespace.length() != 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "'";
		else
			gmlIdCodespace = "null";


		psX3DMaterial = batchConn.prepareStatement("insert into SURFACE_DATA (ID, GMLID, GMLID_CODESPACE, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, TYPE, " +
				"X3D_SHININESS, X3D_TRANSPARENCY, X3D_AMBIENT_INTENSITY, X3D_SPECULAR_COLOR, X3D_DIFFUSE_COLOR, X3D_EMISSIVE_COLOR, X3D_IS_SMOOTH) values " +
				"(?, ?, " + gmlIdCodespace + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		psParaTex = batchConn.prepareStatement("insert into SURFACE_DATA (ID, GMLID, GMLID_CODESPACE, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, TYPE, " +
				"TEX_IMAGE_URI, TEX_MIME_TYPE, TEX_TEXTURE_TYPE, TEX_WRAP_MODE, TEX_BORDER_COLOR) values " +
				"(?, ?, " + gmlIdCodespace + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		psGeoTex = batchConn.prepareStatement("insert into SURFACE_DATA (ID, GMLID, GMLID_CODESPACE, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, TYPE, " +
				"TEX_IMAGE_URI, TEX_MIME_TYPE, TEX_TEXTURE_TYPE, TEX_WRAP_MODE, TEX_BORDER_COLOR, " +
				"GT_PREFER_WORLDFILE, GT_ORIENTATION, GT_REFERENCE_POINT) values " +
				"(?, ?, " + gmlIdCodespace + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		dbAppearToSurfaceDataImporter = (DBAppearToSurfaceData)dbImporterManager.getDBImporter(DBImporterEnum.APPEAR_TO_SURFACE_DATA);
	}

	public long insert(AbstractSurfaceData abstractSurfData, long parentId) throws SQLException {
		long surfaceDataId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_DATA_SEQ);
		if (surfaceDataId == 0)
			return 0;

		PreparedStatement psSurfaceData = null;

		switch (abstractSurfData.getCityGMLClass()) {
		case X3DMATERIAL:
			psSurfaceData = psX3DMaterial;
			break;
		case PARAMETERIZEDTEXTURE:
			psSurfaceData = psParaTex;
			break;
		case GEOREFERENCEDTEXTURE:
			psSurfaceData = psGeoTex;
			break;
		}

		if (psSurfaceData == null)
			return 0;

		// Id
		psSurfaceData.setLong(1, surfaceDataId);

		// gml:id
		if (replaceGmlId) {
			String gmlId = UUIDManager.randomUUID();

			// mapping entry
			if (abstractSurfData.getId() != null)
				dbImporterManager.putGmlId(abstractSurfData.getId(), surfaceDataId, -1, false, gmlId, abstractSurfData.getCityGMLClass());

			abstractSurfData.setId(gmlId);

		} else {
			if (abstractSurfData.getId() != null)
				dbImporterManager.putGmlId(abstractSurfData.getId(), surfaceDataId, abstractSurfData.getCityGMLClass());
			else
				abstractSurfData.setId(UUIDManager.randomUUID());
		}

		psSurfaceData.setString(2, abstractSurfData.getId());

		// gml:name
		if (abstractSurfData.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(abstractSurfData, gmlNameDelimiter);

			psSurfaceData.setString(3, dbGmlName[0]);
			psSurfaceData.setString(4, dbGmlName[1]);
		} else {
			psSurfaceData.setNull(3, Types.VARCHAR);
			psSurfaceData.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (abstractSurfData.getDescription() != null) {
			String description = abstractSurfData.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psSurfaceData.setString(5, description);
		} else {
			psSurfaceData.setNull(5, Types.VARCHAR);
		}

		// isFront
		if (abstractSurfData.getIsFront() != null && !abstractSurfData.getIsFront())
			psSurfaceData.setInt(6, 0);
		else
			psSurfaceData.setInt(6, 1);

		// type
		psSurfaceData.setString(7, abstractSurfData.getCityGMLClass().toString());

		// fill other columns depending on the type
		if (abstractSurfData.getCityGMLClass() == CityGMLClass.X3DMATERIAL) {
			X3DMaterial material = (X3DMaterial)abstractSurfData;

			// shininess
			if (material.getShininess() != null)
				psSurfaceData.setDouble(8, material.getShininess());
			else
				psSurfaceData.setNull(8, Types.DOUBLE);

			// transparency
			if (material.getTransparency() != null)
				psSurfaceData.setDouble(9, material.getTransparency());
			else
				psSurfaceData.setNull(9, Types.DOUBLE);

			// ambientIntensity
			if (material.getAmbientIntensity() != null)
				psSurfaceData.setDouble(10, material.getAmbientIntensity());
			else
				psSurfaceData.setNull(10, Types.DOUBLE);

			// specular color
			if (material.getSpecularColor() != null) {
				Color color = material.getSpecularColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(11, colorString);
			} else
				psSurfaceData.setNull(11, Types.VARCHAR);

			// diffuse color
			if (material.getDiffuseColor() != null) {
				Color color = material.getDiffuseColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(12, colorString);
			} else
				psSurfaceData.setNull(12, Types.VARCHAR);

			// emissive color
			if (material.getEmissiveColor() != null) {
				Color color = material.getEmissiveColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(13, colorString);
			} else
				psSurfaceData.setNull(13, Types.VARCHAR);

			if (material.getIsSmooth() != null && material.getIsSmooth())
				psSurfaceData.setInt(14, 1);
			else
				psSurfaceData.setInt(14, 0);

			psSurfaceData.addBatch();

			List<String> targetList = material.getTarget();
			if (targetList != null) {
				for (String target : targetList) {
					// xlink
					if (target != null && target.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkTextureParam(
								surfaceDataId,
								target,
								DBXlinkTextureParamEnum.X3DMATERIAL));
					}
				}
			}
		}

		else if (abstractSurfData.getCityGMLClass() == CityGMLClass.PARAMETERIZEDTEXTURE ||
				abstractSurfData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
			AbstractTexture absTex = (AbstractTexture)abstractSurfData;

			if (absTex.getImageURI() != null) {
				String imageURI = absTex.getImageURI().trim();
				psSurfaceData.setString(8, imageURI);

				if (importTextureImage) {
					dbImporterManager.propagateXlink(new DBXlinkExternalFile(
							surfaceDataId,
							imageURI,
							DBXlinkExternalFileEnum.TEXTURE_IMAGE
					));
				}

			} else
				psSurfaceData.setNull(8, Types.VARCHAR);

			if (absTex.getMimeType() != null)
				psSurfaceData.setString(9, absTex.getMimeType());
			else
				psSurfaceData.setNull(9, Types.VARCHAR);

			if (absTex.getTextureType() != null)
				psSurfaceData.setString(10, absTex.getTextureType().getValue());
			else
				psSurfaceData.setNull(10, Types.VARCHAR);

			if (absTex.getWrapMode() != null)
				psSurfaceData.setString(11, absTex.getWrapMode().getValue());
			else
				psSurfaceData.setNull(11, Types.VARCHAR);

			if (absTex.getBorderColor() != null)
				psSurfaceData.setString(12, Util.collection2string(absTex.getBorderColor().toList(), " "));
			else
				psSurfaceData.setNull(12, Types.VARCHAR);
		}

		if (abstractSurfData.getCityGMLClass() == CityGMLClass.PARAMETERIZEDTEXTURE) {
			psSurfaceData.addBatch();

			//xlink
			ParameterizedTexture paraTex = (ParameterizedTexture)abstractSurfData;
			List<TextureAssociation> targetList = paraTex.getTarget();
			if (targetList != null) {
				long targetId = 0;

				for (TextureAssociation target : targetList) {
					String targetURI = target.getUri();

					TextureParameterization texPara = target.getTextureParameterization();
					if (texPara != null) {
						String texParamGmlId = texPara.getId();

						switch (texPara.getCityGMLClass()) {
						case TEXCOORDGEN:
							TexCoordGen texCoordGen = (TexCoordGen)texPara;

							if (texCoordGen.getWorldToTexture() != null) {
								String worldToTexture = Util.collection2string(texCoordGen.getWorldToTexture().toList(), " ");

								DBXlinkTextureParam xlink = new DBXlinkTextureParam(
										surfaceDataId,
										targetURI,
										DBXlinkTextureParamEnum.TEXCOORDGEN);

								xlink.setTextureParameterization(true);
								xlink.setTexParamGmlId(texParamGmlId);
								xlink.setWorldToTexture(worldToTexture);

								dbImporterManager.propagateXlink(xlink);
							}

							break;
						case TEXCOORDLIST:
							TexCoordList texCoordList = (TexCoordList)texPara;
							targetId++;

							List<TextureCoordinates> coordList = texCoordList.getTextureCoordinates();
							if (coordList != null) {
								for (TextureCoordinates texCoord : coordList) {
									String ring = texCoord.getRing();

									if (ring != null && ring.length() != 0 && texCoord.getValue() != null) {
										String coords = Util.collection2string(texCoord.getValue(), " ");

										DBXlinkTextureParam xlink = new DBXlinkTextureParam(
												surfaceDataId,
												ring,
												DBXlinkTextureParamEnum.TEXCOORDLIST);

										xlink.setTextureParameterization(true);
										xlink.setTexParamGmlId(texParamGmlId);
										xlink.setTextureCoord(coords);
										xlink.setTargetURI(targetURI);
										xlink.setTexCoordListId(surfaceDataId + "_" + targetId);

										dbImporterManager.propagateXlink(xlink);
									}
								}
							}


							break;
						}
					} else {
						String href = target.getHref();

						if (href != null && href.length() != 0) {
							DBXlinkTextureParam xlink = new DBXlinkTextureParam(
									surfaceDataId,
									href,
									DBXlinkTextureParamEnum.XLINK_TEXTUREASSOCIATION
							);

							xlink.setTargetURI(targetURI);
							dbImporterManager.propagateXlink(xlink);
						}
					}
				}
			}
		}

		else if (abstractSurfData.getCityGMLClass() == CityGMLClass.GEOREFERENCEDTEXTURE) {
			GeoreferencedTexture geoTex = (GeoreferencedTexture)abstractSurfData;

			if (geoTex.getPreferWorldFile() != null && !geoTex.getPreferWorldFile())
				psSurfaceData.setInt(13, 0);
			else
				psSurfaceData.setInt(13, 1);

			if (geoTex.getOrientation() != null) {
				List<Double> coordList = geoTex.getOrientation().toList();
				String orientation = Util.collection2string(coordList, " ");

				if (orientation != null)
					psSurfaceData.setString(14, orientation);
				else
					psSurfaceData.setNull(14, Types.VARCHAR);
			} else
				psSurfaceData.setNull(14, Types.VARCHAR);

			if (geoTex.getReferencePoint() != null) {
				PointProperty pointProp = geoTex.getReferencePoint();

				// the CityGML spec states that referencePoint shall be 2d only
				if (pointProp.getPoint() != null) {
					Point point = pointProp.getPoint();
					List<Double> points = point.toList();

					if (points != null && !points.isEmpty()) {
						JGeometry geom = new JGeometry(points.get(0), points.get(1), Integer.valueOf(dbSrid));
						STRUCT obj = SyncJGeometry.syncStore(geom, batchConn);

						psSurfaceData.setObject(15, obj);
					} else
						psSurfaceData.setNull(15, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				} else {
					// xlink is not supported...
					LogMessageEvent log = new LogMessageEvent(
							"GeoreferencedTexture: Xlink-Verweise auf Referenzpunkte werden nicht unterstützt.",
							LogMessageEnum.ERROR
					);
					dbImporterManager.propagateEvent(log);
				}
			} else
				psSurfaceData.setNull(15, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

			if (geoTex.getImageURI() != null && geoTex.getOrientation() == null && geoTex.getReferencePoint() == null) {
				// do we have a world file?!
				DBXlinkExternalFile xlink = new DBXlinkExternalFile(
						surfaceDataId,
						geoTex.getImageURI(),
						DBXlinkExternalFileEnum.WORLD_FILE
				);

				dbImporterManager.propagateXlink(xlink);
			}

			psSurfaceData.addBatch();

			List<String> targetList = geoTex.getTarget();
			if (targetList != null) {
				for (String target : targetList) {
					// xlink
					if (target != null && target.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkTextureParam(
								surfaceDataId,
								target,
								DBXlinkTextureParamEnum.GEOREFERENCEDTEXTURE
						));
					}
				}
			}
		}

		//appear2surfacedata
		dbAppearToSurfaceDataImporter.insert(surfaceDataId, parentId);

		return surfaceDataId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psX3DMaterial.executeBatch();
		psParaTex.executeBatch();
		psGeoTex.executeBatch();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.SURFACE_DATA;
	}

}
