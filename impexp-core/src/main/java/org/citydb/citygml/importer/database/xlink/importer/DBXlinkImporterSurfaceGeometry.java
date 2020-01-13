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
package org.citydb.citygml.importer.database.xlink.importer;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBXlinkImporterSurfaceGeometry implements DBXlinkImporter {
	private final DBXlinkImporterManager xlinkImporterManager;
	private PreparedStatement psXlink;
	private int batchCounter;

	public DBXlinkImporterSurfaceGeometry(CacheTable tempTable, DBXlinkImporterManager xlinkImporterManager) throws SQLException {
		this.xlinkImporterManager = xlinkImporterManager;

		psXlink = tempTable.getConnection().prepareStatement(new StringBuilder("insert into " + tempTable.getTableName()) 
			.append(" (ID, PARENT_ID, ROOT_ID, REVERSE, GMLID, CITYOBJECT_ID, TABLE_NAME, FROM_COLUMN) values ")
			.append("(?, ?, ?, ?, ?, ?, ?, ?)").toString());
	}

	public boolean insert(DBXlinkSurfaceGeometry xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setLong(2, xlinkEntry.getParentId());
		psXlink.setLong(3, xlinkEntry.getRootId());
		psXlink.setInt(4, xlinkEntry.isReverse() ? 1 : 0);
		psXlink.setString(5, xlinkEntry.getGmlId());
		psXlink.setLong(6, xlinkEntry.getCityObjectId());

		if (xlinkEntry.getTable() != null) {
			psXlink.setString(7, xlinkEntry.getTable());
			psXlink.setString(8, xlinkEntry.getFromColumn());
		} else {
			psXlink.setNull(7, Types.NULL);
			psXlink.setNull(8, Types.VARCHAR);
		}
		
		psXlink.addBatch();
		if (++batchCounter == xlinkImporterManager.getCacheAdapter().getMaxBatchSize())
			executeBatch();
		
		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psXlink.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psXlink.close();
	}

	@Override
	public DBXlinkImporterEnum getDBXlinkImporterType() {
		return DBXlinkImporterEnum.SURFACE_GEOMETRY;
	}

}
