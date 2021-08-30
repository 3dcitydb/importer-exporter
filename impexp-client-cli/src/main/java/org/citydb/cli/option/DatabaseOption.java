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

package org.citydb.cli.option;

import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.core.util.CoreConstants;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

public class DatabaseOption implements CliOption {
    enum Type {postgresql, oracle}

    @CommandLine.Option(names = {"-T", "--db-type"}, paramLabel = "<database>",
            defaultValue = "${env:" + CoreConstants.ENV_CITYDB_TYPE + ":-postgresql}",
            description = "Database type: ${COMPLETION-CANDIDATES} (default: postgresql).")
    private Type type;

    @CommandLine.Option(names = {"-H", "--db-host"},
            defaultValue = "${env:" + CoreConstants.ENV_CITYDB_HOST + "}",
            description = "Name of the host on which the 3DCityDB is running.")
    private String host;

    @CommandLine.Option(names = {"-P", "--db-port"},
            defaultValue = "${env:" + CoreConstants.ENV_CITYDB_PORT + "}",
            description = "Port of the 3DCityDB server (default: 5432 | 1521).")
    private Integer port;

    @CommandLine.Option(names = {"-d", "--db-name"},
            defaultValue = "${env:" + CoreConstants.ENV_CITYDB_NAME + "}",
            description = "Name of the 3DCityDB database to connect to.")
    private String name;

    @CommandLine.Option(names = {"-S", "--db-schema"},
            defaultValue = "${env:" + CoreConstants.ENV_CITYDB_SCHEMA + "}",
            description = "Schema to use when connecting to the 3DCityDB (default: citydb | username).")
    private String schema;

    @CommandLine.Option(names = {"-u", "--db-username"}, paramLabel = "<name>",
            defaultValue = "${env:" + CoreConstants.ENV_CITYDB_USERNAME + "}",
            description = "Username to use when connecting to the 3DCityDB.")
    private String user;

    @CommandLine.Option(names = {"-p", "--db-password"}, arity = "0..1",
            defaultValue = "${env:" + CoreConstants.ENV_CITYDB_PASSWORD + "}",
            description = "Password to use when connecting to the 3DCityDB (leave empty to be prompted).")
    private String password;

    private boolean hasUserInput;

    public boolean hasUserInput() {
        return hasUserInput;
    }

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

    public DatabaseConnection toDatabaseConnection() {
        DatabaseConnection connection = null;
        if (hasUserInput) {
            connection = new DatabaseConnection();
            connection.setDatabaseType(getType());
            connection.setSid(name);
            connection.setSchema(schema);
            connection.setServer(host);
            connection.setPort(getPort());
            connection.setUser(user);
            connection.setPassword(password);
        }

        return connection;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        hasUserInput = commandLine.getParseResult().hasMatchedOption("-T")
                || name != null
                || schema != null
                || host != null
                || port != null
                || user != null
                || password != null;

        if (hasUserInput) {
            List<String> requiredOptions = new ArrayList<>();
            if (host == null) {
                requiredOptions.add("--db-host=<host>");
            }

            if (name == null) {
                requiredOptions.add("--db-name=<name>");
            }

            if (user == null) {
                requiredOptions.add("--db-username=<name>");
            }

            if (!requiredOptions.isEmpty()) {
                throw new CommandLine.ParameterException(commandLine, "Error: Missing required argument(s): " +
                        String.join(", ", requiredOptions));
            }
        }
    }
}
