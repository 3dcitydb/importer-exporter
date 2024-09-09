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
package org.citydb.core.database.adapter.oracle;

import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.AbstractSchemaManagerAdapter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SchemaManagerAdapter extends AbstractSchemaManagerAdapter {

    protected SchemaManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
        super(databaseAdapter);
    }

    @Override
    public String getDefaultSchema() {
        return formatSchema(databaseAdapter.getConnectionDetails().getUser());
    }

    @Override
    public boolean equalsDefaultSchema(String schema) {
        schema = formatSchema(schema);
        return schema == null || schema.isEmpty() || getDefaultSchema().equals(schema);
    }

    @Override
    public boolean existsSchema(Connection connection, String schema) {
        if (schema == null)
            throw new IllegalArgumentException("Schema name may not be null.");

        schema = formatSchema(schema);
        if (schema.isEmpty())
            schema = getDefaultSchema();

        try (PreparedStatement stmt = connection.prepareStatement("select count(*) from all_users where username = ?")) {
            stmt.setString(1, schema);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> fetchSchemasFromDatabase(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select username from all_users order by username")) {
            List<String> schemas = new ArrayList<>();

            while (rs.next()) {
                String schema = rs.getString(1);
                try (Statement check = connection.createStatement();
                     ResultSet checkRs = check.executeQuery("select 1 from " +
                             schema + ".database_srs where rownum = 1")) {
                    if (checkRs.next())
                        schemas.add(schema);
                } catch (SQLException e) {
                    //
                }
            }

            return schemas;
        }
    }

    @Override
    public String formatSchema(String schema) {
        return schema != null ? schema.trim().toUpperCase(Locale.ROOT) : null;
    }
}