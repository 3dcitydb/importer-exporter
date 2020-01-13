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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBTunnelHollowSpace implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psHollowSpace;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBTunnelThematicSurface thematicSurfaceImporter;
	private DBTunnelFurniture tunnelFurnitureImporter;
	private DBTunnelInstallation tunnelInstallationImporter;
	private AttributeValueJoiner valueJoiner;
	
	private boolean hasObjectClassIdColumn;
	private int batchCounter;

	public DBTunnelHollowSpace(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".tunnel_hollow_space (id, class, class_codespace, function, function_codespace, usage, usage_codespace, tunnel_id, " +
				"lod4_multi_surface_id, lod4_solid_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psHollowSpace = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBTunnelThematicSurface.class);
		tunnelFurnitureImporter = importer.getImporter(DBTunnelFurniture.class);
		tunnelInstallationImporter = importer.getImporter(DBTunnelInstallation.class);
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(HollowSpace hollowSpace) throws CityGMLImportException, SQLException {
		return doImport(hollowSpace, 0);
	}

	public long doImport(HollowSpace hollowSpace, long tunnelId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(hollowSpace);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long hollowSpaceId = cityObjectImporter.doImport(hollowSpace, featureType);

		// import hollow space information
		// primary id
		psHollowSpace.setLong(1, hollowSpaceId);

		// tun:class
		if (hollowSpace.isSetClazz() && hollowSpace.getClazz().isSetValue()) {
			psHollowSpace.setString(2, hollowSpace.getClazz().getValue());
			psHollowSpace.setString(3, hollowSpace.getClazz().getCodeSpace());
		} else {
			psHollowSpace.setNull(2, Types.VARCHAR);
			psHollowSpace.setNull(3, Types.VARCHAR);
		}

		// tun:function
		if (hollowSpace.isSetFunction()) {
			valueJoiner.join(hollowSpace.getFunction(), Code::getValue, Code::getCodeSpace);
			psHollowSpace.setString(4, valueJoiner.result(0));
			psHollowSpace.setString(5, valueJoiner.result(1));
		} else {
			psHollowSpace.setNull(4, Types.VARCHAR);
			psHollowSpace.setNull(5, Types.VARCHAR);
		}

		// tun:usage
		if (hollowSpace.isSetUsage()) {
			valueJoiner.join(hollowSpace.getUsage(), Code::getValue, Code::getCodeSpace);
			psHollowSpace.setString(6, valueJoiner.result(0));
			psHollowSpace.setString(7, valueJoiner.result(1));
		} else {
			psHollowSpace.setNull(6, Types.VARCHAR);
			psHollowSpace.setNull(7, Types.VARCHAR);
		}

		// parent tunnel id
		if (tunnelId != 0)
			psHollowSpace.setLong(8, tunnelId);
		else
			psHollowSpace.setNull(8, Types.NULL);

		// tun:lod4MultiSurface
		long geometryId = 0;

		if (hollowSpace.isSetLod4MultiSurface()) {
			MultiSurfaceProperty multiSurfacePropery = hollowSpace.getLod4MultiSurface();

			if (multiSurfacePropery.isSetMultiSurface()) {
				geometryId = surfaceGeometryImporter.doImport(multiSurfacePropery.getMultiSurface(), hollowSpaceId);
				multiSurfacePropery.unsetMultiSurface();
			} else {
				String href = multiSurfacePropery.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.TUNNEL_HOLLOW_SPACE.getName(),
							hollowSpaceId, 
							href, 
							"lod4_multi_surface_id"));
				}
			}
		} 

		if (geometryId != 0)
			psHollowSpace.setLong(9, geometryId);
		else
			psHollowSpace.setNull(9, Types.NULL);

		// tun:lod4Solid
		geometryId = 0;

		if (hollowSpace.isSetLod4Solid()) {
			SolidProperty solidProperty = hollowSpace.getLod4Solid();

			if (solidProperty.isSetSolid()) {
				geometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), hollowSpaceId);
				solidProperty.unsetSolid();
			} else {
				String href = solidProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.TUNNEL_HOLLOW_SPACE.getName(),
							hollowSpaceId, 
							href, 
							"lod4_solid_id"));
				}
			}
		} 

		if (geometryId != 0)
			psHollowSpace.setLong(10, geometryId);
		else
			psHollowSpace.setNull(10, Types.NULL);

		// objectclass id
		if (hasObjectClassIdColumn)
			psHollowSpace.setLong(11, featureType.getObjectClassId());

		psHollowSpace.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TUNNEL_HOLLOW_SPACE);

		// tun:boundedBy
		if (hollowSpace.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : hollowSpace.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, hollowSpace, hollowSpaceId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_THEMATIC_SURFACE.getName(),
								href,
								hollowSpaceId,
								"tunnel_hollow_space_id"));
					}
				}
			}
		}

		// tun:hollowSpaceInstallation
		if (hollowSpace.isSetHollowSpaceInstallation()) {
			for (IntTunnelInstallationProperty property : hollowSpace.getHollowSpaceInstallation()) {
				IntTunnelInstallation installation = property.getObject();

				if (installation != null) {
					tunnelInstallationImporter.doImport(installation, hollowSpace, hollowSpaceId);
					property.unsetIntTunnelInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_INSTALLATION.getName(),
								href,
								hollowSpaceId,
								"tunnel_hollow_space_id"));
					}
				}
			}
		}

		// tun:interiorFurniture
		if (hollowSpace.isSetInteriorFurniture()) {
			for (InteriorFurnitureProperty property : hollowSpace.getInteriorFurniture()) {
				TunnelFurniture furniture = property.getObject();

				if (furniture != null) {
					tunnelFurnitureImporter.doImport(furniture, hollowSpaceId);
					property.unsetTunnelFurniture();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_FURNITURE.getName(),
								href,
								hollowSpaceId,
								"tunnel_hollow_space_id"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(hollowSpace, hollowSpaceId, featureType);

		return hollowSpaceId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psHollowSpace.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psHollowSpace.close();
	}

}
