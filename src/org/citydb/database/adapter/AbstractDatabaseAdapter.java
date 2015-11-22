/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.database.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citydb.api.database.BalloonTemplateFactory;
import org.citydb.api.database.DatabaseAdapter;
import org.citydb.api.database.DatabaseConnectionWarning;
import org.citydb.config.project.database.DBConnection;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.DatabaseMetaDataImpl;
import org.citydb.modules.common.balloon.BalloonTemplateFactoryImpl;

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
	public BalloonTemplateFactory getBalloonTemplateFactory() {
		return BalloonTemplateFactoryImpl.getInstance();
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
