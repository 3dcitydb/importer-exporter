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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.citygml.core.ImplicitRepresentationPropertyImpl;
import org.citygml4j.impl.citygml.generics.GenericCityObjectImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.GeometryPropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.config.Config;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBGenericCityObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psGenericCityObject;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBSdoGeometry sdoGeometry;
	private FeatureClassFilter featureClassFilter;

	private boolean transformCoords;

	public DBGenericCityObject(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {
			psGenericCityObject = connection.prepareStatement("select * from GENERIC_CITYOBJECT where ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psGenericCityObject = connection.prepareStatement("select NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, " +
					"geodb_util.transform_or_null(LOD0_TERRAIN_INTERSECTION, " + srid + ") AS LOD0_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(LOD1_TERRAIN_INTERSECTION, " + srid + ") AS LOD1_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(LOD2_TERRAIN_INTERSECTION, " + srid + ") AS LOD2_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(LOD3_TERRAIN_INTERSECTION, " + srid + ") AS LOD3_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(LOD4_TERRAIN_INTERSECTION, " + srid + ") AS LOD4_TERRAIN_INTERSECTION, " +
					"LOD0_GEOMETRY_ID, LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, " +
					"LOD0_IMPLICIT_REP_ID, LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID," +
					"geodb_util.transform_or_null(LOD0_IMPLICIT_REF_POINT, " + srid + ") AS LOD0_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD1_IMPLICIT_REF_POINT, " + srid + ") AS LOD1_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD2_IMPLICIT_REF_POINT, " + srid + ") AS LOD2_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD3_IMPLICIT_REF_POINT, " + srid + ") AS LOD3_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD4_IMPLICIT_REF_POINT, " + srid + ") AS LOD4_IMPLICIT_REF_POINT, " +
			"LOD0_IMPLICIT_TRANSFORMATION, LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION from GENERIC_CITYOBJECT where ID = ?");					
		}
		
		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		GenericCityObject genericCityObject = new GenericCityObjectImpl();
		long genericCityObjectId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(genericCityObject, genericCityObjectId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psGenericCityObject.setLong(1, genericCityObjectId);
			rs = psGenericCityObject.executeQuery();

			if (rs.next()) {
				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(genericCityObject, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					genericCityObject.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					genericCityObject.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					genericCityObject.setFunction(Arrays.asList(functionList));
				}

				String usage = rs.getString("USAGE");
				if (usage != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] usageList = p.split(usage.trim());
					genericCityObject.setUsage(Arrays.asList(usageList));
				}

				for (int lod = 0; lod < 5 ; lod++) {
					long geometryId = rs.getLong("LOD" + lod + "_GEOMETRY_ID");

					if (!rs.wasNull() && geometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

						if (geometry != null) {
							GeometryProperty<AbstractGeometry> geometryProperty = new GeometryPropertyImpl<AbstractGeometry>();

							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 0:
								genericCityObject.setLod0Geometry(geometryProperty);
								break;
							case 1:
								genericCityObject.setLod1Geometry(geometryProperty);
								break;
							case 2:
								genericCityObject.setLod2Geometry(geometryProperty);
								break;
							case 3:
								genericCityObject.setLod3Geometry(geometryProperty);
								break;
							case 4:
								genericCityObject.setLod4Geometry(geometryProperty);
								break;
							}
						}
					}
				}

				for (int lod = 0; lod < 5 ; lod++) {
					// get implicit geometry details
					long implicitGeometryId = rs.getLong("LOD" + lod + "_IMPLICIT_REP_ID");
					if (rs.wasNull())
						continue;

					JGeometry referencePoint = null;
					STRUCT struct = (STRUCT)rs.getObject("LOD" + lod +"_IMPLICIT_REF_POINT");
					if (!rs.wasNull() && struct != null)
						referencePoint = JGeometry.load(struct);

					String transformationMatrix = rs.getString("LOD" + lod + "_IMPLICIT_TRANSFORMATION");

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationPropertyImpl();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 0:
							genericCityObject.setLod0ImplicitRepresentation(implicitProperty);
							break;
						case 1:
							genericCityObject.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							genericCityObject.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							genericCityObject.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							genericCityObject.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}

				// lodXTerrainIntersection
				for (int lod = 0; lod < 5; lod++) {
					JGeometry terrainIntersection = null;
					STRUCT terrainIntersectionObj = (STRUCT)rs.getObject("LOD" + lod + "_TERRAIN_INTERSECTION");

					if (!rs.wasNull() && terrainIntersectionObj != null) {
						terrainIntersection = JGeometry.load(terrainIntersectionObj);

						if (terrainIntersection != null) {
							MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(terrainIntersection, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									genericCityObject.setLod0TerrainIntersection(multiCurveProperty);
									break;
								case 1:
									genericCityObject.setLod1TerrainIntersection(multiCurveProperty);
									break;
								case 2:
									genericCityObject.setLod2TerrainIntersection(multiCurveProperty);
									break;
								case 3:
									genericCityObject.setLod3TerrainIntersection(multiCurveProperty);
									break;
								case 4:
									genericCityObject.setLod4TerrainIntersection(multiCurveProperty);
									break;
								}
							}
						}
					}			
				}
			}

			if (genericCityObject.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putGmlId(genericCityObject.getId(), genericCityObjectId, genericCityObject.getCityGMLClass());
			dbExporterManager.print(genericCityObject);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psGenericCityObject.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.GENERIC_CITYOBJECT;
	}

}
