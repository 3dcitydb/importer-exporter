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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;

public class XlinkWorldFile implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psUpdate;
	private int dbSrid;
	private int batchCounter;

	public XlinkWorldFile(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		psUpdate = batchConn.prepareStatement("update SURFACE_DATA set GT_ORIENTATION=?, GT_REFERENCE_POINT=? where ID=?");
	}

	public boolean insert(DBXlinkTextureFile xlink) throws SQLException {

		// naming scheme for world files is as follows:
		// 1) if the image file name has a 3-character extension (image.tif), the world file
		// has the same name followed by an extension containing the first and last letters
		// of the image's extension and ending with a 'w' (image.tfw).
		// 2) if the extension has more or less than 3 characters, including no extension at all,
		// then the world file name is formed by simply appending a 'w' to the image file name.

		String imageFileURI = xlink.getFileURI();
		List<String> candidates = new ArrayList<String>();

		// add candidate according to first scheme
		int index = imageFileURI.lastIndexOf('.');
		if (index != -1) {
			String name = imageFileURI.substring(0, index + 1);
			String extension = imageFileURI.substring(index + 1, imageFileURI.length());
			if (extension != null && extension.length() == 3)
				candidates.add(name + extension.substring(0, 1) + extension.substring(2, 3) + 'w');
		}
		
		// add candidate according to second scheme
		candidates.add(imageFileURI + "w");

		for (String candidate : candidates) {  
			InputStream inputStream;
			try {
				inputStream = resolverManager.openStream(candidate);
			} catch (IOException e1) {
				continue;
			}

			LOG.info("Processing world file: " + candidate);

			try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {        		
				double[] content = new double[6];
				int i = 0;

				String line = null;
				while ((line = in.readLine()) != null && i < content.length)
					content[i++] = Double.parseDouble(line);

				if (i == 6) {
					// interpretation of world file content taken from CityGML specification document version 1.0.0
					String orientation = content[0] + " " + content[2] + " " + content[1] + " " + content[3];
					GeometryObject geomObj = GeometryObject.createPoint(new double[]{content[4], content[5]}, 2, dbSrid);
					Object obj = resolverManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, batchConn);

					psUpdate.setString(1, orientation);
					psUpdate.setObject(2, obj);
					psUpdate.setLong(3, xlink.getId());

					psUpdate.addBatch();
					if (++batchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
						executeBatch();

					return true;
				} else {
					LOG.error("Error while importing world file '" + candidate +"': Content could not be interpreted.");
					break;
				}
			} catch (IOException e) {
				LOG.error("Error while importing world file '" + candidate +"': " + e.getMessage());
				break;
			} catch (NumberFormatException e) {
				LOG.error("Error while importing world file '" + candidate +"': Content could not be interpreted.");
				break;
			}
		}

		return false;
	}

	@Override
	public void executeBatch() throws SQLException {
		psUpdate.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psUpdate.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.WORLD_FILE;
	}

}
