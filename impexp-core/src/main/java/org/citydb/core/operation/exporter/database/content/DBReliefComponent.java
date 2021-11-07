/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.exporter.database.content;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.relief.*;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBReliefComponent extends AbstractFeatureExporter<AbstractReliefComponent> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final GMLConverter gmlConverter;

	private final String reliefModule;
	private final List<Table> adeHookTables;

	public DBReliefComponent(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractReliefComponent.class, connection, exporter);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		gmlConverter = exporter.getGMLConverter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.RELIEF_COMPONENT.getName());
		reliefModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.RELIEF).getNamespaceURI();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.RELIEF_COMPONENT.getName(), schema);
		select = addProjection(new Select(), table, projectionFilter, "");
		adeHookTables = addJoinsToADEHookTables(TableEnum.RELIEF_COMPONENT, table);
	}

	protected Select addProjection(Select select, Table table, CombinedProjectionFilter projectionFilter, String prefix) {
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		select.addProjection(table.getColumn("id", prefix + "id"), table.getColumn("objectclass_id", prefix + "objectclass_id"),
				table.getColumn("lod", prefix + "lod"));
		if (projectionFilter.containsProperty("extent", reliefModule))
			select.addProjection(table.getColumn("extent", prefix + "extent"));
		if (projectionFilter.containsProperty("tin", reliefModule)) {
			Table tinRelief = new Table(TableEnum.TIN_RELIEF.getName(), schema);
			select.addProjection(tinRelief.getColumn("max_length", prefix + "max_length"),
					tinRelief.getColumn("max_length_unit", prefix + "max_length_unit"),
					tinRelief.getColumn("surface_geometry_id", prefix + "surface_geometry_id"),
					exporter.getGeometryColumn(tinRelief.getColumn("stop_lines"), prefix + "stop_lines"),
					exporter.getGeometryColumn(tinRelief.getColumn("break_lines"), prefix + "break_lines"),
					exporter.getGeometryColumn(tinRelief.getColumn("control_points"), prefix + "control_points"))
					.addJoin(JoinFactory.left(tinRelief, "id", ComparisonName.EQUAL_TO, table.getColumn("id")));
		}
		if (projectionFilter.containsProperty("reliefPoints", reliefModule)) {
			Table massPointRelief = new Table(TableEnum.MASSPOINT_RELIEF.getName(), schema);
			select.addProjection(exporter.getGeometryColumn(massPointRelief.getColumn("relief_points")))
					.addJoin(JoinFactory.left(massPointRelief, "id", ComparisonName.EQUAL_TO, table.getColumn("id")));
		}
		if (projectionFilter.containsProperty("ridgeOrValleyLines", reliefModule)
				|| projectionFilter.containsProperty("breaklines", reliefModule)) {
			Table breakLineRelief = new Table(TableEnum.BREAKLINE_RELIEF.getName(), schema);
			select.addJoin(JoinFactory.left(breakLineRelief, "id", ComparisonName.EQUAL_TO, table.getColumn("id")));
			if (projectionFilter.containsProperty("ridgeOrValleyLines", reliefModule))
				select.addProjection(exporter.getGeometryColumn(breakLineRelief.getColumn("ridge_or_valley_lines")));
			if (projectionFilter.containsProperty("breaklines", reliefModule))
				select.addProjection(exporter.getGeometryColumn(breakLineRelief.getColumn("break_lines")));
		}

		return select;
	}

	@Override
	protected Collection<AbstractReliefComponent> doExport(long id, AbstractReliefComponent root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<AbstractReliefComponent> components = new ArrayList<>();

			while (rs.next()) {	
				long componentId = rs.getLong("id");
				AbstractReliefComponent component;
				FeatureType featureType;

				if (componentId == id & root != null) {
					component = root;
					featureType = rootType;
				} else {
					// create relief component object
					int objectClassId = rs.getInt("objectclass_id");
					component = exporter.createObject(objectClassId, AbstractReliefComponent.class);
					if (component == null) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, componentId) + " as relief component object.");
						continue;
					}

					featureType = exporter.getFeatureType(objectClassId);
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				doExport(component, componentId, featureType, projectionFilter, "", adeHookTables, rs);
				components.add(component);
			}

			return components;
		}
	}

	protected AbstractReliefComponent doExport(long id, FeatureType featureType, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		AbstractReliefComponent component = null;
		if (featureType != null) {
			component = exporter.createObject(featureType.getObjectClassId(), AbstractReliefComponent.class);
			if (component != null)
				doExport(component, id, featureType, exporter.getProjectionFilter(featureType), prefix, adeHookTables, rs);
		}

		return component;
	}

	private void doExport(AbstractReliefComponent component, long id, FeatureType featureType, ProjectionFilter projectionFilter, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		// export city object information
		cityObjectExporter.addBatch(component, id, featureType, projectionFilter);

		component.setLod(rs.getInt(prefix + "lod"));

		if (projectionFilter.containsProperty("extent", reliefModule)) {
			Object extentObj = rs.getObject(prefix + "extent");
			if (!rs.wasNull()) {
				GeometryObject extent = exporter.getDatabaseAdapter().getGeometryConverter().getPolygon(extentObj);
				if (extent != null)
					component.setExtent(gmlConverter.getPolygonProperty(extent, false));
			}
		}

		// retrieve further content according to the component types
		if (component instanceof TINRelief && projectionFilter.containsProperty("tin", reliefModule)) {
			TINRelief tinRelief = (TINRelief)component;

			long geometryId = rs.getLong(prefix + "surface_geometry_id");
			if (rs.wasNull())
				return;

			Double maxLength = rs.getDouble(prefix + "max_length");
			if (rs.wasNull())
				maxLength = null;

			GeometryObject stopLines = null;
			Object stopLinesObj = rs.getObject(prefix + "stop_lines");
			if (!rs.wasNull())
				stopLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(stopLinesObj);

			GeometryObject breakLines = null;
			Object breakLinesObj = rs.getObject(prefix + "break_lines");
			if (!rs.wasNull())
				breakLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);

			GeometryObject controlPoints = null;
			Object controlPointsObj = rs.getObject(prefix + "control_points");
			if (!rs.wasNull())
				controlPoints = exporter.getDatabaseAdapter().getGeometryConverter().getMultiPoint(controlPointsObj);

			// check whether we deal with a gml:Tin
			if (maxLength != null || stopLines != null || breakLines != null || controlPoints != null) {
				// control points are mandatory
				if (controlPoints == null)
					return;

				// get triangulated surface
				SurfaceGeometry geometry = geometryExporter.doExport(geometryId);
				if (geometry == null || geometry.getType() != GMLClass.TRIANGULATED_SURFACE || !geometry.isSetGeometry())
					return;

				Tin tin = new Tin();
				tin.setTrianglePatches(((TriangulatedSurface) geometry.getGeometry()).getTrianglePatches());

				if (maxLength != null) {
					Length length = new Length(maxLength);
					length.setUom(rs.getString(prefix + "max_length_unit"));
					tin.setMaxLength(length);
				}

				if (stopLines != null) {
					List<LineStringSegmentArrayProperty> property = gmlConverter.getListOfLineStringSegmentArrayProperty(stopLines, false);
					if (property != null)
						tin.setStopLines(property);
				}

				if (breakLines != null) {
					List<LineStringSegmentArrayProperty> property = gmlConverter.getListOfLineStringSegmentArrayProperty(breakLines, false);
					if (property != null)
						tin.setBreakLines(property);
				}

				tin.setControlPoint(gmlConverter.getControlPoint(controlPoints, false));
				tinRelief.setTin(new TinProperty(tin));
			} else
				geometryExporter.addBatch(geometryId, tinRelief::setTin);
		} else if (component instanceof MassPointRelief && projectionFilter.containsProperty("reliefPoints", reliefModule)) {
			MassPointRelief massPointRelief = (MassPointRelief)component;

			Object reliefPointsObj = rs.getObject(prefix + "relief_points");
			if (!rs.wasNull()) {
				GeometryObject reliefPoints = exporter.getDatabaseAdapter().getGeometryConverter().getMultiPoint(reliefPointsObj);
				if (reliefPoints != null)
					massPointRelief.setReliefPoints(gmlConverter.getMultiPointProperty(reliefPoints, false));
			}
		} else if (component instanceof BreaklineRelief) {
			BreaklineRelief breaklineRelief = (BreaklineRelief)component;

			if (projectionFilter.containsProperty("ridgeOrValleyLines", reliefModule)) {
				Object ridgeOrValleyLinesObj = rs.getObject(prefix + "ridge_or_valley_lines");
				if (!rs.wasNull()) {
					GeometryObject ridgeOrValleyLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(ridgeOrValleyLinesObj);
					if (ridgeOrValleyLines != null)
						breaklineRelief.setRidgeOrValleyLines(gmlConverter.getMultiCurveProperty(ridgeOrValleyLines, false));
				}
			}

			if (projectionFilter.containsProperty("breaklines", reliefModule)) {
				Object breakLinesObj = rs.getObject(prefix + "break_lines");
				if (!rs.wasNull()) {
					GeometryObject breakLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);
					if (breakLines != null)
						breaklineRelief.setBreaklines(gmlConverter.getMultiCurveProperty(breakLines, false));
				}
			}
		} else if (component instanceof RasterRelief)
			exporter.logOrThrowErrorMessage(exporter.getObjectSignature(featureType, id) + ": Raster reliefs are not supported.");

		// delegate export of generic ADE properties
		if (adeHookTables != null) {
			List<String> tableNames = retrieveADEHookTables(adeHookTables, rs);
			if (tableNames != null)
				exporter.delegateToADEExporter(tableNames, component, id, featureType, projectionFilter);
		}
	}
}
