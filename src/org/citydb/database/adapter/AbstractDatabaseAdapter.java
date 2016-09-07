/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.database.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citydb.api.database.BalloonTemplateHandler;
import org.citydb.api.database.DatabaseAdapter;
import org.citydb.api.database.DatabaseConnectionWarning;
import org.citydb.config.project.database.DBConnection;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.DatabaseMetaDataImpl;
import org.citydb.modules.common.balloon.BalloonTemplateHandlerImpl;

public abstract class AbstractDatabaseAdapter implements DatabaseAdapter {
	protected DatabaseConnectionPool connectionPool;	
	protected DatabaseMetaDataImpl metaData;
	protected DBConnection connectionDetails;
	protected AbstractGeometryConverterAdapter geometryAdapter;
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
	public abstract int getMaxBatchSize();
		
	@Override
	public DBConnection getConnectionDetails() {
		return connectionDetails;
	}

	public void setConnectionDetails(DBConnection connectionDetails) {
		this.connectionDetails = connectionDetails;
	}
	
	@Override
	public DatabaseMetaDataImpl getConnectionMetaData() {
		return metaData;
	}
	
	public void setConnectionMetaData(DatabaseMetaDataImpl metaData) {
		this.metaData = metaData;
	}

	@Override
	public AbstractGeometryConverterAdapter getGeometryConverter() {
		return geometryAdapter;
	}
	
	@Override
	public AbstractWorkspaceManagerAdapter getWorkspaceManager() {
		return workspaceAdapter;
	}

	@Override
	public AbstractUtilAdapter getUtil() {
		return utilAdapter;
	}

	@Override
	public BalloonTemplateHandler getBalloonTemplateHandler(File template) {
		return new BalloonTemplateHandlerImpl(template, this);
	}

	@Override
	public BalloonTemplateHandler getBalloonTemplateHandler(String template) {
		return new BalloonTemplateHandlerImpl(template, this);
	}

	public AbstractSQLAdapter getSQLAdapter() {
		return sqlAdapter;
	}

	@Override
	public List<DatabaseConnectionWarning> getConnectionWarnings() {
		return connectionWarnings != null ? connectionWarnings : Collections.<DatabaseConnectionWarning>emptyList();
	}

	public void addConnectionWarning(DatabaseConnectionWarning connectionWarning) {
		if (connectionWarnings == null)
			connectionWarnings = new ArrayList<DatabaseConnectionWarning>();
		
		connectionWarnings.add(connectionWarning);
	}
	
	public void addConnectionWarnings(List<DatabaseConnectionWarning> connectionWarnings) {
		if (this.connectionWarnings == null)
			this.connectionWarnings = new ArrayList<DatabaseConnectionWarning>(connectionWarnings);
		else
			this.connectionWarnings.addAll(connectionWarnings);
	}
	
}
