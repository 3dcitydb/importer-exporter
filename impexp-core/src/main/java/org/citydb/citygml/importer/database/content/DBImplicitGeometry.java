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
package org.citydb.citygml.importer.database.content;

import org.citydb.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.ConcurrentLockManager;
import org.citydb.citygml.importer.util.ExternalFileChecker;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.log.Logger;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DBImplicitGeometry implements DBImporter {
	private final ConcurrentLockManager lockManager = ConcurrentLockManager.getInstance(DBImplicitGeometry.class);
	private final Logger log = Logger.getInstance();
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psImplicitGeometry;
	private PreparedStatement psUpdateImplicitGeometry;
	private PreparedStatement psSelectLibraryObject;

	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private ExternalFileChecker externalFileChecker;
	private int batchCounter;

	public DBImplicitGeometry(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String insertStmt = "insert into " + schema + ".implicit_geometry (id, reference_to_library) values (?, ?)";
		String updateStmt = "update " + schema + ".implicit_geometry set mime_type=?, relative_brep_id=?, relative_other_geom=? where id=?";
		String selectStmt = "select ID from " + schema + ".implicit_geometry where reference_to_library=?";

		psImplicitGeometry = batchConn.prepareStatement(insertStmt);
		psUpdateImplicitGeometry = batchConn.prepareStatement(updateStmt);
		psSelectLibraryObject = batchConn.prepareStatement(selectStmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		geometryConverter = importer.getGeometryConverter();
		externalFileChecker = importer.getExternalFileChecker();
	}

	protected long doImport(ImplicitGeometry implicitGeometry) throws CityGMLImportException, SQLException {
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
					importer.logOrThrowErrorMessage(importer.getObjectSignature(implicitGeometry) +
							": XLink reference '" + gmlId + "' to remote relative GML geometry is not supported.");
					return 0;
				}				

				gmlId = gmlId.replaceAll("^#", "");
				isXLink = true;

			} else if (property.isSetGeometry()) {
				relativeGeometry = property.getGeometry();
				gmlId = relativeGeometry.getId();
				updateTable = !relativeGeometry.hasLocalProperty(CoreConstants.GEOMETRY_ORIGINAL);
			}
		}

		// synchronize concurrent processing of the same implicit geometry
		// different implicit geometries however may be processed concurrently
		ReentrantLock lock = lockManager.getLock(gmlId != null ? gmlId : libraryURI);
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
				implicitGeometryId = importer.getObjectId(gmlId);				

			if (implicitGeometryId <= 0) {
				implicitGeometryId = importer.getNextSequenceValue(SequenceEnum.IMPLICIT_GEOMETRY_ID_SEQ.getName());
				psImplicitGeometry.setLong(1, implicitGeometryId);
				psImplicitGeometry.setString(2, libraryURI);
				psImplicitGeometry.addBatch();
				++batchCounter;

				if (gmlId != null)
					importer.putObjectUID(gmlId, implicitGeometryId, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID);

				importer.updateObjectCounter(implicitGeometry, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID, implicitGeometryId);
			}

			if (updateTable) {
				psUpdateImplicitGeometry.setLong(4, implicitGeometryId);

				if (libraryURI != null) {
					// mimeType
					if (implicitGeometry.isSetMimeType() && implicitGeometry.getMimeType().isSetValue())
						psUpdateImplicitGeometry.setString(1, implicitGeometry.getMimeType().getValue());
					else
						psUpdateImplicitGeometry.setNull(1, Types.VARCHAR);

					try {
						// propagate the link to the library object
						Map.Entry<String, String> fileInfo = externalFileChecker.getFileInfo(libraryURI);
						importer.propagateXlink(new DBXlinkLibraryObject(
								implicitGeometryId,
								fileInfo.getKey()));
					} catch (IOException e) {
						log.error("Failed to read library object file at '" + libraryURI + "': " + e.getMessage());
					}
				} else
					psUpdateImplicitGeometry.setNull(1, Types.VARCHAR);

				long geometryId = 0;
				GeometryObject geometryObject = null;

				if (relativeGeometry != null) {
					if (importer.isSurfaceGeometry(relativeGeometry))
						geometryId = surfaceGeometryImporter.importImplicitGeometry(relativeGeometry);
					else if (importer.isPointOrLineGeometry(relativeGeometry))
						geometryObject = geometryConverter.getPointOrCurveGeometry(relativeGeometry);
					else
						importer.logOrThrowUnsupportedGeometryMessage(implicitGeometry, relativeGeometry);

					implicitGeometry.unsetRelativeGMLGeometry();
				}

				if (geometryId != 0)
					psUpdateImplicitGeometry.setLong(2, geometryId);
				else
					psUpdateImplicitGeometry.setNull(2, Types.NULL);

				if (geometryObject != null)
					psUpdateImplicitGeometry.setObject(3, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
				else
					psUpdateImplicitGeometry.setNull(3, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
							importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

				psUpdateImplicitGeometry.addBatch();
				++batchCounter;
			}

			if (batchCounter > 0) {
				importer.executeBatch(TableEnum.IMPLICIT_GEOMETRY);
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

		if (isXLink && !importer.lookupAndPutObjectUID("#xlink#" + gmlId, 1, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID)) {
			importer.propagateXlink(new DBXlinkSurfaceGeometry(
					MappingConstants.IMPLICIT_GEOMETRY_TABLE,
					implicitGeometryId, 
					gmlId, 
					null));
		}

		return implicitGeometryId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psImplicitGeometry.executeBatch();
			psUpdateImplicitGeometry.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psImplicitGeometry.close();
		psUpdateImplicitGeometry.close();
		psSelectLibraryObject.close();
	}

}
