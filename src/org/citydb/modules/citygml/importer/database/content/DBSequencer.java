/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
