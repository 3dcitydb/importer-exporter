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
package de.tub.citydb.modules.citygml.importer.database.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLinearRing;

public class DBXlinkImporterLinearRing implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psLinearRing;
	private int batchCounter;

	public DBXlinkImporterLinearRing(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psLinearRing = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (GMLID, PARENT_GMLID, RING_NO) values " +
			"(?, ?, ?)");
	}

	public boolean insert(DBXlinkLinearRing xlinkEntry) throws SQLException {
		psLinearRing.setString(1, xlinkEntry.getGmlId());
		psLinearRing.setString(2, xlinkEntry.getParentGmlId());
		psLinearRing.setInt(3, xlinkEntry.getRingId());

		psLinearRing.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			executeBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psLinearRing.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psLinearRing.close();
	}

	@Override
	public DBXlinkImporterEnum getDBXlinkImporterType() {
		return DBXlinkImporterEnum.LINEAR_RING;
	}

}
