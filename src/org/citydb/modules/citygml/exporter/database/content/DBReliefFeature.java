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
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.BreaklineRelief;
import org.citygml4j.model.citygml.relief.MassPointRelief;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.relief.TINRelief;
import org.citygml4j.model.citygml.relief.TinProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.primitives.ControlPoint;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBReliefFeature implements DBExporter {
	private final Logger LOG = Logger.getInstance();

	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psReliefFeature;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBOtherGeometry geometryExporter;

	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private String gmlIdPrefix;
	private String infoSys;

	private ProjectionPropertyFilter projectionFilter;

	public DBReliefFeature(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.RELIEF_FEATURE);

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
			.append("select rf.LOD as RF_LOD, ")
			.append("rc.ID as RC_ID, rc.OBJECTCLASS_ID, ")
			.append("tr.ID as TR_ID, mr.ID as MR_ID, br.ID as BR_ID, ")
			.append("rc.LOD as RC_LOD, rc.EXTENT, ")
			.append("tr.MAX_LENGTH, tr.MAX_LENGTH_UNIT, tr.STOP_LINES, tr.BREAK_LINES as TR_BREAK_LINES, tr.CONTROL_POINTS, tr.SURFACE_GEOMETRY_ID, ")
			.append("mr.RELIEF_POINTS, ")
			.append("br.RIDGE_OR_VALLEY_LINES, br.BREAK_LINES as BR_BREAK_LINES ")
			.append("from RELIEF_FEATURE rf inner join RELIEF_FEAT_TO_REL_COMP rf2rc on rf2rc.RELIEF_FEATURE_ID=rf.ID inner join RELIEF_COMPONENT rc on rf2rc.RELIEF_COMPONENT_ID=rc.ID ")
			.append("left join TIN_RELIEF tr on tr.ID=rc.ID ")
			.append("left join MASSPOINT_RELIEF mr on mr.ID=rc.ID ")
			.append("left join BREAKLINE_RELIEF br on br.ID=rc.ID where rf.ID=?");
			psReliefFeature = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select rf.LOD as RF_LOD, ")
			.append("rc.ID as RC_ID, rc.OBJECTCLASS_ID, ")
			.append("tr.ID as TR_ID, mr.ID as MR_ID, br.ID as BR_ID, ")
			.append("rc.LOD as RC_LOD, ")
			.append(transformOrNull).append("(rc.EXTENT, ").append(srid).append(") as EXTENT, ")
			.append("tr.MAX_LENGTH, tr.MAX_LENGTH_UNIT, ")
			.append(transformOrNull).append("(tr.STOP_LINES, ").append(srid).append(") as STOP_LINES, ")
			.append(transformOrNull).append("(tr.BREAK_LINES, ").append(srid).append(") as TR_BREAK_LINES, ")
			.append(transformOrNull).append("(tr.CONTROL_POINTS, ").append(srid).append(") as CONTROL_POINTS, ")
			.append("tr.SURFACE_GEOMETRY_ID, ")
			.append(transformOrNull).append("(mr.RELIEF_POINTS, ").append(srid).append(") AS RELIEF_POINTS, ")
			.append(transformOrNull).append("(br.RIDGE_OR_VALLEY_LINES, ").append(srid).append(") as RIDGE_OR_VALLEY_LINES, ")
			.append(transformOrNull).append("(br.BREAK_LINES, ").append(srid).append(") as BR_BREAK_LINES ")
			.append("from RELIEF_FEATURE rf inner join RELIEF_FEAT_TO_REL_COMP rf2rc on rf2rc.RELIEF_FEATURE_ID=rf.ID inner join RELIEF_COMPONENT rc on rf2rc.RELIEF_COMPONENT_ID=rc.ID ")
			.append("left join TIN_RELIEF tr on tr.ID=rc.ID ")
			.append("left join MASSPOINT_RELIEF mr on mr.ID=rc.ID ")
			.append("left join BREAKLINE_RELIEF br on br.ID=rc.ID where rf.ID=?");
			psReliefFeature = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		ReliefFeature reliefFeature = new ReliefFeature();
		AbstractReliefComponent reliefComponent = null;
		long reliefFeatureId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(reliefFeature, reliefFeatureId, true, projectionFilter);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psReliefFeature.setLong(1, reliefFeatureId);
			rs = psReliefFeature.executeQuery();

			boolean isInited = false;
			String origGmlId = reliefFeature.getId();

			while (rs.next()) {				
				if (!isInited) {
					// reliefFeature object
					// just handle once
					if (projectionFilter.pass(CityGMLModuleType.RELIEF, "lod"))
						reliefFeature.setLod(rs.getInt(1));				

					isInited = true;
				}

				// get reliefComponents content
				if (projectionFilter.filter(CityGMLModuleType.RELIEF, "reliefComponent"))
					break;
				
				long reliefComponentId = rs.getLong(2);
				if (rs.wasNull())
					continue;

				int classId = rs.getInt(3);
				if (rs.wasNull() || classId == 0)
					continue;

				CityGMLClass type = Util.classId2cityObject(classId);
				reliefComponent = null;

				long tinReliefId = rs.getLong(4);
				long massPointReliefId = rs.getLong(5);
				long breaklineReliedId = rs.getLong(6);

				if (tinReliefId != 0 && type == CityGMLClass.TIN_RELIEF)
					reliefComponent = new TINRelief();
				else if (massPointReliefId != 0 && type == CityGMLClass.MASSPOINT_RELIEF)
					reliefComponent = new MassPointRelief();
				else if (breaklineReliedId != 0 && type == CityGMLClass.BREAKLINE_RELIEF)
					reliefComponent = new BreaklineRelief();

				if (reliefComponent == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(reliefComponent, reliefComponentId);

				if (reliefComponent.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(reliefComponent.getId(), reliefComponentId, CityGMLClass.ABSTRACT_RELIEF_COMPONENT)) {
						if (useXLink) {
							ReliefComponentProperty property = new ReliefComponentProperty();
							property.setHref("#" + reliefComponent.getId());

							reliefFeature.addReliefComponent(property);
							continue;
						} else {
							String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + reliefComponent.getId();

							if (keepOldGmlId) {
								ExternalReference externalReference = new ExternalReference();
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = new ExternalObject();
								externalObject.setName(reliefComponent.getId());

								externalReference.setExternalObject(externalObject);
								reliefComponent.addExternalReference(externalReference);
							}

							reliefComponent.setId(newGmlId);
						}
					}
				}

				// get common data for all kinds of relief components
				reliefComponent.setLod(rs.getInt(7));

				Object extentObj = rs.getObject(8);
				if (!rs.wasNull() && extentObj != null) {
					GeometryObject extent = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPolygon(extentObj);
					PolygonProperty polygonProperty = geometryExporter.getPolygonProperty(extent, false);
					if (polygonProperty != null)
						reliefComponent.setExtent(polygonProperty);
				}

				// ok, further content must be retrieved according to the
				// subtype of reliefComponent
				if (type == CityGMLClass.TIN_RELIEF) {
					TINRelief tinRelief = (TINRelief)reliefComponent;

					// get TINRelief content
					Double maxLength = rs.getDouble(9);
					if (rs.wasNull())
						maxLength = null;

					GeometryObject stopLines, breakLines, controlPoints;
					stopLines = breakLines = controlPoints = null;

					Object stopLinesObj = rs.getObject(11);
					if (!rs.wasNull() && stopLinesObj != null)
						stopLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(stopLinesObj);

					Object breakLinesObj = rs.getObject(12);
					if (!rs.wasNull() && breakLinesObj != null)
						breakLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);

					Object controlPointsObj = rs.getObject(13);
					if (!rs.wasNull() && controlPointsObj != null)
						controlPoints = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(controlPointsObj);

					long surfaceGeometryId = rs.getLong(14);

					// check for invalid content
					if (maxLength == null && stopLines == null && breakLines == null && controlPoints == null && surfaceGeometryId == 0)
						continue;

					// check whether we deal with a gml:TrinagulatedSurface or a gml:Tin
					boolean isTin = false;
					if (maxLength != null || stopLines != null || breakLines != null || controlPoints != null)
						isTin = true;

					// get triangle patches
					TinProperty tinProperty = new TinProperty();
					TriangulatedSurface triangulatedSurface = null;
					if (surfaceGeometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);

						// check for null until we have implemented rectifiedgridcoverage
						if (geometry == null)
							return false;

						// we do not allow xlinks here
						if (geometry.getType() == GMLClass.TRIANGULATED_SURFACE && geometry.getAbstractGeometry() != null) 
							triangulatedSurface = (TriangulatedSurface)geometry.getAbstractGeometry();					
					}

					// check for invalid gml:TriangulatedSurface
					if (!isTin && triangulatedSurface == null)
						continue;

					if (isTin) {
						if (triangulatedSurface != null) {
							TrianglePatchArrayProperty patches = triangulatedSurface.getTrianglePatches();
							triangulatedSurface = new Tin();
							triangulatedSurface.setTrianglePatches(patches);
						} else {
							triangulatedSurface = new Tin();
							triangulatedSurface.setTrianglePatches(new TrianglePatchArrayProperty());
						}
					}

					tinProperty.setObject(triangulatedSurface);
					tinRelief.setTin(tinProperty);

					// finally, check gml:Tin specific content
					if (isTin) {
						Tin tin = (Tin)triangulatedSurface;

						if (maxLength != null) {
							Length length = new Length();
							length.setValue(maxLength);
							length.setUom(rs.getString(10));
							tin.setMaxLength(length);
						}

						if (stopLines != null) {
							List<LineStringSegmentArrayProperty> arrayPropertyList = geometryExporter.getListOfLineStringSegmentArrayProperty(stopLines, false);
							if (arrayPropertyList != null)
								tin.setStopLines(arrayPropertyList);
						}

						if (breakLines != null) {
							List<LineStringSegmentArrayProperty> arrayPropertyList = geometryExporter.getListOfLineStringSegmentArrayProperty(breakLines, false);
							if (arrayPropertyList != null)
								tin.setBreakLines(arrayPropertyList);
						}

						if (controlPoints != null) {
							ControlPoint controlPoint = geometryExporter.getControlPoint(controlPoints, false);
							if (controlPoint != null)
								tin.setControlPoint(controlPoint);
						}
					}
				}

				else if (type == CityGMLClass.MASSPOINT_RELIEF) {
					MassPointRelief massPointRelief = (MassPointRelief)reliefComponent;

					Object reliefPointsObj = rs.getObject(15);
					if (!rs.wasNull() && reliefPointsObj != null) {
						GeometryObject reliefPoints = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(reliefPointsObj);
						MultiPointProperty multiPointProperty = geometryExporter.getMultiPointProperty(reliefPoints, false);
						if (multiPointProperty != null)
							massPointRelief.setReliefPoints(multiPointProperty);
					}
				}

				else if (type == CityGMLClass.BREAKLINE_RELIEF) {
					BreaklineRelief breaklineRelief = (BreaklineRelief)reliefComponent;

					Object ridgeOrValleyLinesObj = rs.getObject(16);
					if (!rs.wasNull() && ridgeOrValleyLinesObj != null) {
						GeometryObject ridgeOrValleyLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(ridgeOrValleyLinesObj);
						MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(ridgeOrValleyLines, false);
						if (multiCurveProperty != null)					
							breaklineRelief.setRidgeOrValleyLines(multiCurveProperty);
					}

					Object breakLinesObj = rs.getObject(17);
					if (!rs.wasNull() && breakLinesObj != null) {
						GeometryObject breakLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);
						MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(breakLines, false);
						if (multiCurveProperty != null)					
							breaklineRelief.setBreaklines(multiCurveProperty);
					}
				}

				else if (type == CityGMLClass.RASTER_RELIEF) {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							reliefFeature.getCityGMLClass(), 
							origGmlId));

					msg.append(": RasterRelief is not supported yet.");
					LOG.error(msg.toString());
				}

				// add reliefComponent to reliefFeature
				ReliefComponentProperty property = new ReliefComponentProperty();
				property.setObject(reliefComponent);
				reliefFeature.addReliefComponent(property);
			}

			dbExporterManager.processFeature(reliefFeature);

			if (reliefFeature.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(reliefFeature.getId(), reliefFeatureId, reliefFeature.getCityGMLClass());

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psReliefFeature.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.RELIEF_FEATURE;
	}

}
