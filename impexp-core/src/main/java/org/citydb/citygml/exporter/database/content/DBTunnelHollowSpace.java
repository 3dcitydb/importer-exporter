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
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBTunnelHollowSpace extends AbstractFeatureExporter<HollowSpace> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBTunnelInstallation tunnelInstallationExporter;
	private final DBTunnelThematicSurface thematicSurfaceExporter;
	private final DBTunnelFurniture tunnelFurnitureExporter;

	private final String tunnelModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private List<Table> adeHookTables;

	public DBTunnelHollowSpace(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(HollowSpace.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_HOLLOW_SPACE.getName());
		tunnelModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TUNNEL).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.TUNNEL_HOLLOW_SPACE.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", tunnelModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", tunnelModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", tunnelModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));		
		if (projectionFilter.containsProperty("lod4MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4Solid", tunnelModule)) select.addProjection(table.getColumn("lod4_solid_id"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport())
			adeHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_HOLLOW_SPACE, table);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		tunnelInstallationExporter = exporter.getExporter(DBTunnelInstallation.class);
		thematicSurfaceExporter = exporter.getExporter(DBTunnelThematicSurface.class);
		tunnelFurnitureExporter = exporter.getExporter(DBTunnelFurniture.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Collection<HollowSpace> doExport(AbstractTunnel parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("tunnel_id"));
	}

	@Override
	protected Collection<HollowSpace> doExport(long id, HollowSpace root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<HollowSpace> hollowSpaces = new ArrayList<>();

			while (rs.next()) {
				long hollowSpaceId = rs.getLong("id");
				HollowSpace hollowSpace;
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
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

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
					// tun:boundedBy
					if (projectionFilter.containsProperty("boundedBy", tunnelModule)) {
						for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport(hollowSpace, hollowSpaceId))
							hollowSpace.addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
					}

					// tun:hollowSpaceInstallation
					for (AbstractCityObject installation : tunnelInstallationExporter.doExport(hollowSpace, hollowSpaceId, projectionFilter)) {
						if (installation instanceof IntTunnelInstallation)
							hollowSpace.addHollowSpaceInstallation(new IntTunnelInstallationProperty((IntTunnelInstallation)installation));
					}

					// tun:interiorFurniture
					if (projectionFilter.containsProperty("interiorFurniture", tunnelModule)) {
						for (TunnelFurniture furniture : tunnelFurnitureExporter.doExport(hollowSpace, hollowSpaceId))
							hollowSpace.addInteriorFurniture(new InteriorFurnitureProperty(furniture));
					}

					// brid:lod4MultiSurface
					if (projectionFilter.containsProperty("lod4MultiSurface", tunnelModule)) {					
						long geometryId = rs.getLong("lod4_multi_surface_id");
						if (!rs.wasNull())
							geometryExporter.addBatch(geometryId, hollowSpace::setLod4MultiSurface);
					}

					// brid:lod4Solid
					if (projectionFilter.containsProperty("lod4Solid", tunnelModule)) {
						long geometryId = rs.getLong("lod4_solid_id");
						if (!rs.wasNull())
							geometryExporter.addBatch(geometryId, hollowSpace::setLod4Solid);
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, hollowSpace, hollowSpaceId, featureType, projectionFilter);
				}

				hollowSpaces.add(hollowSpace);
			}

			return hollowSpaces;
		}
	}

}
