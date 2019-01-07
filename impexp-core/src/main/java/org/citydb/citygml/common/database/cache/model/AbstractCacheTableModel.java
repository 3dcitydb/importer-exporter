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
package org.citydb.citygml.common.database.cache.model;

import org.citydb.database.adapter.AbstractSQLAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public abstract class AbstractCacheTableModel {

    AbstractCacheTableModel() {

    }

    public abstract CacheTableModel getType();
    protected abstract String getColumns(AbstractSQLAdapter sqlAdapter);

    public void create(Connection conn, String tableName, AbstractSQLAdapter sqlAdapter) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlAdapter.getCreateUnloggedTable(tableName, getColumns(sqlAdapter)));
            conn.commit();
        }
    }

    public void createAsSelect(Connection conn, String tableName, String select, AbstractSQLAdapter sqlAdapter) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlAdapter.getCreateUnloggedTableAsSelect(tableName, select));
            conn.commit();
        }
    }

    public long size(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select count(*) from " + tableName)) {
            return rs.next() ? rs.getLong(1) : -1;
        }
    }

    public void truncate(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from " + tableName);
            conn.commit();
        }
    }

    public void drop(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("drop table " + tableName);
            conn.commit();
        }
    }

    public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
        // override in subclasses if necessary
    }
}
