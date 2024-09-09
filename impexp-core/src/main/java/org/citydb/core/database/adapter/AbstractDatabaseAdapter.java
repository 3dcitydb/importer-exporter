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
package org.citydb.core.database.adapter;

import org.citydb.config.project.database.DatabaseType;
import org.citydb.core.database.connection.DatabaseConnectionDetails;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.connection.DatabaseConnectionWarning;
import org.citydb.core.database.connection.DatabaseMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDatabaseAdapter {
    protected DatabaseConnectionPool connectionPool;
    protected DatabaseMetaData metaData;
    protected DatabaseConnectionDetails connectionDetails;
    protected AbstractGeometryConverterAdapter geometryAdapter;
    protected AbstractSchemaManagerAdapter schemaAdapter;
    protected AbstractWorkspaceManagerAdapter workspaceAdapter;
    protected AbstractUtilAdapter utilAdapter;
    protected AbstractSQLAdapter sqlAdapter;
    private List<DatabaseConnectionWarning> connectionWarnings;

    public AbstractDatabaseAdapter() {
        connectionPool = DatabaseConnectionPool.getInstance();
    }

    public abstract int getDefaultPort();

    public abstract String getConnectionFactoryClassName();

    public abstract String getJDBCUrl(String server, int port, String database);

    public abstract DatabaseType getDatabaseType();

    public abstract boolean hasVersioningSupport();

    public abstract boolean hasTableStatsSupport();

    public abstract int getMaxBatchSize();

    public DatabaseConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public void setConnectionDetails(DatabaseConnectionDetails connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    public DatabaseMetaData getConnectionMetaData() {
        return metaData;
    }

    public void setConnectionMetaData(DatabaseMetaData metaData) {
        this.metaData = metaData;
    }

    public AbstractGeometryConverterAdapter getGeometryConverter() {
        return geometryAdapter;
    }

    public AbstractSchemaManagerAdapter getSchemaManager() {
        return schemaAdapter;
    }

    public AbstractWorkspaceManagerAdapter getWorkspaceManager() {
        return workspaceAdapter;
    }

    public AbstractUtilAdapter getUtil() {
        return utilAdapter;
    }

    public AbstractSQLAdapter getSQLAdapter() {
        return sqlAdapter;
    }

    public List<DatabaseConnectionWarning> getConnectionWarnings() {
        return connectionWarnings != null ? connectionWarnings : Collections.emptyList();
    }

    public void addConnectionWarning(DatabaseConnectionWarning connectionWarning) {
        if (connectionWarnings == null)
            connectionWarnings = new ArrayList<>();

        connectionWarnings.add(connectionWarning);
    }

    public void addConnectionWarnings(List<DatabaseConnectionWarning> connectionWarnings) {
        if (this.connectionWarnings == null)
            this.connectionWarnings = new ArrayList<>(connectionWarnings);
        else
            this.connectionWarnings.addAll(connectionWarnings);
    }

}
