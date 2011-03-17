/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.gmlId.GmlIdEntry;
import de.tub.citydb.db.xlink.DBXlinkBasic;

public class XlinkBasic implements DBXlinkResolver {
	private final ReentrantLock mainLock = new ReentrantLock();
	
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private HashMap<String, PreparedStatement> psMap;
	private PreparedStatement psUpdateSurfGeom;

	public XlinkBasic(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		psMap = new HashMap<String, PreparedStatement>();
		psUpdateSurfGeom = batchConn.prepareStatement("update SURFACE_GEOMETRY set IS_XLINK=1 where ID=? and IS_XLINK=0");
	}

	public boolean insert(DBXlinkBasic xlink) throws SQLException {
		CityGMLClass type = xlink.getToTable() == DBTableEnum.SURFACE_GEOMETRY ? 
				CityGMLClass.GMLGEOMETRY : CityGMLClass.CITYOBJECT;

		GmlIdEntry entry = resolverManager.getDBId(xlink.getGmlId(), type);
		if (entry == null)
			return false;

		PreparedStatement ps = getPreparedStatement(xlink);
		if (ps != null) {
			ps.setLong(1, entry.getId());
			ps.setLong(2, xlink.getId());
			ps.addBatch();
		}
		
		if (xlink.getToTable() == DBTableEnum.SURFACE_GEOMETRY) {
			psUpdateSurfGeom.setLong(1, entry.getId());
			psUpdateSurfGeom.addBatch();
		}

		return true;
	}

	public PreparedStatement getPreparedStatement(DBXlinkBasic xlink) throws SQLException {
		DBTableEnum fromTable = xlink.getFromTable();
		DBTableEnum toTable = xlink.getToTable();
		String attrName = xlink.getAttrName();

		String key = fromTable.ordinal() + "_" + toTable.ordinal() + "_" + xlink.getAttrName();
		PreparedStatement ps = psMap.get(key);

		if (ps == null) {

			if (fromTable == DBTableEnum.THEMATIC_SURFACE && toTable == DBTableEnum.OPENING) {
				ps = batchConn.prepareStatement("insert into OPENING_TO_THEM_SURFACE (OPENING_ID, THEMATIC_SURFACE_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == DBTableEnum.APPEARANCE && toTable == DBTableEnum.SURFACE_DATA) {
				ps = batchConn.prepareStatement("insert into APPEAR_TO_SURFACE_DATA (SURFACE_DATA_ID, APPEARANCE_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == DBTableEnum.WATERBODY && toTable == DBTableEnum.WATERBOUNDARY_SURFACE) {
				ps = batchConn.prepareStatement("insert into WATERBOD_TO_WATERBND_SRF (WATERBOUNDARY_SURFACE_ID, WATERBODY_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == DBTableEnum.BUILDING && toTable == DBTableEnum.ADDRESS) {
				ps = batchConn.prepareStatement("insert into ADDRESS_TO_BUILDING (ADDRESS_ID, BUILDING_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == DBTableEnum.RELIEF_FEATURE && toTable == DBTableEnum.RELIEF_COMPONENT) {
				ps = batchConn.prepareStatement("insert into RELIEF_FEAT_TO_REL_COMP (RELIEF_COMPONENT_ID, RELIEF_FEATURE_ID) values " +
				"(?, ?)");
			} 

			else if (fromTable == DBTableEnum.CITYOBJECT && toTable == DBTableEnum.CITYOBJECT) {
				ps = batchConn.prepareStatement("insert into GENERALIZATION (GENERALIZES_TO_ID, CITYOBJECT_ID) values " +
				"(?, ?)");
			}

			else if (attrName != null){
				ps = batchConn.prepareStatement("update " + fromTable + " set " + attrName + "=? where ID=?");
			}
		}

		if (ps != null)
			psMap.put(key, ps);

		return ps;
	}

	@Override
	public void executeBatch() throws SQLException {
		for (PreparedStatement ps : psMap.values())
			ps.executeBatch();
		
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			psUpdateSurfGeom.executeBatch();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void close() throws SQLException {
		for (PreparedStatement ps : psMap.values())
			ps.close();

		psUpdateSurfGeom.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.BASIC;
	}

}
