/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.citygml.exporter.util.GeometrySetter;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
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

public class DBTunnelFurniture extends AbstractFeatureExporter<TunnelFurniture> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final GMLConverter gmlConverter;

	private final String tunnelModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private final List<Table> adeHookTables;

	public DBTunnelFurniture(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(TunnelFurniture.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_FURNITURE.getName());
		tunnelModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TUNNEL).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.TUNNEL_FURNITURE.getName(), schema);
		select = addProjection(new Select(), table, projectionFilter, "");
		adeHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_FURNITURE, table);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Select addProjection(Select select, Table table, CombinedProjectionFilter projectionFilter, String prefix) {
		select.addProjection(table.getColumn("id", prefix + "id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id", prefix + "objectclass_id"));
		if (projectionFilter.containsProperty("class", tunnelModule))
			select.addProjection(table.getColumn("class", prefix + "class"), table.getColumn("class_codespace", prefix + "class_codespace"));
		if (projectionFilter.containsProperty("function", tunnelModule))
			select.addProjection(table.getColumn("function", prefix + "function"), table.getColumn("function_codespace", prefix + "function_codespace"));
		if (projectionFilter.containsProperty("usage", tunnelModule))
			select.addProjection(table.getColumn("usage", prefix + "usage"), table.getColumn("usage_codespace", prefix + "usage_codespace"));
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4Geometry", tunnelModule))
				select.addProjection(table.getColumn("lod4_brep_id", prefix + "lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom"), prefix + "lod4_other_geom"));
			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", tunnelModule)) {
				select.addProjection(table.getColumn("lod4_implicit_rep_id", prefix + "lod4_implicit_rep_id"),
						exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point"), prefix + "lod4_implicit_ref_point"),
						table.getColumn("lod4_implicit_transformation", prefix + "lod4_implicit_transformation"));
			}
		}

		return select;
	}

	protected Collection<TunnelFurniture> doExport(HollowSpace parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("tunnel_hollow_space_id"));
	}

	@Override
	protected Collection<TunnelFurniture> doExport(long id, TunnelFurniture root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<TunnelFurniture> tunnelFurnitures = new ArrayList<>();

			while (rs.next()) {
				long tunnelFurnitureId = rs.getLong("id");
				TunnelFurniture tunnelFurniture;
				FeatureType featureType;

				if (tunnelFurnitureId == id && root != null) {
					tunnelFurniture = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						// create tunnel furniture object
						int objectClassId = rs.getInt("objectclass_id");
						tunnelFurniture = exporter.createObject(objectClassId, TunnelFurniture.class);
						if (tunnelFurniture == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, tunnelFurnitureId) + " as tunnel furniture object.");
							continue;
						}

						featureType = exporter.getFeatureType(objectClassId);
					} else {
						tunnelFurniture = new TunnelFurniture();
						featureType = exporter.getFeatureType(tunnelFurniture);
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				doExport(tunnelFurniture, tunnelFurnitureId, featureType, projectionFilter, "", adeHookTables, rs);
				tunnelFurnitures.add(tunnelFurniture);
			}

			return tunnelFurnitures;
		}
	}

	protected TunnelFurniture doExport(long id, FeatureType featureType, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		TunnelFurniture tunnelFurniture = null;
		if (featureType != null) {
			tunnelFurniture = exporter.createObject(featureType.getObjectClassId(), TunnelFurniture.class);
			if (tunnelFurniture != null)
				doExport(tunnelFurniture, id, featureType, exporter.getProjectionFilter(featureType), prefix, adeHookTables, rs);
		}

		return tunnelFurniture;
	}

	private void doExport(TunnelFurniture object, long id, FeatureType featureType, ProjectionFilter projectionFilter, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		// export city object information
		cityObjectExporter.addBatch(object, id, featureType, projectionFilter);

		if (projectionFilter.containsProperty("class", tunnelModule)) {
			String clazz = rs.getString(prefix + "class");
			if (!rs.wasNull()) {
				Code code = new Code(clazz);
				code.setCodeSpace(rs.getString(prefix + "class_codespace"));
				object.setClazz(code);
			}
		}

		if (projectionFilter.containsProperty("function", tunnelModule)) {
			for (SplitValue splitValue : valueSplitter.split(rs.getString(prefix + "function"), rs.getString(prefix + "function_codespace"))) {
				Code function = new Code(splitValue.result(0));
				function.setCodeSpace(splitValue.result(1));
				object.addFunction(function);
			}
		}

		if (projectionFilter.containsProperty("usage", tunnelModule)) {
			for (SplitValue splitValue : valueSplitter.split(rs.getString(prefix + "usage"), rs.getString(prefix + "usage_codespace"))) {
				Code usage = new Code(splitValue.result(0));
				usage.setCodeSpace(splitValue.result(1));
				object.addUsage(usage);
			}
		}

		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4Geometry", tunnelModule)) {
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

			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", tunnelModule)) {
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
