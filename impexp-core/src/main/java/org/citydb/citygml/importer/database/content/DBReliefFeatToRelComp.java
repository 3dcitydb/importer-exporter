/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;

public class DBReliefFeatToRelComp implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psReliefFeatToRelComp;
	private int batchCounter;

	public DBReliefFeatToRelComp(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".relief_feat_to_rel_comp (relief_component_id, relief_feature_id) values " +
				"(?, ?)";
		psReliefFeatToRelComp = batchConn.prepareStatement(stmt);
	}

	protected void doImport(long reliefComponentId, long reliefFeatureId) throws CityGMLImportException, SQLException {
		psReliefFeatToRelComp.setLong(1, reliefComponentId);
		psReliefFeatToRelComp.setLong(2, reliefFeatureId);

		psReliefFeatToRelComp.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.RELIEF_FEAT_TO_REL_COMP);
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psReliefFeatToRelComp.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psReliefFeatToRelComp.close();
	}

}
