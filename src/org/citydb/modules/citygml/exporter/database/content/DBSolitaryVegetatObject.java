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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

public class DBSolitaryVegetatObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psSolVegObject;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBOtherGeometry geometryExporter;

	private ProjectionPropertyFilter projectionFilter;

	public DBSolitaryVegetatObject(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.SOLITARY_VEGETATION_OBJECT);

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
			.append("SPECIES, SPECIES_CODESPACE, HEIGHT, HEIGHT_UNIT, TRUNK_DIAMETER, TRUNK_DIAMETER_UNIT, CROWN_DIAMETER, CROWN_DIAMETER_UNIT, ")
			.append("LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
			.append("LOD1_OTHER_GEOM, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
			.append("LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
			.append("LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from SOLITARY_VEGETAT_OBJECT where ID = ?");
			psSolVegObject = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE,")
			.append("SPECIES, SPECIES_CODESPACE, HEIGHT, HEIGHT_UNIT, TRUNK_DIAMETER, TRUNK_DIAMETER_UNIT, CROWN_DIAMETER, CROWN_DIAMETER_UNIT, ")
			.append("LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
			.append(transformOrNull).append("(LOD1_OTHER_GEOM, ").append(srid).append(") AS LOD1_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD2_OTHER_GEOM, ").append(srid).append(") AS LOD2_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD3_OTHER_GEOM, ").append(srid).append(") AS LOD3_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD4_OTHER_GEOM, ").append(srid).append(") AS LOD4_OTHER_GEOM, ")
			.append("LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID,")
			.append(transformOrNull).append("(LOD1_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD1_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD2_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD2_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD3_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD3_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD4_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION from SOLITARY_VEGETAT_OBJECT where ID = ?");
			psSolVegObject = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		SolitaryVegetationObject solVegObject = new SolitaryVegetationObject();
		long solVegObjectId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(solVegObject, solVegObjectId, true, projectionFilter);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psSolVegObject.setLong(1, solVegObjectId);
			rs = psSolVegObject.executeQuery();

			if (rs.next()) {
				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "class")) {
					String clazz = rs.getString(1);
					if (clazz != null) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString(2));
						solVegObject.setClazz(code);
					}
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "function")) {
					String function = rs.getString(3);
					String functionCodeSpace = rs.getString(4);
					if (function != null)
						solVegObject.setFunction(Util.string2codeList(function, functionCodeSpace));
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "usage")) {
					String usage = rs.getString(5);
					String usageCodeSpace = rs.getString(6);
					if (usage != null)
						solVegObject.setUsage(Util.string2codeList(usage, usageCodeSpace));
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "species")) {
					String species = rs.getString(7);
					if (species != null) {
						Code code = new Code(species);
						code.setCodeSpace(rs.getString(8));
						solVegObject.setSpecies(code);
					}
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "height")) {
					double height = rs.getDouble(9);
					if (!rs.wasNull()) {
						Length length = new Length();
						length.setValue(height);
						length.setUom(rs.getString(10));
						solVegObject.setHeight(length);
					}
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "trunkDiameter")) {
					double trunkDiameter = rs.getDouble(11);
					if (!rs.wasNull()) {
						Length length = new Length();
						length.setValue(trunkDiameter);
						length.setUom(rs.getString(12));
						solVegObject.setTrunkDiameter(length);
					}
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "crownDiameter")) {
					double crownDiameter = rs.getDouble(13);
					if (!rs.wasNull()) {
						Length length = new Length();
						length.setValue(crownDiameter);
						length.setUom(rs.getString(14));
						solVegObject.setCrownDiameter(length);
					}
				}

				// geometry
				for (int lod = 0; lod < 4; lod++) {
					if (projectionFilter.filter(CityGMLModuleType.VEGETATION, new StringBuilder("lod").append(lod + 1).append("Geometry").toString()))
						continue;
					
					long surfaceGeometryId = rs.getLong(15 + lod);
					Object geomObj = rs.getObject(19 + lod);
					if (surfaceGeometryId == 0 && geomObj == null)
						continue;

					GeometryProperty<AbstractGeometry> geometryProperty = null;

					if (surfaceGeometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());
						}
					} else {
						GeometryObject geometry = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(geomObj);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							geometryProperty.setGeometry(geometryExporter.getPointOrCurveGeometry(geometry, true));
						}	
					}

					if (geometryProperty != null) {
						switch (lod) {
						case 0:
							solVegObject.setLod1Geometry(geometryProperty);
							break;
						case 1:
							solVegObject.setLod2Geometry(geometryProperty);
							break;
						case 2:
							solVegObject.setLod3Geometry(geometryProperty);
							break;
						case 3:
							solVegObject.setLod4Geometry(geometryProperty);
							break;
						}
					}
				}

				// implicit geometry
				for (int lod = 0; lod < 4; lod++) {
					if (projectionFilter.filter(CityGMLModuleType.VEGETATION, new StringBuilder("lod").append(lod + 1).append("ImplicitRepresentation").toString()))
						continue;
					
					long implicitGeometryId = rs.getLong(23 + lod);
					if (rs.wasNull())
						continue;

					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(27 + lod);
					if (!rs.wasNull() && referencePointObj != null)
						referencePoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(31 + lod);

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 0:
							solVegObject.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 1:
							solVegObject.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							solVegObject.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							solVegObject.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}
			}

			dbExporterManager.processFeature(solVegObject);

			if (solVegObject.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(solVegObject.getId(), solVegObjectId, solVegObject.getCityGMLClass());

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psSolVegObject.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.SOLITARY_VEGETAT_OBJECT;
	}

}
