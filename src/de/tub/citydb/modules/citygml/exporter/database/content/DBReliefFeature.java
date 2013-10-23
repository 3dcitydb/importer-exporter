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
import java.util.List;

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
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.primitives.ControlPoint;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

public class DBReliefFeature implements DBExporter {
	private final Logger LOG = Logger.getInstance();

	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psReliefFeature;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBOtherGeometry geometryExporter;
	private FeatureClassFilter featureClassFilter;

	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private String gmlIdPrefix;
	private String infoSys;

	public DBReliefFeature(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
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
			.append("select rf.ID as RF_ID, rf.NAME as RF_NAME, rf.NAME_CODESPACE as RF_NAME_CODESPACE, rf.DESCRIPTION as RF_DESCRIPTION, rf.LOD as RF_LOD, ")
			.append("rc.ID as RC_ID, rc.NAME as RC_NAME, rc.NAME_CODESPACE as RC_NAME_CODESPACE, rc.DESCRIPTION as RC_DESCRIPTION, rc.LOD as RC_LOD, rc.EXTENT as RC_EXTENT, ")
			.append("tr.ID as TR_ID, tr.MAX_LENGTH as TR_MAX_LENGTH, tr.STOP_LINES as TR_STOP_LINES, tr.BREAK_LINES as TR_BREAK_LINES, tr.CONTROL_POINTS as TR_CONTROL_POINTS, tr.SURFACE_GEOMETRY_ID as TR_SURFACE_GEOMETRY_ID, ")
			.append("mr.ID as MR_ID, mr.RELIEF_POINTS as MR_RELIEF_POINTS, ")
			.append("br.ID as BR_ID, br.RIDGE_OR_VALLEY_LINES as BR_RIDGE_OR_VALLEY_LINES, br.BREAK_LINES as BR_BREAK_LINES ")
			.append("from RELIEF_FEATURE rf inner join RELIEF_FEAT_TO_REL_COMP rf2rc on rf2rc.RELIEF_FEATURE_ID=rf.ID inner join RELIEF_COMPONENT rc on rf2rc.RELIEF_COMPONENT_ID=rc.ID ")
			.append("left join TIN_RELIEF tr on tr.ID=rc.ID ")
			.append("left join MASSPOINT_RELIEF mr on mr.ID=rc.ID ")
			.append("left join BREAKLINE_RELIEF br on br.ID=rc.ID where rf.ID=?");
			psReliefFeature = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select rf.ID as RF_ID, rf.NAME as RF_NAME, rf.NAME_CODESPACE as RF_NAME_CODESPACE, rf.DESCRIPTION as RF_DESCRIPTION, rf.LOD as RF_LOD, ")
			.append("rc.ID as RC_ID, rc.NAME as RC_NAME, rc.NAME_CODESPACE as RC_NAME_CODESPACE, rc.DESCRIPTION as RC_DESCRIPTION, rc.LOD as RC_LOD, ")
			.append(transformOrNull).append("(rc.EXTENT, ").append(srid).append(") as RC_EXTENT, ")
			.append("tr.ID as TR_ID, tr.MAX_LENGTH as TR_MAX_LENGTH, ")
			.append(transformOrNull).append("(tr.STOP_LINES, ").append(srid).append(") as TR_STOP_LINES, ")
			.append(transformOrNull).append("(tr.BREAK_LINES, ").append(srid).append(") as TR_BREAK_LINES, ")
			.append(transformOrNull).append("(tr.CONTROL_POINTS, ").append(srid).append(") as TR_CONTROL_POINTS, ")
			.append("tr.SURFACE_GEOMETRY_ID as TR_SURFACE_GEOMETRY_ID, ")
			.append("mr.ID as MR_ID, ")
			.append(transformOrNull).append("(mr.RELIEF_POINTS, ").append(srid).append(") AS MR_RELIEF_POINTS, ")
			.append("br.ID as BR_ID, ")
			.append(transformOrNull).append("(br.RIDGE_OR_VALLEY_LINES, ").append(srid).append(") as BR_RIDGE_OR_VALLEY_LINES, ")
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

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		ReliefFeature reliefFeature = new ReliefFeature();
		AbstractReliefComponent reliefComponent = null;
		long reliefFeatureId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(reliefFeature, reliefFeatureId, true);
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
					String gmlName = rs.getString("RF_NAME");
					String gmlNameCodespace = rs.getString("RF_NAME_CODESPACE");

					Util.dbGmlName2featureName(reliefFeature, gmlName, gmlNameCodespace);

					String description = rs.getString("RF_DESCRIPTION");
					if (description != null) {
						StringOrRef stringOrRef = new StringOrRef();
						stringOrRef.setValue(description);
						reliefFeature.setDescription(stringOrRef);
					}

					int lod = rs.getInt("RF_LOD");
					if (rs.wasNull())
						reliefFeature.setLod(0);
					else
						reliefFeature.setLod(lod);

					isInited = true;
				}

				// get reliefComponents content
				long reliefComponentId = rs.getLong("RC_ID");
				if (rs.wasNull())
					continue;

				reliefComponent = null;
				long tinReliefId = rs.getLong("TR_ID");
				long massPointReliefId = rs.getLong("MR_ID");
				long breaklineReliedId = rs.getLong("BR_ID");

				if (tinReliefId != 0)
					reliefComponent = new TINRelief();
				else if (massPointReliefId != 0)
					reliefComponent = new MassPointRelief();
				else if (breaklineReliedId != 0)
					reliefComponent = new BreaklineRelief();

				if (reliefComponent == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(reliefComponent, reliefComponentId, false);

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
				String gmlName = rs.getString("RC_NAME");
				String gmlNameCodespace = rs.getString("RC_NAME_CODESPACE");

				Util.dbGmlName2featureName(reliefComponent, gmlName, gmlNameCodespace);

				String description = rs.getString("RC_DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRef();
					stringOrRef.setValue(description);
					reliefComponent.setDescription(stringOrRef);
				}

				int lod = rs.getInt("RC_LOD");
				if (rs.wasNull())
					reliefComponent.setLod(0);
				else
					reliefComponent.setLod(lod);

				Object extentObj = rs.getObject("RC_EXTENT");
				if (!rs.wasNull() && extentObj != null) {
					GeometryObject extent = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPolygon(extentObj);
					PolygonProperty polygonProperty = geometryExporter.getPolygonProperty(extent, false);
					if (polygonProperty != null)
						reliefComponent.setExtent(polygonProperty);
				}

				// ok, further content must be retrieved according to the
				// subtype of reliefComponent
				if (reliefComponent.getCityGMLClass() == CityGMLClass.TIN_RELIEF) {
					TINRelief tinRelief = (TINRelief)reliefComponent;

					// get TINRelief content
					Double maxLength = rs.getDouble("TR_MAX_LENGTH");
					if (rs.wasNull())
						maxLength = null;

					GeometryObject stopLines, breakLines, controlPoints;
					stopLines = breakLines = controlPoints = null;

					Object stopLinesObj = rs.getObject("TR_STOP_LINES");
					if (!rs.wasNull() && stopLinesObj != null)
						stopLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(stopLinesObj);

					Object breakLinesObj = rs.getObject("TR_BREAK_LINES");
					if (!rs.wasNull() && breakLinesObj != null)
						breakLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);

					Object controlPointsObj = rs.getObject("TR_CONTROL_POINTS");
					if (!rs.wasNull() && controlPointsObj != null)
						controlPoints = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(controlPointsObj);

					long surfaceGeometryId = rs.getLong("TR_SURFACE_GEOMETRY_ID");

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
							length.setUom("urn:ogc:def:uom:UCUM::m");
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

				else if (reliefComponent.getCityGMLClass() == CityGMLClass.MASSPOINT_RELIEF) {
					MassPointRelief massPointRelief = (MassPointRelief)reliefComponent;

					Object reliefPointsObj = rs.getObject("MR_RELIEF_POINTS");
					if (!rs.wasNull() && reliefPointsObj != null) {
						GeometryObject reliefPoints = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(reliefPointsObj);
						MultiPointProperty multiPointProperty = geometryExporter.getMultiPointProperty(reliefPoints, false);
						if (multiPointProperty != null)
							massPointRelief.setReliefPoints(multiPointProperty);
					}
				}

				else if (reliefComponent.getCityGMLClass() == CityGMLClass.BREAKLINE_RELIEF) {
					BreaklineRelief breaklineRelief = (BreaklineRelief)reliefComponent;

					Object ridgeOrValleyLinesObj = rs.getObject("BR_RIDGE_OR_VALLEY_LINES");
					if (!rs.wasNull() && ridgeOrValleyLinesObj != null) {
						GeometryObject ridgeOrValleyLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(ridgeOrValleyLinesObj);
						MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(ridgeOrValleyLines, false);
						if (multiCurveProperty != null)					
							breaklineRelief.setRidgeOrValleyLines(multiCurveProperty);
					}
					
					Object breakLinesObj = rs.getObject("BR_BREAK_LINES");
					if (!rs.wasNull() && breakLinesObj != null) {
						GeometryObject breakLines = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);
						MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(breakLines, false);
						if (multiCurveProperty != null)					
							breaklineRelief.setBreaklines(multiCurveProperty);
					}
				}

				else if (reliefComponent.getCityGMLClass() == CityGMLClass.RASTER_RELIEF) {
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

			if (reliefFeature.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putGmlId(reliefFeature.getId(), reliefFeatureId, reliefFeature.getCityGMLClass());
			dbExporterManager.print(reliefFeature);
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
