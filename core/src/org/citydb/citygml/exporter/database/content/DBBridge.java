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
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import vcs.sqlbuilder.schema.Table;
import vcs.sqlbuilder.select.Select;
import vcs.sqlbuilder.select.join.JoinFactory;
import vcs.sqlbuilder.select.operator.comparison.ComparisonName;

public class DBBridge extends AbstractFeatureExporter<AbstractBridge> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBBridgeThematicSurface thematicSurfaceExporter;
	private DBBridgeInstallation bridgeInstallationExporter;
	private DBBridgeConstrElement bridgeConstrElemExporter;
	private DBBridgeRoom bridgeRoomExporter;
	private DBAddress addressExporter;
	private GMLConverter gmlConverter;

	private String bridgeModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;

	private boolean hasObjectClassIdColumn;
	private Set<String> bridgeADEHookTables;
	private Set<String> addressADEHookTables;

	public DBBridge(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractBridge.class, connection, exporter); 

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

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
		if (projectionFilter.containsProperty("lod1TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
		if (projectionFilter.containsProperty("lod2TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
		if (projectionFilter.containsProperty("lod3TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
		if (projectionFilter.containsProperty("lod4TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
		if (projectionFilter.containsProperty("lod2MultiCurve", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_multi_curve")));
		if (projectionFilter.containsProperty("lod3MultiCurve", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_multi_curve")));
		if (projectionFilter.containsProperty("lod4MultiCurve", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_multi_curve")));
		if (projectionFilter.containsProperty("lod1Solid", bridgeModule)) select.addProjection(table.getColumn("lod1_solid_id"));
		if (projectionFilter.containsProperty("lod2Solid", bridgeModule)) select.addProjection(table.getColumn("lod2_solid_id"));
		if (projectionFilter.containsProperty("lod3Solid", bridgeModule)) select.addProjection(table.getColumn("lod3_solid_id"));
		if (projectionFilter.containsProperty("lod4Solid", bridgeModule)) select.addProjection(table.getColumn("lod4_solid_id"));
		if (projectionFilter.containsProperty("lod1MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
		if (projectionFilter.containsProperty("lod2MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (projectionFilter.containsProperty("lod3MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("address", bridgeModule)) {
			Table addressToBridge = new Table(TableEnum.ADDRESS_TO_BRIDGE.getName(), schema);
			select.addJoin(JoinFactory.left(addressToBridge, "bridge_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
			.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, addressToBridge.getColumn("address_id")))
			.addProjection(address.getColumn("id", "addr_id"), address.getColumn("street"), address.getColumn("house_number"), address.getColumn("po_box"), address.getColumn("zip_code"), address.getColumn("city"),
					address.getColumn("state"), address.getColumn("country"), address.getColumn("xal_source"), exporter.getGeometryColumn(address.getColumn("multi_point")));
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			bridgeADEHookTables = exporter.getADEHookTables(TableEnum.BRIDGE);
			addressADEHookTables = exporter.getADEHookTables(TableEnum.ADDRESS);			
			if (bridgeADEHookTables != null) addJoinsToADEHookTables(bridgeADEHookTables, table);
			if (addressADEHookTables != null) addJoinsToADEHookTables(addressADEHookTables, address);
		}

		thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
		bridgeInstallationExporter = exporter.getExporter(DBBridgeInstallation.class);
		bridgeConstrElemExporter = exporter.getExporter(DBBridgeConstrElement.class);
		bridgeRoomExporter = exporter.getExporter(DBBridgeRoom.class);
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		addressExporter = exporter.getExporter(DBAddress.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
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
			HashMap<Long, AbstractBridge> bridges = new HashMap<>();

			while (rs.next()) {
				long bridgeId = rs.getLong("id");

				if (bridgeId != currentBridgeId) {
					currentBridgeId = bridgeId;

					bridge = bridges.get(bridgeId);
					if (bridge == null) {
						FeatureType featureType = null;						
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
								bridge = exporter.createObject(featureType.getObjectClassId(), AbstractBridge.class);						
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
						boolean success = cityObjectExporter.doExport(bridge, bridgeId, featureType, projectionFilter);
						if (!success) {
							if (bridge == root)
								return Collections.emptyList();
							else if (featureType.isSetTopLevel())
								continue;
						}

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

						if (projectionFilter.containsProperty("yearOfConstruction", bridgeModule)) {
							Date yearOfConstruction = rs.getDate("year_of_construction");				
							if (!rs.wasNull()) {						
								GregorianCalendar calendar = new GregorianCalendar();
								calendar.setTime(yearOfConstruction);
								bridge.setYearOfConstruction(calendar);
							}
						}

						if (projectionFilter.containsProperty("yearOfDemolition", bridgeModule)) {
							Date yearOfDemolition = rs.getDate("year_of_demolition");
							if (!rs.wasNull()) {
								GregorianCalendar calendar = new GregorianCalendar();
								calendar.setTime(yearOfDemolition);
								bridge.setYearOfDemolition(calendar);
							}
						}

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

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("TerrainIntersection").toString(), bridgeModule))
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

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiCurve").toString(), bridgeModule))
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

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("Solid").toString(), bridgeModule))
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
									bridge.setLod1Solid(solidProperty);
									break;
								case 2:
									bridge.setLod2Solid(solidProperty);
									break;
								case 3:
									bridge.setLod3Solid(solidProperty);
									break;
								case 4:
									bridge.setLod4Solid(solidProperty);
									break;
								}
							}
						}

						// brid:lodXMultiSurface
						lodIterator.reset();
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiSurface").toString(), bridgeModule))
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
									bridge.setLod1MultiSurface(multiSurfaceProperty);
									break;
								case 2:
									bridge.setLod2MultiSurface(multiSurfaceProperty);
									break;
								case 3:
									bridge.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									bridge.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
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
					long addressId = rs.getLong("addr_id");
					if (!rs.wasNull()) {
						AddressProperty addressProperty = addressExporter.doExport(addressId, rs);
						if (addressProperty != null) {
							bridge.addAddress(addressProperty);

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

			// check whether lod filter is satisfied
			if (!lodFilter.preservesGeometry()) {
				for (AbstractBridge tmp : result) {
					for (Iterator<BridgePartProperty> iter = tmp.getConsistsOfBridgePart().iterator(); iter.hasNext(); ) {
						if (!exporter.satisfiesLodFilter(iter.next().getBridgePart()))
							iter.remove();
					}
				}
			}

			return result;
		}
	}

}
