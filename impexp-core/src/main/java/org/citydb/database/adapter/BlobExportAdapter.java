/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlobExportAdapter {
	protected final Connection connection;
	private final BlobType blobType;
	private final String schema;

	private PreparedStatement psExport;

	public BlobExportAdapter(Connection connection, BlobType blobType, String schema) {
		this.connection = connection;
		this.blobType = blobType;
		this.schema = schema;
	}

	public byte[] getInByteArray(long id) throws SQLException {
		if (psExport == null) {
			psExport = connection.prepareStatement(blobType == BlobType.TEXTURE_IMAGE ?
					"select tex_image_data from " + schema + ".tex_image where id=?" :
					"select library_object from " + schema + ".implicit_geometry where id=?");
		}

		psExport.setLong(1, id);
		try (ResultSet rs = psExport.executeQuery()) {
            return rs.next() ? rs.getBytes(1) : null;
		}
	}

	public boolean writeToFile(long id, String fileName) throws SQLException, IOException {
        return writeToStream(getInByteArray(id), Files.newOutputStream(Paths.get(fileName)));
	}

	public boolean writeToStream(long id, OutputStream stream) throws SQLException, IOException {
	    return writeToStream(getInByteArray(id), stream);
	}

	private boolean writeToStream(byte[] buffer, OutputStream stream) throws IOException {
	    if (buffer != null && buffer.length != 0) {
	        try (OutputStream out = stream) {
                out.write(buffer);
                return true;
            }
        } else
            return false;
    }

	public void close() throws SQLException {
		if (psExport != null)
			psExport.close();
	}
}
