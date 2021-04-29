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
package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBTrafficArea extends AbstractFeatureExporter<AbstractTransportationObject> {
	private final String transportationModule;
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;

	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final List<Table> adeHookTables;

	public DBTrafficArea(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractTransportationObject.class, connection, exporter);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TRAFFIC_AREA.getName());
		transportationModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.TRAFFIC_AREA.getName(), schema);
		select = addProjection(new Select(), table, projectionFilter, "");
		adeHookTables = addJoinsToADEHookTables(TableEnum.TRAFFIC_AREA, table);
	}

	protected Select addProjection(Select select, Table table, CombinedProjectionFilter projectionFilter, String prefix) {
		select.addProjection(table.getColumn("id", prefix + "id"), table.getColumn("objectclass_id", prefix + "objectclass_id"));
		if (projectionFilter.containsProperty("class", transportationModule))
			select.addProjection(table.getColumn("class", prefix + "class"), table.getColumn("class_codespace", prefix + "class_codespace"));
		if (projectionFilter.containsProperty("function", transportationModule))
			select.addProjection(table.getColumn("function", prefix + "function"), table.getColumn("function_codespace", prefix + "function_codespace"));
		if (projectionFilter.containsProperty("usage", transportationModule))
			select.addProjection(table.getColumn("usage", prefix + "usage"), table.getColumn("usage_codespace", prefix + "usage_codespace"));
		if (projectionFilter.containsProperty("surfaceMaterial", transportationModule))
			select.addProjection(table.getColumn("surface_material", prefix + "surface_material"), table.getColumn("surface_material_codespace", prefix + "surface_material_codespace"));
		if (lodFilter.isEnabled(2) && projectionFilter.containsProperty("lod2MultiSurface", transportationModule))
			select.addProjection(table.getColumn("lod2_multi_surface_id", prefix + "lod2_multi_surface_id"));
		if (lodFilter.isEnabled(3) && projectionFilter.containsProperty("lod3MultiSurface", transportationModule))
			select.addProjection(table.getColumn("lod3_multi_surface_id", prefix + "lod3_multi_surface_id"));
		if (lodFilter.isEnabled(4) && projectionFilter.containsProperty("lod4MultiSurface", transportationModule))
			select.addProjection(table.getColumn("lod4_multi_surface_id", prefix + "lod4_multi_surface_id"));

		return select;
	}

	@Override
	protected Collection<AbstractTransportationObject> doExport(long id, AbstractTransportationObject root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<AbstractTransportationObject> transportationObjects = new ArrayList<>();

			while (rs.next()) {
				long transportationObjectId = 0;
				AbstractTransportationObject transportationObject;
				FeatureType featureType;

				if (transportationObjectId == id && root != null) {
					transportationObject = root;
					featureType = rootType;
				} else {
					// create transportation object
					int objectClassId = rs.getInt("objectclass_id");
					transportationObject = exporter.createObject(objectClassId, AbstractTransportationObject.class);
					if (transportationObject == null || transportationObject instanceof TransportationComplex) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, transportationObjectId) + " as transportation object.");
						continue;
					}

					featureType = exporter.getFeatureType(objectClassId);
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				doExport(transportationObject, transportationObjectId, featureType, projectionFilter, "", adeHookTables, rs);
				transportationObjects.add(transportationObject);
			}

			return transportationObjects;
		}
	}

	protected AbstractTransportationObject doExport(long id, FeatureType featureType, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		AbstractTransportationObject transportationObject = null;
		if (featureType != null) {
			transportationObject = exporter.createObject(featureType.getObjectClassId(), AbstractTransportationObject.class);
			if (transportationObject != null && !(transportationObject instanceof TransportationComplex))
				doExport(transportationObject, id, featureType, exporter.getProjectionFilter(featureType), prefix, adeHookTables, rs);
		}

		return transportationObject;
	}

	private void doExport(AbstractTransportationObject object, long id, FeatureType featureType, ProjectionFilter projectionFilter, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		// export city object information
		cityObjectExporter.addBatch(object, id, featureType, projectionFilter);

		boolean isTrafficArea = object instanceof TrafficArea;

		if (projectionFilter.containsProperty("class", transportationModule)) {
			String clazz = rs.getString(prefix + "class");
			if (!rs.wasNull()) {
				Code code = new Code(clazz);
				code.setCodeSpace(rs.getString(prefix + "class_codespace"));

				if (isTrafficArea)
					((TrafficArea) object).setClazz(code);
				else
					((AuxiliaryTrafficArea) object).setClazz(code);
			}
		}

		if (projectionFilter.containsProperty("function", transportationModule)) {
			for (SplitValue splitValue : valueSplitter.split(rs.getString(prefix + "function"), rs.getString(prefix + "function_codespace"))) {
				Code function = new Code(splitValue.result(0));
				function.setCodeSpace(splitValue.result(1));

				if (isTrafficArea)
					((TrafficArea) object).addFunction(function);
				else
					((AuxiliaryTrafficArea) object).addFunction(function);
			}
		}

		if (projectionFilter.containsProperty("usage", transportationModule)) {
			for (SplitValue splitValue : valueSplitter.split(rs.getString(prefix + "usage"), rs.getString(prefix + "usage_codespace"))) {
				Code usage = new Code(splitValue.result(0));
				usage.setCodeSpace(splitValue.result(1));

				if (isTrafficArea)
					((TrafficArea) object).addUsage(usage);
				else
					((AuxiliaryTrafficArea) object).addUsage(usage);
			}
		}

		if (projectionFilter.containsProperty("surfaceMaterial", transportationModule)) {
			String clazz = rs.getString(prefix + "surface_material");
			if (!rs.wasNull()) {
				Code code = new Code(clazz);
				code.setCodeSpace(rs.getString(prefix + "surface_material_codespace"));

				if (isTrafficArea)
					((TrafficArea) object).setSurfaceMaterial(code);
				else
					((AuxiliaryTrafficArea) object).setSurfaceMaterial(code);
			}
		}

		LodIterator lodIterator = lodFilter.iterator(2, 4);
		while (lodIterator.hasNext()) {
			int lod = lodIterator.next();

			if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", transportationModule))
				continue;

			long geometryId = rs.getLong(prefix + "lod" + lod + "_multi_surface_id");
			if (rs.wasNull())
				continue;

			if (isTrafficArea) {
				TrafficArea trafficArea = (TrafficArea) object;
				switch (lod) {
					case 2:
						geometryExporter.addBatch(geometryId, trafficArea::setLod2MultiSurface);
						break;
					case 3:
						geometryExporter.addBatch(geometryId, trafficArea::setLod3MultiSurface);
						break;
					case 4:
						geometryExporter.addBatch(geometryId, trafficArea::setLod4MultiSurface);
						break;
				}
			} else {
				AuxiliaryTrafficArea auxiliaryTrafficArea = (AuxiliaryTrafficArea) object;
				switch (lod) {
					case 2:
						geometryExporter.addBatch(geometryId, auxiliaryTrafficArea::setLod2MultiSurface);
						break;
					case 3:
						geometryExporter.addBatch(geometryId, auxiliaryTrafficArea::setLod3MultiSurface);
						break;
					case 4:
						geometryExporter.addBatch(geometryId, auxiliaryTrafficArea::setLod4MultiSurface);
						break;
				}
			}
		}

		// delegate export of generic ADE properties
		if (adeHookTables != null) {
			List<String> tableNames = retrieveADEHookTables(adeHookTables, rs);
			if (tableNames != null)
				exporter.delegateToADEExporter(tableNames, object, id, featureType, projectionFilter);
		}
	}
}
