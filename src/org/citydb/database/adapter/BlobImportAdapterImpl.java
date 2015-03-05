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
package org.citydb.database.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.citydb.log.Logger;

public class BlobImportAdapterImpl implements BlobImportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psUpdate;
	private BlobType blobType;

	public BlobImportAdapterImpl(Connection connection, BlobType blobType) throws SQLException {
		this.connection = connection;
		this.blobType = blobType;

		psUpdate = connection.prepareStatement(blobType == BlobType.TEXTURE_IMAGE ?
				"update TEX_IMAGE set TEX_IMAGE_DATA=? where ID=?" : "update IMPLICIT_GEOMETRY set LIBRARY_OBJECT=? where ID=?");
	}

	@Override
	public boolean insert(long id, InputStream in, String fileName) throws SQLException {
		try {
			psUpdate.setBinaryStream(1, in, in.available());
			psUpdate.setLong(2, id);
			psUpdate.executeUpdate();		
			connection.commit();
			
			return true;
		} catch (IOException e) {
			LOG.error("Failed to read " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file '" + fileName + "': " + e.getMessage());
			return false;
		} catch (SQLException e) {
			LOG.error("SQL error while importing " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file '" + fileName + "': " + e.getMessage());
			return false;
		}
	}

	@Override
	public void close() throws SQLException {
		psUpdate.close();
	}

}
