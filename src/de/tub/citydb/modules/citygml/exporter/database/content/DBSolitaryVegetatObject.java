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
import java.util.Arrays;
import java.util.regex.Pattern;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.citygml.core.ImplicitRepresentationPropertyImpl;
import org.citygml4j.impl.citygml.vegetation.SolitaryVegetationObjectImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.GeometryPropertyImpl;
import org.citygml4j.impl.gml.measures.LengthImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.config.Config;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBSolitaryVegetatObject implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psSolVegObject;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private FeatureClassFilter featureClassFilter;

	private boolean transformCoords;

	public DBSolitaryVegetatObject(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {
			psSolVegObject = connection.prepareStatement("select * from SOLITARY_VEGETAT_OBJECT where ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psSolVegObject = connection.prepareStatement("select NAME, NAME_CODESPACE, DESCRIPTION, CLASS, SPECIES, FUNCTION, HEIGHT, TRUNC_DIAMETER, CROWN_DIAMETER," +
					"LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, " +
					"LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID," +
					"geodb_util.transform_or_null(LOD1_IMPLICIT_REF_POINT, " + srid + ") AS LOD1_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD2_IMPLICIT_REF_POINT, " + srid + ") AS LOD2_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD3_IMPLICIT_REF_POINT, " + srid + ") AS LOD3_IMPLICIT_REF_POINT, " +
					"geodb_util.transform_or_null(LOD4_IMPLICIT_REF_POINT, " + srid + ") AS LOD4_IMPLICIT_REF_POINT, " +
			"LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION from SOLITARY_VEGETAT_OBJECT where ID = ?");					
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		SolitaryVegetationObject solVegObject = new SolitaryVegetationObjectImpl();
		long solVegObjectId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(solVegObject, solVegObjectId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psSolVegObject.setLong(1, solVegObjectId);
			rs = psSolVegObject.executeQuery();

			if (rs.next()) {
				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(solVegObject, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					solVegObject.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					solVegObject.setClazz(clazz);
				}

				String species = rs.getString("SPECIES");
				if (species != null) {
					solVegObject.setSpecies(species);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					solVegObject.setFunction(Arrays.asList(functionList));
				}

				double height = rs.getDouble("HEIGHT");
				if (!rs.wasNull()) {
					Length length = new LengthImpl();
					length.setValue(height);
					length.setUom("urn:ogc:def:uom:UCUM::m");
					solVegObject.setHeight(length);
				}

				double truncDiameter = rs.getDouble("TRUNC_DIAMETER");
				if (!rs.wasNull()) {
					Length length = new LengthImpl();
					length.setValue(truncDiameter);
					length.setUom("urn:ogc:def:uom:UCUM::m");
					solVegObject.setTrunkDiameter(length);
				}

				double crownDiameter = rs.getDouble("CROWN_DIAMETER");
				if (!rs.wasNull()) {
					Length length = new LengthImpl();
					length.setValue(crownDiameter);
					length.setUom("urn:ogc:def:uom:UCUM::m");
					solVegObject.setCrownDiameter(length);
				}

				for (int lod = 1; lod < 5 ; lod++) {
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
							case 1:
								solVegObject.setLod1Geometry(geometryProperty);
								break;
							case 2:
								solVegObject.setLod2Geometry(geometryProperty);
								break;
							case 3:
								solVegObject.setLod3Geometry(geometryProperty);
								break;
							case 4:
								solVegObject.setLod4Geometry(geometryProperty);
								break;
							}
						}
					}
				}

				for (int lod = 1; lod < 5 ; lod++) {
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
						case 1:
							solVegObject.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							solVegObject.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							solVegObject.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							solVegObject.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}
			}

			if (solVegObject.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putGmlId(solVegObject.getId(), solVegObjectId, solVegObject.getCityGMLClass());
			dbExporterManager.print(solVegObject);
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
