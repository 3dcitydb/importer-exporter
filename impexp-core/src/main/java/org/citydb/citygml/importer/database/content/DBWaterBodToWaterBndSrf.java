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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;

public class DBWaterBodToWaterBndSrf implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psWaterBodToWaterBndSrf;
	private int batchCounter;

	public DBWaterBodToWaterBndSrf(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".waterbod_to_waterbnd_srf (waterboundary_surface_id, waterbody_id) values " +
				"(?, ?)";
		psWaterBodToWaterBndSrf = batchConn.prepareStatement(stmt);
	}

	protected void doImport(long waterSurfaceId, long waterBodyId) throws CityGMLImportException, SQLException {
		psWaterBodToWaterBndSrf.setLong(1, waterSurfaceId);
		psWaterBodToWaterBndSrf.setLong(2, waterBodyId);

		psWaterBodToWaterBndSrf.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.WATERBOD_TO_WATERBND_SRF);
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psWaterBodToWaterBndSrf.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psWaterBodToWaterBndSrf.close();
	}

}
