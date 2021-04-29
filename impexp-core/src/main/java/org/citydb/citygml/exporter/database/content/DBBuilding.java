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
import org.citydb.citygml.exporter.util.DefaultGeometrySetterHandler;
import org.citydb.citygml.exporter.util.GeometrySetterHandler;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.OpeningProperty;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
			select.addProjection(installation.getColumn("id", "inid"))
					.addJoin(JoinFactory.left(installation, "building_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
		}
		if (lodFilter.isEnabled(4) &&
				projectionFilter.containsProperty("interiorRoom", buildingModule)) {
			Table room = new Table(TableEnum.ROOM.getName(), schema);
			select.addProjection(room.getColumn("id", "roid"))
					.addJoin(JoinFactory.left(room, "building_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
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
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			long currentBuildingId = 0;
			AbstractBuilding building = null;
			ProjectionFilter projectionFilter = null;
			Map<Long, AbstractBuilding> buildings = new HashMap<>();
			Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
			Map<Long, List<String>> adeHookTables = buildingADEHookTables != null ? new HashMap<>() : null;

			long currentBoundarySurfaceId = 0;
			AbstractBoundarySurface boundarySurface = null;
			ProjectionFilter boundarySurfaceProjectionFilter = null;
			Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

			long currentOpeningId = 0;
			OpeningProperty openingProperty = null;
			ProjectionFilter openingProjectionFilter = null;
			Map<String, OpeningProperty> openingProperties = new HashMap<>();

			Set<Long> installations = new HashSet<>();
			Set<Long> rooms = new HashSet<>();
			Set<Long> buildingAddresses = new HashSet<>();
			Set<String> openingAddresses = new HashSet<>();

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

						// get projection filter
						projectionFilter = exporter.getProjectionFilter(featureType);

						// export city object information
						cityObjectExporter.addBatch(building, buildingId, featureType, projectionFilter);

						if (projectionFilter.containsProperty("class", buildingModule)) {
							String clazz = rs.getString("class");
							if (!rs.wasNull()) {
								Code code = new Code(clazz);
								code.setCodeSpace(rs.getString("class_codespace"));
								building.setClazz(code);
							}
						}

						if (projectionFilter.containsProperty("function", buildingModule)) {
							for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
								Code function = new Code(splitValue.result(0));
								function.setCodeSpace(splitValue.result(1));
								building.addFunction(function);
							}
						}

						if (projectionFilter.containsProperty("usage", buildingModule)) {
							for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
								Code usage = new Code(splitValue.result(0));
								usage.setCodeSpace(splitValue.result(1));
								building.addUsage(usage);
							}
						}

						if (projectionFilter.containsProperty("yearOfConstruction", buildingModule))
							building.setYearOfConstruction(rs.getObject("year_of_construction", LocalDate.class));

						if (projectionFilter.containsProperty("yearOfDemolition", buildingModule))
							building.setYearOfDemolition(rs.getObject("year_of_demolition", LocalDate.class));

						if (projectionFilter.containsProperty("roofType", buildingModule)) {
							String roofType = rs.getString("roof_type");
							if (!rs.wasNull()) {
								Code code = new Code(roofType);
								code.setCodeSpace(rs.getString("roof_type_codespace"));
								building.setRoofType(code);
							}
						}

						if (projectionFilter.containsProperty("measuredHeight", buildingModule)) {
							double measuredHeight = rs.getDouble("measured_height");
							if (!rs.wasNull()) {
								Length length = new Length(measuredHeight);
								length.setUom(rs.getString("measured_height_unit"));
								building.setMeasuredHeight(length);
							}
						}

						if (projectionFilter.containsProperty("storeysAboveGround", buildingModule)) {
							int storeysAboveGround = rs.getInt("storeys_above_ground");
							if (!rs.wasNull())
								building.setStoreysAboveGround(storeysAboveGround);
						}

						if (projectionFilter.containsProperty("storeysBelowGround", buildingModule)) {
							int storeysBelowGround = rs.getInt("storeys_below_ground");
							if (!rs.wasNull())
								building.setStoreysBelowGround(storeysBelowGround);
						}

						if (projectionFilter.containsProperty("storeyHeightsAboveGround", buildingModule)) {
							String storeyHeightsAboveGround = rs.getString("storey_heights_above_ground");
							if (!rs.wasNull()) {
								MeasureOrNullList measureList = new MeasureOrNullList();
								for (SplitValue splitValue : valueSplitter.split(Pattern.compile("\\s+"), storeyHeightsAboveGround)) {
									Double value = splitValue.asDouble(0);
									if (value != null)
										measureList.addDoubleOrNull(new DoubleOrNull(value));
								}

								measureList.setUom(rs.getString("storey_heights_ag_unit"));
								building.setStoreyHeightsAboveGround(measureList);
							}
						}

						if (projectionFilter.containsProperty("storeyHeightsBelowGround", buildingModule)) {
							String storeyHeightsBelowGround = rs.getString("storey_heights_below_ground");
							if (!rs.wasNull()) {
								MeasureOrNullList measureList = new MeasureOrNullList();
								for (SplitValue splitValue : valueSplitter.split(Pattern.compile("\\s+"), storeyHeightsBelowGround)) {
									Double value = splitValue.asDouble(0);
									if (value != null)
										measureList.addDoubleOrNull(new DoubleOrNull(value));
								}

								measureList.setUom(rs.getString("storey_heights_bg_unit"));
								building.setStoreyHeightsBelowGround(measureList);
							}
						}

						// bldg:lodXTerrainIntersectionCurve
						LodIterator lodIterator = lodFilter.iterator(1, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "TerrainIntersection", buildingModule))
								continue;

							Object terrainIntersectionObj = rs.getObject("lod" + lod + "_terrain_intersection");
							if (rs.wasNull())
								continue;

							GeometryObject terrainIntersection = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
							if (terrainIntersection != null) {
								MultiCurveProperty multiCurveProperty = gmlConverter.getMultiCurveProperty(terrainIntersection, false);
								if (multiCurveProperty != null) {
									switch (lod) {
									case 1:
										building.setLod1TerrainIntersection(multiCurveProperty);
										break;
									case 2:
										building.setLod2TerrainIntersection(multiCurveProperty);
										break;
									case 3:
										building.setLod3TerrainIntersection(multiCurveProperty);
										break;
									case 4:
										building.setLod4TerrainIntersection(multiCurveProperty);
										break;
									}
								}
							}
						}

						// bldg:lodXMultiCurve
						lodIterator = lodFilter.iterator(2, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "MultiCurve", buildingModule))
								continue;

							Object multiCurveObj = rs.getObject("lod" + lod + "_multi_curve");
							if (rs.wasNull())
								continue;

							GeometryObject multiCurve = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(multiCurveObj);
							if (multiCurve != null) {
								MultiCurveProperty multiCurveProperty = gmlConverter.getMultiCurveProperty(multiCurve, false);
								if (multiCurveProperty != null) {
									switch (lod) {
									case 2:
										building.setLod2MultiCurve(multiCurveProperty);
										break;
									case 3:
										building.setLod3MultiCurve(multiCurveProperty);
										break;
									case 4:
										building.setLod4MultiCurve(multiCurveProperty);
										break;
									}
								}
							}
						}

						// bldg:lod0FootPrint and lod0RoofEdge
						if (lodFilter.isEnabled(0)) {
							for (int i = 0; i < 2; i++) {
								if (i == 0 && !projectionFilter.containsProperty("lod0FootPrint", buildingModule))
									continue;
								else if (i == 1 && !projectionFilter.containsProperty("lod0RoofEdge", buildingModule))
									continue;

								long geometryId = rs.getLong(i == 0 ? "lod0_footprint_id" : "lod0_roofprint_id");
								if (rs.wasNull())
									continue;

								switch (i) {
									case 0:
										geometryExporter.addBatch(geometryId, building::setLod0FootPrint);
										break;
									case 1:
										geometryExporter.addBatch(geometryId, building::setLod0RoofEdge);
										break;
								}
							}
						}

						// bldg:lodXSolid
						lodIterator = lodFilter.iterator(1, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "Solid", buildingModule))
								continue;

							long geometryId = rs.getLong("lod" + lod + "_solid_id");
							if (rs.wasNull())
								continue;

							switch (lod) {
								case 1:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod1Solid));
									break;
								case 2:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod2Solid));
									break;
								case 3:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod3Solid));
									break;
								case 4:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod4Solid));
									break;
							}
						}

						// bldg:lodXMultiSurface
						lodIterator.reset();
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", buildingModule))
								continue;

							long geometryId = rs.getLong("lod" + lod + "_multi_surface_id");
							if (rs.wasNull())
								continue;

							switch (lod) {
								case 1:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod1MultiSurface));
									break;
								case 2:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod2MultiSurface));
									break;
								case 3:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod3MultiSurface));
									break;
								case 4:
									geometries.put(geometryId, new DefaultGeometrySetterHandler(building::setLod4MultiSurface));
									break;
							}
						}

						// get tables of ADE hook properties
						if (buildingADEHookTables != null) {
							List<String> tables = retrieveADEHookTables(buildingADEHookTables, rs);
							if (tables != null) {
								adeHookTables.put(buildingId, tables);
								building.setLocalProperty("type", featureType);
							}
						}

						building.setLocalProperty("parent", rs.getLong("building_parent_id"));
						building.setLocalProperty("projection", projectionFilter);
						buildings.put(buildingId, building);						
					} else
						projectionFilter = (ProjectionFilter) building.getLocalProperty("projection");
				}

				// bldg:outerBuildingInstallation and bldg:interiorBuildingInstallation
				if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
						&& (projectionFilter.containsProperty("outerBuildingInstallation", buildingModule)
						|| projectionFilter.containsProperty("interiorBuildingInstallation", buildingModule))) {
					long installationId = rs.getLong("inid");
					if (!rs.wasNull() && installations.add(installationId))
						buildingInstallationExporter.addBatch(installationId, building);
				}

				// bldg:interiorRoom
				if (lodFilter.isEnabled(4)
						&& projectionFilter.containsProperty("interiorRoom", buildingModule)) {
					long roomId = rs.getLong("roid");
					if (!rs.wasNull() && rooms.add(roomId))
						roomExporter.addBatch(roomId, building);
				}

				// bldg:address
				if (projectionFilter.containsProperty("address", buildingModule)) {
					long addressId = rs.getLong("baid");
					if (!rs.wasNull() && buildingAddresses.add(addressId)) {
						AddressProperty addressProperty = addressExporter.doExport(addressId, "ba", addressADEHookTables, rs);
						if (addressProperty != null)
							building.addAddress(addressProperty);
					}
				}

				if (!lodFilter.containsLodGreaterThanOrEuqalTo(2)
						|| !projectionFilter.containsProperty("boundedBy", buildingModule))
					continue;

				// bldg:boundedBy
				long boundarySurfaceId = rs.getLong("tsid");
				if (rs.wasNull())
					continue;

				if (boundarySurfaceId != currentBoundarySurfaceId || boundarySurface == null) {
					currentBoundarySurfaceId = boundarySurfaceId;
					currentOpeningId = 0;

					boundarySurface = boundarySurfaces.get(boundarySurfaceId);
					if (boundarySurface == null) {
						int objectClassId = rs.getInt("tsobjectclass_id");
						FeatureType featureType = exporter.getFeatureType(objectClassId);

						boundarySurface = thematicSurfaceExporter.doExport(boundarySurfaceId, featureType, "ts", surfaceADEHookTables, rs);
						if (boundarySurface == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, boundarySurfaceId) + " as boundary surface object.");
							continue;
						}

						// get projection filter
						boundarySurfaceProjectionFilter = exporter.getProjectionFilter(featureType);
						boundarySurface.setLocalProperty("projection", boundarySurfaceProjectionFilter);

						building.getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
						boundarySurfaces.put(boundarySurfaceId, boundarySurface);
					} else
						boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
				}

				// continue if openings shall not be exported
				if (!lodFilter.containsLodGreaterThanOrEuqalTo(3)
						|| !boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule))
					continue;

				long openingId = rs.getLong("opid");
				if (rs.wasNull())
					continue;

				if (openingId != currentOpeningId || openingProperty == null) {
					currentOpeningId = openingId;
					String key = currentBoundarySurfaceId + "_" + openingId;

					openingProperty = openingProperties.get(key);
					if (openingProperty == null) {
						int objectClassId = rs.getInt("opobjectclass_id");

						// check whether we need an XLink
						String gmlId = rs.getString("opgmlid");
						boolean generateNewGmlId = false;
						if (!rs.wasNull()) {
							if (exporter.lookupAndPutObjectId(gmlId, openingId, objectClassId)) {
								if (useXLink) {
									openingProperty = new OpeningProperty();
									openingProperty.setHref("#" + gmlId);
									boundarySurface.addOpening(openingProperty);
									openingProperties.put(key, openingProperty);
									continue;
								} else
									generateNewGmlId = true;
							}
						}

						// create new opening object
						FeatureType featureType = exporter.getFeatureType(objectClassId);
						AbstractOpening opening = openingExporter.doExport(openingId, featureType, "op", openingADEHookTables, rs);
						if (opening == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, openingId) + " as opening object.");
							continue;
						}

						if (generateNewGmlId)
							opening.setId(exporter.generateFeatureGmlId(opening, gmlId));

						// get projection filter
						openingProjectionFilter = exporter.getProjectionFilter(featureType);
						opening.setLocalProperty("projection", openingProjectionFilter);

						openingProperty = new OpeningProperty(opening);
						boundarySurface.getOpening().add(openingProperty);
						openingProperties.put(key, openingProperty);
					} else if (openingProperty.isSetOpening())
						openingProjectionFilter = (ProjectionFilter) openingProperty.getOpening().getLocalProperty("projection");
				}

				if (openingProperty.getOpening() instanceof Door
						&& openingProjectionFilter.containsProperty("address", buildingModule)) {
					long openingAddressId = rs.getLong("oaid");
					if (!rs.wasNull() && openingAddresses.add(currentOpeningId + "_" + openingAddressId)) {
						AddressProperty addressProperty = addressExporter.doExport(openingAddressId, "oa", openingAddressADEHookTables, rs);
						if (addressProperty != null) {
							Door door = (Door) openingProperty.getOpening();
							door.addAddress(addressProperty);
						}
					}
				}
			}

			buildingInstallationExporter.executeBatch();
			roomExporter.executeBatch();

			// export postponed geometries
			for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
				geometryExporter.addBatch(entry.getKey(), entry.getValue());

			List<AbstractBuilding> result = new ArrayList<>();
			for (Map.Entry<Long, AbstractBuilding> entry : buildings.entrySet()) {
				building = entry.getValue();
				long buildingId = entry.getKey();
				long parentId = (Long) building.getLocalProperty("parent");

				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> tables = adeHookTables.get(buildingId);
					if (tables != null) {
						exporter.delegateToADEExporter(tables, building, buildingId,
								(FeatureType) building.getLocalProperty("type"),
								(ProjectionFilter) building.getLocalProperty("projection"));
					}
				}

				// rebuild building part hierarchy
				if (parentId == 0) {
					result.add(building);
				} else if (building instanceof BuildingPart) {
					AbstractBuilding parent = buildings.get(parentId);
					if (parent != null) {
						projectionFilter = (ProjectionFilter) parent.getLocalProperty("projection");
						if (projectionFilter.containsProperty("consistsOfBuildingPart", buildingModule))
							parent.addConsistsOfBuildingPart(new BuildingPartProperty((BuildingPart) building));
					}
				} else
					exporter.logOrThrowErrorMessage("Expected " + exporter.getObjectSignature(exporter.getFeatureType(building), buildingId) + " to be a building part.");
			}
			
			return result;
		}
	}
}
