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
package org.citydb.citygml.importer.database.xlink.resolver;

import org.citydb.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.database.schema.TableEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

public class XlinkBasic implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private HashMap<String, PreparedStatement> psMap;
	private HashMap<String, Integer> psBatchCounterMap;
	private String schema;

	public XlinkBasic(Connection batchConn, DBXlinkResolverManager resolverManager) {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		psMap = new HashMap<>();
		psBatchCounterMap = new HashMap<>();
		schema = resolverManager.getDatabaseAdapter().getConnectionDetails().getSchema();
	}

	public boolean insert(DBXlinkBasic xlink) throws SQLException {
		UIDCacheEntry entry = TableEnum.SURFACE_GEOMETRY.getName().equalsIgnoreCase(xlink.getTable()) ? 
				resolverManager.getGeometryId(xlink.getGmlId()) : resolverManager.getObjectId(xlink.getGmlId());
		if (entry == null)
			return false;

		String key = getKey(xlink);
		PreparedStatement ps = getPreparedStatement(xlink, key);
		if (ps != null) {

			if (xlink.isReverse()) {
				ps.setLong(1, xlink.getId());
				ps.setLong(2, entry.getId());
			} else {
				ps.setLong(1, entry.getId());
				ps.setLong(2, xlink.getId());
			}

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
		PreparedStatement ps = psMap.get(key);
		if (ps == null) {
			if (xlink.isBidirectional()) {
				ps = batchConn.prepareStatement("insert into " + schema + "." + xlink.getTable() +
						" (" + xlink.getToColumn() + ", " + xlink.getFromColumn() + ") " +
						"values (?, ?)");
			}

			else {
				ps = batchConn.prepareStatement("update " + schema + "." + xlink.getTable() +
						" set " + (xlink.isForward() ? xlink.getFromColumn() : xlink.getToColumn()) + "=? where ID=?");
			}
			
			if (ps != null) {
				psMap.put(key, ps);
				psBatchCounterMap.put(key, 0);
			}
		}

		return ps;
	}

	private String getKey(DBXlinkBasic xlink) {
		return xlink.getTable() + "_" + xlink.getFromColumn() + "_" + xlink.getToColumn();
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
