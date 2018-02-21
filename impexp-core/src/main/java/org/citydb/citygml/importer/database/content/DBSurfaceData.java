/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;

import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceDataToTexImage;
import org.citydb.citygml.common.database.xlink.DBXlinkTextureAssociation;
import org.citydb.citygml.common.database.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.citygml.common.database.xlink.DBXlinkTextureCoordList;
import org.citydb.citygml.common.database.xlink.DBXlinkTextureParam;
import org.citydb.citygml.common.database.xlink.DBXlinkTextureParamEnum;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.citygml.importer.util.LocalAppearanceHandler;
import org.citydb.citygml.importer.util.LocalAppearanceHandler.SurfaceGeometryTarget;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.log.Logger;
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
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBSurfaceData implements DBImporter {
	private final Logger log = Logger.getInstance();
	private final Connection batchConn;
	private  final CityGMLImportManager importer;

	private PreparedStatement psX3DMaterial;
	private PreparedStatement psParaTex;
	private PreparedStatement psGeoTex;
	private DBTextureParam textureParamImporter;
	private DBTexImage textureImageImporter;
	private DBAppearToSurfaceData appearToSurfaceDataImporter;
	private LocalAppearanceHandler localAppearanceHandler;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private int dbSrid;
	private boolean replaceGmlId;
	private boolean affineTransformation;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBSurfaceData(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
		if (gmlIdCodespace != null && gmlIdCodespace.length() > 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "', ";
		else
			gmlIdCodespace = null;		

		StringBuilder x3dStmt = new StringBuilder()
				.append("insert into ").append(schema).append(".surface_data (id, gmlid, ").append(gmlIdCodespace != null ? "gmlid_codespace, " : "").append("name, name_codespace, description, is_front, objectclass_id, ")
				.append("x3d_shininess, x3d_transparency, x3d_ambient_intensity, x3d_specular_color, x3d_diffuse_color, x3d_emissive_color, x3d_is_smooth) values ")
				.append("(?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psX3DMaterial = batchConn.prepareStatement(x3dStmt.toString());

		StringBuilder paraStmt = new StringBuilder()
				.append("insert into ").append(schema).append(".surface_data (id, gmlid, ").append(gmlIdCodespace != null ? "gmlid_codespace, " : "").append("name, name_codespace, description, is_front, objectclass_id, ")
				.append("tex_texture_type, tex_wrap_mode, tex_border_color) values ")
				.append("(?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?)");
		psParaTex = batchConn.prepareStatement(paraStmt.toString());

		StringBuilder geoStmt = new StringBuilder()
				.append("insert into ").append(schema).append(".surface_data (id, gmlid, ").append(gmlIdCodespace != null ? "gmlid_codespace, " : "").append("name, name_codespace, description, is_front, objectclass_id, ")
				.append("tex_texture_type, tex_wrap_mode, tex_border_color, ")
				.append("gt_prefer_worldfile, gt_orientation, gt_reference_point) values ")
				.append("(?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psGeoTex = batchConn.prepareStatement(geoStmt.toString());

		textureParamImporter = importer.getImporter(DBTextureParam.class);
		textureImageImporter = importer.getImporter(DBTexImage.class);
		appearToSurfaceDataImporter = importer.getImporter(DBAppearToSurfaceData.class);
		localAppearanceHandler = importer.getLocalAppearanceHandler();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	public long doImport(AbstractSurfaceData surfaceData, long parentId, boolean isLocalAppearance) throws CityGMLImportException, SQLException {
		PreparedStatement psSurfaceData = null;

		FeatureType featureType = importer.getFeatureType(surfaceData);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		if (surfaceData instanceof X3DMaterial)
			psSurfaceData = psX3DMaterial;
		else if (surfaceData instanceof ParameterizedTexture)
			psSurfaceData = psParaTex;
		else if (surfaceData instanceof GeoreferencedTexture)
			psSurfaceData = psGeoTex;
		else {
			importer.logOrThrowErrorMessage(new StringBuilder(importer.getObjectSignature(surfaceData))
					.append(": Unsupported surface data type.").toString());
			return 0;
		}

		long surfaceDataId = importer.getNextSequenceValue(SequenceEnum.SURFACE_DATA_ID_SEQ.getName());

		// import surface data information
		// primary id
		psSurfaceData.setLong(1, surfaceDataId);

		// gml:id
		String origGmlId = surfaceData.getId();		
		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (surfaceData.isSetId())
				importer.putObjectUID(surfaceData.getId(), surfaceDataId, gmlId, featureType.getObjectClassId());

			surfaceData.setId(gmlId);

		} else {
			if (surfaceData.isSetId())
				importer.putObjectUID(surfaceData.getId(), surfaceDataId, featureType.getObjectClassId());
			else
				surfaceData.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		psSurfaceData.setString(2, surfaceData.getId());

		// gml:name
		if (surfaceData.isSetName()) {
			valueJoiner.join(surfaceData.getName(), Code::getValue, Code::getCodeSpace);
			psSurfaceData.setString(3, valueJoiner.result(0));
			psSurfaceData.setString(4, valueJoiner.result(1));
		} else {
			psSurfaceData.setNull(3, Types.VARCHAR);
			psSurfaceData.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (surfaceData.isSetDescription()) {
			String description = surfaceData.getDescription().getValue();
			if (description != null)
				description = description.trim();

			psSurfaceData.setString(5, description);
		} else {
			psSurfaceData.setNull(5, Types.VARCHAR);
		}

		// app:isFront
		if (surfaceData.isSetIsFront() && !surfaceData.getIsFront())
			psSurfaceData.setInt(6, 0);
		else
			psSurfaceData.setInt(6, 1);

		// objectclass id
		psSurfaceData.setInt(7, featureType.getObjectClassId());

		// fill other columns depending on the type
		String featureSignature = importer.getObjectSignature(surfaceData, origGmlId);
		if (surfaceData instanceof X3DMaterial) {
			X3DMaterial material = (X3DMaterial)surfaceData;

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
			if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
				importer.executeBatch(TableEnum.SURFACE_DATA);

			if (material.isSetTarget()) {
				HashSet<String> duplicateTargets = new HashSet<String>(material.getTarget().size());

				for (String target : material.getTarget()) {
					if (target != null && target.length() != 0) {

						// check for duplicate targets
						if (!duplicateTargets.add(target.replaceAll("^#", ""))) {
							log.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target '").append(target).append("'.").toString());
							continue;
						}

						importer.propagateXlink(new DBXlinkTextureParam(
								surfaceDataId,
								target,
								DBXlinkTextureParamEnum.X3DMATERIAL));
					}
				}
			}
		}

		else {
			AbstractTexture absTex = (AbstractTexture)surfaceData;

			// handle texture image
			long texImageId = 0;
			if (absTex.isSetImageURI()) {
				texImageId = textureImageImporter.doImport(absTex, surfaceDataId);

				if (texImageId != 0) {
					importer.propagateXlink(new DBXlinkSurfaceDataToTexImage(
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
				psSurfaceData.setString(10, valueJoiner.join(" ", absTex.getBorderColor().toList()));
			else
				psSurfaceData.setNull(10, Types.VARCHAR);

			// ParameterizedTexture
			if (surfaceData instanceof ParameterizedTexture) {
				psSurfaceData.addBatch();
				if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
					importer.executeBatch(TableEnum.SURFACE_DATA);

				ParameterizedTexture paraTex = (ParameterizedTexture)surfaceData;
				if (paraTex.isSetTarget()) {
					HashSet<String> duplicateTargets = new HashSet<String>(paraTex.getTarget().size());
					int targetId = 0;

					for (TextureAssociation target : paraTex.getTarget()) {
						String targetURI = target.getUri();
						if (targetURI == null || targetURI.length() == 0)
							continue;

						// check for duplicate targets
						if (!duplicateTargets.add(targetURI.replaceAll("^#", ""))) {
							log.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target '").append(targetURI).append("'.").toString());
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
									worldToTexture = importer.getAffineTransformer().transformWorldToTexture(worldToTexture);

								String worldToTextureString = valueJoiner.join(" ", worldToTexture.toRowPackedList());

								boolean isResolved = false;
								if (isLocalAppearance) {
									// check whether we can query the target from the in-memory gml:id cache
									long surfaceGeometryId = importer.getGeometryIdFromMemory(targetURI.replaceAll("^#", ""));
									isResolved = surfaceGeometryId != 0;

									if (isResolved) {
										textureParamImporter.doImport(worldToTextureString, surfaceDataId, surfaceGeometryId);

										if (texParamGmlId != null) {
											// make sure xlinks to this texture parameterization can be resolved
											importer.propagateXlink(new DBXlinkTextureAssociationTarget(
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

									importer.propagateXlink(xlink);
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
										log.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target ring '").append(ringId).append("'.").toString());
										continue;
									}

									// fix unclosed texture coordinates
									int nrOfCoord = texCoord.getValue().size();
									if (!texCoord.getValue().get(0).equals(texCoord.getValue().get(nrOfCoord - 2)) ||
											!texCoord.getValue().get(1).equals(texCoord.getValue().get(nrOfCoord - 1))) {
										texCoord.getValue().add(texCoord.getValue().get(0));
										texCoord.getValue().add(texCoord.getValue().get(1));
										log.debug(new StringBuilder(featureSignature).append(": Fixed unclosed texture coordinates for ring '").append(ringId).append("'.").toString());										
									}

									// check for minimum number of texture coordinates
									if (texCoord.getValue().size() < 8) {
										importer.logOrThrowErrorMessage(new StringBuilder(featureSignature).append(": Less than four texture coordinates for ring '").append(ringId).append("'.").toString());
										continue;
									}

									// check for even number of texture coordinates
									if ((texCoord.getValue().size() & 1) == 1) {
										String msg = new StringBuilder(featureSignature).append(": Odd number of texture coordinates for ring '").append(ringId).append("'.").toString();
										if (!importer.isFailOnError()) {
											log.error(msg);
											continue;
										} else
											throw new CityGMLImportException(msg);
									}

									boolean isResolved = false;
									if (isLocalAppearance) {
										// announce texture coordinates to local appearance handler. this will return false
										// in case there is no corresponding linear ring
										isResolved = localAppearanceHandler.setTextureCoordinates(ringId, texCoord.getValue());
									}

									if (!isResolved) {
										// in case of a global appearance or lacking information for resolving
										// a local appearance, we send the target information to the cache
										double[] coordinates = new double[texCoord.getValue().size()];
										for (int i = 0; i < texCoord.getValue().size(); i++)
											coordinates[i] = texCoord.getValue().get(i);

										importer.propagateXlink(new DBXlinkTextureCoordList(
												surfaceDataId, 
												ringId, 
												texCoordId++ == 0 ? texParamGmlId : null, 
														GeometryObject.createPolygon(coordinates, 2, 0),
														targetId));
									}
								}

								if (isLocalAppearance) {
									for (SurfaceGeometryTarget tmp : localAppearanceHandler.getLocalContext()) {
										if (!tmp.isComplete()) {
											importer.logOrThrowErrorMessage(new StringBuilder(featureSignature).append(": Not all rings in target geometry '").append(targetURI).append("' receive texture coordinates. Skipping target.").toString());
											continue;
										}

										textureParamImporter.doImport(tmp, surfaceDataId);

										// make sure xlinks to this texture parameterization can be resolved
										if (texParamGmlId != null) {
											importer.propagateXlink(new DBXlinkTextureAssociationTarget(
													surfaceDataId,
													tmp.getSurfaceGeometryId(),
													texParamGmlId));
										}
									}

									localAppearanceHandler.clearLocalContext();
								}
							}

						} else {
							String href = target.getHref();

							if (href != null && href.length() != 0) {
								importer.propagateXlink(new DBXlinkTextureAssociation(
										surfaceDataId,
										href,
										targetURI));
							}
						}
					}
				}
			}

			// GeoreferencedTexture
			else if (surfaceData instanceof GeoreferencedTexture) {
				GeoreferencedTexture geoTex = (GeoreferencedTexture)surfaceData;

				if (geoTex.isSetPreferWorldFile() && !geoTex.getPreferWorldFile())
					psSurfaceData.setInt(11, 0);
				else
					psSurfaceData.setInt(11, 1);

				if (geoTex.isSetOrientation()) {
					Matrix orientation = geoTex.getOrientation().getMatrix();
					if (affineTransformation)
						orientation = importer.getAffineTransformer().transformGeoreferencedTextureOrientation(orientation);

					psSurfaceData.setString(12, valueJoiner.join(" ", orientation.toRowPackedList()));
				} else
					psSurfaceData.setNull(12, Types.VARCHAR);

				GeometryObject geom = null;
				if (geoTex.isSetReferencePoint()) {
					PointProperty property = geoTex.getReferencePoint();

					// the CityGML spec states that the referencePoint shall be 2d only
					if (property.isSetPoint()) {
						Point point = property.getPoint();
						List<Double> coords = point.toList3d();

						if (coords != null && !coords.isEmpty()) {
							if (affineTransformation)
								importer.getAffineTransformer().transformCoordinates(coords);

							geom = GeometryObject.createPoint(new double[]{coords.get(0), coords.get(1)}, 2, dbSrid);
						}
					} else {
						String href = property.getHref();
						if (href != null && href.length() != 0)
							importer.logOrThrowUnsupportedXLinkMessage(geoTex, Point.class, href);
					}
				}

				if (geom != null)
					psSurfaceData.setObject(13, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geom, batchConn));
				else
					psSurfaceData.setNull(13, nullGeometryType, nullGeometryTypeName);

				psSurfaceData.addBatch();
				if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
					importer.executeBatch(TableEnum.SURFACE_DATA);

				if (geoTex.isSetTarget()) {
					HashSet<String> duplicateTargets = new HashSet<String>(geoTex.getTarget().size());

					for (String target : geoTex.getTarget()) {
						if (target != null && target.length() != 0) {

							// check for duplicate targets
							if (!duplicateTargets.add(target.replaceAll("^#", ""))) {
								log.debug(new StringBuilder(featureSignature).append(": Skipping duplicate target '").append(target).append("'.").toString());
								continue;
							}

							boolean isResolved = false;
							if (isLocalAppearance) {
								// check whether we can query the target from the in-memory gml:id cache
								long surfaceGeometryId = importer.getGeometryIdFromMemory(target.replaceAll("^#", ""));
								isResolved = surfaceGeometryId != 0;
								if (isResolved)
									textureParamImporter.doImport(surfaceDataId, surfaceGeometryId);								
							}

							if (!isResolved) {
								importer.propagateXlink(new DBXlinkTextureParam(
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

		// appearance to surface data
		appearToSurfaceDataImporter.doImport(surfaceDataId, parentId);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(surfaceData, surfaceDataId, featureType);

		return surfaceDataId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psX3DMaterial.executeBatch();
			psParaTex.executeBatch();
			psGeoTex.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psX3DMaterial.close();
		psParaTex.close();
		psGeoTex.close();
	}

}
