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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import de.tub.citydb.util.Util;

public class DBImplicitGeometry implements DBImporter {
	private final static ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final Connection commitConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psImplicitGeometry;
	private PreparedStatement psUpdateImplicitGeometry;
	private PreparedStatement psSelectLibraryObject;
	private DBSurfaceGeometry surfaceGeometryImporter;

	private boolean affineTransformation;
	private int batchCounter;

	public DBImplicitGeometry(Connection batchConn, Connection commitConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.commitConn = commitConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		psImplicitGeometry = commitConn.prepareStatement("insert into IMPLICIT_GEOMETRY (ID, REFERENCE_TO_LIBRARY) values (?, ?)");
		psUpdateImplicitGeometry = batchConn.prepareStatement("update IMPLICIT_GEOMETRY set MIME_TYPE=?, RELATIVE_GEOMETRY_ID=? where ID=?");
		psSelectLibraryObject = batchConn.prepareStatement("select ID from IMPLICIT_GEOMETRY where REFERENCE_TO_LIBRARY=?");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
	}

	public long insert(ImplicitGeometry implicitGeometry, long parentId) throws SQLException {
		// writing implicit geometries differs from other importers. we want to avoid duplicate
		// entries for library objects. thus we have to make sure on this prior to inserting entries.
		long implicitGeometryId = 0;
		boolean updateTable = false;
		boolean isXLink = false;

		String libraryURI = implicitGeometry.getLibraryObject();
		if (libraryURI != null)
			libraryURI = libraryURI.trim();

		AbstractGeometry relativeGeometry = null;
		String gmlId = null;

		if (implicitGeometry.isSetRelativeGMLGeometry()) {
			GeometryProperty<? extends AbstractGeometry> property = implicitGeometry.getRelativeGMLGeometry();

			if (property.isSetHref()) {
				gmlId = property.getHref();
				if (Util.isRemoteXlink(gmlId)) {
					LOG.error("XLink reference '" + gmlId + "' to remote relative GML geometry is not supported.");					
					return 0;
				}				

				gmlId = gmlId.replaceAll("^#", "");
				isXLink = true;

			} else if (property.isSetGeometry()) {
				relativeGeometry = property.getGeometry();
				gmlId = relativeGeometry.getId();
				updateTable = true;
			}
		}

		// we need to synchronize this check.
		final ReentrantLock lock = mainLock;
		lock.lock();

		ResultSet rs = null;
		try {
			if (libraryURI != null) {
				// check if we have the same library object in database
				psSelectLibraryObject.setString(1, libraryURI);
				rs = psSelectLibraryObject.executeQuery();
				if (rs.next())
					implicitGeometryId = rs.getLong(1);
				else
					updateTable = true;
			} 

			// check relative geometry reference
			else if (gmlId != null)
				implicitGeometryId = dbImporterManager.getDBId(gmlId, CityGMLClass.ABSTRACT_CITY_OBJECT);				

			if (implicitGeometryId == 0) {
				implicitGeometryId = dbImporterManager.getDBId(DBSequencerEnum.IMPLICIT_GEOMETRY_SEQ);
				psImplicitGeometry.setLong(1, implicitGeometryId);
				psImplicitGeometry.setString(2, libraryURI);
				psImplicitGeometry.execute();

				if (gmlId != null)
					dbImporterManager.putGmlId(gmlId, implicitGeometryId, CityGMLClass.ABSTRACT_CITY_OBJECT);

				dbImporterManager.updateFeatureCounter(CityGMLClass.IMPLICIT_GEOMETRY);
			}

		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}

				rs = null;
			}

			lock.unlock();
		}

		// ok, the rest can be handled concurrently and as batch update...
		if (updateTable) {
			psUpdateImplicitGeometry.setLong(3, implicitGeometryId);

			if (libraryURI != null) {
				// mimeType
				if (implicitGeometry.isSetMimeType())
					psUpdateImplicitGeometry.setString(1, implicitGeometry.getMimeType());
				else
					psUpdateImplicitGeometry.setNull(1, Types.VARCHAR);

				// propagate the link to the library object
				dbImporterManager.propagateXlink(new DBXlinkLibraryObject(
						implicitGeometryId,
						libraryURI
						));
			} else
				psUpdateImplicitGeometry.setNull(1, Types.VARCHAR);

			if (relativeGeometry != null) {
				// if affine transformation is activated we apply the user-defined affine
				// transformation to the transformation matrix associated with the implicit geometry.
				// thus, we do not need to apply it to the coordinate values
				if (affineTransformation)
					surfaceGeometryImporter.setApplyAffineTransformation(false);

				long surfaceGeometryId = surfaceGeometryImporter.insert(relativeGeometry, parentId);
				if (surfaceGeometryId != 0)
					psUpdateImplicitGeometry.setLong(2, surfaceGeometryId);
				else
					psUpdateImplicitGeometry.setNull(2, 0);

				// re-activate affine transformation on surface geometry writer if necessary
				if (affineTransformation)
					surfaceGeometryImporter.setApplyAffineTransformation(true);
			} else
				psUpdateImplicitGeometry.setNull(2, 0);

			psUpdateImplicitGeometry.addBatch();
			if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
				dbImporterManager.executeBatch(DBImporterEnum.IMPLICIT_GEOMETRY);
		}

		if (isXLink && !dbImporterManager.lookupAndPutGmlId("#xlink#" + gmlId, 1, CityGMLClass.IMPLICIT_GEOMETRY)) {
			dbImporterManager.propagateXlink(new DBXlinkBasic(
					implicitGeometryId, 
					TableEnum.IMPLICIT_GEOMETRY, 
					gmlId, 
					TableEnum.SURFACE_GEOMETRY)
					);
		}

		return implicitGeometryId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psUpdateImplicitGeometry.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psImplicitGeometry.close();
		psUpdateImplicitGeometry.close();
		psSelectLibraryObject.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.IMPLICIT_GEOMETRY;
	}
}
