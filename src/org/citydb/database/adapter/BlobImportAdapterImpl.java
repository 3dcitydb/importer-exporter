/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
			psUpdate.setBinaryStream(1, in);
			psUpdate.setLong(2, id);
			psUpdate.executeUpdate();		
			connection.commit();
			
			return true;
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
