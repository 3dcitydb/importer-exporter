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
package de.tub.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;

public class XlinkBasic implements DBXlinkResolver {
	private static final ReentrantLock mainLock = new ReentrantLock();
	
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private HashMap<String, PreparedStatement> psMap;
	private HashMap<String, Integer> psBatchCounterMap;
	private PreparedStatement psUpdateSurfGeom;
	private int updateBatchCounter;

	public XlinkBasic(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		psMap = new HashMap<String, PreparedStatement>();
		psBatchCounterMap = new HashMap<String, Integer>();
		psUpdateSurfGeom = batchConn.prepareStatement("update SURFACE_GEOMETRY set IS_XLINK=1 where ID=? and IS_XLINK=0");
	}

	public boolean insert(DBXlinkBasic xlink) throws SQLException {
		CityGMLClass type = xlink.getToTable() == TableEnum.SURFACE_GEOMETRY ? 
				CityGMLClass.ABSTRACT_GML_GEOMETRY : CityGMLClass.ABSTRACT_CITY_OBJECT;

		GmlIdEntry entry = resolverManager.getDBId(xlink.getGmlId(), type);
		if (entry == null)
			return false;

		String key = getKey(xlink);
		PreparedStatement ps = getPreparedStatement(xlink, key);
		if (ps != null) {
			ps.setLong(1, entry.getId());
			ps.setLong(2, xlink.getId());
	
			ps.addBatch();
			int counter = psBatchCounterMap.get(key);
			if (++counter == Internal.ORACLE_MAX_BATCH_SIZE) {
				ps.executeBatch();
				psBatchCounterMap.put(key, 0);
			} else
				psBatchCounterMap.put(key, counter);
		}
		
		if (xlink.getToTable() == TableEnum.SURFACE_GEOMETRY) {
			psUpdateSurfGeom.setLong(1, entry.getId());
			psUpdateSurfGeom.addBatch();
			if (++updateBatchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
				executeUpdateSurfGeomBatch();
		}

		return true;
	}

	private PreparedStatement getPreparedStatement(DBXlinkBasic xlink, String key) throws SQLException {
		TableEnum fromTable = xlink.getFromTable();
		TableEnum toTable = xlink.getToTable();
		String attrName = xlink.getAttrName();
		
		PreparedStatement ps = psMap.get(key);

		if (ps == null) {
			if (fromTable == TableEnum.THEMATIC_SURFACE && toTable == TableEnum.OPENING) {
				ps = batchConn.prepareStatement("insert into OPENING_TO_THEM_SURFACE (OPENING_ID, THEMATIC_SURFACE_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == TableEnum.APPEARANCE && toTable == TableEnum.SURFACE_DATA) {
				ps = batchConn.prepareStatement("insert into APPEAR_TO_SURFACE_DATA (SURFACE_DATA_ID, APPEARANCE_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == TableEnum.WATERBODY && toTable == TableEnum.WATERBOUNDARY_SURFACE) {
				ps = batchConn.prepareStatement("insert into WATERBOD_TO_WATERBND_SRF (WATERBOUNDARY_SURFACE_ID, WATERBODY_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == TableEnum.BUILDING && toTable == TableEnum.ADDRESS) {
				ps = batchConn.prepareStatement("insert into ADDRESS_TO_BUILDING (ADDRESS_ID, BUILDING_ID) values " +
				"(?, ?)");
			}

			else if (fromTable == TableEnum.RELIEF_FEATURE && toTable == TableEnum.RELIEF_COMPONENT) {
				ps = batchConn.prepareStatement("insert into RELIEF_FEAT_TO_REL_COMP (RELIEF_COMPONENT_ID, RELIEF_FEATURE_ID) values " +
				"(?, ?)");
			} 

			else if (fromTable == TableEnum.CITYOBJECT && toTable == TableEnum.CITYOBJECT) {
				ps = batchConn.prepareStatement("insert into GENERALIZATION (GENERALIZES_TO_ID, CITYOBJECT_ID) values " +
				"(?, ?)");
			}

			else if (attrName != null){
				ps = batchConn.prepareStatement("update " + fromTable + " set " + attrName + "=? where ID=?");
			}
			
			if (ps != null) {
				psMap.put(key, ps);
				psBatchCounterMap.put(key, 0);
			}
		}

		return ps;
	}
	
	private String getKey(DBXlinkBasic xlink) {
		return xlink.getFromTable().ordinal() + "_" + xlink.getToTable().ordinal() + "_" + xlink.getAttrName();
	}

	@Override
	public void executeBatch() throws SQLException {
		for (PreparedStatement ps : psMap.values())
			ps.executeBatch();
		
		for (Entry<String, Integer> entry : psBatchCounterMap.entrySet())
			entry.setValue(0);
	
		executeUpdateSurfGeomBatch();
	}
	
	private void executeUpdateSurfGeomBatch() throws SQLException {
		// we need to synchronize updates otherwise Oracle will run
		// into deadlocks
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			psUpdateSurfGeom.executeBatch();
			updateBatchCounter = 0;
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
