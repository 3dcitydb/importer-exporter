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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//import org.postgresql.largeobject.LargeObject;
//import org.postgresql.largeobject.LargeObjectManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;

public class XlinkTextureImage implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection externalFileConn;
	private final Config config;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psInsert;
	private String localPath;
	private CounterEvent counter;
	private boolean replacePathSeparator;

	public XlinkTextureImage(Connection externalFileConn, Config config, DBXlinkResolverManager resolverManager) throws SQLException {
		this.externalFileConn = externalFileConn;
		this.config = config;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getImportPath();
		counter = new CounterEvent(CounterType.TEXTURE_IMAGE, 1, this);
		replacePathSeparator = File.separatorChar == '/';
		
		psInsert = externalFileConn.prepareStatement("update SURFACE_DATA set TEX_IMAGE=? where ID=?");
	}

	public boolean insert(DBXlinkTextureFile xlink) throws SQLException {
		String imageFileName = xlink.getFileURI();
		boolean isRemote = true;
		URL imageURL = null;
		File imageFile = new File(imageFileName);
		
		try {
			// first step: check whether we deal with a local or remote texture file
			try {
				imageURL = new URL(imageFileName);
				imageFileName = imageURL.toString();

			} catch (MalformedURLException malURL) {
				isRemote = false;
				
				if (replacePathSeparator)
					imageFileName = imageFileName.replace("\\", "/");
				
//				File imageFile = new File(imageFileName);
				if (!imageFile.isAbsolute()) {
					imageFileName = localPath + File.separator + imageFile.getPath();
					imageFile = new File(imageFileName);
				}
				
				// check minimum requirements for local texture files
				if (!imageFile.exists() || !imageFile.isFile() || !imageFile.canRead()) {
					LOG.error("Failed to read texture file '" + imageFileName + "'.");
					return false;
				} else if (imageFile.length() == 0) {
					LOG.error("Skipping 0 byte texture file '" + imageFileName + "'.");
					return false;
				}
				

			}
			
			// next step: try and upload image data
			LOG.debug("Importing texture file: " + imageFileName);
			resolverManager.propagateEvent(counter);
			
			InputStream in = null;

			if (isRemote) {
				in = imageURL.openStream();
			} else {
				in = new FileInputStream(imageFile);
			}
		
			if (in == null) {
				LOG.error("Database error while importing texture file: " + imageFileName);

				externalFileConn.rollback();
				return false;
			}

/*			// insert large object (OID) data type into database
			
			// All LargeObject API calls must be within a transaction block
			externalFileConn.setAutoCommit(false);
		
			// Get the Large Object Manager to perform operations with
			LargeObjectManager lobj = ((org.postgresql.PGConnection)externalFileConn).getLargeObjectAPI();

			// Create a new large object
			long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);

			// Open the large object for writing
			LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

			// Copy the data from the file to the large object
			byte buf[] = new byte[2048];
			int s, tl = 0;

			while ((s = in.read(buf, 0, 2048)) > 0)
			{
			obj.write(buf, 0, s);
			tl = tl + s;
			}

			// Close the large object
			obj.close();
*/
			
			// Now insert the row into SURFACE_DATA
//			psInsert.setLong(1, oid); // for large object
			psInsert.setBinaryStream(1, in, in.available()); // for bytea
			psInsert.setLong(2, xlink.getId());
			psInsert.execute();		

			in.close();
			externalFileConn.commit();
			return true;
			
		} catch (FileNotFoundException fnfEx) {
			LOG.error("Failed to find texture file '" + imageFileName + "'.");
			
			externalFileConn.rollback();
			return false;
		} catch (IOException ioEx) {
			LOG.error("Failed to read texture file '" + imageFileName + "': " + ioEx.getMessage());

			externalFileConn.rollback();
			return false;
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while importing texture file '" + imageFileName + "': " + sqlEx.getMessage());

			externalFileConn.rollback();
			return false;
		} 
		
	}

	@Override
	public void executeBatch() throws SQLException {
		// we do not have any action here, since we are heavily committing and roll-backing
		// within the insert-method. that's also the reason why we need a separated connection instance.
	}

	@Override
	public void close() throws SQLException {
		psInsert.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXTURE_IMAGE;
	}

}
