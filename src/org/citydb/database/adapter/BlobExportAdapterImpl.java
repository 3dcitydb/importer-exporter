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

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.log.Logger;

public class BlobExportAdapterImpl implements BlobExportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psExport;
	private BlobType blobType;

	public BlobExportAdapterImpl(Connection connection, BlobType blobType) {
		this.connection = connection;
		this.blobType = blobType;		
	}

	@Override
	public byte[] getInByteArray(long id, String objectName) throws SQLException {
		ResultSet rs = null;

		try {
			if (psExport == null)
				psExport = connection.prepareStatement(blobType == BlobType.TEXTURE_IMAGE ?
						"select TEX_IMAGE_DATA from TEX_IMAGE where ID=?" : "select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=?");

			// try and read texture image attribute from SURFACE_DATA table
			psExport.setLong(1, id);
			rs = psExport.executeQuery();
			if (!rs.next()) {
				LOG.error("Error while exporting a " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + " does not exist in database.");
				return null;
			}

			byte[] buf = rs.getBytes(1);
			if (rs.wasNull() || buf.length == 0) {
				LOG.error("Failed to read " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + ".");
				return null;
			}

			return buf;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}

	@Override
	public boolean getInFile(long id, String objectName, String fileName) throws SQLException {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(fileName);

			byte[] buf = getInByteArray(id, objectName);
			if (buf != null) {
				out.write(buf);
				return true;
			} else
				return false;
			
		} catch (IOException e) {
			LOG.error("Failed to write " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file " + fileName + ": " + e.getMessage());
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	@Override
	public ByteArrayInputStream getInStream(ResultSet rs, String columnName, String objectName) throws SQLException {
		return new ByteArrayInputStream(rs.getBytes(columnName));
	}

	@Override
	public void close() throws SQLException {
		if (psExport != null)
			psExport.close();
	}

}
