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
//import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
//import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
//import java.sql.ResultSet;
import java.sql.SQLException;

//import org.postgresql.largeobject.LargeObject;
//import org.postgresql.largeobject.LargeObjectManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;

public class XlinkLibraryObject implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection externalFileConn;
	private final Config config;

	private PreparedStatement psInsert;
	private PreparedStatement psPrepare;
	PreparedStatement psSelect;
	private String localPath;

	public XlinkLibraryObject(Connection textureImageConn, Config config) throws SQLException {
		this.externalFileConn = textureImageConn;
		this.config = config;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getImportPath();

		// for large object (OID) -- did not work correctly
//		psPrepare = externalFileConn.prepareStatement("update IMPLICIT_GEOMETRY set LIBRARY_OBJECT=lo_create(-1) where ID=?");
//		psSelect = externalFileConn.prepareStatement("select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=? for update");
		psInsert = externalFileConn.prepareStatement("update IMPLICIT_GEOMETRY set LIBRARY_OBJECT=? where ID=?");
	}

	public boolean insert(DBXlinkLibraryObject xlink) throws SQLException {
		String objectFileName = xlink.getFileURI();
		boolean isRemote = true;
		URL objectURL = null;
		
		try {
/*			// first step: prepare BLOB
			psPrepare.setLong(1, xlink.getId());
			psPrepare.executeUpdate();

			// second step: get prepared BLOB to fill it with contents
			psSelect.setLong(1, xlink.getId());
			ResultSet rs = psSelect.executeQuery();
			if (!rs.next()) {
				LOG.error("Database error while importing library object: " + objectFileName);

				rs.close();
				externalFileConn.rollback();
				return false;
			}

			Blob blob = rs.getBlob(1);
			blob.truncate(1);
*/		
			// next step: try and upload library object data
			try {
				objectURL = new URL(objectFileName);
				objectFileName = objectURL.toString();

			} catch (MalformedURLException malURL) {
				isRemote = false;
				File objectFile = new File(objectFileName);
				objectFileName = localPath + File.separator + objectFile.getPath();
			}

			LOG.debug("Importing library object: " + objectFileName);

			InputStream in = null;

			if (isRemote) {
				in = objectURL.openStream();
			} else {
				in = new FileInputStream(objectFileName);
			}
		
			if (in == null) {
				LOG.error("Database error while importing library object: " + objectFileName);

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
		
			// insert bytea data type into database
//			psInsert.setLong(1, oid); // for large object
			psInsert.setBinaryStream(1, in, in.available());
			psInsert.setLong(2, xlink.getId());
			psInsert.execute();
			
			in.close();			
			externalFileConn.commit();
			return true;
			
		} catch (FileNotFoundException fnfEx) {
			LOG.error("Failed to find library object file '" + objectFileName + "'.");

			externalFileConn.rollback();
			return false;
		} catch (IOException ioEx) {
			LOG.error("Failed to read library object file '" + objectFileName + "': " + ioEx.getMessage());

			externalFileConn.rollback();
			return false;
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while importing library object '" + objectFileName + "': " + sqlEx.getMessage());

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
		psPrepare.close();
		psSelect.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.LIBRARY_OBJECT;
	}

}
