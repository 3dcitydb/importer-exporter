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

import org.citygml4j.impl.citygml.vegetation.PlantCoverImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiSolidPropertyImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiSurfacePropertyImpl;
import org.citygml4j.impl.gml.measures.LengthImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBPlantCover implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Connection connection;

	private PreparedStatement psPlantCover;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private FeatureClassFilter featureClassFilter;


	public DBPlantCover(Connection connection, ExportFilter exportFilter, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		psPlantCover = connection.prepareStatement("select * from PLANT_COVER where ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		PlantCover plantCover = new PlantCoverImpl();
		long plantCoverId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(plantCover, plantCoverId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psPlantCover.setLong(1, plantCoverId);
			rs = psPlantCover.executeQuery();

			if (rs.next()) {
				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(plantCover, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					plantCover.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					plantCover.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					plantCover.setFunction(Arrays.asList(functionList));
				}

				double averageHeight = rs.getDouble("AVERAGE_HEIGHT");
				if (!rs.wasNull()) {
					Length length = new LengthImpl();
					length.setValue(averageHeight);
					length.setUom("urn:ogc:def:uom:UCUM::m");
					plantCover.setAverageHeight(length);
				}

				for (int lod = 1; lod < 5 ; lod++) {
					long geometryId = rs.getLong("LOD" + lod + "_GEOMETRY_ID");

					if (!rs.wasNull() && geometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

						if (geometry != null) {
							if (geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

								if (geometry.getAbstractGeometry() != null)
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 1:
									plantCover.setLod1MultiSurface(multiSurfaceProperty);
									break;
								case 2:
									plantCover.setLod2MultiSurface(multiSurfaceProperty);
									break;
								case 3:
									plantCover.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									plantCover.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
							}

							else if (geometry.getType() == GMLClass.MULTI_SOLID) {
								MultiSolidProperty multiSolidProperty = new MultiSolidPropertyImpl();

								if (geometry.getAbstractGeometry() != null)
									multiSolidProperty.setMultiSolid((MultiSolid)geometry.getAbstractGeometry());
								else
									multiSolidProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 1:
									plantCover.setLod1MultiSolid(multiSolidProperty);
									break;
								case 2:
									plantCover.setLod2MultiSolid(multiSolidProperty);
									break;
								case 3:
									plantCover.setLod3MultiSolid(multiSolidProperty);
									break;
								}
							}
						}
					}
				}
			}

			if (plantCover.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putGmlId(plantCover.getId(), plantCoverId, plantCover.getCityGMLClass());
			dbExporterManager.print(plantCover);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psPlantCover.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.PLANT_COVER;
	}

}
