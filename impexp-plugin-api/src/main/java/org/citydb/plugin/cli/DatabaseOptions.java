/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

package org.citydb.plugin.cli;

import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseType;
import picocli.CommandLine;

public class DatabaseOptions {
    enum Type {postgresql, oracle}

    @CommandLine.Option(names = {"-t", "--db-type"}, paramLabel = "<database>", defaultValue = "postgresql",
            description = "Database type: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Type type;

    @CommandLine.Option(names = {"-H", "--db-host"}, paramLabel = "<host>",
            required = true, description = "Name of the host on which the 3DCityDB is running.")
    private String host;

    @CommandLine.Option(names = {"-P", "--db-port"}, paramLabel = "<port>",
            description = "Port of the 3DCityDB server (default: 5432 | 1521).")
    private Integer port;

    @CommandLine.Option(names = {"-d", "--db-name"}, paramLabel = "<name>",
            required = true, description = "Name of the 3DCityDB database to connect to.")
    private String name;

    @CommandLine.Option(names = {"-s", "--db-schema"}, paramLabel = "<schema>",
            description = "Schema to use when connecting to the 3DCityDB (default: citydb | username).")
    private String schema;

    @CommandLine.Option(names = {"-u", "--db-user"}, paramLabel = "<name>",
            required = true, description = "Username to use when connecting to the 3DCityDB.")
    private String user;

    @CommandLine.Option(names = {"-p", "--db-password"}, paramLabel = "<password>", arity = "0..1",
            description = "Password to use when connecting to the 3DCityDB (leave empty to be prompted).")
    private String password;

    public DatabaseType getType() {
        return type == Type.oracle ? DatabaseType.ORACLE : DatabaseType.POSTGIS;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        if (port == null) {
            return type == Type.oracle ? 1521 : 5432;
        } else {
            return port;
        }
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isValid() {
        return name != null
                && host != null
                && user != null;
    }

    public DBConnection toDBConnection() {
        DBConnection connection = new DBConnection();
        connection.setDatabaseType(getType());
        connection.setSid(name);
        connection.setSchema(schema);
        connection.setServer(host);
        connection.setPort(getPort());
        connection.setUser(user);
        connection.setPassword(password);
        
        return connection;
    }
}
