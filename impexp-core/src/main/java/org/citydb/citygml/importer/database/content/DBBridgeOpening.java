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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractOpening;
import org.citygml4j.model.citygml.bridge.Door;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBBridgeOpening implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psOpening;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBBridgeOpenToThemSrf openingToThemSurfaceImporter;
	private DBAddress addressImporter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean affineTransformation;

	public DBBridgeOpening(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".bridge_opening (id, objectclass_id, address_id, lod3_multi_surface_id, lod4_multi_surface_id, " +
				"lod3_implicit_rep_id, lod4_implicit_rep_id, " +
				"lod3_implicit_ref_point, lod4_implicit_ref_point, " +
				"lod3_implicit_transformation, lod4_implicit_transformation) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		psOpening = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		openingToThemSurfaceImporter = importer.getImporter(DBBridgeOpenToThemSrf.class);
		addressImporter = importer.getImporter(DBAddress.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(AbstractOpening opening) throws CityGMLImportException, SQLException {
		return doImport(opening, null, 0);
	}

	protected long doImport(AbstractOpening opening, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(opening);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long openingId =  cityObjectImporter.doImport(opening, featureType);

		// import opening information
		// primary id
		psOpening.setLong(1, openingId);

		// objectclass id
		psOpening.setInt(2, featureType.getObjectClassId());

		// brid:address
		long addressId = 0;
		if (opening instanceof Door) {
			Door door = (Door)opening;

			if (door.isSetAddress() && !door.getAddress().isEmpty()) {
				// unfortunately, we can just represent one address in database...
				AddressProperty property = door.getAddress().get(0);
				Address address = property.getObject();

				if (address != null) {
					addressImporter.doImport(address);
					property.unsetAddress();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								featureType.getTable(),
								openingId,
								href,
								"ADDRESS_ID"));
					}
				}
			}
		}

		if (addressId != 0)
			psOpening.setLong(3, addressId);
		else
			psOpening.setNull(3, Types.NULL);

		// brid:lodXMultiSurface
		for (int i = 0; i < 2; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = opening.getLod3MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = opening.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), openingId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.BRIDGE_OPENING.getName(),
								openingId, 
								href, 
								"lod" + (i + 3) + "_multi_surface_id"));
					}
				}
			}

			if (multiSurfaceId != 0)
				psOpening.setLong(4 + i, multiSurfaceId);
			else
				psOpening.setNull(4 + i, Types.NULL);
		}

		// brid:lodXImplicitRepresentation
		for (int i = 0; i < 2; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = opening.getLod3ImplicitRepresentation();
				break;
			case 1:
				implicit = opening.getLod4ImplicitRepresentation();
				break;
			}

			if (implicit != null) {
				if (implicit.isSetObject()) {
					ImplicitGeometry geometry = implicit.getObject();

					// reference Point
					if (geometry.isSetReferencePoint())
						pointGeom = geometryConverter.getPoint(geometry.getReferencePoint());

					// transformation matrix
					if (geometry.isSetTransformationMatrix()) {
						Matrix matrix = geometry.getTransformationMatrix().getMatrix();
						if (affineTransformation)
							matrix = importer.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);

						matrixString = valueJoiner.join(" ", matrix.toRowPackedList());
					}

					// reference to IMPLICIT_GEOMETRY
					implicitId = implicitGeometryImporter.doImport(geometry);
				}
			}

			if (implicitId != 0)
				psOpening.setLong(6 + i, implicitId);
			else
				psOpening.setNull(6 + i, Types.NULL);

			if (pointGeom != null)
				psOpening.setObject(8 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psOpening.setNull(8 + i, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
						importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

			if (matrixString != null)
				psOpening.setString(10 + i, matrixString);
			else
				psOpening.setNull(10 + i, Types.VARCHAR);
		}

		psOpening.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BRIDGE_OPENING);

		if (parent instanceof AbstractBoundarySurface)
			openingToThemSurfaceImporter.doImport(openingId, parentId);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(opening, openingId, featureType);

		return openingId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psOpening.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psOpening.close();
	}

}
