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
import java.sql.Types;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;

public class DBXlinkImporterTextureParam implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psXlink;
	private int batchCounter;

	public DBXlinkImporterTextureParam(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, GMLID, TYPE, IS_TEXTURE_PARAMETERIZATION, TEXPARAM_GMLID, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, TARGET_URI, TEXCOORDLIST_ID) values " +
			"(?, ?, ?, ?, ?, ?, ?, ?, ?)");
	}

	public boolean insert(DBXlinkTextureParam xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setString(2, xlinkEntry.getGmlId());
		psXlink.setInt(3, xlinkEntry.getType().ordinal());
		psXlink.setInt(4, xlinkEntry.isTextureParameterization() ? 1 : 0);

		if (xlinkEntry.getTexParamGmlId() != null && xlinkEntry.getTexParamGmlId().length() != 0)
			psXlink.setString(5, xlinkEntry.getTexParamGmlId());
		else
			psXlink.setNull(5, Types.VARCHAR);

		if (xlinkEntry.getWorldToTexture() != null && xlinkEntry.getWorldToTexture().length() != 0)
			psXlink.setString(6, xlinkEntry.getWorldToTexture());
		else
			psXlink.setNull(6, Types.VARCHAR);

		if (xlinkEntry.getTextureCoord() != null && xlinkEntry.getTextureCoord().length() != 0)
			psXlink.setString(7, xlinkEntry.getTextureCoord());
		else
			psXlink.setNull(7, Types.VARCHAR);

		if (xlinkEntry.getTargetURI() != null && xlinkEntry.getTargetURI().length() != 0)
			psXlink.setString(8, xlinkEntry.getTargetURI());
		else
			psXlink.setNull(8, Types.VARCHAR);

		if (xlinkEntry.getTexCoordListId() != null && xlinkEntry.getTexCoordListId().length() != 0)
			psXlink.setString(9, xlinkEntry.getTexCoordListId());
		else
			psXlink.setNull(9, Types.VARCHAR);

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
		return DBXlinkImporterEnum.XLINK_TEXTUREPARAM;
	}

}
