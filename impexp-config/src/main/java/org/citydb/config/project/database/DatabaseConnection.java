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
package org.citydb.config.project.database;

import org.citydb.config.project.database.DatabaseConfigurationException.ErrorCode;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.util.UUID;

@XmlType(name = "ConnectionType", propOrder = {})
public class DatabaseConnection implements Comparable<DatabaseConnection> {
    @XmlID
    @XmlAttribute
    private String id;
    private String description;
    @XmlElement(required = true)
    private DatabaseType type = DatabaseType.POSTGIS;
    @XmlSchemaType(name = "anyURI")
    @XmlElement(required = true)
    private String server;
    @XmlSchemaType(name = "positiveInteger")
    @XmlElement(required = true)
    private Integer port = 5432;
    @XmlElement(required = true)
    private String sid;
    private String schema;
    private Workspace workspace;
    @XmlElement(required = true)
    private String user;
    private String password;
    private Boolean savePassword = false;
    @XmlTransient
    private String tempPassword;

    @XmlAttribute
    private Integer loginTimeout = 60;
    @XmlAttribute
    private Integer initialSize = 0;
    @XmlAttribute
    private Integer maxActive;
    @XmlAttribute
    private Integer maxIdle;
    @XmlAttribute
    private Integer minIdle;
    @XmlAttribute
    private Integer maxWait;
    @XmlAttribute
    private Boolean testOnBorrow;
    @XmlAttribute
    private Boolean testOnReturn;
    @XmlAttribute
    private Boolean testWhileIdle;
    @XmlAttribute
    private String validationQuery;
    @XmlAttribute
    private String validatorClassName;
    @XmlAttribute
    private Integer timeBetweenEvictionRunsMillis;
    @XmlAttribute
    private Integer numTestsPerEvictionRun;
    @XmlAttribute
    private Integer minEvictableIdleTimeMillis;
    @XmlAttribute
    private Boolean removeAbandoned;
    @XmlAttribute
    private Integer removeAbandonedTimeout;
    @XmlAttribute
    private Boolean logAbandoned;
    @XmlAttribute
    private String connectionProperties;
    @XmlAttribute
    private String initSQL;
    @XmlAttribute
    private Long validationInterval;
    @XmlAttribute
    private Boolean jmxEnabled;
    @XmlAttribute
    private Boolean fairQueue;
    @XmlAttribute
    private Integer abandonWhenPercentageFull;
    @XmlAttribute
    private Long maxAge;
    @XmlAttribute
    private Boolean useEquals;
    @XmlAttribute
    private Integer suspectTimeout;

    public DatabaseConnection() {
        id = "UUID_" + UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DatabaseType getDatabaseType() {
        return type;
    }

    public void setDatabaseType(DatabaseType type) {
        this.type = type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = (schema != null && !schema.trim().isEmpty()) ? schema : null;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public boolean isSetWorkspace() {
        return workspace != null;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSetSavePassword() {
        return savePassword != null ? savePassword : false;
    }

    public Boolean getSavePassword() {
        return savePassword;
    }

    public void setSavePassword(Boolean savePassword) {
        this.savePassword = savePassword;
    }

    public Integer getLoginTimeout() {
        return loginTimeout;
    }

    public boolean isSetLoginTimeout() {
        return loginTimeout != null;
    }

    public void setLoginTimeout(Integer loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(Boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getValidatorClassName() {
        return validatorClassName;
    }

    public void setValidatorClassName(String validatorClassName) {
        this.validatorClassName = validatorClassName;
    }

    public Integer getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(
            Integer timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Integer getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(Integer numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public Integer getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public Boolean getRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(Boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public Integer getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public Boolean getLogAbandoned() {
        return logAbandoned;
    }

    public void setLogAbandoned(Boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getInitSQL() {
        return initSQL;
    }

    public void setInitSQL(String initSQL) {
        this.initSQL = initSQL;
    }

    public Long getValidationInterval() {
        return validationInterval;
    }

    public void setValidationInterval(Long validationInterval) {
        this.validationInterval = validationInterval;
    }

    public Boolean getJmxEnabled() {
        return jmxEnabled;
    }

    public void setJmxEnabled(Boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
    }

    public Boolean getFairQueue() {
        return fairQueue;
    }

    public void setFairQueue(Boolean fairQueue) {
        this.fairQueue = fairQueue;
    }

    public Integer getAbandonWhenPercentageFull() {
        return abandonWhenPercentageFull;
    }

    public void setAbandonWhenPercentageFull(Integer abandonWhenPercentageFull) {
        this.abandonWhenPercentageFull = abandonWhenPercentageFull;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    public Boolean getUseEquals() {
        return useEquals;
    }

    public void setUseEquals(Boolean useEquals) {
        this.useEquals = useEquals;
    }

    public Integer getSuspectTimeout() {
        return suspectTimeout;
    }

    public void setSuspectTimeout(Integer suspectTimeout) {
        this.suspectTimeout = suspectTimeout;
    }

    @Override
    public String toString() {
        return description != null ? getDescription() : toConnectString();
    }

    @Override
    public int compareTo(DatabaseConnection o) {
        return getDescription().toUpperCase().compareTo(o.getDescription().toUpperCase());
    }

    public void validate() throws DatabaseConfigurationException {
        if (user == null || user.trim().isEmpty()) {
            throw new DatabaseConfigurationException(ErrorCode.MISSING_DB_USERNAME, "Missing username.");
        }

        if (server == null || server.trim().isEmpty()) {
            throw new DatabaseConfigurationException(ErrorCode.MISSING_DB_HOSTNAME, "Missing server hostname.");
        }

        if (port == null) {
            throw new DatabaseConfigurationException(ErrorCode.MISSING_DB_PORT, "Missing server port.");
        }

        if (sid == null || sid.trim().isEmpty()) {
            throw new DatabaseConfigurationException(ErrorCode.MISSING_DB_NAME, "Missing database name.");
        }

        if (schema != null && schema.trim().isEmpty()) {
            throw new DatabaseConfigurationException(ErrorCode.EMPTY_DB_SCHEMA, "Database schema cannot be empty.");
        }

        if (workspace != null && !workspace.isSetName() && !workspace.isSetTimestamp()) {
            throw new DatabaseConfigurationException(ErrorCode.INVALID_DB_WORKSPACE, "The database workspace must define a name and/or a timestamp.");
        }
    }

    public String toConnectString() {
        return user + "@" + server + ":" + port + "/" + sid;
    }

    void beforeMarshal(Marshaller marshaller) {
        if (!isSetSavePassword()) {
            tempPassword = password;
            password = null;
        }
    }

    void afterMarshal(Marshaller marshaller) {
        if (!isSetSavePassword()) {
            password = tempPassword;
            tempPassword = null;
        }
    }
}
