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
package org.citydb.core.operation.importer.database.xlink.resolver;

import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.common.cache.IdCacheEntry;
import org.citydb.core.operation.common.xlink.DBXlinkBasic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class XlinkBasic implements DBXlinkResolver {
    private final Connection connection;
    private final DBXlinkResolverManager manager;

    private final Map<String, PreparedStatement> statements;
    private final Map<String, Integer> counters;
    private final String schema;

    public XlinkBasic(Connection connection, DBXlinkResolverManager manager) {
        this.connection = connection;
        this.manager = manager;

        schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();
        statements = new HashMap<>();
        counters = new HashMap<>();
    }

    public boolean insert(DBXlinkBasic xlink) throws SQLException {
        IdCacheEntry entry = TableEnum.SURFACE_GEOMETRY.getName().equalsIgnoreCase(xlink.getTable()) ?
                manager.getGeometryId(xlink.getGmlId()) :
                manager.getObjectId(xlink.getGmlId());

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
            if (counters.merge(key, 1, Integer::sum) == manager.getDatabaseAdapter().getMaxBatchSize()) {
                manager.executeBatchWithLock(ps, this);
                counters.put(key, 0);
            }
        }

        return true;
    }

    private PreparedStatement getPreparedStatement(DBXlinkBasic xlink, String key) throws SQLException {
        PreparedStatement ps = statements.get(key);
        if (ps == null) {
            ps = connection.prepareStatement(xlink.isBidirectional() ?
                    "insert into " + schema + "." + xlink.getTable() + " (" + xlink.getToColumn() + ", " + xlink.getFromColumn() + ") values (?, ?)" :
                    "update " + schema + "." + xlink.getTable() + " set " + (xlink.isForward() ? xlink.getFromColumn() : xlink.getToColumn()) + "=? where ID=?");

            statements.put(key, ps);
        }

        return ps;
    }

    private String getKey(DBXlinkBasic xlink) {
        return xlink.getTable() + "_" + xlink.getFromColumn() + "_" + xlink.getToColumn();
    }

    @Override
    public void executeBatch() throws SQLException {
        for (PreparedStatement ps : statements.values())
            ps.executeBatch();

        counters.replaceAll((k, v) -> v = 0);
    }

    @Override
    public void close() throws SQLException {
        for (PreparedStatement ps : statements.values())
            ps.close();
    }

    @Override
    public DBXlinkResolverEnum getDBXlinkResolverType() {
        return DBXlinkResolverEnum.BASIC;
    }
}
