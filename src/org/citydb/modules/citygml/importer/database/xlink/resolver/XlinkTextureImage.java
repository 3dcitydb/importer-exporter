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
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.database.adapter.BlobImportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;

public class XlinkTextureImage implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	private final DBXlinkResolverManager resolverManager;

	private BlobImportAdapter textureImportAdapter;	
	private CounterEvent counter;

	public XlinkTextureImage(Connection externalFileConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.resolverManager = resolverManager;
		
		counter = new CounterEvent(CounterType.TEXTURE_IMAGE, 1, this);
		textureImportAdapter = resolverManager.getDatabaseAdapter().getSQLAdapter().getBlobImportAdapter(externalFileConn, BlobType.TEXTURE_IMAGE);
	}

	public boolean insert(DBXlinkTextureFile xlink) throws SQLException {
		resolverManager.propagateEvent(counter);			
		String fileURI = xlink.getFileURI();
		
		try (InputStream inputStream = resolverManager.openStream(fileURI)) {
			return textureImportAdapter.insert(xlink.getId(), inputStream, fileURI);
		} catch (IOException e) {
			LOG.error("Failed to read texture file '" + fileURI + "': " + e.getMessage());
			return false;
		}
	}

	@Override
	public void executeBatch() throws SQLException {
		// we do not have any action here
	}

	@Override
	public void close() throws SQLException {
		textureImportAdapter.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXTURE_IMAGE;
	}

}
