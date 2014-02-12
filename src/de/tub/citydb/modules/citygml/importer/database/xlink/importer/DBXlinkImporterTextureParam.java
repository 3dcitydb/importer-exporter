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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import de.tub.citydb.modules.citygml.common.database.cache.CacheTable;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;

public class DBXlinkImporterTextureParam implements DBXlinkImporter {
	private final CacheTable tempTable;
	private final DBXlinkImporterManager xlinkImporterManager;
	
	private Connection connection;
	private PreparedStatement psXlink;
	private int batchCounter;

	public DBXlinkImporterTextureParam(CacheTable tempTable, DBXlinkImporterManager xlinkImporterManager) throws SQLException {
		this.tempTable = tempTable;
		this.xlinkImporterManager = xlinkImporterManager;

		init();
	}

	private void init() throws SQLException {
		connection = tempTable.getConnection();
		psXlink = connection.prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, GMLID, TYPE, IS_TEXTURE_PARAMETERIZATION, TEXPARAM_GMLID, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, TARGET_URI, TEXTURE_COORDINATES_ID) values " +
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

		if (xlinkEntry.getTextureCoord() != null)
			psXlink.setObject(7, xlinkImporterManager.getCacheAdapter().getGeometryConverter().getDatabaseObject(xlinkEntry.getTextureCoord(), connection));
		else
			psXlink.setNull(7, xlinkImporterManager.getCacheAdapter().getGeometryConverter().getNullGeometryType(),
					xlinkImporterManager.getCacheAdapter().getGeometryConverter().getNullGeometryTypeName());

		if (xlinkEntry.getTargetURI() != null && xlinkEntry.getTargetURI().length() != 0)
			psXlink.setString(8, xlinkEntry.getTargetURI());
		else
			psXlink.setNull(8, Types.VARCHAR);
		
		if (xlinkEntry.getTextureCoordId() != 0)
			psXlink.setInt(9, xlinkEntry.getTextureCoordId());
		else
			psXlink.setNull(9, Types.NULL);

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
		return DBXlinkImporterEnum.XLINK_TEXTUREPARAM;
	}

}
