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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BlobImportAdapter {
	protected final Connection connection;
	private final BlobType blobType;
	private final String schema;

	private PreparedStatement psUpdate;

	public BlobImportAdapter(Connection connection, BlobType blobType, String schema) {
		this.connection = connection;
		this.blobType = blobType;
		this.schema = schema;
	}

	public void insert(long id, InputStream stream) throws SQLException {
		if (psUpdate == null) {
			psUpdate = connection.prepareStatement(blobType == BlobType.TEXTURE_IMAGE ?
					"update " + schema + ".tex_image set tex_image_data=? where id=?" :
					"update " + schema + ".implicit_geometry set library_object=? where id=?");
		}

		psUpdate.setBinaryStream(1, stream);
		psUpdate.setLong(2, id);
		psUpdate.executeUpdate();
	}

	public void close() throws SQLException {
		if (psUpdate != null)
			psUpdate.close();
	}
}
