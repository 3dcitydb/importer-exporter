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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.InteriorHollowSpaceProperty;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.tunnel.TunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.TunnelPart;
import org.citygml4j.model.citygml.tunnel.TunnelPartProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBTunnel implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTunnel;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBTunnelThematicSurface thematicSurfaceImporter;
	private DBTunnelInstallation tunnelInstallationImporter;
	private DBTunnelHollowSpace hollowSpaceImporter;
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;	

	public DBTunnel(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into TUNNEL (ID, TUNNEL_PARENT_ID, TUNNEL_ROOT_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, YEAR_OF_CONSTRUCTION, YEAR_OF_DEMOLITION, ")
		.append("LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, LOD2_MULTI_CURVE, LOD3_MULTI_CURVE, LOD4_MULTI_CURVE, ")
		.append("LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID, LOD4_SOLID_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psTunnel = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBTunnelThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.TUNNEL_THEMATIC_SURFACE);
		tunnelInstallationImporter = (DBTunnelInstallation)dbImporterManager.getDBImporter(DBImporterEnum.TUNNEL_INSTALLATION);
		hollowSpaceImporter = (DBTunnelHollowSpace)dbImporterManager.getDBImporter(DBImporterEnum.TUNNEL_HOLLOW_SPACE);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(AbstractTunnel tunnel) throws SQLException {
		long tunnelId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (tunnelId != 0)
			success = insert(tunnel, tunnelId, 0, tunnelId);

		if (success)
			return tunnelId;
		else
			return 0;
	}

	public boolean insert(AbstractTunnel tunnel,
			long tunnelId,
			long parentId,
			long rootId) throws SQLException {
		String origGmlId = tunnel.getId();

		// CityObject
		long cityObjectId = cityObjectImporter.insert(tunnel, tunnelId, parentId == 0);
		if (cityObjectId == 0)
			return false;

		// Bridge
		// ID
		psTunnel.setLong(1, tunnelId);

		// TUNNEL_PARENT_ID
		if (parentId != 0)
			psTunnel.setLong(2, parentId);
		else
			psTunnel.setNull(2, Types.NULL);

		// TUNNEL_ROOT_ID
		psTunnel.setLong(3, rootId);

		// tun:class
		if (tunnel.isSetClazz() && tunnel.getClazz().isSetValue()) {
			psTunnel.setString(4, tunnel.getClazz().getValue());
			psTunnel.setString(5, tunnel.getClazz().getCodeSpace());
		} else {
			psTunnel.setNull(4, Types.VARCHAR);
			psTunnel.setNull(5, Types.VARCHAR);
		}

		// tun:function
		if (tunnel.isSetFunction()) {
			String[] function = Util.codeList2string(tunnel.getFunction());
			psTunnel.setString(6, function[0]);
			psTunnel.setString(7, function[1]);
		} else {
			psTunnel.setNull(6, Types.VARCHAR);
			psTunnel.setNull(7, Types.VARCHAR);
		}

		// tun:usage
		if (tunnel.isSetUsage()) {
			String[] usage = Util.codeList2string(tunnel.getUsage());
			psTunnel.setString(8, usage[0]);
			psTunnel.setString(9, usage[1]);
		} else {
			psTunnel.setNull(8, Types.VARCHAR);
			psTunnel.setNull(9, Types.VARCHAR);
		}

		// tun:yearOfConstruction
		if (tunnel.isSetYearOfConstruction()) {
			psTunnel.setDate(10, new Date(tunnel.getYearOfConstruction().getTime().getTime()));
		} else {
			psTunnel.setNull(10, Types.DATE);
		}

		// tun:yearOfDemolition
		if (tunnel.isSetYearOfDemolition()) {
			psTunnel.setDate(11, new Date(tunnel.getYearOfDemolition().getTime().getTime()));
		} else {
			psTunnel.setNull(11, Types.DATE);
		}

		// Geometry
		// lodXTerrainIntersectionCurve
		for (int i = 0; i < 4; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = tunnel.getLod1TerrainIntersection();
				break;
			case 1:
				multiCurveProperty = tunnel.getLod2TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = tunnel.getLod3TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = tunnel.getLod4TerrainIntersection();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psTunnel.setObject(12 + i, multiLineObj);
			} else
				psTunnel.setNull(12 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXMultiCurve
		for (int i = 0; i < 3; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = tunnel.getLod2MultiCurve();
				break;
			case 1:
				multiCurveProperty = tunnel.getLod3MultiCurve();
				break;
			case 2:
				multiCurveProperty = tunnel.getLod4MultiCurve();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psTunnel.setObject(16 + i, multiLineObj);
			} else
				psTunnel.setNull(16 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXMultiSurface
		for (int i = 0; i < 4; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = tunnel.getLod1MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = tunnel.getLod2MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = tunnel.getLod3MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = tunnel.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), tunnelId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								tunnelId, 
								TableEnum.TUNNEL, 
								"LOD" + (i + 1) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psTunnel.setLong(19 + i, multiGeometryId);
			else
				psTunnel.setNull(19 + i, Types.NULL);
		}

		// lodXSolid
		for (int i = 0; i < 4; i++) {
			SolidProperty solidProperty = null;
			long solidGeometryId = 0;

			switch (i) {
			case 0:
				solidProperty = tunnel.getLod1Solid();
				break;
			case 1:
				solidProperty = tunnel.getLod2Solid();
				break;
			case 2:
				solidProperty = tunnel.getLod3Solid();
				break;
			case 3:
				solidProperty = tunnel.getLod4Solid();
				break;
			}

			if (solidProperty != null) {
				if (solidProperty.isSetSolid()) {
					solidGeometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), tunnelId);
					solidProperty.unsetSolid();
				} else {
					// xlink
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								tunnelId, 
								TableEnum.TUNNEL, 
								"LOD" + (i + 1) + "_SOLID_ID"));
					}
				}
			}

			if (solidGeometryId != 0)
				psTunnel.setLong(23 + i, solidGeometryId);
			else
				psTunnel.setNull(23 + i, Types.NULL);
		}

		psTunnel.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.TUNNEL);

		// BoundarySurfaces
		if (tunnel.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : tunnel.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, tunnel.getCityGMLClass(), tunnelId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								tunnel.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					boundarySurfaceProperty.unsetBoundarySurface();
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_TUNNEL_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}

		// TunnelInstallation
		if (tunnel.isSetOuterTunnelInstallation()) {
			for (TunnelInstallationProperty tunnelInstProperty : tunnel.getOuterTunnelInstallation()) {
				TunnelInstallation tunnelInst = tunnelInstProperty.getTunnelInstallation();

				if (tunnelInst != null) {
					String gmlId = tunnelInst.getId();
					long id = tunnelInstallationImporter.insert(tunnelInst, tunnel.getCityGMLClass(), tunnelId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								tunnel.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.TUNNEL_INSTALLATION, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					tunnelInstProperty.unsetTunnelInstallation();
				} else {
					// xlink
					String href = tunnelInstProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.TUNNEL_INSTALLATION + " feature is not supported.");
					}
				}
			}
		}

		// IntTunnelInstallation
		if (tunnel.isSetInteriorTunnelInstallation()) {
			for (IntTunnelInstallationProperty intTunnelInstProperty : tunnel.getInteriorTunnelInstallation()) {
				IntTunnelInstallation intTunnelInst = intTunnelInstProperty.getIntTunnelInstallation();

				if (intTunnelInst != null) {
					String gmlId = intTunnelInst.getId();
					long id = tunnelInstallationImporter.insert(intTunnelInst, tunnel.getCityGMLClass(), tunnelId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								tunnel.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.INT_TUNNEL_INSTALLATION, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					intTunnelInstProperty.unsetIntTunnelInstallation();
				} else {
					// xlink
					String href = intTunnelInstProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.INT_TUNNEL_INSTALLATION + " feature is not supported.");
					}
				}
			}
		}

		// HollowSpace
		if (tunnel.isSetInteriorHollowSpace()) {
			for (InteriorHollowSpaceProperty hollowSpaceProperty : tunnel.getInteriorHollowSpace()) {
				HollowSpace hollowSpace = hollowSpaceProperty.getHollowSpace();

				if (hollowSpace != null) {
					String gmlId = hollowSpace.getId();
					long id = hollowSpaceImporter.insert(hollowSpace, tunnelId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								tunnel.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.HOLLOW_SPACE, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					hollowSpaceProperty.unsetHollowSpace();
				} else {
					// xlink
					String href = hollowSpaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.HOLLOW_SPACE + " feature is not supported.");
					}
				}
			}
		}

		// TunnelPart
		if (tunnel.isSetConsistsOfTunnelPart()) {
			for (TunnelPartProperty tunnelPartProperty : tunnel.getConsistsOfTunnelPart()) {
				TunnelPart tunnelPart = tunnelPartProperty.getTunnelPart();

				if (tunnelPart != null) {
					long id = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);

					if (id != 0)
						insert(tunnelPart, id, tunnelId, rootId);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								tunnel.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.TUNNEL_PART, 
								tunnelPart.getId()));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					tunnelPartProperty.unsetTunnelPart();
				} else {
					// xlink
					String href = tunnelPartProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.TUNNEL_PART + " feature is not supported.");
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(tunnel, tunnelId);

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTunnel.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTunnel.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TUNNEL;
	}

}
