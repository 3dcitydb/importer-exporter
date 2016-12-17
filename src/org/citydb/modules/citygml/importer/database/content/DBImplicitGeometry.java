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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.internal.Internal;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.modules.citygml.importer.util.ConcurrentLockManager;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBImplicitGeometry implements DBImporter {
	private final ConcurrentLockManager lockManager = ConcurrentLockManager.getInstance(DBImplicitGeometry.class);
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psImplicitGeometry;
	private PreparedStatement psUpdateImplicitGeometry;
	private PreparedStatement psSelectLibraryObject;

	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;

	public DBImplicitGeometry(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;
		init();
	}

	private void init() throws SQLException {
		psImplicitGeometry = batchConn.prepareStatement("insert into IMPLICIT_GEOMETRY (ID, REFERENCE_TO_LIBRARY) values (?, ?)");
		psUpdateImplicitGeometry = batchConn.prepareStatement("update IMPLICIT_GEOMETRY set MIME_TYPE=?, RELATIVE_BREP_ID=?, RELATIVE_OTHER_GEOM=? where ID=?");
		psSelectLibraryObject = batchConn.prepareStatement("select ID from IMPLICIT_GEOMETRY where REFERENCE_TO_LIBRARY=?");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
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
				updateTable = !relativeGeometry.hasLocalProperty(Internal.GEOMETRY_ORIGINAL);
			}
		}

		// synchronize concurrent processing of the same implicit geometry
		// different implicit geometries however may be processed concurrently
		ReentrantLock lock = lockManager.putAndGetLock(gmlId != null ? gmlId : libraryURI);
		lock.lock();

		ResultSet rs = null;
		try {
			if (libraryURI != null && !libraryURI.isEmpty()) {
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
				implicitGeometryId = dbImporterManager.getDBId(DBSequencerEnum.IMPLICIT_GEOMETRY_ID_SEQ);
				psImplicitGeometry.setLong(1, implicitGeometryId);
				psImplicitGeometry.setString(2, libraryURI);
				psImplicitGeometry.addBatch();
				++batchCounter;

				if (gmlId != null)
					dbImporterManager.putUID(gmlId, implicitGeometryId, CityGMLClass.ABSTRACT_CITY_OBJECT);

				dbImporterManager.updateFeatureCounter(CityGMLClass.IMPLICIT_GEOMETRY, implicitGeometryId, gmlId, false);
			}

			if (updateTable) {
				psUpdateImplicitGeometry.setLong(4, implicitGeometryId);

				if (libraryURI != null) {
					// mimeType
					if (implicitGeometry.isSetMimeType() && implicitGeometry.getMimeType().isSetValue())
						psUpdateImplicitGeometry.setString(1, implicitGeometry.getMimeType().getValue());
					else
						psUpdateImplicitGeometry.setNull(1, Types.VARCHAR);

					// propagate the link to the library object
					dbImporterManager.propagateXlink(new DBXlinkLibraryObject(
							implicitGeometryId,
							libraryURI
							));
				} else
					psUpdateImplicitGeometry.setNull(1, Types.VARCHAR);

				long geometryId = 0;
				GeometryObject geometryObject = null;

				if (relativeGeometry != null) {
					if (surfaceGeometryImporter.isSurfaceGeometry(relativeGeometry))
						geometryId = surfaceGeometryImporter.insertImplicitGeometry(relativeGeometry);
					else if (otherGeometryImporter.isPointOrLineGeometry(relativeGeometry))
						geometryObject = otherGeometryImporter.getPointOrCurveGeometry(relativeGeometry);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								implicitGeometry.getCityGMLClass(), 
								implicitGeometry.getId()));
						msg.append(": Unsupported geometry type ");
						msg.append(relativeGeometry.getGMLClass()).append('.');

						LOG.error(msg.toString());
					}

					implicitGeometry.unsetRelativeGMLGeometry();
				}

				if (geometryId != 0)
					psUpdateImplicitGeometry.setLong(2, geometryId);
				else
					psUpdateImplicitGeometry.setNull(2, Types.NULL);

				if (geometryObject != null)
					psUpdateImplicitGeometry.setObject(3, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
				else
					psUpdateImplicitGeometry.setNull(3, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
							dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

				psUpdateImplicitGeometry.addBatch();
				++batchCounter;
			}

			if (batchCounter > 0) {
				dbImporterManager.executeBatch(DBImporterEnum.IMPLICIT_GEOMETRY);
				batchConn.commit();
				batchCounter = 0;
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

			lockManager.releaseLock(gmlId != null ? gmlId : libraryURI);
			lock.unlock();
		}

		if (isXLink && !dbImporterManager.lookupAndPutUID("#xlink#" + gmlId, 1, CityGMLClass.IMPLICIT_GEOMETRY)) {
			dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
					gmlId, 
					implicitGeometryId, 
					TableEnum.IMPLICIT_GEOMETRY, 
					null));
		}

		return implicitGeometryId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psImplicitGeometry.executeBatch();
		psUpdateImplicitGeometry.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psImplicitGeometry.close();
		psUpdateImplicitGeometry.close();
		psSelectLibraryObject.close();
		lockManager.clear();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.IMPLICIT_GEOMETRY;
	}
}
