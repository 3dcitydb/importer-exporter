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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.citydb.database.adapter.AbstractDatabaseAdapter;

public class DBSequencer {
	private final Connection conn;
	private final AbstractDatabaseAdapter databaseAdapter;
	private HashMap<DBSequencerEnum, PreparedStatement> psIdMap;

	public DBSequencer(Connection conn, AbstractDatabaseAdapter databaseAdapter) throws SQLException {
		this.conn = conn;
		this.databaseAdapter = databaseAdapter;
		psIdMap = new HashMap<DBSequencerEnum, PreparedStatement>();
	}

	public long getDBId(DBSequencerEnum sequence) throws SQLException {
		if (sequence == null)
			return 0;

		PreparedStatement pstsmt = psIdMap.get(sequence);
		if (pstsmt == null) {
			StringBuilder query = new StringBuilder()
			.append("select ")
			.append(databaseAdapter.getSQLAdapter().getNextSequenceValue(sequence));
			if (databaseAdapter.getSQLAdapter().requiresPseudoTableInSelect())
				query.append(" from ").append(databaseAdapter.getSQLAdapter().getPseudoTableName());
			
			pstsmt = conn.prepareStatement(query.toString());
			psIdMap.put(sequence, pstsmt);
		}

		ResultSet rs = null;
		long id = 0;

		try {
			rs = pstsmt.executeQuery();

			if (rs.next())
				id = rs.getLong(1);

		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				rs = null;
			}
		}

		return id;
	}
	
	public void close() throws SQLException {
		for (PreparedStatement stmt : psIdMap.values())
			stmt.close();
	}
}
