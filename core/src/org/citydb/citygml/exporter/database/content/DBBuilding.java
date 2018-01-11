/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import vcs.sqlbuilder.schema.Table;
import vcs.sqlbuilder.select.Select;
import vcs.sqlbuilder.select.join.JoinFactory;
import vcs.sqlbuilder.select.operator.comparison.ComparisonName;

public class DBBuilding extends AbstractFeatureExporter<AbstractBuilding> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBThematicSurface thematicSurfaceExporter;
	private DBBuildingInstallation buildingInstallationExporter;
	private DBRoom roomExporter;
	private DBAddress addressExporter;
	private GMLConverter gmlConverter;

	private String buildingModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;

	private boolean hasObjectClassIdColumn;
	private Set<String> buildingADEHookTables;
	private Set<String> addressADEHookTables;

	public DBBuilding(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractBuilding.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BUILDING.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();		
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.BUILDING.getName(), schema);
		Table address = new Table(TableEnum.ADDRESS.getName(), schema);

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
		if (projectionFilter.containsProperty("lod1TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
		if (projectionFilter.containsProperty("lod2TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
		if (projectionFilter.containsProperty("lod3TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
		if (projectionFilter.containsProperty("lod4TerrainIntersection", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
		if (projectionFilter.containsProperty("lod2MultiCurve", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_multi_curve")));
		if (projectionFilter.containsProperty("lod3MultiCurve", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_multi_curve")));
		if (projectionFilter.containsProperty("lod4MultiCurve", buildingModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_multi_curve")));
		if (projectionFilter.containsProperty("lod0FootPrint", buildingModule)) select.addProjection(table.getColumn("lod0_footprint_id"));
		if (projectionFilter.containsProperty("lod0RoofEdge", buildingModule)) select.addProjection(table.getColumn("lod0_roofprint_id"));
		if (projectionFilter.containsProperty("lod1Solid", buildingModule)) select.addProjection(table.getColumn("lod1_solid_id"));
		if (projectionFilter.containsProperty("lod2Solid", buildingModule)) select.addProjection(table.getColumn("lod2_solid_id"));
		if (projectionFilter.containsProperty("lod3Solid", buildingModule)) select.addProjection(table.getColumn("lod3_solid_id"));
		if (projectionFilter.containsProperty("lod4Solid", buildingModule)) select.addProjection(table.getColumn("lod4_solid_id"));
		if (projectionFilter.containsProperty("lod1MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
		if (projectionFilter.containsProperty("lod2MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (projectionFilter.containsProperty("lod3MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("address", buildingModule)) {
			Table addressToBuilding = new Table(TableEnum.ADDRESS_TO_BUILDING.getName(), schema);
			select.addJoin(JoinFactory.left(addressToBuilding, "building_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
			.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, addressToBuilding.getColumn("address_id")))
			.addProjection(address.getColumn("id", "addr_id"), address.getColumn("street"), address.getColumn("house_number"), address.getColumn("po_box"), address.getColumn("zip_code"), address.getColumn("city"),
					address.getColumn("state"), address.getColumn("country"), address.getColumn("xal_source"), exporter.getGeometryColumn(address.getColumn("multi_point")));
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			buildingADEHookTables = exporter.getADEHookTables(TableEnum.BUILDING);
			addressADEHookTables = exporter.getADEHookTables(TableEnum.ADDRESS);			
			if (buildingADEHookTables != null) addJoinsToADEHookTables(buildingADEHookTables, table);
			if (addressADEHookTables != null) addJoinsToADEHookTables(addressADEHookTables, address);
		}

		thematicSurfaceExporter = exporter.getExporter(DBThematicSurface.class);
		buildingInstallationExporter = exporter.getExporter(DBBuildingInstallation.class);
		roomExporter = exporter.getExporter(DBRoom.class);
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		addressExporter = exporter.getExporter(DBAddress.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
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
			HashMap<Long, AbstractBuilding> buildings = new HashMap<>();

			while (rs.next()) {
				long buildingId = rs.getLong("id");

				if (buildingId != currentBuildingId) {
					currentBuildingId = buildingId;

					building = buildings.get(buildingId);
					if (building == null) {
						FeatureType featureType = null;						
						if (buildingId == id && root != null) {
							building = root;
							featureType = rootType;
						} else {
							if (hasObjectClassIdColumn) {
								int objectClassId = rs.getInt("objectclass_id");
								featureType = exporter.getFeatureType(objectClassId);
								if (featureType == null)
									continue;

								// create building object
								building = exporter.createObject(featureType.getObjectClassId(), AbstractBuilding.class);						
								if (building == null) {
									exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, buildingId) + " as building object.");
									continue;
								}
							} else {
								building = new BuildingPart();
								featureType = exporter.getFeatureType(building);
							}
						}

						// get projection filter
						projectionFilter = exporter.getProjectionFilter(featureType);

						// export city object information
						boolean success = cityObjectExporter.doExport(building, buildingId, featureType, projectionFilter);
						if (!success) {
							if (building == root)
								return Collections.emptyList();
							else if (featureType.isSetTopLevel())
								continue;
						}

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

						if (projectionFilter.containsProperty("yearOfConstruction", buildingModule)) {
							Date yearOfConstruction = rs.getDate("year_of_construction");				
							if (!rs.wasNull()) {						
								GregorianCalendar calendar = new GregorianCalendar();
								calendar.setTime(yearOfConstruction);
								building.setYearOfConstruction(calendar);
							}
						}

						if (projectionFilter.containsProperty("yearOfDemolition", buildingModule)) {
							Date yearOfDemolition = rs.getDate("year_of_demolition");
							if (!rs.wasNull()) {
								GregorianCalendar calendar = new GregorianCalendar();
								calendar.setTime(yearOfDemolition);
								building.setYearOfDemolition(calendar);
							}
						}

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

						// bldg:boundedBy
						if (projectionFilter.containsProperty("boundedBy", buildingModule) 
								&& lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
							for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport(building, buildingId))
								building.addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
						}

						// bldg:outerBuildingInstallation and bldg:interiorBuildingInstallation
						if (lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
							for (AbstractCityObject installation : buildingInstallationExporter.doExport(building, buildingId, projectionFilter)) {
								if (installation instanceof BuildingInstallation)
									building.addOuterBuildingInstallation(new BuildingInstallationProperty((BuildingInstallation)installation));
								else if (installation instanceof IntBuildingInstallation)
									building.addInteriorBuildingInstallation(new IntBuildingInstallationProperty((IntBuildingInstallation)installation));
							}
						}

						// bldg:interiorRoom
						if (projectionFilter.containsProperty("interiorRoom", buildingModule)
								&& lodFilter.isEnabled(4)) {
							for (Room room : roomExporter.doExport(building, buildingId))
								building.addInteriorRoom(new InteriorRoomProperty(room));
						}

						// bldg:lodXTerrainIntersectionCurve
						LodIterator lodIterator = lodFilter.iterator(1, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("TerrainIntersection").toString(), buildingModule))
								continue;

							Object terrainIntersectionObj = rs.getObject(new StringBuilder("lod").append(lod).append("_terrain_intersection").toString());
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

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiCurve").toString(), buildingModule))
								continue;

							Object multiCurveObj = rs.getObject(new StringBuilder("lod").append(lod).append("_multi_curve").toString());
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

								long surfaceGeometryId = rs.getLong(i == 0 ? "lod0_footprint_id" : "lod0_roofprint_id");
								if (rs.wasNull())
									continue;

								SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
								if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
									MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
									if (geometry.isSetGeometry())
										multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
									else
										multiSurfaceProperty.setHref(geometry.getReference());

									switch (i) {
									case 0:
										building.setLod0FootPrint(multiSurfaceProperty);
										break;
									case 1:
										building.setLod0RoofEdge(multiSurfaceProperty);
										break;
									}
								}
							}
						}

						// bldg:lodXSolid
						lodIterator = lodFilter.iterator(1, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("Solid").toString(), buildingModule))
								continue;

							long surfaceGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_solid_id").toString());
							if (rs.wasNull())
								continue;

							SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
							if (geometry != null && (geometry.getType() == GMLClass.SOLID || geometry.getType() == GMLClass.COMPOSITE_SOLID)) {
								SolidProperty solidProperty = new SolidProperty();
								if (geometry.isSetGeometry())
									solidProperty.setSolid((AbstractSolid)geometry.getGeometry());
								else
									solidProperty.setHref(geometry.getReference());

								switch (lod) {
								case 1:
									building.setLod1Solid(solidProperty);
									break;
								case 2:
									building.setLod2Solid(solidProperty);
									break;
								case 3:
									building.setLod3Solid(solidProperty);
									break;
								case 4:
									building.setLod4Solid(solidProperty);
									break;
								}
							}
						}

						// bldg:lodXMultiSurface
						lodIterator.reset();
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiSurface").toString(), buildingModule))
								continue;

							long surfaceGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_multi_surface_id").toString());
							if (rs.wasNull())
								continue;

							SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
							if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
								if (geometry.isSetGeometry())
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getReference());

								switch (lod) {
								case 1:
									building.setLod1MultiSurface(multiSurfaceProperty);
									break;
								case 2:
									building.setLod2MultiSurface(multiSurfaceProperty);
									break;
								case 3:
									building.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									building.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}

						// delegate export of generic ADE properties
						if (buildingADEHookTables != null) {
							List<String> adeHookTables = retrieveADEHookTables(buildingADEHookTables, rs);
							if (adeHookTables != null)
								exporter.delegateToADEExporter(adeHookTables, building, buildingId, featureType, projectionFilter);
						}

						building.setLocalProperty("parent", rs.getLong("building_parent_id"));
						building.setLocalProperty("projection", projectionFilter);
						buildings.put(buildingId, building);						
					} else
						projectionFilter = (ProjectionFilter)building.getLocalProperty("projection");
				}			

				// bldg:address
				if (projectionFilter.containsProperty("address", buildingModule)) {
					long addressId = rs.getLong("addr_id");
					if (!rs.wasNull()) {
						AddressProperty addressProperty = addressExporter.doExport(addressId, rs);
						if (addressProperty != null) {
							building.addAddress(addressProperty);

							// delegate export of generic ADE properties
							if (addressADEHookTables != null) {
								List<String> adeHookTables = retrieveADEHookTables(addressADEHookTables, rs);
								if (adeHookTables != null) {
									Address address = addressProperty.getAddress();
									FeatureType featureType = exporter.getFeatureType(address);
									exporter.delegateToADEExporter(adeHookTables, address, addressId, featureType, exporter.getProjectionFilter(featureType));
								}
							}
						}
					}
				}
			}

			// rebuild building part hierarchy
			List<AbstractBuilding> result = new ArrayList<>();
			for (Entry<Long, AbstractBuilding> entry : buildings.entrySet()) {
				building = entry.getValue();
				long buildingId = entry.getKey();
				long parentId = (Long)building.getLocalProperty("parent");

				if (parentId == 0) {
					result.add(building);
					continue;
				}

				if (!(building instanceof BuildingPart)) {
					exporter.logOrThrowErrorMessage("Expected " + exporter.getObjectSignature(exporter.getFeatureType(building), buildingId) + " to be a building part.");
					continue;
				}

				AbstractBuilding parent = buildings.get(parentId);
				if (parent != null) {				
					projectionFilter = (ProjectionFilter)parent.getLocalProperty("projection");				
					if (projectionFilter.containsProperty("consistsOfBuildingPart", buildingModule))
						parent.addConsistsOfBuildingPart(new BuildingPartProperty((BuildingPart)building));
				}
			}

			// check whether lod filter is satisfied
			if (!lodFilter.preservesGeometry()) {
				for (AbstractBuilding tmp : result) {
					for (Iterator<BuildingPartProperty> iter = tmp.getConsistsOfBuildingPart().iterator(); iter.hasNext(); ) {
						if (!exporter.satisfiesLodFilter(iter.next().getBuildingPart()))
							iter.remove();
					}
				}
			}
			
			return result;
		}
	}

}
