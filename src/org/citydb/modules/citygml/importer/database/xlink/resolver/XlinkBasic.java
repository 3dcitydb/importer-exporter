/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.citydb.database.TableEnum;
import org.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citygml4j.model.citygml.CityGMLClass;

public class XlinkBasic implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private HashMap<String, PreparedStatement> psMap;
	private HashMap<String, Integer> psBatchCounterMap;

	public XlinkBasic(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		psMap = new HashMap<String, PreparedStatement>();
		psBatchCounterMap = new HashMap<String, Integer>();
	}

	public boolean insert(DBXlinkBasic xlink) throws SQLException {
		CityGMLClass type = xlink.getToTable() == TableEnum.SURFACE_GEOMETRY ? 
				CityGMLClass.ABSTRACT_GML_GEOMETRY : CityGMLClass.ABSTRACT_CITY_OBJECT;

		UIDCacheEntry entry = resolverManager.getDBId(xlink.getGmlId(), type);
		if (entry == null)
			return false;

		String key = getKey(xlink);
		PreparedStatement ps = getPreparedStatement(xlink, key);
		if (ps != null) {
			ps.setLong(1, entry.getId());
			ps.setLong(2, xlink.getId());

			ps.addBatch();
			int counter = psBatchCounterMap.get(key);
			if (++counter == resolverManager.getDatabaseAdapter().getMaxBatchSize()) {
				ps.executeBatch();
				psBatchCounterMap.put(key, 0);
			} else
				psBatchCounterMap.put(key, counter);
		}

		return true;
	}

	private PreparedStatement getPreparedStatement(DBXlinkBasic xlink, String key) throws SQLException {
		TableEnum fromTable = xlink.getFromTable();
		TableEnum toTable = xlink.getToTable();
		String attrName = xlink.getAttrName();

		PreparedStatement ps = psMap.get(key);

		if (ps == null) {
			if (fromTable == TableEnum.THEMATIC_SURFACE && toTable == TableEnum.OPENING)
				ps = batchConn.prepareStatement("insert into OPENING_TO_THEM_SURFACE (OPENING_ID, THEMATIC_SURFACE_ID) values (?, ?)");

			else if (fromTable == TableEnum.BRIDGE_THEMATIC_SURFACE && toTable == TableEnum.BRIDGE_OPENING)
				ps = batchConn.prepareStatement("insert into BRIDGE_OPEN_TO_THEM_SRF (BRIDGE_OPENING_ID, BRIDGE_THEMATIC_SURFACE_ID) values (?, ?)");

			else if (fromTable == TableEnum.TUNNEL_THEMATIC_SURFACE && toTable == TableEnum.TUNNEL_OPENING)
				ps = batchConn.prepareStatement("insert into TUNNEL_OPEN_TO_THEM_SRF (TUNNEL_OPENING_ID, TUNNEL_THEMATIC_SURFACE_ID) values (?, ?)");

			else if (fromTable == TableEnum.APPEARANCE && toTable == TableEnum.SURFACE_DATA)
				ps = batchConn.prepareStatement("insert into APPEAR_TO_SURFACE_DATA (SURFACE_DATA_ID, APPEARANCE_ID) values (?, ?)");

			else if (fromTable == TableEnum.WATERBODY && toTable == TableEnum.WATERBOUNDARY_SURFACE)
				ps = batchConn.prepareStatement("insert into WATERBOD_TO_WATERBND_SRF (WATERBOUNDARY_SURFACE_ID, WATERBODY_ID) values (?, ?)");

			else if (fromTable == TableEnum.BUILDING && toTable == TableEnum.ADDRESS)
				ps = batchConn.prepareStatement("insert into ADDRESS_TO_BUILDING (ADDRESS_ID, BUILDING_ID) values (?, ?)");

			else if (fromTable == TableEnum.BRIDGE && toTable == TableEnum.ADDRESS)
				ps = batchConn.prepareStatement("insert into ADDRESS_TO_BRIDGE (ADDRESS_ID, BRIDGE_ID) values (?, ?)");

			else if (fromTable == TableEnum.RELIEF_FEATURE && toTable == TableEnum.RELIEF_COMPONENT)
				ps = batchConn.prepareStatement("insert into RELIEF_FEAT_TO_REL_COMP (RELIEF_COMPONENT_ID, RELIEF_FEATURE_ID) values (?, ?)");

			else if (fromTable == TableEnum.CITYOBJECT && toTable == TableEnum.CITYOBJECT)
				ps = batchConn.prepareStatement("insert into GENERALIZATION (GENERALIZES_TO_ID, CITYOBJECT_ID) values (?, ?)");

			else if (attrName != null)
				ps = batchConn.prepareStatement("update " + fromTable + " set " + attrName + "=? where ID=?");

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
	}

	@Override
	public void close() throws SQLException {
		for (PreparedStatement ps : psMap.values())
			ps.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.BASIC;
	}

}
