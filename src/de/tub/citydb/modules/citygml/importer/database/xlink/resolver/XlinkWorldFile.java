/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.modules.citygml.importer.database.xlink.resolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import de.tub.citydb.util.Util;

public class XlinkWorldFile implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	
    private final Connection batchConn;
    private final Config config;

    private PreparedStatement psUpdate;
    private String localPath;
    private int dbSrid;
    private int batchCounter;

    public XlinkWorldFile(Connection batchConn, Config config) throws SQLException {
        this.batchConn = batchConn;
        this.config = config;

        init();
    }

    private void init() throws SQLException {
        localPath = config.getInternal().getImportPath();
        dbSrid = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().getSrid();

        psUpdate = batchConn.prepareStatement("update SURFACE_DATA set GT_ORIENTATION=?, GT_REFERENCE_POINT=? where ID=?");
    }

    public boolean insert(DBXlinkTextureFile xlink) throws SQLException {
        // get uri of image file
        String imageURI = xlink.getFileURI();

        if (Util.isRemoteXlink(imageURI)) {
        	// remote world files are not supported so far...
            return false;
        }

        File imageFile = new File(imageURI);
        String worldFileName = imageFile.getPath();
        List<String> fileList = new ArrayList<String>();

        // naming schema for world files is as follows:
        // 1) if the image file name has a 3-character extension (image1.tif), the world file
        // has the same name followed by an extension containing the first and last letters
        // of the image's extension and ending with a 'w' (image1.tfw).
        // 2) if the extension has more or less than 3 characters, including no extension at all,
        // then the world file name is formed by simply appending a 'w' to the image file name.
        if (worldFileName != null) {
        	// following rule 2
            fileList.add(worldFileName + 'w');

            int index = worldFileName.lastIndexOf('.');
            if (index != -1) {
            	String name = worldFileName.substring(0, index + 1);
            	String extension = worldFileName.substring(index + 1, worldFileName.length());

                if (extension != null && extension.length() == 3)
                    fileList.add(name + extension.substring(0, 1) + extension.substring(2, 3) + 'w');
            }

            for (String fileName : fileList) {
                File worldFile = new File(localPath + File.separator + fileName);
                if (!worldFile.exists() || !worldFile.canRead() || worldFile.isDirectory())
                    continue;

                LOG.info("Processing world file: " + worldFile);

                FileReader fr = null;
                BufferedReader in = null;
                try {
                	fr = new FileReader(worldFile);
                    in = new BufferedReader(fr);
                    List<Double> content = new ArrayList<Double>();

                    String line = null;
                    int i = 0;
                    while ((line = in.readLine()) != null && i++ < 6)
                    	content.add(Double.parseDouble(line));

                    if (content.size() == 6) {
                    	// interpretation of world file content taken from CityGML specification document version 1.0.0
                    	String orientation = content.get(0) + " " + content.get(2) + " " + content.get(1) + " " + content.get(3);

                    	JGeometry geom = new JGeometry(content.get(4), content.get(5), dbSrid);
						STRUCT obj = JGeometry.store(geom, batchConn);

						psUpdate.setString(1, orientation);
						psUpdate.setObject(2, obj);
						psUpdate.setLong(3, xlink.getId());
						
						psUpdate.addBatch();
						if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
							executeBatch();

						return true;

                    } else {
                    	LOG.error("Error while importing world file '" + worldFile +"': Content could not be interpreted.");
                    }

                } catch (FileNotFoundException fnfe) {
                	LOG.error("Failed to find world file '" + worldFile +"'.");
                    continue;
                } catch (IOException ioe) {
                	LOG.error("I/O error while importing world file '" + worldFile +"': " + ioe.getMessage());
					continue;
				} catch (NumberFormatException nfe) {
					LOG.error("Error while importing world file '" + worldFile +"': Content could not be interpreted.");
					continue;
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							//
						}

						in = null;
					}

					if (fr != null) {
						try {
							fr.close();
						} catch (IOException e) {
							//
						}

						fr = null;
					}
				}
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
