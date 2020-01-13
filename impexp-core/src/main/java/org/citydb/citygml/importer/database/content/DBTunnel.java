/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
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
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBTunnel implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psTunnel;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBTunnelThematicSurface thematicSurfaceImporter;
	private DBTunnelInstallation tunnelInstallationImporter;
	private DBTunnelHollowSpace hollowSpaceImporter;
	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;	
	private int nullGeometryType;
	private String nullGeometryTypeName;	

	public DBTunnel(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".tunnel (id, tunnel_parent_id, tunnel_root_id, class, class_codespace, function, function_codespace, usage, usage_codespace, year_of_construction, year_of_demolition, " +
				"lod1_terrain_intersection, lod2_terrain_intersection, lod3_terrain_intersection, lod4_terrain_intersection, lod2_multi_curve, lod3_multi_curve, lod4_multi_curve, " +
				"lod1_multi_surface_id, lod2_multi_surface_id, lod3_multi_surface_id, lod4_multi_surface_id, " +
				"lod1_solid_id, lod2_solid_id, lod3_solid_id, lod4_solid_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psTunnel = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBTunnelThematicSurface.class);
		tunnelInstallationImporter = importer.getImporter(DBTunnelInstallation.class);
		hollowSpaceImporter = importer.getImporter(DBTunnelHollowSpace.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(AbstractTunnel tunnel) throws CityGMLImportException, SQLException {
		return doImport(tunnel, 0, 0);
	}

	public long doImport(AbstractTunnel tunnel, long parentId, long rootId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(tunnel);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long tunnelId = cityObjectImporter.doImport(tunnel, featureType);
		if (rootId == 0)
			rootId = tunnelId;

		// import tunnel information
		// primary id
		psTunnel.setLong(1, tunnelId);

		// parent tunnel id
		if (parentId != 0)
			psTunnel.setLong(2, parentId);
		else
			psTunnel.setNull(2, Types.NULL);

		// root tunnel id
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
			valueJoiner.join(tunnel.getFunction(), Code::getValue, Code::getCodeSpace);
			psTunnel.setString(6, valueJoiner.result(0));
			psTunnel.setString(7, valueJoiner.result(1));
		} else {
			psTunnel.setNull(6, Types.VARCHAR);
			psTunnel.setNull(7, Types.VARCHAR);
		}

		// tun:usage
		if (tunnel.isSetUsage()) {
			valueJoiner.join(tunnel.getUsage(), Code::getValue, Code::getCodeSpace);
			psTunnel.setString(8, valueJoiner.result(0));
			psTunnel.setString(9, valueJoiner.result(1));
		} else {
			psTunnel.setNull(8, Types.VARCHAR);
			psTunnel.setNull(9, Types.VARCHAR);
		}

		// tun:yearOfConstruction
		if (tunnel.isSetYearOfConstruction()) {
			psTunnel.setDate(10, Date.valueOf(tunnel.getYearOfConstruction()));
		} else {
			psTunnel.setNull(10, Types.DATE);
		}

		// tun:yearOfDemolition
		if (tunnel.isSetYearOfDemolition()) {
			psTunnel.setDate(11, Date.valueOf(tunnel.getYearOfDemolition()));
		} else {
			psTunnel.setNull(11, Types.DATE);
		}

		// tun:lodXTerrainIntersectionCurve
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
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psTunnel.setObject(12 + i, multiLineObj);
			} else
				psTunnel.setNull(12 + i, nullGeometryType, nullGeometryTypeName);
		}

		// tun:lodXMultiCurve
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
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psTunnel.setObject(16 + i, multiLineObj);
			} else
				psTunnel.setNull(16 + i, nullGeometryType, nullGeometryTypeName);
		}

		// tun:lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), tunnelId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.TUNNEL.getName(),
								tunnelId, 
								href, 
								"lod" + (i + 1) + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psTunnel.setLong(19 + i, multiGeometryId);
			else
				psTunnel.setNull(19 + i, Types.NULL);
		}

		// tun:lodXSolid
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
					solidGeometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), tunnelId);
					solidProperty.unsetSolid();
				} else {
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.TUNNEL.getName(),
								tunnelId, 
								href, 
								"lod" + (i + 1) + "_solid_id"));
					}
				}
			}

			if (solidGeometryId != 0)
				psTunnel.setLong(23 + i, solidGeometryId);
			else
				psTunnel.setNull(23 + i, Types.NULL);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psTunnel.setLong(27, featureType.getObjectClassId());

		psTunnel.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TUNNEL);

		// tun:boundedBy
		if (tunnel.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : tunnel.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, tunnel, tunnelId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_THEMATIC_SURFACE.getName(),
								href,
								tunnelId,
								"tunnel_id"));
					}
				}
			}
		}

		// tun:outerTunnelInstallation
		if (tunnel.isSetOuterTunnelInstallation()) {
			for (TunnelInstallationProperty property : tunnel.getOuterTunnelInstallation()) {
				TunnelInstallation installation = property.getTunnelInstallation();

				if (installation != null) {
					tunnelInstallationImporter.doImport(installation, tunnel, tunnelId);
					property.unsetTunnelInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_INSTALLATION.getName(),
								href,
								tunnelId,
								"tunnel_id"));
					}
				}
			}
		}

		// tun:interiorTunnelInstallation
		if (tunnel.isSetInteriorTunnelInstallation()) {
			for (IntTunnelInstallationProperty property : tunnel.getInteriorTunnelInstallation()) {
				IntTunnelInstallation installation = property.getIntTunnelInstallation();

				if (installation != null) {
					tunnelInstallationImporter.doImport(installation, tunnel, tunnelId);
					property.unsetIntTunnelInstallation();
				} else {
					// xlink
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_INSTALLATION.getName(),
								href,
								tunnelId,
								"tunnel_id"));
					}
				}
			}
		}

		// tun:interiorHollowSpace
		if (tunnel.isSetInteriorHollowSpace()) {
			for (InteriorHollowSpaceProperty property : tunnel.getInteriorHollowSpace()) {
				HollowSpace hollowSpace = property.getHollowSpace();

				if (hollowSpace != null) {
					hollowSpaceImporter.doImport(hollowSpace, tunnelId);
					property.unsetHollowSpace();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_HOLLOW_SPACE.getName(),
								href,
								tunnelId,
								"tunnel_id"));
					}
				}
			}
		}

		// tun:consistsOfTunnelPart
		if (tunnel.isSetConsistsOfTunnelPart()) {
			for (TunnelPartProperty property : tunnel.getConsistsOfTunnelPart()) {
				TunnelPart tunnelPart = property.getTunnelPart();

				if (tunnelPart != null) {
					doImport(tunnelPart, tunnelId, rootId);
					property.unsetTunnelPart();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0)
						importer.logOrThrowUnsupportedXLinkMessage(tunnel, TunnelPart.class, href);
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(tunnel, tunnelId, featureType);

		return tunnelId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psTunnel.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psTunnel.close();
	}

}
