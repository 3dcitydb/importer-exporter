/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceDataToTexImage;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureCoordList;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParamEnum;
import org.citydb.modules.citygml.importer.util.LocalTextureCoordinatesResolver;
import org.citydb.modules.citygml.importer.util.LocalTextureCoordinatesResolver.SurfaceGeometryTarget;
import org.citydb.util.Util;
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

public class DBSurfaceData implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psX3DMaterial;
	private PreparedStatement psParaTex;
	private PreparedStatement psGeoTex;
	private DBTextureParam textureParamImporter;
	private DBTexImage textureImageImporter;
	private DBAppearToSurfaceData appearToSurfaceDataImporter;
	private LocalTextureCoordinatesResolver localTexCoordResolver;

	private int dbSrid;
	private boolean replaceGmlId;
	private boolean affineTransformation;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBSurfaceData(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder x3dStmt = new StringBuilder()
		.append("insert into SURFACE_DATA (ID, GMLID, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, OBJECTCLASS_ID, ")
		.append("X3D_SHININESS, X3D_TRANSPARENCY, X3D_AMBIENT_INTENSITY, X3D_SPECULAR_COLOR, X3D_DIFFUSE_COLOR, X3D_EMISSIVE_COLOR, X3D_IS_SMOOTH) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psX3DMaterial = batchConn.prepareStatement(x3dStmt.toString());

		StringBuilder paraStmt = new StringBuilder()
		.append("insert into SURFACE_DATA (ID, GMLID, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, OBJECTCLASS_ID, ")
		.append("TEX_TEXTURE_TYPE, TEX_WRAP_MODE, TEX_BORDER_COLOR) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psParaTex = batchConn.prepareStatement(paraStmt.toString());

		StringBuilder geoStmt = new StringBuilder()
		.append("insert into SURFACE_DATA (ID, GMLID, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, OBJECTCLASS_ID, ")
		.append("TEX_TEXTURE_TYPE, TEX_WRAP_MODE, TEX_BORDER_COLOR, ")
		.append("GT_PREFER_WORLDFILE, GT_ORIENTATION, GT_REFERENCE_POINT) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psGeoTex = batchConn.prepareStatement(geoStmt.toString());

		textureParamImporter = (DBTextureParam)dbImporterManager.getDBImporter(DBImporterEnum.TEXTURE_PARAM);
		textureImageImporter = (DBTexImage)dbImporterManager.getDBImporter(DBImporterEnum.TEX_IMAGE);
		appearToSurfaceDataImporter = (DBAppearToSurfaceData)dbImporterManager.getDBImporter(DBImporterEnum.APPEAR_TO_SURFACE_DATA);
		localTexCoordResolver = dbImporterManager.getLocalTextureCoordinatesResolver();
	}

	public long insert(AbstractSurfaceData abstractSurfData, long parentId, boolean isLocalAppearance) throws SQLException {
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
		default:
			return 0;
		}

		long surfaceDataId = dbImporterManager.getDBId(DBSequencerEnum.SURFACE_DATA_ID_SEQ);
		if (surfaceDataId == 0)
			return 0;

		// Id
		psSurfaceData.setLong(1, surfaceDataId);

		// gml:id
		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (abstractSurfData.isSetId())
				dbImporterManager.putUID(abstractSurfData.getId(), surfaceDataId, -1, false, gmlId, abstractSurfData.getCityGMLClass());

			abstractSurfData.setId(gmlId);

		} else {
			if (abstractSurfData.isSetId())
				dbImporterManager.putUID(abstractSurfData.getId(), surfaceDataId, abstractSurfData.getCityGMLClass());
			else
				abstractSurfData.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		psSurfaceData.setString(2, abstractSurfData.getId());

		// gml:name
		if (abstractSurfData.isSetName()) {
			String[] dbGmlName = Util.codeList2string(abstractSurfData.getName());
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

		// app:isFront
		if (abstractSurfData.isSetIsFront() && !abstractSurfData.getIsFront())
			psSurfaceData.setInt(6, 0);
		else
			psSurfaceData.setInt(6, 1);

		// OBJECTCLASS_ID
		psSurfaceData.setInt(7, Util.cityObject2classId(abstractSurfData.getCityGMLClass()));

		// fill other columns depending on the type
		String featureSignature = Util.getFeatureSignature(abstractSurfData.getCityGMLClass(), abstractSurfData.getId());
		if (abstractSurfData.getCityGMLClass() == CityGMLClass.X3D_MATERIAL) {
			X3DMaterial material = (X3DMaterial)abstractSurfData;

			// app:shininess
			if (material.isSetShininess())
				psSurfaceData.setDouble(8, material.getShininess());
			else
				psSurfaceData.setNull(8, Types.DOUBLE);

			// app:transparency
			if (material.isSetTransparency())
				psSurfaceData.setDouble(9, material.getTransparency());
			else
				psSurfaceData.setNull(9, Types.DOUBLE);

			// app:ambientIntensity
			if (material.isSetAmbientIntensity())
				psSurfaceData.setDouble(10, material.getAmbientIntensity());
			else
				psSurfaceData.setNull(10, Types.DOUBLE);

			// app:specularColor
			if (material.isSetSpecularColor()) {
				Color color = material.getSpecularColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(11, colorString);
			} else
				psSurfaceData.setNull(11, Types.VARCHAR);

			// app:diffuseColor
			if (material.isSetDiffuseColor()) {
				Color color = material.getDiffuseColor();
				String colorString = color.getRed() + " " + color.getGreen() + " " + color.getBlue();
				psSurfaceData.setString(12, colorString);
			} else
				psSurfaceData.setNull(12, Types.VARCHAR);

			// app:emissiveColor
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
			if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
				dbImporterManager.executeBatch(DBImporterEnum.SURFACE_DATA);

			if (material.isSetTarget()) {
				HashSet<String> duplicateTargets = new HashSet<String>(material.getTarget().size());

				for (String target : material.getTarget()) {
					if (target != null && target.length() != 0) {

						// check for duplicate targets
						if (!duplicateTargets.add(target.replaceAll("^#", ""))) {
							LOG.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target '").append(target).append("'.").toString());
							continue;
						}

						dbImporterManager.propagateXlink(new DBXlinkTextureParam(
								surfaceDataId,
								target,
								DBXlinkTextureParamEnum.X3DMATERIAL));
					}
				}
			}
		}

		else {
			AbstractTexture absTex = (AbstractTexture)abstractSurfData;

			// handle texture image
			long texImageId = 0;
			if (absTex.isSetImageURI()) {
				texImageId = textureImageImporter.insert(absTex, surfaceDataId);

				if (texImageId != 0) {
					dbImporterManager.propagateXlink(new DBXlinkSurfaceDataToTexImage(
							surfaceDataId, 
							texImageId));
				}
			}

			if (absTex.isSetTextureType())
				psSurfaceData.setString(8, absTex.getTextureType().getValue());
			else
				psSurfaceData.setNull(8, Types.VARCHAR);

			if (absTex.isSetWrapMode())
				psSurfaceData.setString(9, absTex.getWrapMode().getValue());
			else
				psSurfaceData.setNull(9, Types.VARCHAR);

			if (absTex.isSetBorderColor())
				psSurfaceData.setString(10, Util.collection2string(absTex.getBorderColor().toList(), " "));
			else
				psSurfaceData.setNull(10, Types.VARCHAR);

			// ParameterizedTexture
			if (abstractSurfData.getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE) {
				psSurfaceData.addBatch();
				if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
					dbImporterManager.executeBatch(DBImporterEnum.SURFACE_DATA);

				ParameterizedTexture paraTex = (ParameterizedTexture)abstractSurfData;
				if (paraTex.isSetTarget()) {
					HashSet<String> duplicateTargets = new HashSet<String>(paraTex.getTarget().size());
					int targetId = 0;

					for (TextureAssociation target : paraTex.getTarget()) {
						String targetURI = target.getUri();
						if (targetURI == null || targetURI.length() == 0)
							continue;

						// check for duplicate targets
						if (!duplicateTargets.add(targetURI.replaceAll("^#", ""))) {
							LOG.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target '").append(targetURI).append("'.").toString());
							continue;
						}

						if (target.isSetTextureParameterization()) {
							AbstractTextureParameterization texPara = target.getTextureParameterization();
							String texParamGmlId = texPara.getId();

							if (texPara.getCityGMLClass() == CityGMLClass.TEX_COORD_GEN) {
								TexCoordGen texCoordGen = (TexCoordGen)texPara;
								if (!texCoordGen.isSetWorldToTexture())
									continue;

								Matrix worldToTexture = texCoordGen.getWorldToTexture().getMatrix();
								if (affineTransformation)
									worldToTexture = dbImporterManager.getAffineTransformer().transformWorldToTexture(worldToTexture);

								String worldToTextureString = Util.collection2string(worldToTexture.toRowPackedList(), " ");

								boolean isResolved = false;
								if (isLocalAppearance) {
									// check whether we can query the target from the in-memory gml:id cache
									long surfaceGeometryId = dbImporterManager.getDBIdFromMemory(targetURI.replaceAll("^#", ""), CityGMLClass.ABSTRACT_GML_GEOMETRY);
									isResolved = surfaceGeometryId != 0;

									if (isResolved) {
										textureParamImporter.insert(worldToTextureString, surfaceDataId, surfaceGeometryId);

										if (texParamGmlId != null) {
											// make sure xlinks to this texture parameterization can be resolved
											dbImporterManager.propagateXlink(new DBXlinkTextureAssociationTarget(
													surfaceDataId,
													surfaceGeometryId,
													texParamGmlId));
										}
									}
								}

								if (!isResolved) {
									DBXlinkTextureParam xlink = new DBXlinkTextureParam(
											surfaceDataId,
											targetURI,
											DBXlinkTextureParamEnum.TEXCOORDGEN);

									xlink.setTextureParameterization(true);
									xlink.setTexParamGmlId(texParamGmlId);
									xlink.setWorldToTexture(worldToTextureString);

									dbImporterManager.propagateXlink(xlink);
								}

							} else {
								TexCoordList texCoordList = (TexCoordList)texPara;
								if (!texCoordList.isSetTextureCoordinates())
									continue;
								
								targetId++;
								int texCoordId = 0;
								
								for (TextureCoordinates texCoord : texCoordList.getTextureCoordinates()) {
									String ring = texCoord.getRing();
									if (ring == null || ring.length() == 0 || !texCoord.isSetValue()) {
										continue;
									}

									String ringId = ring.replaceAll("^#", "");
									
									// check for duplicate references to the same geometry object
									if (!duplicateTargets.add(ringId)) {
										LOG.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target ring '").append(ringId).append("'.").toString());
										continue;
									}
									
									// fix unclosed texture coordinates
									int nrOfCoord = texCoord.getValue().size();
									if (!texCoord.getValue().get(0).equals(texCoord.getValue().get(nrOfCoord - 2)) ||
											!texCoord.getValue().get(1).equals(texCoord.getValue().get(nrOfCoord - 1))) {
										texCoord.getValue().add(texCoord.getValue().get(0));
										texCoord.getValue().add(texCoord.getValue().get(1));
										LOG.debug(new StringBuilder(featureSignature).append(": Fixed unclosed texture coordinates for ring '").append(ringId).append("'.").toString());										
									}

									// check for minimum number of texture coordinates
									if (texCoord.getValue().size() < 8) {
										LOG.error(new StringBuilder(featureSignature).append(": Less than four texture coordinates for ring '").append(ringId).append("'.").toString());										
										continue;
									}

									// check for even number of texture coordinates
									if ((texCoord.getValue().size() & 1) == 1) {
										LOG.error(new StringBuilder(featureSignature).append(": Odd number of texture coordinates for ring '").append(ringId).append("'.").toString());										
										continue;
									}

									boolean isResolved = false;
									if (isLocalAppearance) {
										// announce texture coordinates to local appearance resolver. this will return false
										// in case there is no corresponding linear ring
										isResolved = localTexCoordResolver.setTextureCoordinates(ringId, texCoord.getValue());
									}

									if (!isResolved) {
										// in case of a global appearance or lacking information for resolving
										// a local appearance, we send the target information to the cache
										double[] coordinates = new double[texCoord.getValue().size()];
										for (int i = 0; i < texCoord.getValue().size(); i++)
											coordinates[i] = texCoord.getValue().get(i);
											
										dbImporterManager.propagateXlink(new DBXlinkTextureCoordList(
												surfaceDataId, 
												ringId, 
												texCoordId++ == 0 ? texParamGmlId : null, 
												GeometryObject.createPolygon(coordinates, 2, 0),
												targetId));
									}
								}

								if (isLocalAppearance) {
									for (SurfaceGeometryTarget tmp : localTexCoordResolver.getLocalContext()) {
										if (!tmp.isComplete()) {
											LOG.error(new StringBuilder(featureSignature).append(": Not all rings in target geometry '").append(targetURI).append("' receive texture coordinates. Skipping target.").toString());										
											continue;
										}

										textureParamImporter.insert(tmp, surfaceDataId);

										// make sure xlinks to this texture parameterization can be resolved
										if (texParamGmlId != null) {
											dbImporterManager.propagateXlink(new DBXlinkTextureAssociationTarget(
													surfaceDataId,
													tmp.getSurfaceGeometryId(),
													texParamGmlId));
										}
									}

									localTexCoordResolver.clearLocalContext();
								}
							}

						} else {
							String href = target.getHref();

							if (href != null && href.length() != 0) {
								dbImporterManager.propagateXlink(new DBXlinkTextureAssociation(
										surfaceDataId,
										href,
										targetURI));
							}
						}
					}
				}
			}

			// GeoreferencedTexture
			else if (abstractSurfData.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE) {
				GeoreferencedTexture geoTex = (GeoreferencedTexture)abstractSurfData;

				if (geoTex.isSetPreferWorldFile() && !geoTex.getPreferWorldFile())
					psSurfaceData.setInt(11, 0);
				else
					psSurfaceData.setInt(11, 1);

				if (geoTex.isSetOrientation()) {
					Matrix orientation = geoTex.getOrientation().getMatrix();
					if (affineTransformation)
						orientation = dbImporterManager.getAffineTransformer().transformGeoreferencedTextureOrientation(orientation);

					psSurfaceData.setString(12, Util.collection2string(orientation.toRowPackedList(), " "));
				} else
					psSurfaceData.setNull(12, Types.VARCHAR);

				GeometryObject geom = null;
				if (geoTex.isSetReferencePoint()) {
					PointProperty pointProp = geoTex.getReferencePoint();

					// the CityGML spec states that the referencePoint shall be 2d only
					if (pointProp.isSetPoint()) {
						Point point = pointProp.getPoint();
						List<Double> coords = point.toList3d();

						if (coords != null && !coords.isEmpty()) {
							if (affineTransformation)
								dbImporterManager.getAffineTransformer().transformCoordinates(coords);

							geom = GeometryObject.createPoint(new double[]{coords.get(0), coords.get(1)}, 2, dbSrid);
						}
					} else {
						// xlink is not supported...	
						String href = pointProp.getHref();
						if (href != null && href.length() != 0)
							LOG.error("XLink reference '" + href + "' to reference point of " + CityGMLClass.GEOREFERENCED_TEXTURE + " is not supported.");
					}
				}

				if (geom != null)
					psSurfaceData.setObject(13, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geom, batchConn));
				else
					psSurfaceData.setNull(13, nullGeometryType, nullGeometryTypeName);

				psSurfaceData.addBatch();
				if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
					dbImporterManager.executeBatch(DBImporterEnum.SURFACE_DATA);

				if (geoTex.isSetTarget()) {
					HashSet<String> duplicateTargets = new HashSet<String>(geoTex.getTarget().size());

					for (String target : geoTex.getTarget()) {
						if (target != null && target.length() != 0) {

							// check for duplicate targets
							if (!duplicateTargets.add(target.replaceAll("^#", ""))) {
								LOG.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target '").append(target).append("'.").toString());
								continue;
							}

							boolean isResolved = false;
							if (isLocalAppearance) {
								// check whether we can query the target from the in-memory gml:id cache
								long surfaceGeometryId = dbImporterManager.getDBIdFromMemory(target.replaceAll("^#", ""), CityGMLClass.ABSTRACT_GML_GEOMETRY);
								isResolved = surfaceGeometryId != 0;
								if (isResolved)
									textureParamImporter.insert(surfaceDataId, surfaceGeometryId);								
							}

							if (!isResolved) {
								dbImporterManager.propagateXlink(new DBXlinkTextureParam(
										surfaceDataId,
										target,
										DBXlinkTextureParamEnum.GEOREFERENCEDTEXTURE
										));
							}
						}
					}
				}
			}
		}

		// appear2surfacedata
		appearToSurfaceDataImporter.insert(surfaceDataId, parentId);

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
