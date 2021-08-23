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
package org.citydb.core.operation.importer.database.xlink.resolver;

import org.citydb.core.operation.common.xlink.DBXlinkSurfaceDataToTexImage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class XlinkSurfaceDataToTexImage implements DBXlinkResolver {
	private final DBXlinkResolverManager manager;
	private final PreparedStatement psUpdate;

	private int batchCounter;

	public XlinkSurfaceDataToTexImage(Connection connection, DBXlinkResolverManager manager) throws SQLException {
		this.manager = manager;

		String schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();
		psUpdate = connection.prepareStatement("update " + schema + ".SURFACE_DATA set TEX_IMAGE_ID=? where ID=?");
	}

	public boolean insert(DBXlinkSurfaceDataToTexImage xlink) throws SQLException {
		psUpdate.setLong(1, xlink.getToId());
		psUpdate.setLong(2, xlink.getFromId());

		psUpdate.addBatch();
		if (++batchCounter == manager.getDatabaseAdapter().getMaxBatchSize())
			manager.executeBatch(this);

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
