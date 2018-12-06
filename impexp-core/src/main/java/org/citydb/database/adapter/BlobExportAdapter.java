/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.database.adapter;

import org.citydb.log.Logger;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlobExportAdapter {
	protected final Logger log = Logger.getInstance();
	protected final Connection connection;
	private final String schema;

	private PreparedStatement psExport;
	private BlobType blobType;

	public BlobExportAdapter(Connection connection, BlobType blobType, String schema) {
		this.connection = connection;
		this.blobType = blobType;
		this.schema = schema;
	}

	public byte[] getInByteArray(long id, String objectName) throws SQLException {
		if (psExport == null)
			psExport = connection.prepareStatement(blobType == BlobType.TEXTURE_IMAGE ?
					"select TEX_IMAGE_DATA from " + schema + ".TEX_IMAGE where ID=?" : "select LIBRARY_OBJECT from " + schema + ".IMPLICIT_GEOMETRY where ID=?");

		psExport.setLong(1, id);

		// try and read texture image attribute from SURFACE_DATA table
		try (ResultSet rs = psExport.executeQuery()) {
			if (!rs.next()) {
				log.error("Error while exporting a " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + " does not exist in database.");
				return null;
			}

			byte[] buf = rs.getBytes(1);
			if (rs.wasNull() || buf.length == 0) {
				log.error("Failed to read " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + " (ID = " + id + ").");
				return null;
			}

			return buf;
		}
	}

	public boolean writeToFile(long id, String objectName, String fileName) throws SQLException {
		try (FileOutputStream out = new FileOutputStream(fileName)) {
			byte[] buf = getInByteArray(id, objectName);
			if (buf != null) {
				out.write(buf);
				return true;
			} else
				return false;
			
		} catch (IOException e) {
			log.error("Failed to write " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file " + fileName + ": " + e.getMessage());
			return false;
		}
	}

	public boolean writeToStream(long id, String objectName, OutputStream stream) throws SQLException {
		try (BufferedOutputStream out = new BufferedOutputStream(stream)) {
			byte[] buf = getInByteArray(id, objectName);
			if (buf != null) {
				out.write(buf);
				return true;
			} else
				return false;

		} catch (IOException e) {
			log.error("Failed to write " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + e.getMessage());
			return false;
		}
	}

	public ByteArrayInputStream getInStream(ResultSet rs, String columnName, String objectName) throws SQLException {
		return new ByteArrayInputStream(rs.getBytes(columnName));
	}

	public void close() throws SQLException {
		if (psExport != null)
			psExport.close();
	}

}
