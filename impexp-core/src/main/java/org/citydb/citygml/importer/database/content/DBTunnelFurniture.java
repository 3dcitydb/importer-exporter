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

import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBTunnelFurniture implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psTunnelFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private GeometryConverter geometryImporter;
	private AttributeValueJoiner valueJoiner;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;	
	private boolean affineTransformation;

	public DBTunnelFurniture(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".tunnel_furniture (id, class, class_codespace, function, function_codespace, usage, usage_codespace, tunnel_hollow_space_id, " +
				"lod4_brep_id, lod4_other_geom, " +
				"lod4_implicit_rep_id, lod4_implicit_ref_point, lod4_implicit_transformation" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psTunnelFurniture = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(TunnelFurniture tunnelFurniture) throws CityGMLImportException, SQLException {
		return doImport(tunnelFurniture, 0);
	}

	protected long doImport(TunnelFurniture tunnelFurniture, long roomId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(tunnelFurniture);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long tunnelFurnitureId = cityObjectImporter.doImport(tunnelFurniture, featureType);

		// import tunnel furniture information
		// primary id
		psTunnelFurniture.setLong(1, tunnelFurnitureId);

		// tun:class
		if (tunnelFurniture.isSetClazz() && tunnelFurniture.getClazz().isSetValue()) {
			psTunnelFurniture.setString(2, tunnelFurniture.getClazz().getValue());
			psTunnelFurniture.setString(3, tunnelFurniture.getClazz().getCodeSpace());
		} else {
			psTunnelFurniture.setNull(2, Types.VARCHAR);
			psTunnelFurniture.setNull(3, Types.VARCHAR);
		}

		// tun:function
		if (tunnelFurniture.isSetFunction()) {
			valueJoiner.join(tunnelFurniture.getFunction(), Code::getValue, Code::getCodeSpace);
			psTunnelFurniture.setString(4, valueJoiner.result(0));
			psTunnelFurniture.setString(5, valueJoiner.result(1));
		} else {
			psTunnelFurniture.setNull(4, Types.VARCHAR);
			psTunnelFurniture.setNull(5, Types.VARCHAR);
		}

		// tun:usage
		if (tunnelFurniture.isSetUsage()) {
			valueJoiner.join(tunnelFurniture.getUsage(), Code::getValue, Code::getCodeSpace);
			psTunnelFurniture.setString(6, valueJoiner.result(0));
			psTunnelFurniture.setString(7, valueJoiner.result(1));
		} else {
			psTunnelFurniture.setNull(6, Types.VARCHAR);
			psTunnelFurniture.setNull(7, Types.VARCHAR);
		}

		// parent room id
		if (roomId != 0)
			psTunnelFurniture.setLong(8, roomId);
		else
			psTunnelFurniture.setNull(8, Types.NULL);

		// tun:lod4Geometry
		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (tunnelFurniture.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = tunnelFurniture.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (importer.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.doImport(abstractGeometry, tunnelFurnitureId);
				else if (importer.isPointOrLineGeometry(abstractGeometry))
					geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
				else 
					importer.logOrThrowUnsupportedGeometryMessage(tunnelFurniture, abstractGeometry);

				geometryProperty.unsetGeometry();
			} else {
				String href = geometryProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.TUNNEL_FURNITURE.getName(),
							tunnelFurnitureId, 
							href, 
							"lod4_brep_id"));
				}
			}
		}

		if (geometryId != 0)
			psTunnelFurniture.setLong(9, geometryId);
		else
			psTunnelFurniture.setNull(9, Types.NULL);

		if (geometryObject != null)
			psTunnelFurniture.setObject(10, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psTunnelFurniture.setNull(10, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// tun:lod4ImplicitRepresentation
		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (tunnelFurniture.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = tunnelFurniture.getLod4ImplicitRepresentation();

			if (implicit.isSetObject()) {
				ImplicitGeometry geometry = implicit.getObject();

				// reference Point
				if (geometry.isSetReferencePoint())
					pointGeom = geometryImporter.getPoint(geometry.getReferencePoint());

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
			psTunnelFurniture.setLong(11, implicitId);
		else
			psTunnelFurniture.setNull(11, Types.NULL);

		if (pointGeom != null)
			psTunnelFurniture.setObject(12, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psTunnelFurniture.setNull(12, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		if (matrixString != null)
			psTunnelFurniture.setString(13, matrixString);
		else
			psTunnelFurniture.setNull(13, Types.VARCHAR);

		// objectclass id
		if (hasObjectClassIdColumn)
			psTunnelFurniture.setLong(14, featureType.getObjectClassId());

		psTunnelFurniture.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TUNNEL_FURNITURE);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(tunnelFurniture, tunnelFurnitureId, featureType);

		return tunnelFurnitureId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psTunnelFurniture.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psTunnelFurniture.close();
	}

}
