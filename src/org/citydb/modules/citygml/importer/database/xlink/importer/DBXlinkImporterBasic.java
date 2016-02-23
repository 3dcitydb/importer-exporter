/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
package org.citydb.modules.citygml.importer.database.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;

public class DBXlinkImporterBasic implements DBXlinkImporter {
	private final CacheTable tempTable;
	private final DBXlinkImporterManager xlinkImporterManager;
	private PreparedStatement psXlink;	
	private int batchCounter;

	public DBXlinkImporterBasic(CacheTable tempTable, DBXlinkImporterManager xlinkImporterManager) throws SQLException {
		this.tempTable = tempTable;
		this.xlinkImporterManager = xlinkImporterManager;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement(new StringBuilder()
		.append("insert into ").append(tempTable.getTableName())
		.append(" (ID, FROM_TABLE, GMLID, TO_TABLE, ATTRNAME) values ")
		.append("(?, ?, ?, ?, ?)").toString());
	}

	public boolean insert(DBXlinkBasic xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setInt(2, xlinkEntry.getFromTable().ordinal());
		psXlink.setString(3, xlinkEntry.getGmlId());
		psXlink.setInt(4, xlinkEntry.getToTable().ordinal());

		if (xlinkEntry.getAttrName() != null)
			psXlink.setString(5, xlinkEntry.getAttrName());
		else
			psXlink.setNull(5, Types.VARCHAR);

		psXlink.addBatch();
		if (++batchCounter == xlinkImporterManager.getCacheAdapter().getMaxBatchSize())
			executeBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psXlink.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psXlink.close();
	}

	@Override
	public DBXlinkImporterEnum getDBXlinkImporterType() {
		return DBXlinkImporterEnum.XLINK_BASIC;
	}

}
