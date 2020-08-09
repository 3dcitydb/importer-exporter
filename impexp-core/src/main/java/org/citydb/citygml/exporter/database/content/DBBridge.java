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
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElementProperty;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.BridgePart;
import org.citygml4j.model.citygml.bridge.BridgePartProperty;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.InteriorBridgeRoomProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DBBridge extends AbstractFeatureExporter<AbstractBridge> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBBridgeThematicSurface thematicSurfaceExporter;
	private final DBBridgeInstallation bridgeInstallationExporter;
	private final DBBridgeConstrElement bridgeConstrElemExporter;
	private final DBBridgeRoom bridgeRoomExporter;
	private final DBAddress addressExporter;
	private final GMLConverter gmlConverter;

	private final String bridgeModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private List<Table> bridgeADEHookTables;
	private List<Table> addressADEHookTables;

	public DBBridge(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractBridge.class, connection, exporter); 

		thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
		bridgeInstallationExporter = exporter.getExporter(DBBridgeInstallation.class);
		bridgeConstrElemExporter = exporter.getExporter(DBBridgeConstrElement.class);
		bridgeRoomExporter = exporter.getExporter(DBBridgeRoom.class);
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		addressExporter = exporter.getExporter(DBAddress.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.BRIDGE.getName(), schema);
		Table address = new Table(TableEnum.ADDRESS.getName(), schema);

		select = new Select().addProjection(table.getColumn("id"), table.getColumn("bridge_parent_id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", bridgeModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", bridgeModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", bridgeModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("yearOfConstruction", bridgeModule)) select.addProjection(table.getColumn("year_of_construction"));
		if (projectionFilter.containsProperty("yearOfDemolition", bridgeModule)) select.addProjection(table.getColumn("year_of_demolition"));
		if (projectionFilter.containsProperty("isMovable", bridgeModule)) select.addProjection(table.getColumn("is_movable"));
		if (lodFilter.isEnabled(1)) {
			if (projectionFilter.containsProperty("lod1TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
			if (projectionFilter.containsProperty("lod1Solid", bridgeModule)) select.addProjection(table.getColumn("lod1_solid_id"));
			if (projectionFilter.containsProperty("lod1MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
		}
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
			if (projectionFilter.containsProperty("lod2MultiCurve", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_multi_curve")));
			if (projectionFilter.containsProperty("lod2Solid", bridgeModule)) select.addProjection(table.getColumn("lod2_solid_id"));
			if (projectionFilter.containsProperty("lod2MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
			if (projectionFilter.containsProperty("lod3MultiCurve", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_multi_curve")));
			if (projectionFilter.containsProperty("lod3Solid", bridgeModule)) select.addProjection(table.getColumn("lod3_solid_id"));
			if (projectionFilter.containsProperty("lod3MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
			if (projectionFilter.containsProperty("lod4MultiCurve", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_multi_curve")));
			if (projectionFilter.containsProperty("lod4Solid", bridgeModule)) select.addProjection(table.getColumn("lod4_solid_id"));
			if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		}
		if (projectionFilter.containsProperty("address", bridgeModule)) {
			Table addressToBridge = new Table(TableEnum.ADDRESS_TO_BRIDGE.getName(), schema);
			addressExporter.addProjection(select, address, "a")
					.addJoin(JoinFactory.left(addressToBridge, "bridge_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
					.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, addressToBridge.getColumn("address_id")));
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			bridgeADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE, table);
			addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
		}
	}

	@Override
	protected boolean doExport(AbstractBridge object, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
		String column = projectionFilter.containsProperty("consistsOfBridgePart", bridgeModule) ? "bridge_root_id" : "id";
		return !doExport(id, object, featureType, getOrCreateStatement(column)).isEmpty();
	}

	@Override
	protected Collection<AbstractBridge> doExport(long id, AbstractBridge root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			long currentBridgeId = 0;
			AbstractBridge bridge = null;
			ProjectionFilter projectionFilter = null;
			Map<Long, AbstractBridge> bridges = new HashMap<>();

			while (rs.next()) {
				long bridgeId = rs.getLong("id");

				if (bridgeId != currentBridgeId || bridge == null) {
					currentBridgeId = bridgeId;

					bridge = bridges.get(bridgeId);
					if (bridge == null) {
						FeatureType featureType;
						if (bridgeId == id & root != null) {
							bridge = root;
							featureType = rootType;
						} else {
							if (hasObjectClassIdColumn) {
								int objectClassId = rs.getInt("objectclass_id");
								featureType = exporter.getFeatureType(objectClassId);
								if (featureType == null)
									continue;

								// create bridge object
								bridge = exporter.createObject(objectClassId, AbstractBridge.class);
								if (bridge == null) {
									exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, bridgeId) + " as bridge object.");
									continue;
								}
							} else {
								bridge = new BridgePart();
								featureType = exporter.getFeatureType(bridge);
							}
						}

						// get projection filter
						projectionFilter = exporter.getProjectionFilter(featureType);

						// export city object information
						cityObjectExporter.addBatch(bridge, bridgeId, featureType, projectionFilter);

						if (projectionFilter.containsProperty("class", bridgeModule)) {
							String clazz = rs.getString("class");
							if (!rs.wasNull()) {
								Code code = new Code(clazz);
								code.setCodeSpace(rs.getString("class_codespace"));
								bridge.setClazz(code);
							}
						}

						if (projectionFilter.containsProperty("function", bridgeModule)) {
							for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
								Code function = new Code(splitValue.result(0));
								function.setCodeSpace(splitValue.result(1));
								bridge.addFunction(function);
							}
						}

						if (projectionFilter.containsProperty("usage", bridgeModule)) {
							for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
								Code usage = new Code(splitValue.result(0));
								usage.setCodeSpace(splitValue.result(1));
								bridge.addUsage(usage);
							}
						}

						if (projectionFilter.containsProperty("yearOfConstruction", bridgeModule))
							bridge.setYearOfConstruction(rs.getObject("year_of_construction", LocalDate.class));

						if (projectionFilter.containsProperty("yearOfDemolition", bridgeModule))
							bridge.setYearOfDemolition(rs.getObject("year_of_demolition", LocalDate.class));

						if (projectionFilter.containsProperty("isMovable", bridgeModule)) {
							boolean isMovable = rs.getBoolean("is_movable");
							if (!rs.wasNull())
								bridge.setIsMovable(isMovable);
						}

						// brid:boundedBy
						if (projectionFilter.containsProperty("boundedBy", bridgeModule) 
								&& lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
							for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport(bridge, bridgeId))
								bridge.addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
						}

						// brid:outerBridgeInstallation and bldg:interiorBridgeInstallation
						if (lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
							for (AbstractCityObject installation : bridgeInstallationExporter.doExport(bridge, bridgeId, projectionFilter)) {
								if (installation instanceof BridgeInstallation)
									bridge.addOuterBridgeInstallation(new BridgeInstallationProperty((BridgeInstallation)installation));
								else if (installation instanceof IntBridgeInstallation)
									bridge.addInteriorBridgeInstallation(new IntBridgeInstallationProperty((IntBridgeInstallation)installation));
							}
						}

						// brid:outerBridgeConstruction
						if (projectionFilter.containsProperty("outerBridgeConstruction", bridgeModule)) {
							for (BridgeConstructionElement constructionElement : bridgeConstrElemExporter.doExport(bridge, bridgeId))
								bridge.addOuterBridgeConstructionElement(new BridgeConstructionElementProperty(constructionElement));
						}

						// brid:interiorBridgeRoom
						if (projectionFilter.containsProperty("interiorBridgeRoom", bridgeModule)
								&& lodFilter.isEnabled(4)) {
							for (BridgeRoom bridgeRoom : bridgeRoomExporter.doExport(bridge, bridgeId))
								bridge.addInteriorBridgeRoom(new InteriorBridgeRoomProperty(bridgeRoom));
						}

						// brid:lodXTerrainIntersectionCurve
						LodIterator lodIterator = lodFilter.iterator(1, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "TerrainIntersection", bridgeModule))
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
										bridge.setLod1TerrainIntersection(multiCurveProperty);
										break;
									case 2:
										bridge.setLod2TerrainIntersection(multiCurveProperty);
										break;
									case 3:
										bridge.setLod3TerrainIntersection(multiCurveProperty);
										break;
									case 4:
										bridge.setLod4TerrainIntersection(multiCurveProperty);
										break;
									}
								}
							}
						}

						// brid:lodXMultiCurve
						lodIterator = lodFilter.iterator(2, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "MultiCurve", bridgeModule))
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
										bridge.setLod2MultiCurve(multiCurveProperty);
										break;
									case 3:
										bridge.setLod3MultiCurve(multiCurveProperty);
										break;
									case 4:
										bridge.setLod4MultiCurve(multiCurveProperty);
										break;
									}
								}
							}
						}

						// brid:lodXSolid
						lodIterator = lodFilter.iterator(1, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "Solid", bridgeModule))
								continue;

							long geometryId = rs.getLong("lod" + lod + "_solid_id");
							if (rs.wasNull())
								continue;

							switch (lod) {
								case 1:
									geometryExporter.addBatch(geometryId, bridge::setLod1Solid);
									break;
								case 2:
									geometryExporter.addBatch(geometryId, bridge::setLod2Solid);
									break;
								case 3:
									geometryExporter.addBatch(geometryId, bridge::setLod3Solid);
									break;
								case 4:
									geometryExporter.addBatch(geometryId, bridge::setLod4Solid);
									break;
							}
						}

						// brid:lodXMultiSurface
						lodIterator.reset();
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", bridgeModule))
								continue;

							long geometryId = rs.getLong("lod" + lod + "_multi_surface_id");
							if (rs.wasNull())
								continue;

							switch (lod) {
								case 1:
									geometryExporter.addBatch(geometryId, bridge::setLod1MultiSurface);
									break;
								case 2:
									geometryExporter.addBatch(geometryId, bridge::setLod2MultiSurface);
									break;
								case 3:
									geometryExporter.addBatch(geometryId, bridge::setLod3MultiSurface);
									break;
								case 4:
									geometryExporter.addBatch(geometryId, bridge::setLod4MultiSurface);
									break;
							}
						}

						// delegate export of generic ADE properties
						if (bridgeADEHookTables != null) {
							List<String> adeHookTables = retrieveADEHookTables(bridgeADEHookTables, rs);
							if (adeHookTables != null)
								exporter.delegateToADEExporter(adeHookTables, bridge, bridgeId, featureType, projectionFilter);
						}

						bridge.setLocalProperty("parent", rs.getLong("bridge_parent_id"));
						bridge.setLocalProperty("projection", projectionFilter);
						bridges.put(bridgeId, bridge);
					} else
						projectionFilter = (ProjectionFilter)bridge.getLocalProperty("projection");
				}

				// brid:address
				if (projectionFilter.containsProperty("address", bridgeModule)) {
					long addressId = rs.getLong("aid");
					if (!rs.wasNull()) {
						AddressProperty addressProperty = addressExporter.doExport(addressId, "a", addressADEHookTables, rs);
						if (addressProperty != null)
							bridge.addAddress(addressProperty);
					}
				}
			}

			// rebuild bridge part hierarchy
			List<AbstractBridge> result = new ArrayList<>();
			for (Entry<Long, AbstractBridge> entry : bridges.entrySet()) {
				bridge = entry.getValue();
				long bridgeId = entry.getKey();			
				long parentId = (Long)bridge.getLocalProperty("parent");

				if (parentId == 0) {
					result.add(bridge);
					continue;
				}

				if (!(bridge instanceof BridgePart)) {
					exporter.logOrThrowErrorMessage("Expected " + exporter.getObjectSignature(exporter.getFeatureType(bridge), bridgeId) + " to be a bridge part.");
					continue;
				}

				AbstractBridge parent = bridges.get(parentId);
				if (parent != null) {				
					projectionFilter = (ProjectionFilter)parent.getLocalProperty("projection");				
					if (projectionFilter.containsProperty("consistsOfBridgePart", bridgeModule))
						parent.addConsistsOfBridgePart(new BridgePartProperty((BridgePart)bridge));
				}
			}

			return result;
		}
	}

}
