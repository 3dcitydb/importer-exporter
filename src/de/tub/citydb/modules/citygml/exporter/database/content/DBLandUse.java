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

import org.citygml4j.impl.citygml.landuse.LandUseImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiSurfacePropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBLandUse implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Connection connection;

	private PreparedStatement psLandUse;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private FeatureClassFilter featureClassFilter;


	public DBLandUse(Connection connection, ExportFilter exportFilter, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		psLandUse = connection.prepareStatement("select * from LAND_USE where ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		LandUse landUse = new LandUseImpl();
		long landUseId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(landUse, landUseId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psLandUse.setLong(1, landUseId);
			rs = psLandUse.executeQuery();

			if (rs.next()) {
				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(landUse, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					landUse.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					landUse.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					landUse.setFunction(Arrays.asList(functionList));
				}

				String usage = rs.getString("USAGE");
				if (usage != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] usageList = p.split(usage.trim());
					landUse.setUsage(Arrays.asList(usageList));
				}

				for (int lod = 0; lod < 5 ; lod++) {
					long multiSurfaceId = rs.getLong("LOD" + lod + "_MULTI_SURFACE_ID");

					if (!rs.wasNull() && multiSurfaceId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(multiSurfaceId);

						if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
							MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
							else
								multiSurfaceProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 0:
								landUse.setLod0MultiSurface(multiSurfaceProperty);
								break;
							case 1:
								landUse.setLod1MultiSurface(multiSurfaceProperty);
								break;
							case 2:
								landUse.setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								landUse.setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 4:
								landUse.setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}
				}
			}

			if (landUse.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putGmlId(landUse.getId(), landUseId, landUse.getCityGMLClass());
			dbExporterManager.print(landUse);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psLandUse.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.LAND_USE;
	}

}
