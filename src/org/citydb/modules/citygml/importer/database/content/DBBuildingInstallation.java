/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBBuildingInstallation implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBuildingInstallation;
	private DBCityObject cityObjectImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;

	private boolean affineTransformation;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBBuildingInstallation(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into BUILDING_INSTALLATION (ID, OBJECTCLASS_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, BUILDING_ID, ROOM_ID, ")
		.append("LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
		.append("LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
		.append("LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
		.append("LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psBuildingInstallation = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.THEMATIC_SURFACE);
	}

	public long insert(BuildingInstallation buildingInstallation, CityGMLClass parent, long parentId) throws SQLException {
		long buildingInstallationId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (buildingInstallationId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(buildingInstallation, buildingInstallationId);

		// BuildingInstallation
		// ID
		psBuildingInstallation.setLong(1, buildingInstallationId);

		// OBJECTCLASS_ID
		psBuildingInstallation.setLong(2, Util.cityObject2classId(buildingInstallation.getCityGMLClass()));

		// bldg:class
		if (buildingInstallation.isSetClazz() && buildingInstallation.getClazz().isSetValue()) {
			psBuildingInstallation.setString(3, buildingInstallation.getClazz().getValue());
			psBuildingInstallation.setString(4, buildingInstallation.getClazz().getCodeSpace());
		} else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// bldg:function
		if (buildingInstallation.isSetFunction()) {
			String[] function = Util.codeList2string(buildingInstallation.getFunction());
			psBuildingInstallation.setString(5, function[0]);
			psBuildingInstallation.setString(6, function[1]);
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
			psBuildingInstallation.setNull(6, Types.VARCHAR);
		}

		// bldg:usage
		if (buildingInstallation.isSetUsage()) {
			String[] usage = Util.codeList2string(buildingInstallation.getUsage());
			psBuildingInstallation.setString(7, usage[0]);
			psBuildingInstallation.setString(8, usage[1]);
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
			psBuildingInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		switch (parent) {
		case BUILDING:
		case BUILDING_PART:
			psBuildingInstallation.setLong(9, parentId);
			psBuildingInstallation.setNull(10, Types.NULL);
			break;
		case BUILDING_ROOM:
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setLong(10, parentId);
			break;
		default:
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setNull(10, Types.NULL);
		}

		// Geometry
		for (int i = 0; i < 3; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = buildingInstallation.getLod2Geometry();
				break;
			case 1:
				geometryProperty = buildingInstallation.getLod3Geometry();
				break;
			case 2:
				geometryProperty = buildingInstallation.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.insert(abstractGeometry, buildingInstallationId);
					else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
						geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								buildingInstallation.getCityGMLClass(), 
								buildingInstallation.getId()));
						msg.append(": Unsupported geometry type ");
						msg.append(abstractGeometry.getGMLClass()).append('.');

						LOG.error(msg.toString());
					}

					geometryProperty.unsetGeometry();
				} else {
					// xlink
					String href = geometryProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								buildingInstallationId, 
								TableEnum.BUILDING_INSTALLATION, 
								"LOD" + (i + 2) + "_BREP_ID"));
					}
				}
			}

			if (geometryId != 0)
				psBuildingInstallation.setLong(11 + i, geometryId);
			else
				psBuildingInstallation.setNull(11 + i, Types.NULL);

			if (geometryObject != null)
				psBuildingInstallation.setObject(14 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psBuildingInstallation.setNull(14 + i, nullGeometryType, nullGeometryTypeName);
		}

		// implicit geometry
		for (int i = 0; i < 3; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = buildingInstallation.getLod2ImplicitRepresentation();
				break;
			case 1:
				implicit = buildingInstallation.getLod3ImplicitRepresentation();
				break;
			case 2:
				implicit = buildingInstallation.getLod4ImplicitRepresentation();
				break;
			}

			if (implicit != null) {
				if (implicit.isSetObject()) {
					ImplicitGeometry geometry = implicit.getObject();

					// reference Point
					if (geometry.isSetReferencePoint())
						pointGeom = otherGeometryImporter.getPoint(geometry.getReferencePoint());

					// transformation matrix
					if (geometry.isSetTransformationMatrix()) {
						Matrix matrix = geometry.getTransformationMatrix().getMatrix();
						if (affineTransformation)
							matrix = dbImporterManager.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);

						matrixString = Util.collection2string(matrix.toRowPackedList(), " ");
					}

					// reference to IMPLICIT_GEOMETRY
					implicitId = implicitGeometryImporter.insert(geometry, buildingInstallationId);
				}
			}

			if (implicitId != 0)
				psBuildingInstallation.setLong(17 + i, implicitId);
			else
				psBuildingInstallation.setNull(17 + i, Types.NULL);

			if (pointGeom != null)
				psBuildingInstallation.setObject(20 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psBuildingInstallation.setNull(20 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psBuildingInstallation.setString(23 + i, matrixString);
			else
				psBuildingInstallation.setNull(23 + i, Types.VARCHAR);
		}

		psBuildingInstallation.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BUILDING_INSTALLATION);

		// BoundarySurfaces
		if (buildingInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : buildingInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, buildingInstallation.getCityGMLClass(), buildingInstallationId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								buildingInstallation.getCityGMLClass(), 
								buildingInstallation.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					boundarySurfaceProperty.unsetBoundarySurface();
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_BUILDING_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(buildingInstallation, buildingInstallationId);

		return buildingInstallationId;
	}

	public long insert(IntBuildingInstallation intBuildingInstallation, CityGMLClass parent, long parentId) throws SQLException {
		long intBuildingInstallationId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (intBuildingInstallationId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(intBuildingInstallation, intBuildingInstallationId);

		// IntBuildingInstallation
		// ID
		psBuildingInstallation.setLong(1, intBuildingInstallationId);

		// OBJECTCLASS_ID
		psBuildingInstallation.setLong(2, Util.cityObject2classId(intBuildingInstallation.getCityGMLClass()));

		// bldg:class
		if (intBuildingInstallation.isSetClazz() && intBuildingInstallation.getClazz().isSetValue()) {
			psBuildingInstallation.setString(3, intBuildingInstallation.getClazz().getValue());
			psBuildingInstallation.setString(4, intBuildingInstallation.getClazz().getCodeSpace());
		} else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// bldg:function
		if (intBuildingInstallation.isSetFunction()) {
			String[] function = Util.codeList2string(intBuildingInstallation.getFunction());
			psBuildingInstallation.setString(5, function[0]);
			psBuildingInstallation.setString(6, function[1]);
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
			psBuildingInstallation.setNull(6, Types.VARCHAR);
		}

		// bldg:usage
		if (intBuildingInstallation.isSetUsage()) {
			String[] usage = Util.codeList2string(intBuildingInstallation.getUsage());
			psBuildingInstallation.setString(7, usage[0]);
			psBuildingInstallation.setString(8, usage[1]);
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
			psBuildingInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		switch (parent) {
		case BUILDING:
		case BUILDING_PART:
			psBuildingInstallation.setLong(9, parentId);
			psBuildingInstallation.setNull(10, Types.NULL);
			break;
		case BUILDING_ROOM:
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setLong(10, parentId);
			break;
		default:
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setNull(10, Types.NULL);
		}	

		// Geometry
		psBuildingInstallation.setNull(11, Types.NULL);
		psBuildingInstallation.setNull(12, Types.NULL);
		psBuildingInstallation.setNull(14, nullGeometryType, nullGeometryTypeName);
		psBuildingInstallation.setNull(15, nullGeometryType, nullGeometryTypeName);

		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (intBuildingInstallation.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = intBuildingInstallation.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.insert(abstractGeometry, intBuildingInstallationId);
				else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
					geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
				else {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							intBuildingInstallation.getCityGMLClass(), 
							intBuildingInstallation.getId()));
					msg.append(": Unsupported geometry type ");
					msg.append(abstractGeometry.getGMLClass()).append('.');

					LOG.error(msg.toString());
				}

				geometryProperty.unsetGeometry();
			} else {
				// xlink
				String href = geometryProperty.getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
							href, 
							intBuildingInstallationId, 
							TableEnum.BUILDING_INSTALLATION, 
							"LOD4_BREP_ID"));
				}
			}
		}

		if (geometryId != 0)
			psBuildingInstallation.setLong(13, geometryId);
		else
			psBuildingInstallation.setNull(13, Types.NULL);

		if (geometryObject != null)
			psBuildingInstallation.setObject(16, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psBuildingInstallation.setNull(16, nullGeometryType, nullGeometryTypeName);

		// implicit geometry
		psBuildingInstallation.setNull(17, Types.NULL);
		psBuildingInstallation.setNull(18, Types.NULL);
		psBuildingInstallation.setNull(20, nullGeometryType, nullGeometryTypeName);
		psBuildingInstallation.setNull(21, nullGeometryType, nullGeometryTypeName);
		psBuildingInstallation.setNull(23, Types.VARCHAR);
		psBuildingInstallation.setNull(24, Types.VARCHAR);

		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (intBuildingInstallation.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = intBuildingInstallation.getLod4ImplicitRepresentation();

			if (implicit.isSetObject()) {
				ImplicitGeometry geometry = implicit.getObject();

				// reference Point
				if (geometry.isSetReferencePoint())
					pointGeom = otherGeometryImporter.getPoint(geometry.getReferencePoint());

				// transformation matrix
				if (geometry.isSetTransformationMatrix()) {
					Matrix matrix = geometry.getTransformationMatrix().getMatrix();
					if (affineTransformation)
						matrix = dbImporterManager.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);

					matrixString = Util.collection2string(matrix.toRowPackedList(), " ");
				}

				// reference to IMPLICIT_GEOMETRY
				implicitId = implicitGeometryImporter.insert(geometry, intBuildingInstallationId);
			}
		}

		if (implicitId != 0)
			psBuildingInstallation.setLong(19, implicitId);
		else
			psBuildingInstallation.setNull(19, Types.NULL);

		if (pointGeom != null)
			psBuildingInstallation.setObject(22, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psBuildingInstallation.setNull(22, nullGeometryType, nullGeometryTypeName);

		if (matrixString != null)
			psBuildingInstallation.setString(25, matrixString);
		else
			psBuildingInstallation.setNull(25, Types.VARCHAR);

		psBuildingInstallation.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BUILDING_INSTALLATION);

		// BoundarySurfaces
		if (intBuildingInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : intBuildingInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, intBuildingInstallation.getCityGMLClass(), intBuildingInstallationId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								intBuildingInstallation.getCityGMLClass(), 
								intBuildingInstallation.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					boundarySurfaceProperty.unsetBoundarySurface();
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_BUILDING_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(intBuildingInstallation, intBuildingInstallationId);

		return intBuildingInstallationId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBuildingInstallation.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBuildingInstallation.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BUILDING_INSTALLATION;
	}

}
