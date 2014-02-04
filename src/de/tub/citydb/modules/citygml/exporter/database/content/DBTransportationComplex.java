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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.Railway;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.Square;
import org.citygml4j.model.citygml.transportation.Track;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBTransportationComplex implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTranComplex;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBOtherGeometry geometryExporter;
	private FeatureClassFilter featureClassFilter;

	public DBTransportationComplex(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select tc.ID as TC_ID, tc.NAME as TC_NAME, tc.NAME_CODESPACE as TC_NAME_CODESPACE, tc.DESCRIPTION as TC_DESCRIPTION, tc.FUNCTION as TC_FUNCTION, tc.USAGE as TC_USAGE, ")
			.append("upper(tc.TYPE) as TC_TYPE, tc.LOD1_MULTI_SURFACE_ID as TC_LOD1_MULTI_SURFACE_ID, tc.LOD2_MULTI_SURFACE_ID as TC_LOD2_MULTI_SURFACE_ID, tc.LOD3_MULTI_SURFACE_ID as TC_LOD3_MULTI_SURFACE_ID, ")
			.append("tc.LOD4_MULTI_SURFACE_ID as TC_LOD4_MULTI_SURFACE_ID, tc.LOD0_NETWORK as TC_LOD0_NETWORK, ")
			.append("ta.ID as TA_ID, ta.IS_AUXILIARY, ta.NAME as TA_NAME, ta.NAME_CODESPACE as TA_NAME_CODESPACE, ta.DESCRIPTION as TA_DESCRIPTION, ta.FUNCTION as TA_FUNCTION, ta.USAGE as TA_USAGE, ")
			.append("ta.SURFACE_MATERIAL, ta.LOD2_MULTI_SURFACE_ID as TA_LOD2_MULTI_SURFACE_ID, ta.LOD3_MULTI_SURFACE_ID as TA_LOD3_MULTI_SURFACE_ID, ")
			.append("ta.LOD4_MULTI_SURFACE_ID as TA_LOD4_MULTI_SURFACE_ID from TRANSPORTATION_COMPLEX tc left join TRAFFIC_AREA ta on tc.ID=ta.TRANSPORTATION_COMPLEX_ID where tc.ID=?");
			psTranComplex = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select tc.ID as TC_ID, tc.NAME as TC_NAME, tc.NAME_CODESPACE as TC_NAME_CODESPACE, tc.DESCRIPTION as TC_DESCRIPTION, tc.FUNCTION as TC_FUNCTION, tc.USAGE as TC_USAGE, ")
			.append("upper(tc.TYPE) as TC_TYPE, tc.LOD1_MULTI_SURFACE_ID as TC_LOD1_MULTI_SURFACE_ID, tc.LOD2_MULTI_SURFACE_ID as TC_LOD2_MULTI_SURFACE_ID, tc.LOD3_MULTI_SURFACE_ID as TC_LOD3_MULTI_SURFACE_ID, ")
			.append("tc.LOD4_MULTI_SURFACE_ID as TC_LOD4_MULTI_SURFACE_ID, ")
			.append(transformOrNull).append("(tc.LOD0_NETWORK, ").append(srid).append(") as TC_LOD0_NETWORK, ")
			.append("ta.ID as TA_ID, ta.IS_AUXILIARY, ta.NAME as TA_NAME, ta.NAME_CODESPACE as TA_NAME_CODESPACE, ta.DESCRIPTION as TA_DESCRIPTION, ta.FUNCTION as TA_FUNCTION, ta.USAGE as TA_USAGE, ")
			.append("ta.SURFACE_MATERIAL, ta.LOD2_MULTI_SURFACE_ID as TA_LOD2_MULTI_SURFACE_ID, ta.LOD3_MULTI_SURFACE_ID as TA_LOD3_MULTI_SURFACE_ID, ")
			.append("ta.LOD4_MULTI_SURFACE_ID as TA_LOD4_MULTI_SURFACE_ID from TRANSPORTATION_COMPLEX tc left join TRAFFIC_AREA ta on tc.ID=ta.TRANSPORTATION_COMPLEX_ID where tc.ID=?");
			psTranComplex = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		TransportationComplex transComplex = null;
		long transComplexId = splitter.getPrimaryKey();

		switch (splitter.getCityObjectType()) {
		case ROAD:
			transComplex = new Road();
			break;
		case RAILWAY:
			transComplex = new Railway();
			break;
		case SQUARE:
			transComplex = new Square();
			break;
		case TRACK:
			transComplex = new Track();
			break;
		default:
			transComplex = new TransportationComplex();
		}

		// cityObject stuff
		boolean success = cityObjectExporter.read(transComplex, transComplexId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psTranComplex.setLong(1, transComplexId);
			rs = psTranComplex.executeQuery();

			boolean isInited = false;

			while (rs.next()) {
				if (!isInited) {
					String gmlName = rs.getString("TC_NAME");
					String gmlNameCodespace = rs.getString("TC_NAME_CODESPACE");

					Util.string2codeList(transComplex, gmlName, gmlNameCodespace);

					String description = rs.getString("TC_DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRef();
						stringOrRef.setValue(description);
						transComplex.setDescription(stringOrRef);
					}

					String function = rs.getString("TC_FUNCTION");
					if (function != null) {
						Pattern p = Pattern.compile("\\s+");
						for (String value : p.split(function.trim()))
							transComplex.addFunction(new Code(value));
					}

					String usage = rs.getString("TC_USAGE");
					if (usage != null) {
						Pattern p = Pattern.compile("\\s+");
						for (String value : p.split(usage.trim()))
							transComplex.addUsage(new Code(value));
					}

					for (int lod = 1; lod < 5 ; lod++) {
						long multiSurfaceId = rs.getLong("TC_LOD" + lod + "_MULTI_SURFACE_ID");

						if (!rs.wasNull() && multiSurfaceId != 0) {
							DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(multiSurfaceId);

							if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();

								if (geometry.getAbstractGeometry() != null)
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 1:
									transComplex.setLod1MultiSurface(multiSurfaceProperty);
									break;
								case 2:
									transComplex.setLod2MultiSurface(multiSurfaceProperty);
									break;
								case 3:
									transComplex.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									transComplex.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}
					}

					// lod0Network
					Object object = rs.getObject("TC_LOD0_NETWORK");
					if (!rs.wasNull() && object != null) {
						GeometryObject lod0Network = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(object);
						GeometricComplexProperty complexProperty = geometryExporter.getPointOrCurveComplexProperty(lod0Network, false);
						transComplex.addLod0Network(complexProperty);
					}

					isInited = true;
				}

				long trafficAreaId = rs.getLong("TA_ID");
				if (rs.wasNull())
					continue;

				AbstractTransportationObject transObject = null;
				boolean isAuxiliary = rs.getBoolean("IS_AUXILIARY");

				if (isAuxiliary)
					transObject = new AuxiliaryTrafficArea();
				else
					transObject = new TrafficArea();

				// cityobject stuff
				cityObjectExporter.read(transObject, trafficAreaId);

				String gmlName = rs.getString("TA_NAME");
				String gmlNameCodespace = rs.getString("TA_NAME_CODESPACE");

				Util.string2codeList(transObject, gmlName, gmlNameCodespace);

				String description = rs.getString("TA_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRef();
					stringOrRef.setValue(description);
					transObject.setDescription(stringOrRef);
				}

				String function = rs.getString("TA_FUNCTION");
				if (function != null) {
					List<Code> functionList = new ArrayList<Code>();
					Pattern p = Pattern.compile("\\s+");
					for (String value : p.split(function.trim()))
						functionList.add(new Code(value));

					if (isAuxiliary)
						((AuxiliaryTrafficArea)transObject).setFunction(functionList);
					else
						((TrafficArea)transObject).setFunction(functionList);
				}

				String usage = rs.getString("TA_USAGE");
				if (usage != null && !isAuxiliary) {
					Pattern p = Pattern.compile("\\s+");
					for (String value : p.split(usage.trim()))
						((TrafficArea)transObject).addUsage(new Code(value));
				}

				String surfaceMaterial = rs.getString("SURFACE_MATERIAL");
				if (surfaceMaterial != null) {
					if (isAuxiliary)
						((AuxiliaryTrafficArea)transObject).setSurfaceMaterial(new Code(surfaceMaterial));
					else
						((TrafficArea)transObject).setSurfaceMaterial(new Code(surfaceMaterial));
				}

				for (int lod = 2; lod < 5 ; lod++) {
					long multiSurfaceId = rs.getLong("TA_LOD" + lod + "_MULTI_SURFACE_ID");

					if (!rs.wasNull() && multiSurfaceId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(multiSurfaceId);

						if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
							MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();

							if (geometry.getAbstractGeometry() != null)
								multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
							else
								multiSurfaceProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 2:
								if (isAuxiliary)
									((AuxiliaryTrafficArea)transObject).setLod2MultiSurface(multiSurfaceProperty);
								else
									((TrafficArea)transObject).setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								if (isAuxiliary)
									((AuxiliaryTrafficArea)transObject).setLod3MultiSurface(multiSurfaceProperty);
								else
									((TrafficArea)transObject).setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 4:
								if (isAuxiliary)
									((AuxiliaryTrafficArea)transObject).setLod4MultiSurface(multiSurfaceProperty);
								else
									((TrafficArea)transObject).setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}
				}

				if (isAuxiliary) {
					AuxiliaryTrafficAreaProperty auxProperty  = new AuxiliaryTrafficAreaProperty();
					auxProperty.setObject((AuxiliaryTrafficArea)transObject);
					transComplex.addAuxiliaryTrafficArea(auxProperty);
				} else {
					TrafficAreaProperty trafficProperty = new TrafficAreaProperty();
					trafficProperty.setObject((TrafficArea)transObject);
					transComplex.addTrafficArea(trafficProperty);
				}
			}

			if (transComplex.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putGmlId(transComplex.getId(), transComplexId, transComplex.getCityGMLClass());
			dbExporterManager.print(transComplex);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTranComplex.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TRANSPORTATION_COMPLEX;
	}

}
