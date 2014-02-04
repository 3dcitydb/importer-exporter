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
import java.util.regex.Pattern;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.citygml.waterbody.WaterClosureSurface;
import org.citygml4j.model.citygml.waterbody.WaterGroundSurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBWaterBody implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psWaterBody;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBOtherGeometry geometryExporter;
	private FeatureClassFilter featureClassFilter;

	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private String gmlIdPrefix;
	private String infoSys;

	public DBWaterBody(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			keepOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetKeepGmlIdAsExternalReference();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
			infoSys = config.getInternal().getExportFileName();
		}	

		if (!config.getInternal().isTransformCoordinates()) {		
			StringBuilder query = new StringBuilder()
			.append("select wb.NAME as WB_NAME, wb.NAME_CODESPACE as WB_NAME_CODESPACE, wb.DESCRIPTION as WB_DESCRIPTION, wb.CLASS, wb.FUNCTION, wb.USAGE, ")
			.append("wb.LOD1_SOLID_ID, wb.LOD2_SOLID_ID, wb.LOD3_SOLID_ID, wb.LOD4_SOLID_ID, wb.LOD0_MULTI_SURFACE_ID, wb.LOD1_MULTI_SURFACE_ID, ")
			.append("wb.LOD0_MULTI_CURVE, wb.LOD1_MULTI_CURVE, ")
			.append("ws.ID as WS_ID, ws.NAME as WS_NAME, ws.NAME_CODESPACE as WS_NAME_CODESPACE, ws.DESCRIPTION as WS_DESCRIPTION, upper(ws.TYPE) as TYPE, ws.WATER_LEVEL, ")
			.append("ws.LOD2_SURFACE_ID, ws.LOD3_SURFACE_ID, ws.LOD4_SURFACE_ID ")
			.append("from WATERBODY wb left join WATERBOD_TO_WATERBND_SRF w2s on wb.ID=w2s.WATERBODY_ID left join WATERBOUNDARY_SURFACE ws on ws.ID=w2s.WATERBOUNDARY_SURFACE_ID where wb.ID=?");
			psWaterBody = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select wb.NAME as WB_NAME, wb.NAME_CODESPACE as WB_NAME_CODESPACE, wb.DESCRIPTION as WB_DESCRIPTION, wb.CLASS, wb.FUNCTION, wb.USAGE, ")
			.append("wb.LOD1_SOLID_ID, wb.LOD2_SOLID_ID, wb.LOD3_SOLID_ID, wb.LOD4_SOLID_ID, wb.LOD0_MULTI_SURFACE_ID, wb.LOD1_MULTI_SURFACE_ID, ")
			.append(transformOrNull).append("(wb.LOD0_MULTI_CURVE, ").append(srid).append(") AS LOD0_MULTI_CURVE, ")
			.append(transformOrNull).append("(wb.LOD1_MULTI_CURVE, ").append(srid).append(") AS LOD1_MULTI_CURVE, ")
			.append("ws.ID as WS_ID, ws.NAME as WS_NAME, ws.NAME_CODESPACE as WS_NAME_CODESPACE, ws.DESCRIPTION as WS_DESCRIPTION, upper(ws.TYPE) as TYPE, ws.WATER_LEVEL, ")
			.append("ws.LOD2_SURFACE_ID, ws.LOD3_SURFACE_ID, ws.LOD4_SURFACE_ID ")
			.append("from WATERBODY wb left join WATERBOD_TO_WATERBND_SRF w2s on wb.ID=w2s.WATERBODY_ID left join WATERBOUNDARY_SURFACE ws on ws.ID=w2s.WATERBOUNDARY_SURFACE_ID where wb.ID=?");
			psWaterBody = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		WaterBody waterBody = new WaterBody();
		long waterBodyId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(waterBody, waterBodyId, true);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psWaterBody.setLong(1, waterBodyId);
			rs = psWaterBody.executeQuery();
			boolean waterBodyRead = false;

			while (rs.next()) {

				if (!waterBodyRead) {
					// name and name_codespace
					String gmlName = rs.getString("WB_NAME");
					String gmlNameCodespace = rs.getString("WB_NAME_CODESPACE");

					Util.string2codeList(waterBody, gmlName, gmlNameCodespace);

					String description = rs.getString("WB_DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRef();
						stringOrRef.setValue(description);
						waterBody.setDescription(stringOrRef);
					}

					String clazz = rs.getString("CLASS");
					if (clazz != null) {
						waterBody.setClazz(new Code(clazz));
					}

					String function = rs.getString("FUNCTION");
					if (function != null) {
						Pattern p = Pattern.compile("\\s+");
						for (String value : p.split(function.trim()))
							waterBody.addFunction(new Code(value));
					}

					String usage = rs.getString("USAGE");
					if (usage != null) {
						Pattern p = Pattern.compile("\\s+");
						for (String value : p.split(usage.trim()))
							waterBody.addUsage(new Code(value));
					}

					for (int lod = 1; lod < 5 ; lod++) {
						long geometryId = rs.getLong("LOD" + lod + "_SOLID_ID");

						if (!rs.wasNull() && geometryId != 0) {
							DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

							if (geometry != null) {
								SolidProperty solidProperty = new SolidProperty();

								if (geometry.getAbstractGeometry() != null)
									solidProperty.setSolid((AbstractSolid)geometry.getAbstractGeometry());
								else
									solidProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 1:
									waterBody.setLod1Solid(solidProperty);
									break;
								case 2:
									waterBody.setLod2Solid(solidProperty);
									break;
								case 3:
									waterBody.setLod3Solid(solidProperty);
									break;
								case 4:
									waterBody.setLod4Solid(solidProperty);
									break;
								}
							}
						}
					}

					for (int lod = 0; lod < 2 ; lod++) {
						long geometryId = rs.getLong("LOD" + lod + "_MULTI_SURFACE_ID");

						if (!rs.wasNull() && geometryId != 0) {
							DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

							if (geometry != null) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();

								if (geometry.getAbstractGeometry() != null)
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getTarget());

								switch (lod) {
								case 0:
									waterBody.setLod0MultiSurface(multiSurfaceProperty);
									break;
								case 1:
									waterBody.setLod1MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}
					}

					// lodXMultiCurve
					for (int lod = 0; lod < 2; lod++) {
						Object multiCurveObj = rs.getObject("LOD" + lod + "_MULTI_CURVE");
						if (!rs.wasNull() && multiCurveObj != null) {
							GeometryObject multiCurve = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(multiCurveObj);
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(multiCurve, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									waterBody.setLod0MultiCurve(multiCurveProperty);
									break;
								case 1:
									waterBody.setLod1MultiCurve(multiCurveProperty);
									break;
								}
							}
						}
					}

					waterBodyRead = true;
				}

				// water boundary surfaces
				long waterBoundarySurfaceId = rs.getLong("WS_ID");
				if (rs.wasNull())
					continue;

				// create new water boundary object
				AbstractWaterBoundarySurface waterBoundarySurface = null;
				String type = rs.getString("TYPE");
				if (rs.wasNull() || type == null || type.length() == 0)
					continue;

				if (type.equals(TypeAttributeValueEnum.WATER_SURFACE.toString().toUpperCase()))
					waterBoundarySurface = new WaterSurface();
				else if (type.equals(TypeAttributeValueEnum.WATER_GROUND_SURFACE.toString().toUpperCase()))
					waterBoundarySurface = new WaterGroundSurface();
				else if (type.equals(TypeAttributeValueEnum.WATER_CLOSURE_SURFACE.toString().toUpperCase()))
					waterBoundarySurface = new WaterClosureSurface();

				if (waterBoundarySurface == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(waterBoundarySurface, waterBoundarySurfaceId);

				if (waterBoundarySurface.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(waterBoundarySurface.getId(), waterBoundarySurfaceId, CityGMLClass.ABSTRACT_WATER_BOUNDARY_SURFACE)) {
						if (useXLink) {
							BoundedByWaterSurfaceProperty boundedByProperty = new BoundedByWaterSurfaceProperty();
							boundedByProperty.setHref("#" + waterBoundarySurface.getId());

							waterBody.addBoundedBySurface(boundedByProperty);
							continue;
						} else {
							String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + waterBoundarySurface.getId();

							if (keepOldGmlId) {
								ExternalReference externalReference = new ExternalReference();
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = new ExternalObject();
								externalObject.setName(waterBoundarySurface.getId());

								externalReference.setExternalObject(externalObject);
								waterBoundarySurface.addExternalReference(externalReference);
							}

							waterBoundarySurface.setId(newGmlId);
						}
					}
				}

				String gmlName = rs.getString("WS_NAME");
				String gmlNameCodespace = rs.getString("WS_NAME_CODESPACE");

				Util.string2codeList(waterBoundarySurface, gmlName, gmlNameCodespace);

				String description = rs.getString("WS_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRef();
					stringOrRef.setValue(description);

					waterBoundarySurface.setDescription(stringOrRef);
				}

				if (waterBoundarySurface.getCityGMLClass() == CityGMLClass.WATER_SURFACE) {
					String waterLevel = rs.getString("WATER_LEVEL");
					if (waterLevel != null)
						((WaterSurface)waterBoundarySurface).setWaterLevel(new Code(waterLevel));
				}

				for (int lod = 2; lod < 5 ; lod++) {
					long geometryId = rs.getLong("LOD" + lod + "_SURFACE_ID");

					if (!rs.wasNull() && geometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(geometryId);

						if (geometry != null) {
							SurfaceProperty surfaceProperty = new SurfaceProperty();

							if (geometry.getAbstractGeometry() != null)
								surfaceProperty.setSurface((AbstractSurface)geometry.getAbstractGeometry());
							else
								surfaceProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 2:
								waterBoundarySurface.setLod2Surface(surfaceProperty);
								break;
							case 3:
								waterBoundarySurface.setLod3Surface(surfaceProperty);
								break;
							case 4:
								waterBoundarySurface.setLod4Surface(surfaceProperty);
								break;
							}
						}
					}
				}

				BoundedByWaterSurfaceProperty boundedByProperty = new BoundedByWaterSurfaceProperty();
				boundedByProperty.setObject(waterBoundarySurface);
				waterBody.addBoundedBySurface(boundedByProperty);
			}

			if (waterBody.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putGmlId(waterBody.getId(), waterBodyId, waterBody.getCityGMLClass());
			dbExporterManager.print(waterBody);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psWaterBody.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.WATERBODY;
	}

}
