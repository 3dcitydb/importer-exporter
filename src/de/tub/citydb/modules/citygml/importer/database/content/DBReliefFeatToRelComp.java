/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.config.internal.Internal;

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
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
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
