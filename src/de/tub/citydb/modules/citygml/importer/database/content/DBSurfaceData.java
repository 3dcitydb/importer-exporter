/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.AbstractTextureParameterization;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFileEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParamEnum;
import de.tub.citydb.util.Util;

public class DBSurfaceData implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psX3DMaterial;
	private PreparedStatement psParaTex;
	private PreparedStatement psGeoTex;
	private DBAppearToSurfaceData dbAppearToSurfaceDataImporter;

	private int dbSrid;
	private boolean replaceGmlId;
	private boolean importTextureImage;
	private boolean affineTransformation;
	private int batchCounter;

	public DBSurfaceData(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().getSrid();
		importTextureImage = config.getProject().getImporter().getAppearances().isSetImportTextureFiles();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
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
		case X3D_MATERIAL:
			psSurfaceData = psX3DMaterial;
			break;
		case PARAMETERIZED_TEXTURE:
			psSurfaceData = psParaTex;
			break;
		case GEOREFERENCED_TEXTURE:
			psSurfaceData = psGeoTex;
			break;
		}

		if (psSurfaceData == null)
			return 0;

		// Id
		psSurfaceData.setLong(1, surfaceDataId);

		// gml:id
		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (abstractSurfData.isSetId())
				dbImporterManager.putGmlId(abstractSurfData.getId(), surfaceDataId, -1, false, gmlId, abstractSurfData.getCityGMLClass());

			abstractSurfData.setId(gmlId);

		} else {
			if (abstractSurfData.isSetId())
				dbImporterManager.putGmlId(abstractSurfData.getId(), surfaceDataId, abstractSurfData.getCityGMLClass());
			else
				abstractSurfData.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		psSurfaceData.setString(2, abstractSurfData.getId());

		// gml:name
		if (abstractSurfData.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(abstractSurfData);

			psSurfaceData.setString(3, dbGmlName[0]);
			psSurfaceData.setString(4, dbGmlName[1]);
		} else {
			psSurfaceData.setNull(3, Types.VARCHAR);
			psSurfaceData.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (abstractSurfData.isSetDescription()) {
			String description = abstractSurfData.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psSurfaceData.setString(5, description);
		} else {
			psSurfaceData.setNull(5, Types.VARCHAR);
		}

		// isFront
		if (abstractSurfData.isSetIsFront() && !abstractSurfData.getIsFront())
			psSurfaceData.setInt(6, 0);
		else
			psSurfaceData.setInt(6, 1);

		// type
		psSurfaceData.setString(7, TypeAttributeValueEnum.fromCityGMLClass(abstractSurfData.getCityGMLClass()).toString());

		// fill other columns depending on the type
		if (abstractSurfData.getCityGMLClass() == CityGMLClass.X3D_MATERIAL) {
			X3DMaterial material = (X3DMaterial)abstractSurfData;

			// shininess
			if (material.isSetShininess())
				psSurfaceData.setDouble(8, material.getShininess());
			else
				psSurfaceData.setNull(8, Types.DOUBLE);

			// transparency
			if (material.isSetTransparency())
				psSurfaceData.setDouble(9, material.getTransparency());
			else
				psSurfaceData.setNull(9, Types.DOUBLE);

			// ambientIntensity
			if (material.isSetAmbientIntensity())
				psSurfaceData.setDouble(10, material.getAmbientIntensity());
			else
				psSurfaceData.setNull(10, Types.DOUBLE);

			// specular color
			if (material.isSetSpecularColor()) {
				Color color = material.getSpecularColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(11, colorString);
			} else
				psSurfaceData.setNull(11, Types.VARCHAR);

			// diffuse color
			if (material.isSetDiffuseColor()) {
				Color color = material.getDiffuseColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(12, colorString);
			} else
				psSurfaceData.setNull(12, Types.VARCHAR);

			// emissive color
			if (material.isSetEmissiveColor()) {
				Color color = material.getEmissiveColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(13, colorString);
			} else
				psSurfaceData.setNull(13, Types.VARCHAR);

			if (material.isSetIsSmooth() && material.getIsSmooth())
				psSurfaceData.setInt(14, 1);
			else
				psSurfaceData.setInt(14, 0);

			psSurfaceData.addBatch();
			if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
				dbImporterManager.executeBatch(DBImporterEnum.SURFACE_DATA);

			if (material.isSetTarget()) {
				for (String target : material.getTarget()) {
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

		else if (abstractSurfData.getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE ||
				abstractSurfData.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE) {
			AbstractTexture absTex = (AbstractTexture)abstractSurfData;

			if (absTex.isSetImageURI()) {
				String imageURI = absTex.getImageURI().trim();
				psSurfaceData.setString(8, imageURI);

				if (importTextureImage) {
					dbImporterManager.propagateXlink(new DBXlinkTextureFile(
							surfaceDataId,
							imageURI,
							DBXlinkTextureFileEnum.TEXTURE_IMAGE
					));
				}

			} else
				psSurfaceData.setNull(8, Types.VARCHAR);

			if (absTex.isSetMimeType())
				psSurfaceData.setString(9, absTex.getMimeType());
			else
				psSurfaceData.setNull(9, Types.VARCHAR);

			if (absTex.isSetTextureType())
				psSurfaceData.setString(10, absTex.getTextureType().getValue());
			else
				psSurfaceData.setNull(10, Types.VARCHAR);

			if (absTex.isSetWrapMode())
				psSurfaceData.setString(11, absTex.getWrapMode().getValue());
			else
				psSurfaceData.setNull(11, Types.VARCHAR);

			if (absTex.isSetBorderColor())
				psSurfaceData.setString(12, Util.collection2string(absTex.getBorderColor().toList(), " "));
			else
				psSurfaceData.setNull(12, Types.VARCHAR);
		}

		if (abstractSurfData.getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE) {
			psSurfaceData.addBatch();
			if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
				dbImporterManager.executeBatch(DBImporterEnum.SURFACE_DATA);

			//xlink
			ParameterizedTexture paraTex = (ParameterizedTexture)abstractSurfData;
			if (paraTex.isSetTarget()) {
				long targetId = 0;

				for (TextureAssociation target : paraTex.getTarget()) {
					String targetURI = target.getUri();

					if (target.isSetTextureParameterization()) {
						AbstractTextureParameterization texPara = target.getTextureParameterization();
						String texParamGmlId = texPara.getId();

						switch (texPara.getCityGMLClass()) {
						case TEX_COORD_GEN:
							TexCoordGen texCoordGen = (TexCoordGen)texPara;

							if (texCoordGen.isSetWorldToTexture()) {
								Matrix worldToTexture = texCoordGen.getWorldToTexture().getMatrix();
								if (affineTransformation)
									worldToTexture = dbImporterManager.getAffineTransformer().transformWorldToTexture(worldToTexture);
								
								String worldToTextureString = Util.collection2string(worldToTexture.toRowPackedList(), " ");

								DBXlinkTextureParam xlink = new DBXlinkTextureParam(
										surfaceDataId,
										targetURI,
										DBXlinkTextureParamEnum.TEXCOORDGEN);

								xlink.setTextureParameterization(true);
								xlink.setTexParamGmlId(texParamGmlId);
								xlink.setWorldToTexture(worldToTextureString);

								dbImporterManager.propagateXlink(xlink);
							}

							break;
						case TEX_COORD_LIST:
							TexCoordList texCoordList = (TexCoordList)texPara;
							targetId++;

							if (texCoordList.isSetTextureCoordinates()) {
								for (TextureCoordinates texCoord : texCoordList.getTextureCoordinates()) {
									String ring = texCoord.getRing();

									if (ring != null && ring.length() != 0 && texCoord.isSetValue()) {
										
										// check for even number of texture coordinates
										if ((texCoord.getValue().size() & 1) == 1) {
											texCoord.addValue(0.0);
											
											StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
													abstractSurfData.getCityGMLClass(), 
													abstractSurfData.getId()));
											
											msg.append(": Odd number of texture coordinates found. Adding 0.0 to fix this.");
											LOG.error(msg.toString());
										}
										
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

		else if (abstractSurfData.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE) {
			GeoreferencedTexture geoTex = (GeoreferencedTexture)abstractSurfData;

			if (geoTex.isSetPreferWorldFile() && !geoTex.getPreferWorldFile())
				psSurfaceData.setInt(13, 0);
			else
				psSurfaceData.setInt(13, 1);

			if (geoTex.isSetOrientation()) {
				Matrix orientation = geoTex.getOrientation().getMatrix();
				if (affineTransformation)
					orientation = dbImporterManager.getAffineTransformer().transformGeoreferencedTextureOrientation(orientation);

				psSurfaceData.setString(14, Util.collection2string(orientation.toRowPackedList(), " "));
			} else
				psSurfaceData.setNull(14, Types.VARCHAR);

			if (geoTex.isSetReferencePoint()) {
				PointProperty pointProp = geoTex.getReferencePoint();

				// the CityGML spec states that referencePoint shall be 2d only
				if (pointProp.isSetPoint()) {
					Point point = pointProp.getPoint();
					List<Double> coords = point.toList3d();

					if (coords != null && !coords.isEmpty()) {
						if (affineTransformation)
							dbImporterManager.getAffineTransformer().transformCoordinates(coords);

						JGeometry geom = new JGeometry(coords.get(0), coords.get(1), dbSrid);
						STRUCT obj = SyncJGeometry.syncStore(geom, batchConn);

						psSurfaceData.setObject(15, obj);
					} else
						psSurfaceData.setNull(15, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				} else {
					// xlink is not supported...	
					String href = pointProp.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to reference point is not supported.");
					}
				}
			} else
				psSurfaceData.setNull(15, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

			// do we have a world file?!
			if (geoTex.isSetImageURI() && !geoTex.isSetOrientation() && !geoTex.isSetReferencePoint()) {
				DBXlinkTextureFile xlink = new DBXlinkTextureFile(
						surfaceDataId,
						geoTex.getImageURI(),
						DBXlinkTextureFileEnum.WORLD_FILE
				);

				dbImporterManager.propagateXlink(xlink);
			}

			psSurfaceData.addBatch();
			if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
				dbImporterManager.executeBatch(DBImporterEnum.SURFACE_DATA);

			if (geoTex.isSetTarget()) {
				for (String target : geoTex.getTarget()) {
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
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psX3DMaterial.close();
		psParaTex.close();
		psGeoTex.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.SURFACE_DATA;
	}

}
