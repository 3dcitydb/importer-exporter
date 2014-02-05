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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBGenericCityObject implements DBExporter {
	private final Logger LOG = Logger.getInstance();
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psGenericCityObject;
	private PreparedStatement psGenericGeometryAttribute;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBOtherGeometry geometryExporter;
	private FeatureClassFilter featureClassFilter;

	public DBGenericCityObject(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			psGenericCityObject = connection.prepareStatement("select * from GENERIC_CITYOBJECT where ID = ?");
			psGenericGeometryAttribute = connection.prepareStatement("select ATTRNAME, GEOMVAL from CITYOBJECT_GENERICATTRIB where CITYOBJECT_ID = ? and DATATYPE = 6");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

			StringBuilder objectQuery = new StringBuilder()
			.append("select NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, ")
			.append(transformOrNull).append("(LOD0_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD0_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD1_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD1_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD2_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD2_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD3_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD3_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD4_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD4_TERRAIN_INTERSECTION, ")
			.append("LOD0_GEOMETRY_ID, LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, ")
			.append("LOD0_IMPLICIT_REP_ID, LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID,")
			.append(transformOrNull).append("(LOD0_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD0_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD1_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD1_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD2_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD2_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD3_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD3_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD4_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD0_IMPLICIT_TRANSFORMATION, LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION from GENERIC_CITYOBJECT where ID = ?");					
			psGenericCityObject = connection.prepareStatement(objectQuery.toString());

			StringBuilder attributeQuery = new StringBuilder()
			.append("select ATTRNAME, ")
			.append(transformOrNull).append("(GEOMVAL, ").append(srid).append(") AS GEOMVAL from CITYOBJECT_GENERICATTRIB where CITYOBJECT_ID = ? and DATATYPE = 6");
			psGenericGeometryAttribute = connection.prepareStatement(attributeQuery.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		GenericCityObject genericCityObject = new GenericCityObject();
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

				Util.string2codeList(genericCityObject, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRef();
					stringOrRef.setValue(description);
					genericCityObject.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					genericCityObject.setClazz(new Code(clazz));
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					for (String value : p.split(function.trim()))
						genericCityObject.addFunction(new Code(value));
				}

				String usage = rs.getString("USAGE");
				if (usage != null) {
					Pattern p = Pattern.compile("\\s+");
					for (String value : p.split(usage.trim()))
						genericCityObject.addUsage(new Code(value));
				}

				for (int lod = 0; lod < 5 ; lod++) {
					long geometryId = rs.getLong("LOD" + lod + "_GEOMETRY_ID");

					if (!rs.wasNull() && geometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

						if (geometry != null) {
							GeometryProperty<AbstractGeometry> geometryProperty = new GeometryProperty<AbstractGeometry>();

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

					GeometryObject referencePoint = null;
					Object object = rs.getObject("LOD" + lod +"_IMPLICIT_REF_POINT");
					if (!rs.wasNull() && object != null)
						referencePoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePoint);

					String transformationMatrix = rs.getString("LOD" + lod + "_IMPLICIT_TRANSFORMATION");

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
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
					Object terrainIntersectionObj = rs.getObject("LOD" + lod + "_TERRAIN_INTERSECTION");

					if (!rs.wasNull() && terrainIntersectionObj != null) {
						GeometryObject terrainIntersection = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);

						if (terrainIntersection != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(terrainIntersection, false);
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

			rs.close();

			// read point or curve geometries from generic attributes
			psGenericGeometryAttribute.setLong(1, genericCityObjectId);
			rs = psGenericGeometryAttribute.executeQuery();

			while (rs.next()) {
				String attributeName = rs.getString("ATTRNAME");
				if (!rs.wasNull() && attributeName != null) {
					Pattern p = Pattern.compile("^LOD[0-4]_Geometry$", Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(attributeName);
					if (m.find()) {
						int lod = Integer.parseInt(attributeName.substring(3, 4));
						boolean hasGeometry = false;

						switch (lod) {
						case 0:
							hasGeometry = genericCityObject.isSetLod0Geometry();
							break;
						case 1:
							hasGeometry = genericCityObject.isSetLod1Geometry();
							break;
						case 2:
							hasGeometry = genericCityObject.isSetLod2Geometry();
							break;
						case 3:
							hasGeometry = genericCityObject.isSetLod3Geometry();
							break;
						case 4:
							hasGeometry = genericCityObject.isSetLod4Geometry();
							break;
						}

						if (!hasGeometry) {
							Object object = rs.getObject("GEOMVAL");
							if (!rs.wasNull() && object != null) {
								GeometryObject geom = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(object);
								GeometryProperty<? extends AbstractGeometry> property = geometryExporter.getPointOrCurveGeometryProperty(geom, false);
								if (property != null) {
									switch (lod) {
									case 0:
										genericCityObject.setLod0Geometry(property);
										break;
									case 1:
										genericCityObject.setLod1Geometry(property);
										break;
									case 2:
										genericCityObject.setLod2Geometry(property);
										break;
									case 3:
										genericCityObject.setLod3Geometry(property);
										break;
									case 4:
										genericCityObject.setLod4Geometry(property);
										break;
									}
								}
							}
						} else {
							StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
									genericCityObject.getCityGMLClass(), 
									genericCityObject.getId()));
							msg.append(": Found multiple geometries for LOD").append(lod).append('.');
							LOG.error(msg.toString());
						}

					} else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								genericCityObject.getCityGMLClass(), 
								genericCityObject.getId()));
						msg.append(": Failed to interpret generic geometry attribute '").append(attributeName).append("'.");
						LOG.error(msg.toString());
					}
				}
			}

			if (genericCityObject.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putUID(genericCityObject.getId(), genericCityObjectId, genericCityObject.getCityGMLClass());
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
		psGenericGeometryAttribute.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.GENERIC_CITYOBJECT;
	}

}
