/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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
package org.citydb.core.database.adapter;

import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.util.log.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public abstract class AbstractSchemaManagerAdapter {
    private final Logger log = Logger.getInstance();
    protected final AbstractDatabaseAdapter databaseAdapter;

    protected AbstractSchemaManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
    }

    public abstract String getDefaultSchema();

    public abstract boolean equalsDefaultSchema(String schema);

    public abstract boolean existsSchema(Connection connection, String schema);

    public abstract List<String> fetchSchemasFromDatabase(Connection connection) throws SQLException;

    public abstract String formatSchema(String schema);

    public boolean existsSchema(String schema) {
        return existsSchema(schema, false);
    }

    public boolean existsSchema(String schema, boolean logResult) {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            boolean exists = existsSchema(conn, schema);
            if (logResult) {
                if (!exists) {
                    log.error("The database schema '" + schema + "' does not exist.");
                } else {
                    log.info("Switching to database schema '" + schema + "'.");
                }
            }

            return exists;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<String> fetchSchemasFromDatabase(DatabaseConnection databaseConnection) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", databaseConnection.getUser());
        properties.setProperty("password", databaseConnection.getPassword());

        try (Connection conn = DriverManager.getConnection(databaseAdapter.getJDBCUrl(
                databaseConnection.getServer(), databaseConnection.getPort(), databaseConnection.getSid()), properties)) {
            return fetchSchemasFromDatabase(conn);
        }
    }

}
