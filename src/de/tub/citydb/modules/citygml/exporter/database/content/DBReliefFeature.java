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
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.citygml.core.ExternalObjectImpl;
import org.citygml4j.impl.citygml.core.ExternalReferenceImpl;
import org.citygml4j.impl.citygml.relief.BreaklineReliefImpl;
import org.citygml4j.impl.citygml.relief.MassPointReliefImpl;
import org.citygml4j.impl.citygml.relief.ReliefComponentPropertyImpl;
import org.citygml4j.impl.citygml.relief.ReliefFeatureImpl;
import org.citygml4j.impl.citygml.relief.TINReliefImpl;
import org.citygml4j.impl.citygml.relief.TinPropertyImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.primitives.TinImpl;
import org.citygml4j.impl.gml.geometry.primitives.TrianglePatchArrayPropertyImpl;
import org.citygml4j.impl.gml.measures.LengthImpl;
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
	private DBSdoGeometry sdoGeometry;
	private FeatureClassFilter featureClassFilter;

	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private boolean transformCoords;
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

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {		
			psReliefFeature = connection.prepareStatement("select rf.ID as RF_ID, rf.NAME as RF_NAME, rf.NAME_CODESPACE as RF_NAME_CODESPACE, rf.DESCRIPTION as RF_DESCRIPTION, rf.LOD as RF_LOD, " +
					"rc.ID as RC_ID, rc.NAME as RC_NAME, rc.NAME_CODESPACE as RC_NAME_CODESPACE, rc.DESCRIPTION as RC_DESCRIPTION, rc.LOD as RC_LOD, rc.EXTENT as RC_EXTENT, " +
					"tr.ID as TR_ID, tr.MAX_LENGTH as TR_MAX_LENGTH, tr.STOP_LINES as TR_STOP_LINES, tr.BREAK_LINES as TR_BREAK_LINES, tr.CONTROL_POINTS as TR_CONTROL_POINTS, tr.SURFACE_GEOMETRY_ID as TR_SURFACE_GEOMETRY_ID, " +
					"mr.ID as MR_ID, mr.RELIEF_POINTS as MR_RELIEF_POINTS, " +
					"br.ID as BR_ID, br.RIDGE_OR_VALLEY_LINES as BR_RIDGE_OR_VALLEY_LINES, br.BREAK_LINES as BR_BREAK_LINES " +
					"from RELIEF_FEATURE rf inner join RELIEF_FEAT_TO_REL_COMP rf2rc on rf2rc.RELIEF_FEATURE_ID=rf.ID inner join RELIEF_COMPONENT rc on rf2rc.RELIEF_COMPONENT_ID=rc.ID " +
					"left join TIN_RELIEF tr on tr.ID=rc.ID " +
					"left join MASSPOINT_RELIEF mr on mr.ID=rc.ID " +
			"left join BREAKLINE_RELIEF br on br.ID=rc.ID where rf.ID=?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psReliefFeature = connection.prepareStatement("select rf.ID as RF_ID, rf.NAME as RF_NAME, rf.NAME_CODESPACE as RF_NAME_CODESPACE, rf.DESCRIPTION as RF_DESCRIPTION, rf.LOD as RF_LOD, " +
					"rc.ID as RC_ID, rc.NAME as RC_NAME, rc.NAME_CODESPACE as RC_NAME_CODESPACE, rc.DESCRIPTION as RC_DESCRIPTION, rc.LOD as RC_LOD, " +
					"geodb_util.transform_or_null(rc.EXTENT, " + srid + ") as RC_EXTENT, " +
					"tr.ID as TR_ID, tr.MAX_LENGTH as TR_MAX_LENGTH, " +
					"geodb_util.transform_or_null(tr.STOP_LINES, " + srid + ") as TR_STOP_LINES, " +
					"geodb_util.transform_or_null(tr.BREAK_LINES, " + srid + ") as TR_BREAK_LINES, " +
					"geodb_util.transform_or_null(tr.CONTROL_POINTS, " + srid + ") as TR_CONTROL_POINTS, " +
					"tr.SURFACE_GEOMETRY_ID as TR_SURFACE_GEOMETRY_ID, " +
					"mr.ID as MR_ID, " +
					"geodb_util.transform_or_null(mr.RELIEF_POINTS, " + srid + ") AS MR_RELIEF_POINTS, " +
					"br.ID as BR_ID, " +
					"geodb_util.transform_or_null(br.RIDGE_OR_VALLEY_LINES, " + srid + ") as BR_RIDGE_OR_VALLEY_LINES, " +
					"geodb_util.transform_or_null(br.BREAK_LINES, " + srid + ") as BR_BREAK_LINES " +
					"from RELIEF_FEATURE rf inner join RELIEF_FEAT_TO_REL_COMP rf2rc on rf2rc.RELIEF_FEATURE_ID=rf.ID inner join RELIEF_COMPONENT rc on rf2rc.RELIEF_COMPONENT_ID=rc.ID " +
					"left join TIN_RELIEF tr on tr.ID=rc.ID " +
					"left join MASSPOINT_RELIEF mr on mr.ID=rc.ID " +
			"left join BREAKLINE_RELIEF br on br.ID=rc.ID where rf.ID=?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		ReliefFeature reliefFeature = new ReliefFeatureImpl();
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
						StringOrRef stringOrRef = new StringOrRefImpl();
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
					reliefComponent = new TINReliefImpl();
				else if (massPointReliefId != 0)
					reliefComponent = new MassPointReliefImpl();
				else if (breaklineReliedId != 0)
					reliefComponent = new BreaklineReliefImpl();

				if (reliefComponent == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(reliefComponent, reliefComponentId, false);

				if (reliefComponent.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(reliefComponent.getId(), reliefComponentId, CityGMLClass.ABSTRACT_RELIEF_COMPONENT)) {
						if (useXLink) {
							ReliefComponentProperty property = new ReliefComponentPropertyImpl();
							property.setHref("#" + reliefComponent.getId());

							reliefFeature.addReliefComponent(property);
							continue;
						} else {
							String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + reliefComponent.getId();

							if (keepOldGmlId) {
								ExternalReference externalReference = new ExternalReferenceImpl();
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = new ExternalObjectImpl();
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
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					reliefComponent.setDescription(stringOrRef);
				}

				int lod = rs.getInt("RC_LOD");
				if (rs.wasNull())
					reliefComponent.setLod(0);
				else
					reliefComponent.setLod(lod);

				JGeometry extent = null;
				STRUCT extentObj = (STRUCT)rs.getObject("RC_EXTENT");
				if (!rs.wasNull() && extentObj != null) {
					extent = JGeometry.load(extentObj);

					PolygonProperty polygonProperty = sdoGeometry.getPolygonProperty(extent, false);
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

					JGeometry stopLines, breakLines, controlPoints;
					stopLines = breakLines = controlPoints = null;

					STRUCT stopLinesObj = (STRUCT)rs.getObject("TR_STOP_LINES");
					if (!rs.wasNull() && stopLinesObj != null)
						stopLines = JGeometry.load(stopLinesObj);

					STRUCT breakLinesObj = (STRUCT)rs.getObject("TR_BREAK_LINES");
					if (!rs.wasNull() && breakLinesObj != null)
						breakLines = JGeometry.load(breakLinesObj);

					STRUCT controlPointsObj = (STRUCT)rs.getObject("TR_CONTROL_POINTS");
					if (!rs.wasNull() && controlPointsObj != null)
						controlPoints = JGeometry.load(controlPointsObj);

					long surfaceGeometryId = rs.getLong("TR_SURFACE_GEOMETRY_ID");

					// check for invalid content
					if (maxLength == null && stopLines == null && breakLines == null && controlPoints == null && surfaceGeometryId == 0)
						continue;

					// check whether we deal with a gml:TrinagulatedSurface or a gml:Tin
					boolean isTin = false;
					if (maxLength != null || stopLines != null || breakLines != null || controlPoints != null)
						isTin = true;

					// get triangle patches
					TinProperty tinProperty = new TinPropertyImpl();
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
							triangulatedSurface = new TinImpl();
							triangulatedSurface.setTrianglePatches(patches);
						} else {
							triangulatedSurface = new TinImpl();
							triangulatedSurface.setTrianglePatches(new TrianglePatchArrayPropertyImpl());
						}
					}

					tinProperty.setObject(triangulatedSurface);
					tinRelief.setTin(tinProperty);

					// finally, check gml:Tin specific content
					if (isTin) {
						Tin tin = (Tin)triangulatedSurface;

						if (maxLength != null) {
							Length length = new LengthImpl();
							length.setValue(maxLength);
							length.setUom("urn:ogc:def:uom:UCUM::m");
							tin.setMaxLength(length);
						}

						if (stopLines != null) {
							List<LineStringSegmentArrayProperty> arrayPropertyList = sdoGeometry.getListOfLineStringSegmentArrayProperty(stopLines, false);
							if (arrayPropertyList != null)
								tin.setStopLines(arrayPropertyList);
						}

						if (breakLines != null) {
							List<LineStringSegmentArrayProperty> arrayPropertyList = sdoGeometry.getListOfLineStringSegmentArrayProperty(breakLines, false);
							if (arrayPropertyList != null)
								tin.setBreakLines(arrayPropertyList);
						}

						if (controlPoints != null) {
							ControlPoint controlPoint = sdoGeometry.getControlPoint(controlPoints, false);
							if (controlPoint != null)
								tin.setControlPoint(controlPoint);
						}
					}
				}

				else if (reliefComponent.getCityGMLClass() == CityGMLClass.MASSPOINT_RELIEF) {
					MassPointRelief massPointRelief = (MassPointRelief)reliefComponent;

					JGeometry reliefPoints = null;				
					STRUCT reliefPointsObj = (STRUCT)rs.getObject("MR_RELIEF_POINTS");
					if (!rs.wasNull() && reliefPointsObj != null)
						reliefPoints = JGeometry.load(reliefPointsObj);

					if (reliefPoints != null) {
						MultiPointProperty multiPointProperty = sdoGeometry.getMultiPointProperty(reliefPoints, false);
						if (multiPointProperty != null)
							massPointRelief.setReliefPoints(multiPointProperty);
					}
				}

				else if (reliefComponent.getCityGMLClass() == CityGMLClass.BREAKLINE_RELIEF) {
					BreaklineRelief breaklineRelief = (BreaklineRelief)reliefComponent;

					JGeometry ridgeOrValleyLines, breakLines;
					ridgeOrValleyLines = breakLines = null;

					STRUCT ridgeOrValleyLinesObj = (STRUCT)rs.getObject("BR_RIDGE_OR_VALLEY_LINES");
					if (!rs.wasNull() && ridgeOrValleyLinesObj != null)
						ridgeOrValleyLines = JGeometry.load(ridgeOrValleyLinesObj);

					STRUCT breakLinesObj = (STRUCT)rs.getObject("BR_BREAK_LINES");
					if (!rs.wasNull() && breakLinesObj != null)
						breakLines = JGeometry.load(breakLinesObj);

					if (ridgeOrValleyLines != null) {
						MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(ridgeOrValleyLines, false);
						if (multiCurveProperty != null)					
							breaklineRelief.setRidgeOrValleyLines(multiCurveProperty);
					}

					if (breakLines != null) {
						MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(breakLines, false);
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
				ReliefComponentProperty property = new ReliefComponentPropertyImpl();
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
