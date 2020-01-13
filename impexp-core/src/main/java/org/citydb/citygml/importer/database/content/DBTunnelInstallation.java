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
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBTunnelInstallation implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psTunnelInstallation;
	private DBCityObject cityObjectImporter;
	private DBTunnelThematicSurface thematicSurfaceImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean affineTransformation;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBTunnelInstallation(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".tunnel_installation (id, objectclass_id, class, class_codespace, function, function_codespace, usage, usage_codespace, tunnel_id, tunnel_hollow_space_id, " +
				"lod2_brep_id, lod3_brep_id, lod4_brep_id, lod2_other_geom, lod3_other_geom, lod4_other_geom, " +
				"lod2_implicit_rep_id, lod3_implicit_rep_id, lod4_implicit_rep_id, " +
				"lod2_implicit_ref_point, lod3_implicit_ref_point, lod4_implicit_ref_point, " +
				"lod2_implicit_transformation, lod3_implicit_transformation, lod4_implicit_transformation) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		psTunnelInstallation = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBTunnelThematicSurface.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(TunnelInstallation tunnelInstallation) throws CityGMLImportException, SQLException {
		return doImport(tunnelInstallation, null, 0);
	}

	protected long doImport(IntTunnelInstallation intTunnelInstallation) throws CityGMLImportException, SQLException {
		return doImport(intTunnelInstallation, null, 0);
	}

	public long doImport(TunnelInstallation tunnelInstallation, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(tunnelInstallation);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long tunnelInstallationId = cityObjectImporter.doImport(tunnelInstallation, featureType);

		// import tunnel installation information
		// primary id
		psTunnelInstallation.setLong(1, tunnelInstallationId);

		// objectclass id
		psTunnelInstallation.setLong(2, featureType.getObjectClassId());

		// tun:class
		if (tunnelInstallation.isSetClazz() && tunnelInstallation.getClazz().isSetValue()) {
			psTunnelInstallation.setString(3, tunnelInstallation.getClazz().getValue());
			psTunnelInstallation.setString(4, tunnelInstallation.getClazz().getCodeSpace());
		} else {
			psTunnelInstallation.setNull(3, Types.VARCHAR);
			psTunnelInstallation.setNull(4, Types.VARCHAR);
		}

		// tun:function
		if (tunnelInstallation.isSetFunction()) {
			valueJoiner.join(tunnelInstallation.getFunction(), Code::getValue, Code::getCodeSpace);
			psTunnelInstallation.setString(5, valueJoiner.result(0));
			psTunnelInstallation.setString(6, valueJoiner.result(1));
		} else {
			psTunnelInstallation.setNull(5, Types.VARCHAR);
			psTunnelInstallation.setNull(6, Types.VARCHAR);
		}

		// tun:usage
		if (tunnelInstallation.isSetUsage()) {
			valueJoiner.join(tunnelInstallation.getUsage(), Code::getValue, Code::getCodeSpace);
			psTunnelInstallation.setString(7, valueJoiner.result(0));
			psTunnelInstallation.setString(8, valueJoiner.result(1));
		} else {
			psTunnelInstallation.setNull(7, Types.VARCHAR);
			psTunnelInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		if (parent instanceof AbstractTunnel) {
			psTunnelInstallation.setLong(9, parentId);
			psTunnelInstallation.setNull(10, Types.NULL);
		} else if (parent instanceof HollowSpace) {
			psTunnelInstallation.setNull(9, Types.NULL);
			psTunnelInstallation.setLong(10, parentId);
		} else {
			psTunnelInstallation.setNull(9, Types.NULL);
			psTunnelInstallation.setNull(10, Types.NULL);
		}

		// tun:lodXGeometry
		for (int i = 0; i < 3; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = tunnelInstallation.getLod2Geometry();
				break;
			case 1:
				geometryProperty = tunnelInstallation.getLod3Geometry();
				break;
			case 2:
				geometryProperty = tunnelInstallation.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (importer.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.doImport(abstractGeometry, tunnelInstallationId);
					else if (importer.isPointOrLineGeometry(abstractGeometry))
						geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
					else 
						importer.logOrThrowUnsupportedGeometryMessage(tunnelInstallation, abstractGeometry);

					geometryProperty.unsetGeometry();
				} else {
					String href = geometryProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.TUNNEL_INSTALLATION.getName(),
								tunnelInstallationId, 
								href, 
								"lod" + (i + 2) + "_brep_id"));
					}
				}
			}

			if (geometryId != 0)
				psTunnelInstallation.setLong(11 + i, geometryId);
			else
				psTunnelInstallation.setNull(11 + i, Types.NULL);

			if (geometryObject != null)
				psTunnelInstallation.setObject(14 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psTunnelInstallation.setNull(14 + i, nullGeometryType, nullGeometryTypeName);
		}

		// tun:lodXImplicitRepresentation
		for (int i = 0; i < 3; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = tunnelInstallation.getLod2ImplicitRepresentation();
				break;
			case 1:
				implicit = tunnelInstallation.getLod3ImplicitRepresentation();
				break;
			case 2:
				implicit = tunnelInstallation.getLod4ImplicitRepresentation();
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
				psTunnelInstallation.setLong(17 + i, implicitId);
			else
				psTunnelInstallation.setNull(17 + i, Types.NULL);

			if (pointGeom != null)
				psTunnelInstallation.setObject(20 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psTunnelInstallation.setNull(20 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psTunnelInstallation.setString(23 + i, matrixString);
			else
				psTunnelInstallation.setNull(23 + i, Types.VARCHAR);
		}

		psTunnelInstallation.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TUNNEL_INSTALLATION);

		// tun:boundedBy
		if (tunnelInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : tunnelInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, tunnelInstallation, tunnelInstallationId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.TUNNEL_THEMATIC_SURFACE.getName(),
								href,
								tunnelInstallationId,
								"tunnel_installation_id"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(tunnelInstallation, tunnelInstallationId, featureType);

		return tunnelInstallationId;
	}

	public long doImport(IntTunnelInstallation intTunnelInstallation, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(intTunnelInstallation);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long intTunnelInstallationId = cityObjectImporter.doImport(intTunnelInstallation, featureType);

		// import interior bridge installation information
		// primary id
		psTunnelInstallation.setLong(1, intTunnelInstallationId);

		// OBJECTCLASS_ID
		psTunnelInstallation.setLong(2, featureType.getObjectClassId());

		// tun:class
		if (intTunnelInstallation.isSetClazz() && intTunnelInstallation.getClazz().isSetValue()) {
			psTunnelInstallation.setString(3, intTunnelInstallation.getClazz().getValue());
			psTunnelInstallation.setString(4, intTunnelInstallation.getClazz().getCodeSpace());
		} else {
			psTunnelInstallation.setNull(3, Types.VARCHAR);
			psTunnelInstallation.setNull(4, Types.VARCHAR);
		}

		// tun:function
		if (intTunnelInstallation.isSetFunction()) {
			valueJoiner.join(intTunnelInstallation.getFunction(), Code::getValue, Code::getCodeSpace);
			psTunnelInstallation.setString(5, valueJoiner.result(0));
			psTunnelInstallation.setString(6, valueJoiner.result(1));
		} else {
			psTunnelInstallation.setNull(5, Types.VARCHAR);
			psTunnelInstallation.setNull(6, Types.VARCHAR);
		}

		// tun:usage
		if (intTunnelInstallation.isSetUsage()) {
			valueJoiner.join(intTunnelInstallation.getUsage(), Code::getValue, Code::getCodeSpace);
			psTunnelInstallation.setString(7, valueJoiner.result(0));
			psTunnelInstallation.setString(8, valueJoiner.result(1));
		} else {
			psTunnelInstallation.setNull(7, Types.VARCHAR);
			psTunnelInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		if (parent instanceof AbstractTunnel) {
			psTunnelInstallation.setLong(9, parentId);
			psTunnelInstallation.setNull(10, Types.NULL);
		} else if (parent instanceof HollowSpace) {
			psTunnelInstallation.setNull(9, Types.NULL);
			psTunnelInstallation.setLong(10, parentId);
		} else {
			psTunnelInstallation.setNull(9, Types.NULL);
			psTunnelInstallation.setNull(10, Types.NULL);
		}	

		// tun:lod4Geometry
		psTunnelInstallation.setNull(11, Types.NULL);
		psTunnelInstallation.setNull(12, Types.NULL);
		psTunnelInstallation.setNull(14, nullGeometryType, nullGeometryTypeName);
		psTunnelInstallation.setNull(15, nullGeometryType, nullGeometryTypeName);

		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (intTunnelInstallation.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = intTunnelInstallation.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (importer.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.doImport(abstractGeometry, intTunnelInstallationId);
				else if (importer.isPointOrLineGeometry(abstractGeometry))
					geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
				else 
					importer.logOrThrowUnsupportedGeometryMessage(intTunnelInstallation, abstractGeometry);

				geometryProperty.unsetGeometry();
			} else {
				String href = geometryProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.TUNNEL_INSTALLATION.getName(),
							intTunnelInstallationId, 
							href, 
							"lod4_brep_id"));
				}
			}
		}

		if (geometryId != 0)
			psTunnelInstallation.setLong(13, geometryId);
		else
			psTunnelInstallation.setNull(13, Types.NULL);

		if (geometryObject != null)
			psTunnelInstallation.setObject(16, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psTunnelInstallation.setNull(16, nullGeometryType, nullGeometryTypeName);

		// tun:lod4ImplicitRepresentation
		psTunnelInstallation.setNull(17, Types.NULL);
		psTunnelInstallation.setNull(18, Types.NULL);
		psTunnelInstallation.setNull(20, nullGeometryType, nullGeometryTypeName);
		psTunnelInstallation.setNull(21, nullGeometryType, nullGeometryTypeName);
		psTunnelInstallation.setNull(23, Types.VARCHAR);
		psTunnelInstallation.setNull(24, Types.VARCHAR);

		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (intTunnelInstallation.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = intTunnelInstallation.getLod4ImplicitRepresentation();

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
			psTunnelInstallation.setLong(19, implicitId);
		else
			psTunnelInstallation.setNull(19, Types.NULL);

		if (pointGeom != null)
			psTunnelInstallation.setObject(22, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psTunnelInstallation.setNull(22, nullGeometryType, nullGeometryTypeName);

		if (matrixString != null)
			psTunnelInstallation.setString(25, matrixString);
		else
			psTunnelInstallation.setNull(25, Types.VARCHAR);

		psTunnelInstallation.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TUNNEL_INSTALLATION);

		// tun:boundedBy
		if (intTunnelInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : intTunnelInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, intTunnelInstallation, intTunnelInstallationId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0)
						importer.logOrThrowUnsupportedXLinkMessage(intTunnelInstallation, AbstractBoundarySurface.class, href);
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(intTunnelInstallation, intTunnelInstallationId, featureType);

		return intTunnelInstallationId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psTunnelInstallation.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psTunnelInstallation.close();
	}

}
