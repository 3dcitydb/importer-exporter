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

import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.operation.exporter.util.SplitValue;
import org.citydb.core.operation.exporter.util.DefaultGeometrySetterHandler;
import org.citydb.core.operation.exporter.util.GeometrySetterHandler;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractOpening;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.tunnel.InteriorHollowSpaceProperty;
import org.citygml4j.model.citygml.tunnel.OpeningProperty;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
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

public class DBTunnelHollowSpace extends AbstractFeatureExporter<HollowSpace> {
	private final Map<Long, AbstractTunnel> batches;
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBTunnelInstallation tunnelInstallationExporter;
	private final DBTunnelThematicSurface thematicSurfaceExporter;
	private final DBTunnelOpening openingExporter;
	private final DBTunnelFurniture tunnelFurnitureExporter;

	private final int batchSize;
	private final String tunnelModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private final boolean useXLink;
	private final List<Table> hollowSpaceADEHookTables;
	private List<Table> surfaceADEHookTables;
	private List<Table> openingADEHookTables;
	private List<Table> tunnelFurnitureADEHookTables;

	public DBTunnelHollowSpace(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(HollowSpace.class, connection, exporter);

		batches = new LinkedHashMap<>();
		batchSize = exporter.getFeatureBatchSize();
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		tunnelInstallationExporter = exporter.getExporter(DBTunnelInstallation.class);
		thematicSurfaceExporter = exporter.getExporter(DBTunnelThematicSurface.class);
		openingExporter = exporter.getExporter(DBTunnelOpening.class);
		tunnelFurnitureExporter = exporter.getExporter(DBTunnelFurniture.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_HOLLOW_SPACE.getName());
		tunnelModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TUNNEL).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		useXLink = exporter.getInternalConfig().isExportFeatureReferences();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.TUNNEL_HOLLOW_SPACE.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", tunnelModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", tunnelModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", tunnelModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
			if (projectionFilter.containsProperty("lod4Solid", tunnelModule)) select.addProjection(table.getColumn("lod4_solid_id"));
			if (projectionFilter.containsProperty("boundedBy", tunnelModule)) {
				CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_THEMATIC_SURFACE.getName());
				Table thematicSurface = new Table(TableEnum.TUNNEL_THEMATIC_SURFACE.getName(), schema);
				thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
						.addJoin(JoinFactory.left(thematicSurface, "tunnel_hollow_space_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
				if (boundarySurfaceProjectionFilter.containsProperty("opening", tunnelModule)) {
					CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_OPENING.getName());
					Table opening = new Table(TableEnum.TUNNEL_OPENING.getName(), schema);
					Table openingToThemSurface = new Table(TableEnum.TUNNEL_OPEN_TO_THEM_SRF.getName(), schema);
					Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
					openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
							.addProjection(cityObject.getColumn("gmlid", "opgmlid"))
							.addJoin(JoinFactory.left(openingToThemSurface, "tunnel_thematic_surface_id", ComparisonName.EQUAL_TO, thematicSurface.getColumn("id")))
							.addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("tunnel_opening_id")))
							.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
					openingADEHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_OPENING, opening);
				}
				surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.THEMATIC_SURFACE, thematicSurface);
			}
			if (projectionFilter.containsProperty("interiorFurniture", tunnelModule)) {
				CombinedProjectionFilter tunnelFurnitureProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_FURNITURE.getName());
				Table tunnelFurniture = new Table(TableEnum.TUNNEL_FURNITURE.getName(), schema);
				tunnelFurnitureExporter.addProjection(select, tunnelFurniture, tunnelFurnitureProjectionFilter, "tf")
						.addJoin(JoinFactory.left(tunnelFurniture, "tunnel_hollow_space_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
				tunnelFurnitureADEHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_FURNITURE, tunnelFurniture);
			}
			if (projectionFilter.containsProperty("hollowSpaceInstallation", tunnelModule)) {
				Table installation = new Table(TableEnum.TUNNEL_INSTALLATION.getName(), schema);
				select.addProjection(installation.getColumn("id", "inid"))
						.addJoin(JoinFactory.left(installation, "tunnel_hollow_space_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
			}
		}
		hollowSpaceADEHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_HOLLOW_SPACE, table);
	}

	protected void addBatch(long id, AbstractTunnel parent) throws CityGMLExportException, SQLException {
		batches.put(id, parent);
		if (batches.size() == batchSize)
			executeBatch();
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
				ps = getOrCreateBulkStatement(batchSize);
				prepareBulkStatement(ps, batches.keySet().toArray(new Long[0]), batchSize);
			}

			try (ResultSet rs = ps.executeQuery()) {
				Map<Long, HollowSpace> hollowSpaces = doExport(0, null, null, rs);
				for (Map.Entry<Long, HollowSpace> entry : hollowSpaces.entrySet()) {
					AbstractTunnel tunnel = batches.get(entry.getKey());
					if (tunnel == null) {
						exporter.logOrThrowErrorMessage("Failed to assign hollow space with id " + entry.getKey() + " to a tunnel.");
						continue;
					}

					tunnel.addInteriorHollowSpace(new InteriorHollowSpaceProperty(entry.getValue()));
				}
			}
		} finally {
			batches.clear();
		}
	}

	protected Collection<HollowSpace> doExport(AbstractTunnel parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("tunnel_id"));
	}

	@Override
	protected Collection<HollowSpace> doExport(long id, HollowSpace root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			return doExport(id, root, rootType, rs).values();
		}
	}

	private Map<Long, HollowSpace> doExport(long id, HollowSpace root, FeatureType rootType, ResultSet rs) throws CityGMLExportException, SQLException {
		long currentHollowSpaceId = 0;
		HollowSpace hollowSpace = null;
		ProjectionFilter projectionFilter = null;
		Map<Long, HollowSpace> hollowSpaces = new HashMap<>();
		Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
		Map<Long, List<String>> adeHookTables = hollowSpaceADEHookTables != null ? new HashMap<>() : null;

		long currentBoundarySurfaceId = 0;
		AbstractBoundarySurface boundarySurface = null;
		ProjectionFilter boundarySurfaceProjectionFilter = null;
		Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

		long currentOpeningId = 0;
		OpeningProperty openingProperty = null;
		ProjectionFilter openingProjectionFilter = null;
		Map<String, OpeningProperty> openingProperties = new HashMap<>();

		Set<Long> tunnelFurnitures = new HashSet<>();
		Set<Long> installations = new HashSet<>();

		while (rs.next()) {
			long hollowSpaceId = rs.getLong("id");

			if (hollowSpaceId != currentHollowSpaceId || hollowSpace == null) {
				currentHollowSpaceId = hollowSpaceId;

				hollowSpace = hollowSpaces.get(hollowSpaceId);
				if (hollowSpace == null) {
					FeatureType featureType;
					if (hollowSpaceId == id && root != null) {
						hollowSpace = root;
						featureType = rootType;
					} else {
						if (hasObjectClassIdColumn) {
							// create hollow space object
							int objectClassId = rs.getInt("objectclass_id");
							hollowSpace = exporter.createObject(objectClassId, HollowSpace.class);
							if (hollowSpace == null) {
								exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, hollowSpaceId) + " as hollow space object.");
								continue;
							}

							featureType = exporter.getFeatureType(objectClassId);
						} else {
							hollowSpace = new HollowSpace();
							featureType = exporter.getFeatureType(hollowSpace);
						}
					}

					// get projection filter
					projectionFilter = exporter.getProjectionFilter(featureType);

					// export city object information
					cityObjectExporter.addBatch(hollowSpace, hollowSpaceId, featureType, projectionFilter);

					if (projectionFilter.containsProperty("class", tunnelModule)) {
						String clazz = rs.getString("class");
						if (!rs.wasNull()) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString("class_codespace"));
							hollowSpace.setClazz(code);
						}
					}

					if (projectionFilter.containsProperty("function", tunnelModule)) {
						for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
							Code function = new Code(splitValue.result(0));
							function.setCodeSpace(splitValue.result(1));
							hollowSpace.addFunction(function);
						}
					}

					if (projectionFilter.containsProperty("usage", tunnelModule)) {
						for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
							Code usage = new Code(splitValue.result(0));
							usage.setCodeSpace(splitValue.result(1));
							hollowSpace.addUsage(usage);
						}
					}

					if (lodFilter.isEnabled(4)) {
						// tun:lod4MultiSurface
						if (projectionFilter.containsProperty("lod4MultiSurface", tunnelModule)) {
							long geometryId = rs.getLong("lod4_multi_surface_id");
							if (!rs.wasNull())
								geometries.put(geometryId, new DefaultGeometrySetterHandler(hollowSpace::setLod4MultiSurface));
						}

						// tun:lod4Solid
						if (projectionFilter.containsProperty("lod4Solid", tunnelModule)) {
							long geometryId = rs.getLong("lod4_solid_id");
							if (!rs.wasNull())
								geometries.put(geometryId, new DefaultGeometrySetterHandler(hollowSpace::setLod4Solid));
						}
					}

					// get tables of ADE hook properties
					if (hollowSpaceADEHookTables != null) {
						List<String> tables = retrieveADEHookTables(hollowSpaceADEHookTables, rs);
						if (tables != null) {
							adeHookTables.put(hollowSpaceId, tables);
							hollowSpace.setLocalProperty("type", featureType);
						}
					}

					hollowSpace.setLocalProperty("projection", projectionFilter);
					hollowSpaces.put(hollowSpaceId, hollowSpace);
				} else
					projectionFilter = (ProjectionFilter) hollowSpace.getLocalProperty("projection");
			}

			// tun:hollowSpaceInstallation
			if (lodFilter.isEnabled(4)
					&& projectionFilter.containsProperty("hollowSpaceInstallation", tunnelModule)) {
				long installationId = rs.getLong("inid");
				if (!rs.wasNull() && installations.add(installationId))
					tunnelInstallationExporter.addBatch(installationId, hollowSpace);
			}

			// tun:interiorFurniture
			if (lodFilter.isEnabled(4)
					&& projectionFilter.containsProperty("interiorFurniture", tunnelModule)) {
				long tunnelFurnitureId = rs.getLong("tfid");
				if (!rs.wasNull() && tunnelFurnitures.add(tunnelFurnitureId)) {
					int objectClassId = rs.getInt("tfobjectclass_id");
					FeatureType featureType = exporter.getFeatureType(objectClassId);

					TunnelFurniture tunnelFurniture = tunnelFurnitureExporter.doExport(tunnelFurnitureId, featureType, "tf", tunnelFurnitureADEHookTables, rs);
					if (tunnelFurniture == null) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, tunnelFurnitureId) + " as tunnel furniture object.");
						continue;
					}

					hollowSpace.getInteriorFurniture().add(new InteriorFurnitureProperty(tunnelFurniture));
				}
			}

			if (!lodFilter.isEnabled(4)
					|| !projectionFilter.containsProperty("boundedBy", tunnelModule))
				continue;

			// tun:boundedBy
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

					hollowSpace.getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
					boundarySurfaces.put(boundarySurfaceId, boundarySurface);
				} else
					boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
			}

			// continue if openings shall not be exported
			if (!boundarySurfaceProjectionFilter.containsProperty("opening", tunnelModule))
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
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, openingId) + " as tunnel opening object.");
						continue;
					}

					if (generateNewGmlId)
						opening.setId(exporter.generateFeatureGmlId(opening, gmlId));

					openingProperty = new OpeningProperty(opening);
					boundarySurface.getOpening().add(openingProperty);
					openingProperties.put(key, openingProperty);
				}
			}
		}

		tunnelInstallationExporter.executeBatch();

		// export postponed geometries
		for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
			geometryExporter.addBatch(entry.getKey(), entry.getValue());

		// delegate export of generic ADE properties
		if (adeHookTables != null) {
			for (Map.Entry<Long, List<String>> entry : adeHookTables.entrySet()) {
				long hollowSpaceId = entry.getKey();
				hollowSpace = hollowSpaces.get(hollowSpaceId);
				exporter.delegateToADEExporter(entry.getValue(), hollowSpace, hollowSpaceId,
						(FeatureType) hollowSpace.getLocalProperty("type"),
						(ProjectionFilter) hollowSpace.getLocalProperty("projection"));
			}
		}

		return hollowSpaces;
	}
}
