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
package org.citydb.citygml.importer.database.xlink.resolver;

import org.citydb.citygml.common.xlink.DBXlinkLibraryObject;
import org.citydb.database.adapter.BlobImportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.log.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class XlinkLibraryObject implements DBXlinkResolver {
	private final Logger log = Logger.getInstance();
	private final DBXlinkResolverManager resolverManager;
	private final BlobImportAdapter blobImportAdapter;

	public XlinkLibraryObject(Connection connection, DBXlinkResolverManager resolverManager) throws SQLException {
		this.resolverManager = resolverManager;
		
		blobImportAdapter = resolverManager.getDatabaseAdapter().getSQLAdapter().getBlobImportAdapter(
				connection, BlobType.LIBRARY_OBJECT);
	}

	public boolean insert(DBXlinkLibraryObject xlink) throws SQLException {
		String fileURI = xlink.getFileURI();
		
		try (InputStream stream = new BufferedInputStream(resolverManager.openStream(fileURI)))  {
			blobImportAdapter.insert(xlink.getId(), stream);
			return true;
		} catch (IOException e) {
			log.error("Failed to read library object file '" + fileURI + "'.", e);
			return false;
		}
	}

	@Override
	public void executeBatch() throws SQLException {
		// we do not have any action here
	}

	@Override
	public void close() throws SQLException {
		blobImportAdapter.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.LIBRARY_OBJECT;
	}

}
