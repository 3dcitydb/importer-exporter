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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBBridgeOpenToThemSrf implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBridgeOpenToThemSrf;
	private int batchCounter;

	public DBBridgeOpenToThemSrf(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psBridgeOpenToThemSrf = batchConn.prepareStatement("insert into BRIDGE_OPEN_TO_THEM_SRF (BRIDGE_OPENING_ID, BRIDGE_THEMATIC_SURFACE_ID) values (?, ?)");
	}

	public void insert(long openingId, long thematicSurfaceId) throws SQLException {
        psBridgeOpenToThemSrf.setLong(1, openingId);
        psBridgeOpenToThemSrf.setLong(2, thematicSurfaceId);

        psBridgeOpenToThemSrf.addBatch();
        if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BRIDGE_OPEN_TO_THEM_SRF);
	}

	@Override
	public void executeBatch() throws SQLException {
		psBridgeOpenToThemSrf.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBridgeOpenToThemSrf.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BRIDGE_OPEN_TO_THEM_SRF;
	}

}
