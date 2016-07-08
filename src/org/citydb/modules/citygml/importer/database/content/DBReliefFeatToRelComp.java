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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBReliefFeatToRelComp implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psReliefFeatToRelComp;
	private int batchCounter;

	public DBReliefFeatToRelComp(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psReliefFeatToRelComp = batchConn.prepareStatement("insert into RELIEF_FEAT_TO_REL_COMP (RELIEF_COMPONENT_ID, RELIEF_FEATURE_ID) values " +
			"(?, ?)");
	}
	
	public void insert(long reliefComponentId, long reliefFeatureId) throws SQLException {
		psReliefFeatToRelComp.setLong(1, reliefComponentId);
		psReliefFeatToRelComp.setLong(2, reliefFeatureId);

		psReliefFeatToRelComp.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.RELIEF_FEAT_TO_REL_COMP);
	}
	
	@Override
	public void executeBatch() throws SQLException {
		psReliefFeatToRelComp.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psReliefFeatToRelComp.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.RELIEF_FEAT_TO_REL_COMP;
	}

}
