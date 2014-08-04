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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractOpening;
import org.citygml4j.model.citygml.tunnel.OpeningProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBTunnelThematicSurface implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psThematicSurface;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBTunnelOpening openingImporter;

	private int batchCounter;

	public DBTunnelThematicSurface(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into TUNNEL_THEMATIC_SURFACE (ID, OBJECTCLASS_ID, TUNNEL_ID, TUNNEL_HOLLOW_SPACE_ID, TUNNEL_INSTALLATION_ID, ")
		.append("LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?)");
		psThematicSurface = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		openingImporter = (DBTunnelOpening)dbImporterManager.getDBImporter(DBImporterEnum.TUNNEL_OPENING);
	}

	public long insert(AbstractBoundarySurface boundarySurface, CityGMLClass parent, long parentId) throws SQLException {
		long boundarySurfaceId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (boundarySurfaceId == 0)
			return 0;

		String origGmlId = boundarySurface.getId();

		// CityObject
		cityObjectImporter.insert(boundarySurface, boundarySurfaceId);

		// BoundarySurface
		// ID
		psThematicSurface.setLong(1, boundarySurfaceId);

		// OBJECTCLASS_ID
		psThematicSurface.setInt(2, Util.cityObject2classId(boundarySurface.getCityGMLClass()));

		// parentId
		switch (parent) {
		case TUNNEL:
		case TUNNEL_PART:
			psThematicSurface.setLong(3, parentId);
			psThematicSurface.setNull(4, Types.NULL);
			psThematicSurface.setNull(5, Types.NULL);
			break;
		case HOLLOW_SPACE:
			psThematicSurface.setNull(3, Types.NULL);
			psThematicSurface.setLong(4, parentId);
			psThematicSurface.setNull(5, Types.NULL);
			break;
		case TUNNEL_INSTALLATION:
		case INT_TUNNEL_INSTALLATION:
			psThematicSurface.setNull(3, Types.NULL);
			psThematicSurface.setNull(4, Types.NULL);
			psThematicSurface.setLong(5, parentId);
			break;
		default:
			psThematicSurface.setNull(3, Types.NULL);
			psThematicSurface.setNull(4, Types.NULL);
			psThematicSurface.setNull(5, Types.NULL);
		}

		// Geometry
		for (int i = 0; i < 3; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = boundarySurface.getLod2MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = boundarySurface.getLod3MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = boundarySurface.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), boundarySurfaceId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								boundarySurfaceId, 
								TableEnum.TUNNEL_THEMATIC_SURFACE, 
								"LOD" + (i + 2) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiSurfaceId != 0)
				psThematicSurface.setLong(6 + i, multiSurfaceId);
			else
				psThematicSurface.setNull(6 + i, Types.NULL);
		}

		psThematicSurface.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.TUNNEL_THEMATIC_SURFACE);

		// Openings
		if (boundarySurface.isSetOpening()) {
			for (OpeningProperty openingProperty : boundarySurface.getOpening()) {
				if (openingProperty.isSetOpening()) {
					AbstractOpening opening = openingProperty.getOpening();
					String gmlId = opening.getId();
					long id = openingImporter.insert(opening, boundarySurfaceId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								opening.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					openingProperty.unsetOpening();
				} else {
					// xlink
					String href = openingProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
								boundarySurfaceId,
								TableEnum.TUNNEL_THEMATIC_SURFACE,
								href,
								TableEnum.TUNNEL_OPENING
								));
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(boundarySurface, boundarySurfaceId);

		return boundarySurfaceId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psThematicSurface.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psThematicSurface.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TUNNEL_THEMATIC_SURFACE;
	}

}
