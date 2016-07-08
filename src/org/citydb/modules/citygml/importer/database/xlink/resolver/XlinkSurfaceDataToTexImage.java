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
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceDataToTexImage;

public class XlinkSurfaceDataToTexImage implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psUpdate;

	private int batchCounter;

	public XlinkSurfaceDataToTexImage(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psUpdate = batchConn.prepareStatement("update SURFACE_DATA set TEX_IMAGE_ID=? where ID=?");
	}

	public boolean insert(DBXlinkSurfaceDataToTexImage xlink) throws SQLException {
		psUpdate.setLong(1, xlink.getToId());
		psUpdate.setLong(2, xlink.getFromId());

		psUpdate.addBatch();
		if (++batchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
			executeBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psUpdate.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psUpdate.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.SURFACE_DATA_TO_TEX_IMAGE;
	}

}
