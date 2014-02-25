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

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
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
		StringBuilder query = new StringBuilder()
		.append("select CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, AVERAGE_HEIGHT, AVERAGE_HEIGHT_UNIT, ")
		.append("LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("LOD1_MULTI_SOLID_ID, LOD2_MULTI_SOLID_ID, LOD3_MULTI_SOLID_ID, LOD4_MULTI_SOLID_ID ")
		.append("from PLANT_COVER where ID = ?");
		psPlantCover = connection.prepareStatement(query.toString());

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		PlantCover plantCover = new PlantCover();
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
				String clazz = rs.getString(1);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(2));
					plantCover.setClazz(code);
				}

				String function = rs.getString(3);
				String functionCodeSpace = rs.getString(4);
				if (function != null)
					plantCover.setFunction(Util.string2codeList(function, functionCodeSpace));

				String usage = rs.getString(5);
				String usageCodeSpace = rs.getString(6);
				if (usage != null)
					plantCover.setUsage(Util.string2codeList(usage, usageCodeSpace));
				
				double averageHeight = rs.getDouble(7);
				if (!rs.wasNull()) {
					Length length = new Length();
					length.setValue(averageHeight);
					length.setUom(rs.getString(8));
					plantCover.setAverageHeight(length);
				}

				// multiSurface
				for (int lod = 0; lod < 4; lod++) {
					long surfaceGeometryId = rs.getLong(9 + lod);
					if (rs.wasNull() || surfaceGeometryId == 0)
						continue;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.getAbstractGeometry() != null)
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 0:
							plantCover.setLod1MultiSurface(multiSurfaceProperty);
							break;
						case 1:
							plantCover.setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							plantCover.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 3:
							plantCover.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}
				
				// solid
				for (int lod = 0; lod < 4; lod++) {
					long surfaceGeometryId = rs.getLong(13 + lod);
					if (rs.wasNull() || surfaceGeometryId == 0)
						continue;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SOLID) {
						MultiSolidProperty multiSolidProperty = new MultiSolidProperty();
						if (geometry.getAbstractGeometry() != null)
							multiSolidProperty.setMultiSolid((MultiSolid)geometry.getAbstractGeometry());
						else
							multiSolidProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 0:
							plantCover.setLod1MultiSolid(multiSolidProperty);
							break;
						case 1:
							plantCover.setLod2MultiSolid(multiSolidProperty);
							break;
						case 2:
							plantCover.setLod3MultiSolid(multiSolidProperty);
							break;
						case 3:
							plantCover.setLod4MultiSolid(multiSolidProperty);
							break;
						}
					}
				}
			}

			if (plantCover.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putUID(plantCover.getId(), plantCoverId, plantCover.getCityGMLClass());
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
