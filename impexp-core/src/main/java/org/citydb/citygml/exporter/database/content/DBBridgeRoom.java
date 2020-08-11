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
import org.citydb.citygml.exporter.util.DefaultGeometrySetterHandler;
import org.citydb.citygml.exporter.util.GeometrySetterHandler;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.AbstractOpening;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.Door;
import org.citygml4j.model.citygml.bridge.InteriorBridgeRoomProperty;
import org.citygml4j.model.citygml.bridge.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.bridge.OpeningProperty;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBBridgeRoom extends AbstractFeatureExporter<BridgeRoom> {
	private final Map<Long, AbstractBridge> batches;
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBBridgeInstallation bridgeInstallationExporter;
	private final DBBridgeThematicSurface thematicSurfaceExporter;
	private final DBBridgeOpening openingExporter;
	private final DBBridgeFurniture bridgeFurnitureExporter;
	private final DBAddress addressExporter;

	private final String bridgeModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private final boolean useXLink;
	private final List<Table> bridgeRoomADEHookTables;
	private List<Table> surfaceADEHookTables;
	private List<Table> openingADEHookTables;
	private List<Table> addressADEHookTables;
	private List<Table> bridgeFurnitureADEHookTables;

	public DBBridgeRoom(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(BridgeRoom.class, connection, exporter);

		batches = new HashMap<>();
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		bridgeInstallationExporter = exporter.getExporter(DBBridgeInstallation.class);
		thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
		openingExporter = exporter.getExporter(DBBridgeOpening.class);
		bridgeFurnitureExporter = exporter.getExporter(DBBridgeFurniture.class);
		addressExporter = exporter.getExporter(DBAddress.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_ROOM.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		useXLink = exporter.getExportConfig().getXlink().getFeature().isModeXLink();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.BRIDGE_ROOM.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", bridgeModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", bridgeModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", bridgeModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
			if (projectionFilter.containsProperty("lod4Solid", bridgeModule)) select.addProjection(table.getColumn("lod4_solid_id"));
			if (projectionFilter.containsProperty("boundedBy", bridgeModule)) {
				CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_THEMATIC_SURFACE.getName());
				Table thematicSurface = new Table(TableEnum.BRIDGE_THEMATIC_SURFACE.getName(), schema);
				thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
						.addJoin(JoinFactory.left(thematicSurface, "bridge_room_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
				if (boundarySurfaceProjectionFilter.containsProperty("opening", bridgeModule)) {
					CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_OPENING.getName());
					Table opening = new Table(TableEnum.BRIDGE_OPENING.getName(), schema);
					Table openingToThemSurface = new Table(TableEnum.BRIDGE_OPEN_TO_THEM_SRF.getName(), schema);
					Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
					openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
							.addProjection(cityObject.getColumn("gmlid", "opgmlid"))
							.addJoin(JoinFactory.left(openingToThemSurface, "bridge_thematic_surface_id", ComparisonName.EQUAL_TO, thematicSurface.getColumn("id")))
							.addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("bridge_opening_id")))
							.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
					if (openingProjectionFilter.containsProperty("address", bridgeModule)) {
						Table address = new Table(TableEnum.ADDRESS.getName(), schema);
						addressExporter.addProjection(select, address, "oa")
								.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")));
						addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
					}
					openingADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_OPENING, opening);
				}
				surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_THEMATIC_SURFACE, table);
			}
			if (projectionFilter.containsProperty("interiorFurniture", bridgeModule)) {
				CombinedProjectionFilter bridgeFurnitureProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_FURNITURE.getName());
				Table bridgeFurniture = new Table(TableEnum.BRIDGE_FURNITURE.getName(), schema);
				bridgeFurnitureExporter.addProjection(select, bridgeFurniture, bridgeFurnitureProjectionFilter, "bf")
						.addJoin(JoinFactory.left(bridgeFurniture, "bridge_room_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
				bridgeFurnitureADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_FURNITURE, bridgeFurniture);
			}
			if (projectionFilter.containsProperty("bridgeRoomInstallation", bridgeModule)) {
				Table installation = new Table(TableEnum.BRIDGE_INSTALLATION.getName(), schema);
				select.addProjection(installation.getColumn("id", "inid"))
						.addJoin(JoinFactory.left(installation, "bridge_room_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
			}
		}
		bridgeRoomADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_ROOM, table);
	}

	protected void addBatch(long id, AbstractBridge parent) {
		batches.put(id, parent);
	}

	protected void executeBatch() throws CityGMLExportException, SQLException {
		if (batches.isEmpty())
			return;

		try {
			PreparedStatement ps;
			if (batches.size() == 1) {
				ps = getOrCreateStatement("id");
				ps.setLong(1, batches.keySet().iterator().next());
			} else {
				ps = getOrCreateBulkStatement();
				ps.setArray(1, exporter.getDatabaseAdapter().getSQLAdapter().createIdArray(batches.keySet().toArray(new Long[0]), connection));
			}

			try (ResultSet rs = ps.executeQuery()) {
				Map<Long, BridgeRoom> bridgeRooms = doExport(0, null, null, rs);
				for (Map.Entry<Long, BridgeRoom> entry : bridgeRooms.entrySet()) {
					AbstractBridge bridge = batches.get(entry.getKey());
					if (bridge == null) {
						exporter.logOrThrowErrorMessage("Failed to assign bridge room with id " + entry.getKey() + " to a building.");
						continue;
					}

					bridge.addInteriorBridgeRoom(new InteriorBridgeRoomProperty(entry.getValue()));
				}
			}
		} finally {
			batches.clear();
		}
	}

	protected Collection<BridgeRoom> doExport(AbstractBridge parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("bridge_id"));
	}
	
	@Override
	protected Collection<BridgeRoom> doExport(long id, BridgeRoom root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			return doExport(id, root, rootType, rs).values();
		}
	}

	private Map<Long, BridgeRoom> doExport(long id, BridgeRoom root, FeatureType rootType, ResultSet rs) throws CityGMLExportException, SQLException {
		long currentBridgeRoomId = 0;
		BridgeRoom bridgeRoom = null;
		ProjectionFilter projectionFilter = null;
		Map<Long, BridgeRoom> bridgeRooms = new HashMap<>();
		Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();

		long currentBoundarySurfaceId = 0;
		AbstractBoundarySurface boundarySurface = null;
		ProjectionFilter boundarySurfaceProjectionFilter = null;
		Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

		long currentOpeningId = 0;
		OpeningProperty openingProperty = null;
		ProjectionFilter openingProjectionFilter = null;
		Map<String, OpeningProperty> openingProperties = new HashMap<>();

		Set<Long> bridgeFurnitures = new HashSet<>();
		Set<Long> installations = new HashSet<>();
		Set<String> addresses = new HashSet<>();

		while (rs.next()) {
			long bridgeRoomId = rs.getLong("id");

			if (bridgeRoomId != currentBridgeRoomId || bridgeRoom == null) {
				currentBridgeRoomId = bridgeRoomId;

				bridgeRoom = bridgeRooms.get(bridgeRoomId);
				if (bridgeRoom == null) {
					FeatureType featureType;
					if (bridgeRoomId == id && root != null) {
						bridgeRoom = root;
						featureType = rootType;
					} else {
						if (hasObjectClassIdColumn) {
							// create bridge room object
							int objectClassId = rs.getInt("objectclass_id");
							bridgeRoom = exporter.createObject(objectClassId, BridgeRoom.class);
							if (bridgeRoom == null) {
								exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, bridgeRoomId) + " as bridge room object.");
								continue;
							}

							featureType = exporter.getFeatureType(objectClassId);
						} else {
							bridgeRoom = new BridgeRoom();
							featureType = exporter.getFeatureType(bridgeRoom);
						}
					}

					// get projection filter
					projectionFilter = exporter.getProjectionFilter(featureType);

					// export city object information
					cityObjectExporter.addBatch(bridgeRoom, bridgeRoomId, featureType, projectionFilter);

					if (projectionFilter.containsProperty("class", bridgeModule)) {
						String clazz = rs.getString("class");
						if (!rs.wasNull()) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString("class_codespace"));
							bridgeRoom.setClazz(code);
						}
					}

					if (projectionFilter.containsProperty("function", bridgeModule)) {
						for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
							Code function = new Code(splitValue.result(0));
							function.setCodeSpace(splitValue.result(1));
							bridgeRoom.addFunction(function);
						}
					}

					if (projectionFilter.containsProperty("usage", bridgeModule)) {
						for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
							Code usage = new Code(splitValue.result(0));
							usage.setCodeSpace(splitValue.result(1));
							bridgeRoom.addUsage(usage);
						}
					}

					if (lodFilter.isEnabled(4)) {
						// brid:lod4MultiSurface
						if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) {
							long geometryId = rs.getLong("lod4_multi_surface_id");
							if (!rs.wasNull())
								geometries.put(geometryId, new DefaultGeometrySetterHandler(bridgeRoom::setLod4MultiSurface));
						}

						// brid:lod4Solid
						if (projectionFilter.containsProperty("lod4Solid", bridgeModule)) {
							long geometryId = rs.getLong("lod4_solid_id");
							if (!rs.wasNull())
								geometries.put(geometryId, new DefaultGeometrySetterHandler(bridgeRoom::setLod4Solid));
						}
					}

					// delegate export of generic ADE properties
					if (bridgeRoomADEHookTables != null) {
						List<String> adeHookTables = retrieveADEHookTables(this.bridgeRoomADEHookTables, rs);
						if (adeHookTables != null)
							exporter.delegateToADEExporter(adeHookTables, bridgeRoom, bridgeRoomId, featureType, projectionFilter);
					}

					bridgeRoom.setLocalProperty("projection", projectionFilter);
					bridgeRooms.put(bridgeRoomId, bridgeRoom);
				} else
					projectionFilter = (ProjectionFilter) bridgeRoom.getLocalProperty("projection");
			}

			// brid:bridgeRoomInstallation
			if (lodFilter.isEnabled(4)
					&& projectionFilter.containsProperty("bridgeRoomInstallation", bridgeModule)) {
				long installationId = rs.getLong("inid");
				if (!rs.wasNull() && installations.add(installationId))
					bridgeInstallationExporter.addBatch(installationId, bridgeRoom);
			}

			// brid:interiorFurniture
			if (lodFilter.isEnabled(4)
					&& projectionFilter.containsProperty("interiorFurniture", bridgeModule)) {
				long bridgeFurnitureId = rs.getLong("bfid");
				if (!rs.wasNull() && bridgeFurnitures.add(bridgeFurnitureId)) {
					int objectClassId = rs.getInt("bfobjectclass_id");
					FeatureType featureType = exporter.getFeatureType(objectClassId);

					BridgeFurniture bridgeFurniture = bridgeFurnitureExporter.doExport(bridgeFurnitureId, featureType, "bf", bridgeFurnitureADEHookTables, rs);
					if (bridgeFurniture == null) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, bridgeFurnitureId) + " as bridge furniture object.");
						continue;
					}

					bridgeRoom.getInteriorFurniture().add(new InteriorFurnitureProperty(bridgeFurniture));
				}
			}

			if (!lodFilter.isEnabled(4)
					|| !projectionFilter.containsProperty("boundedBy", bridgeModule))
				continue;

			// brid:boundedBy
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

					bridgeRoom.getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
					boundarySurfaces.put(boundarySurfaceId, boundarySurface);
				} else
					boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
			}

			// continue if openings shall not be exported
			if (!boundarySurfaceProjectionFilter.containsProperty("opening", bridgeModule))
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
						if (exporter.lookupAndPutObjectUID(gmlId, openingId, objectClassId)) {
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
						opening.setId(exporter.generateNewGmlId(opening, gmlId));

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
					&& (openingProjectionFilter == null
					|| openingProjectionFilter.containsProperty("address", bridgeModule))) {
				long addressId = rs.getLong("oaid");
				if (!rs.wasNull() && addresses.add(currentOpeningId + "_" + addressId)) {
					AddressProperty addressProperty = addressExporter.doExport(addressId, "oa", addressADEHookTables, rs);
					if (addressProperty != null) {
						Door door = (Door) openingProperty.getOpening();
						door.addAddress(addressProperty);
					}
				}
			}
		}

		bridgeInstallationExporter.executeBatch();

		// export postponed geometries
		for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
			geometryExporter.addBatch(entry.getKey(), entry.getValue());

		return bridgeRooms;
	}
}
