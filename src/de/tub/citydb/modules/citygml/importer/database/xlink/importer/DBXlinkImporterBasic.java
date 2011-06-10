/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import java.sql.Types;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;

public class DBXlinkImporterBasic implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psXlink;	
	private int batchCounter;

	public DBXlinkImporterBasic(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, FROM_TABLE, GMLID, TO_TABLE, ATTRNAME) values " +
			"(?, ?, ?, ?, ?)");
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
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
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
