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
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.operation.exporter.util.SplitValue;
import org.citydb.core.operation.exporter.util.GeometrySetter;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBBridgeFurniture extends AbstractFeatureExporter<BridgeFurniture> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final GMLConverter gmlConverter;

	private final String bridgeModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private final List<Table> adeHookTables;

	public DBBridgeFurniture(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(BridgeFurniture.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_FURNITURE.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.BRIDGE_FURNITURE.getName(), schema);
		select = addProjection(new Select(), table, projectionFilter, "");
		adeHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_FURNITURE, table);
		
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Select addProjection(Select select, Table table, CombinedProjectionFilter projectionFilter, String prefix) {
		select.addProjection(table.getColumn("id", prefix + "id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id", prefix + "objectclass_id"));
		if (projectionFilter.containsProperty("class", bridgeModule))
			select.addProjection(table.getColumn("class", prefix + "class"), table.getColumn("class_codespace", prefix + "class_codespace"));
		if (projectionFilter.containsProperty("function", bridgeModule))
			select.addProjection(table.getColumn("function", prefix + "function"), table.getColumn("function_codespace", prefix + "function_codespace"));
		if (projectionFilter.containsProperty("usage", bridgeModule))
			select.addProjection(table.getColumn("usage", prefix + "usage"), table.getColumn("usage_codespace", prefix + "usage_codespace"));
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4Geometry", bridgeModule))
				select.addProjection(table.getColumn("lod4_brep_id", prefix + "lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom"), prefix + "lod4_other_geom"));
			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", bridgeModule)) {
				select.addProjection(table.getColumn("lod4_implicit_rep_id", prefix + "lod4_implicit_rep_id"),
						exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point"), prefix + "lod4_implicit_ref_point"),
						table.getColumn("lod4_implicit_transformation", prefix + "lod4_implicit_transformation"));
			}
		}

		return select;
	}

	protected Collection<BridgeFurniture> doExport(BridgeRoom parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("bridge_room_id"));
	}
	
	@Override
	protected Collection<BridgeFurniture> doExport(long id, BridgeFurniture root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<BridgeFurniture> bridgeFurnitures = new ArrayList<>();
			
			while (rs.next()) {
				long bridgeFurnitureId = rs.getLong("id");
				BridgeFurniture bridgeFurniture;
				FeatureType featureType;
				
				if (bridgeFurnitureId == id && root != null) {
					bridgeFurniture = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						// create bridge furniture object
						int objectClassId = rs.getInt("objectclass_id");
						bridgeFurniture = exporter.createObject(objectClassId, BridgeFurniture.class);
						if (bridgeFurniture == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, bridgeFurnitureId) + " as bridge furniture object.");
							continue;
						}

						featureType = exporter.getFeatureType(objectClassId);
					} else {
						bridgeFurniture = new BridgeFurniture();
						featureType = exporter.getFeatureType(bridgeFurniture);
					}
				}
				
				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				doExport(bridgeFurniture, bridgeFurnitureId, featureType, projectionFilter, "", adeHookTables, rs);
				bridgeFurnitures.add(bridgeFurniture);
			}
			
			return bridgeFurnitures;
		}
	}

	protected BridgeFurniture doExport(long id, FeatureType featureType, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		BridgeFurniture bridgeFurniture = null;
		if (featureType != null) {
			bridgeFurniture = exporter.createObject(featureType.getObjectClassId(), BridgeFurniture.class);
			if (bridgeFurniture != null)
				doExport(bridgeFurniture, id, featureType, exporter.getProjectionFilter(featureType), prefix, adeHookTables, rs);
		}

		return bridgeFurniture;
	}

	private void doExport(BridgeFurniture object, long id, FeatureType featureType, ProjectionFilter projectionFilter, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		// export city object information
		cityObjectExporter.addBatch(object, id, featureType, projectionFilter);

		if (projectionFilter.containsProperty("class", bridgeModule)) {
			String clazz = rs.getString("class");
			if (!rs.wasNull()) {
				Code code = new Code(clazz);
				code.setCodeSpace(rs.getString(prefix + "class_codespace"));
				object.setClazz(code);
			}
		}

		if (projectionFilter.containsProperty("function", bridgeModule)) {
			for (SplitValue splitValue : valueSplitter.split(rs.getString(prefix + "function"), rs.getString(prefix + "function_codespace"))) {
				Code function = new Code(splitValue.result(0));
				function.setCodeSpace(splitValue.result(1));
				object.addFunction(function);
			}
		}

		if (projectionFilter.containsProperty("usage", bridgeModule)) {
			for (SplitValue splitValue : valueSplitter.split(rs.getString(prefix + "usage"), rs.getString(prefix + "usage_codespace"))) {
				Code usage = new Code(splitValue.result(0));
				usage.setCodeSpace(splitValue.result(1));
				object.addUsage(usage);
			}
		}

		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4Geometry", bridgeModule)) {
				long geometryId = rs.getLong(prefix + "lod4_brep_id");
				if (!rs.wasNull())
					geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) object::setLod4Geometry);
				else {
					Object geometryObj = rs.getObject(prefix + "lod4_other_geom");
					if (!rs.wasNull()) {
						GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
						if (geometry != null) {
							GeometryProperty<AbstractGeometry> property = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
							object.setLod4Geometry(property);
						}
					}
				}
			}

			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", bridgeModule)) {
				long implicitGeometryId = rs.getLong(prefix + "lod4_implicit_rep_id");
				if (!rs.wasNull()) {
					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(prefix + "lod4_implicit_ref_point");
					if (!rs.wasNull())
						referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(prefix + "lod4_implicit_transformation");

					ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);
						object.setLod4ImplicitRepresentation(implicitProperty);
					}
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
