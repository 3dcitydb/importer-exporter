/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.citygml.importer.database.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.modules.citygml.common.database.cache.CacheTable;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceDataToTexImage;

public class DBXlinkImporterSurfaceDataToTexImage implements DBXlinkImporter {
	private final CacheTable tempTable;
	private final DBXlinkImporterManager xlinkImporterManager;
	private PreparedStatement psXlink;
	private int batchCounter;

	public DBXlinkImporterSurfaceDataToTexImage(CacheTable tempTable, DBXlinkImporterManager xlinkImporterManager) throws SQLException {
		this.tempTable = tempTable;
		this.xlinkImporterManager = xlinkImporterManager;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement(new StringBuilder()
		.append("insert into ").append(tempTable.getTableName())
		.append(" (FROM_ID, TO_ID) values (?, ?)").toString());
	}

	public boolean insert(DBXlinkSurfaceDataToTexImage xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getFromId());
		psXlink.setLong(2, xlinkEntry.getToId());

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
		return DBXlinkImporterEnum.SURFACE_DATA_TO_TEX_IMAGE;
	}

}
