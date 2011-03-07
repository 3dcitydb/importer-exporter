package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DBSequencer {
	private Connection conn;
	private HashMap<DBSequencerEnum, PreparedStatement> psIdMap;

	public DBSequencer(Connection conn) throws SQLException {
		this.conn = conn;
		psIdMap = new HashMap<DBSequencerEnum, PreparedStatement>();
	}

	public long getDBId(DBSequencerEnum sequence) throws SQLException {
		if (sequence == null)
			return 0;

		PreparedStatement pstsmt = psIdMap.get(sequence);
		if (pstsmt == null) {
			pstsmt = conn.prepareStatement("select " + sequence.toString() + ".nextval from dual");
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
}
