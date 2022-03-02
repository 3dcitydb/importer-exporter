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
import org.citydb.core.operation.exporter.util.DefaultGeometrySetterHandler;
import org.citydb.core.operation.exporter.util.GeometrySetterHandler;
import org.citydb.core.operation.exporter.util.SplitValue;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.lod.LodIterator;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.FetchToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.projection.ColumnExpression;
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class DBBuilding extends AbstractFeatureExporter<AbstractBuilding> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBThematicSurface thematicSurfaceExporter;
	private final DBOpening openingExporter;
	private final DBBuildingInstallation buildingInstallationExporter;
	private final DBRoom roomExporter;
	private final DBAddress addressExporter;
	private final GMLConverter gmlConverter;

	private final String buildingModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private final boolean useXLink;
	private final List<Table> buildingADEHookTables;
	private List<Table> addressADEHookTables;
	private List<Table> surfaceADEHookTables;
	private List<Table> openingADEHookTables;
	private List<Table> openingAddressADEHookTables;

	public DBBuilding(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractBuilding.class, connection, exporter);

		thematicSurfaceExporter = exporter.getExporter(DBThematicSurface.class);
		openingExporter = exporter.getExporter(DBOpening.class);
		buildingInstallationExporter = exporter.getExporter(DBBuildingInstallation.class);
		roomExporter = exporter.getExporter(DBRoom.class);
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		addressExporter = exporter.getExporter(DBAddress.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BUILDING.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();		
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		useXLink = exporter.getInternalConfig().isExportFeatureReferences();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.BUILDING.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("building_parent_id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", buildingModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", buildingModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", buildingModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("yearOfConstruction", buildingModule)) select.addProjection(table.getColumn("year_of_construction"));
		if (projectionFilter.containsProperty("yearOfDemolition", buildingModule)) select.addProjection(table.getColumn("year_of_demolition"));
		if (projectionFilter.containsProperty("roofType", buildingModule)) select.addProjection(table.getColumn("roof_type"), table.getColumn("roof_type_codespace"));
		if (projectionFilter.containsProperty("measuredHeight", buildingModule)) select.addProjection(table.getColumn("measured_height"), table.getColumn("measured_height_unit"));
		if (projectionFilter.containsProperty("storeysAboveGround", buildingModule)) select.addProjection(table.getColumn("storeys_above_ground"));
		if (projectionFilter.containsProperty("storeysBelowGround", buildingModule)) select.addProjection(table.getColumn("storeys_below_ground"));
		if (projectionFilter.containsProperty("storeyHeightsAboveGround", buildingModule)) select.addProjection(table.getColumn("storey_heights_above_ground"), table.getColumn("storey_heights_ag_unit"));
		if (projectionFilter.containsProperty("storeyHeightsBelowGround", buildingModule)) select.addProjection(table.getColumn("storey_heights_below_ground"), table.getColumn("storey_heights_bg_unit"));
		if (lodFilter.isEnabled(0)) {
			if (projectionFilter.containsProperty("lod0FootPrint", buildingModule)) select.addProjection(table.getColumn("lod0_footprint_id"));
			if (projectionFilter.containsProperty("lod0RoofEdge", buildingModule)) select.addProjection(table.getColumn("lod0_roofprint_id"));
		}
		if (lodFilter.isEnabled(1)) {
			if (projectionFilter.containsProperty("lod1TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
			if (projectionFilter.containsProperty("lod1Solid", buildingModule)) select.addProjection(table.getColumn("lod1_solid_id"));
			if (projectionFilter.containsProperty("lod1MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
		}
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
			if (projectionFilter.containsProperty("lod2MultiCurve", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_multi_curve")));
			if (projectionFilter.containsProperty("lod2Solid", buildingModule)) select.addProjection(table.getColumn("lod2_solid_id"));
			if (projectionFilter.containsProperty("lod2MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
			if (projectionFilter.containsProperty("lod3MultiCurve", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_multi_curve")));
			if (projectionFilter.containsProperty("lod3Solid", buildingModule)) select.addProjection(table.getColumn("lod3_solid_id"));
			if (projectionFilter.containsProperty("lod3MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
			if (projectionFilter.containsProperty("lod4MultiCurve", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_multi_curve")));
			if (projectionFilter.containsProperty("lod4Solid", buildingModule)) select.addProjection(table.getColumn("lod4_solid_id"));
			if (projectionFilter.containsProperty("lod4MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		}
		if (projectionFilter.containsProperty("address", buildingModule)) {
			Table address = new Table(TableEnum.ADDRESS.getName(), schema);
			Table addressToBuilding = new Table(TableEnum.ADDRESS_TO_BUILDING.getName(), schema);
			addressExporter.addProjection(select, address, "ba")
					.addJoin(JoinFactory.left(addressToBuilding, "building_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
					.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, addressToBuilding.getColumn("address_id")));

			addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
		}
		if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
				&& projectionFilter.containsProperty("boundedBy", buildingModule)) {
			CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.THEMATIC_SURFACE.getName());
			Table thematicSurface = new Table(TableEnum.THEMATIC_SURFACE.getName(), schema);
			thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
					.addJoin(JoinFactory.left(thematicSurface, "building_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
			if (lodFilter.containsLodGreaterThanOrEuqalTo(3)
					&& boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule)) {
				CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.OPENING.getName());
				Table opening = new Table(TableEnum.OPENING.getName(), schema);
				Table openingToThemSurface = new Table(TableEnum.OPENING_TO_THEM_SURFACE.getName(), schema);
				Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
				openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
						.addProjection(cityObject.getColumn("gmlid", "opgmlid"))
						.addJoin(JoinFactory.left(openingToThemSurface, "thematic_surface_id", ComparisonName.EQUAL_TO, thematicSurface.getColumn("id")))
						.addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("opening_id")))
						.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
				if (openingProjectionFilter.containsProperty("address", buildingModule)) {
					Table openingAddress = new Table(TableEnum.ADDRESS.getName(), schema);
					addressExporter.addProjection(select, openingAddress, "oa")
							.addJoin(JoinFactory.left(openingAddress, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")));
					openingAddressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, openingAddress);
				}
				openingADEHookTables = addJoinsToADEHookTables(TableEnum.OPENING, opening);
			}
			surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.THEMATIC_SURFACE, thematicSurface);
		}
		if (lodFilter.containsLodGreaterThanOrEuqalTo(2) &&
				(projectionFilter.containsProperty("outerBuildingInstallation", buildingModule)
						|| projectionFilter.containsProperty("interiorBuildingInstallation", buildingModule))) {
			Table installation = new Table(TableEnum.BUILDING_INSTALLATION.getName(), schema);
			select.addProjection(new ColumnExpression(new Select()
					.addProjection(installation.getColumn("id"))
					.addSelection(ComparisonFactory.equalTo(installation.getColumn("building_id"), table.getColumn("id")))
					.withFetch(new FetchToken(1)), "inid"));
		}
		if (lodFilter.isEnabled(4) &&
				projectionFilter.containsProperty("interiorRoom", buildingModule)) {
			Table room = new Table(TableEnum.ROOM.getName(), schema);
			select.addProjection(new ColumnExpression(new Select()
					.addProjection(room.getColumn("id"))
					.addSelection(ComparisonFactory.equalTo(room.getColumn("building_id"), table.getColumn("id")))
					.withFetch(new FetchToken(1)), "roid"));
		}
		buildingADEHookTables = addJoinsToADEHookTables(TableEnum.BUILDING, table);
	}

	@Override
	protected boolean doExport(AbstractBuilding object, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
		String column = projectionFilter.containsProperty("consistsOfBuildingPart", buildingModule) ? "building_root_id" : "id";
		return !doExport(id, object, featureType, getOrCreateStatement(column)).isEmpty();
	}

	@Override
	protected Collection<AbstractBuilding> doExport(long id, AbstractBuilding root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
	//	 ps = connection.prepareStatement("select id, 26 as objectclass_id, roof_type, measured_Height from building where id = ? ");

		ps = connection.prepareStatement("SELECT f.id, 26 as objectclass_id, attr3.roofType as roof_type, attr.height as measured_height FROM feature AS f LEFT JOIN ( SELECT feature_id AS feature_id, val_double AS height FROM property WHERE name = 'height' ) attr ON f.id = attr.feature_id LEFT JOIN ( SELECT feature_id AS feature_id, val_code AS roofType FROM property WHERE name = 'roofType' ) attr3 ON f.id = attr3.feature_id WHERE f.id = ? ");

		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			long currentBuildingId = 0;
			AbstractBuilding building = null;
			ProjectionFilter projectionFilter = null;
			Map<Long, AbstractBuilding> buildings = new HashMap<>();

			while (rs.next()) {
				long buildingId = rs.getLong("id");

				if (buildingId != currentBuildingId || building == null) {
					currentBuildingId = buildingId;

					building = buildings.get(buildingId);
					if (building == null) {
						FeatureType featureType;
						if (buildingId == id && root != null) {
							building = root;
							featureType = rootType;
						} else {
							if (hasObjectClassIdColumn) {
								// create building object
								int objectClassId = rs.getInt("objectclass_id");
								building = exporter.createObject(objectClassId, AbstractBuilding.class);
								if (building == null) {
									exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, buildingId) + " as building object.");
									continue;
								}

								featureType = exporter.getFeatureType(objectClassId);
							} else {
								building = new BuildingPart();
								featureType = exporter.getFeatureType(building);
							}
						}

						String roofType = rs.getString("roof_type");
						if (!rs.wasNull()) {
							Code code = new Code(roofType);
							building.setRoofType(code);
						}

						double measuredHeight = rs.getDouble("measured_height");
						if (!rs.wasNull()) {
							Length length = new Length(measuredHeight);
							building.setMeasuredHeight(length);
						}

						buildings.put(buildingId, building);						
					} else
						projectionFilter = (ProjectionFilter) building.getLocalProperty("projection");
				}
			}



			List<AbstractBuilding> result = new ArrayList<>();
			for (Map.Entry<Long, AbstractBuilding> entry : buildings.entrySet()) {
				building = entry.getValue();
				result.add(building);
			}
			
			return result;
		}
	}
}
