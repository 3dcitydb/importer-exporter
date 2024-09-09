/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.importer.database;

import org.citydb.config.Config;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class SequenceHelper {
    private final Connection connection;
    private final AbstractDatabaseAdapter databaseAdapter;

    private HashMap<String, PreparedStatement> psIdMap;

    public SequenceHelper(Connection connection, AbstractDatabaseAdapter databaseAdapter, Config config) throws SQLException {
        this.connection = connection;
        this.databaseAdapter = databaseAdapter;

        psIdMap = new HashMap<String, PreparedStatement>();
    }

    public long getNextSequenceValue(String sequence) throws SQLException {
        PreparedStatement stmt = psIdMap.get(sequence);
        if (stmt == null) {
            StringBuilder query = new StringBuilder("select ").append(databaseAdapter.getSQLAdapter().getNextSequenceValue(sequence));
            if (databaseAdapter.getSQLAdapter().requiresPseudoTableInSelect())
                query.append(" from ").append(databaseAdapter.getSQLAdapter().getPseudoTableName());

            stmt = connection.prepareStatement(query.toString());
            psIdMap.put(sequence, stmt);
        }

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next())
                return rs.getLong(1);

            throw new SQLException("Failed to retrieve the next sequence value from " + sequence + ".");
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve the next sequence value from " + sequence + ".", e);
        }
    }

    public void close() throws SQLException {
        for (PreparedStatement stmt : psIdMap.values())
            stmt.close();
    }
}
