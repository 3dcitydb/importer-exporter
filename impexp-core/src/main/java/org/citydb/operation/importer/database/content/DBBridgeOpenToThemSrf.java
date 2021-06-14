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
package org.citydb.operation.importer.database.content;

import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.operation.importer.CityGMLImportException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBBridgeOpenToThemSrf implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psBridgeOpenToThemSrf;
	private int batchCounter;

	public DBBridgeOpenToThemSrf(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".bridge_open_to_them_srf (bridge_opening_id, bridge_thematic_surface_id) values " +
				"(?, ?)";
		psBridgeOpenToThemSrf = batchConn.prepareStatement(stmt);
	}

	protected void doImport(long openingId, long thematicSurfaceId) throws CityGMLImportException, SQLException {
		psBridgeOpenToThemSrf.setLong(1, openingId);
		psBridgeOpenToThemSrf.setLong(2, thematicSurfaceId);

		psBridgeOpenToThemSrf.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BRIDGE_OPEN_TO_THEM_SRF);
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psBridgeOpenToThemSrf.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psBridgeOpenToThemSrf.close();
	}

}
