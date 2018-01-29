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
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.InteriorHollowSpaceProperty;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.tunnel.TunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.TunnelPart;
import org.citygml4j.model.citygml.tunnel.TunnelPartProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;

public class DBTunnel extends AbstractFeatureExporter<AbstractTunnel> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBTunnelThematicSurface thematicSurfaceExporter;
	private DBTunnelInstallation tunnelInstallationExporter;
	private DBTunnelHollowSpace hollowSpaceExporter;
	private GMLConverter gmlConverter;

	private String tunnelModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;
	private boolean hasObjectClassIdColumn;
	private Set<String> adeHookTables;

	public DBTunnel(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractTunnel.class, connection, exporter);		

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL.getName());
		tunnelModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TUNNEL).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.TUNNEL.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("tunnel_parent_id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", tunnelModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", tunnelModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", tunnelModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("yearOfConstruction", tunnelModule)) select.addProjection(table.getColumn("year_of_construction"));
		if (projectionFilter.containsProperty("yearOfDemolition", tunnelModule)) select.addProjection(table.getColumn("year_of_demolition"));
		if (projectionFilter.containsProperty("lod1TerrainIntersection", tunnelModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
		if (projectionFilter.containsProperty("lod2TerrainIntersection", tunnelModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
		if (projectionFilter.containsProperty("lod3TerrainIntersection", tunnelModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
		if (projectionFilter.containsProperty("lod4TerrainIntersection", tunnelModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
		if (projectionFilter.containsProperty("lod2MultiCurve", tunnelModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_multi_curve")));
		if (projectionFilter.containsProperty("lod3MultiCurve", tunnelModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_multi_curve")));
		if (projectionFilter.containsProperty("lod4MultiCurve", tunnelModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_multi_curve")));
		if (projectionFilter.containsProperty("lod1Solid", tunnelModule)) select.addProjection(table.getColumn("lod1_solid_id"));
		if (projectionFilter.containsProperty("lod2Solid", tunnelModule)) select.addProjection(table.getColumn("lod2_solid_id"));
		if (projectionFilter.containsProperty("lod3Solid", tunnelModule)) select.addProjection(table.getColumn("lod3_solid_id"));
		if (projectionFilter.containsProperty("lod4Solid", tunnelModule)) select.addProjection(table.getColumn("lod4_solid_id"));
		if (projectionFilter.containsProperty("lod1MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
		if (projectionFilter.containsProperty("lod2MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (projectionFilter.containsProperty("lod3MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.TUNNEL);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}

		thematicSurfaceExporter = exporter.getExporter(DBTunnelThematicSurface.class);
		tunnelInstallationExporter = exporter.getExporter(DBTunnelInstallation.class);
		hollowSpaceExporter = exporter.getExporter(DBTunnelHollowSpace.class);
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	@Override
	protected boolean doExport(AbstractTunnel object, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
		String column = projectionFilter.containsProperty("consistsOfTunnelPart", tunnelModule) ? "tunnel_root_id" : "id";
		return !doExport(id, object, featureType, getOrCreateStatement(column)).isEmpty();
	}

	@Override
	protected Collection<AbstractTunnel> doExport(long id, AbstractTunnel root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			HashMap<Long, AbstractTunnel> tunnels = new HashMap<>();

			while (rs.next()) {
				long tunnelId = rs.getLong("id");
				AbstractTunnel tunnel = null;
				FeatureType featureType = null;

				if (tunnelId == id && root != null) {
					tunnel = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						int objectClassId = rs.getInt("objectclass_id");
						featureType = exporter.getFeatureType(objectClassId);
						if (featureType == null)
							continue;

						// create tunnel object
						tunnel = exporter.createObject(objectClassId, AbstractTunnel.class);
						if (tunnel == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, tunnelId) + " as tunnel object.");
							continue;
						}
					} else {
						tunnel = new TunnelPart();
						featureType = exporter.getFeatureType(tunnel);
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				// export city object information
				boolean success = cityObjectExporter.doExport(tunnel, tunnelId, featureType, projectionFilter);
				if (!success) {
					if (tunnel == root)
						return Collections.emptyList();
					else if (featureType.isSetTopLevel())
						continue;
				}

				if (projectionFilter.containsProperty("class", tunnelModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						tunnel.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", tunnelModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						tunnel.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", tunnelModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						tunnel.addUsage(usage);
					}
				}

				if (projectionFilter.containsProperty("yearOfConstruction", tunnelModule)) {
					Date yearOfConstruction = rs.getDate("year_of_construction");				
					if (!rs.wasNull()) {						
						GregorianCalendar calendar = new GregorianCalendar();
						calendar.setTime(yearOfConstruction);
						tunnel.setYearOfConstruction(calendar);
					}
				}

				if (projectionFilter.containsProperty("yearOfDemolition", tunnelModule)) {
					Date yearOfDemolition = rs.getDate("year_of_demolition");
					if (!rs.wasNull()) {
						GregorianCalendar calendar = new GregorianCalendar();
						calendar.setTime(yearOfDemolition);
						tunnel.setYearOfDemolition(calendar);
					}
				}

				// bldg:boundedBy
				if (projectionFilter.containsProperty("boundedBy", tunnelModule) 
						&& lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
					for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport(tunnel, tunnelId))
						tunnel.addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
				}

				// tun:outerTunnelInstallation and tun:interiorTunnelInstallation
				if (lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
					for (AbstractCityObject installation : tunnelInstallationExporter.doExport(tunnel, tunnelId, projectionFilter)) {
						if (installation instanceof TunnelInstallation)
							tunnel.addOuterTunnelInstallation(new TunnelInstallationProperty((TunnelInstallation)installation));
						else if (installation instanceof IntTunnelInstallation)
							tunnel.addInteriorTunnelInstallation(new IntTunnelInstallationProperty((IntTunnelInstallation)installation));
					}
				}

				// tun:interiorHollowSpace
				if (projectionFilter.containsProperty("interiorHollowSpace", tunnelModule)
						&& lodFilter.isEnabled(4)) {
					for (HollowSpace hollowSpace : hollowSpaceExporter.doExport(tunnel, tunnelId))
						tunnel.addInteriorHollowSpace(new InteriorHollowSpaceProperty(hollowSpace));
				}

				// tun:lodXTerrainIntersectionCurve
				LodIterator lodIterator = lodFilter.iterator(1, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("TerrainIntersection").toString(), tunnelModule))
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
								tunnel.setLod1TerrainIntersection(multiCurveProperty);
								break;
							case 2:
								tunnel.setLod2TerrainIntersection(multiCurveProperty);
								break;
							case 3:
								tunnel.setLod3TerrainIntersection(multiCurveProperty);
								break;
							case 4:
								tunnel.setLod4TerrainIntersection(multiCurveProperty);
								break;
							}
						}
					}
				}

				// tun:lodXMultiCurve
				lodIterator = lodFilter.iterator(2, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiCurve").toString(), tunnelModule))
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
								tunnel.setLod2MultiCurve(multiCurveProperty);
								break;
							case 3:
								tunnel.setLod3MultiCurve(multiCurveProperty);
								break;
							case 4:
								tunnel.setLod4MultiCurve(multiCurveProperty);
								break;
							}
						}
					}
				}

				// tun:lodXSolid
				lodIterator = lodFilter.iterator(1, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("Solid").toString(), tunnelModule))
						continue;

					long surfaceGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_solid_id").toString());
					if (rs.wasNull())
						continue;

					SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
					if (geometry != null && (geometry.getType() == GMLClass.SOLID || geometry.getType() == GMLClass.COMPOSITE_SOLID)) {
						SolidProperty solidProperty = new SolidProperty();
						if (geometry.getGeometry() != null)
							solidProperty.setSolid((AbstractSolid)geometry.getGeometry());
						else
							solidProperty.setHref(geometry.getReference());

						switch (lod) {
						case 1:
							tunnel.setLod1Solid(solidProperty);
							break;
						case 2:
							tunnel.setLod2Solid(solidProperty);
							break;
						case 3:
							tunnel.setLod3Solid(solidProperty);
							break;
						case 4:
							tunnel.setLod4Solid(solidProperty);
							break;
						}
					}
				}

				// tun:lodXMultiSurface
				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiSurface").toString(), tunnelModule))
						continue;

					long surfaceGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_multi_surface_id").toString());
					if (rs.wasNull())
						continue;

					SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.getGeometry() != null)
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getReference());

						switch (lod) {
						case 1:
							tunnel.setLod1MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							tunnel.setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 3:
							tunnel.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 4:
							tunnel.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}

				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, tunnel, tunnelId, featureType, projectionFilter);
				}

				tunnel.setLocalProperty("parent", rs.getLong("tunnel_parent_id"));
				tunnel.setLocalProperty("projection", projectionFilter);
				tunnels.put(tunnelId, tunnel);
			}

			// rebuild tunnel part hierarchy
			List<AbstractTunnel> result = new ArrayList<>();
			for (Entry<Long, AbstractTunnel> entry : tunnels.entrySet()) {
				AbstractTunnel tunnel = entry.getValue();				
				long tunnelId = entry.getKey();
				long parentId = (Long)tunnel.getLocalProperty("parent");

				if (parentId == 0) {
					result.add(tunnel);
					continue;
				}

				if (!(tunnel instanceof TunnelPart)) {
					exporter.logOrThrowErrorMessage("Expected " + exporter.getObjectSignature(exporter.getFeatureType(tunnel), tunnelId) + " to be a tunnel part.");
					continue;
				}

				AbstractTunnel parent = tunnels.get(parentId);
				if (parent != null) {
					ProjectionFilter projectionFilter = (ProjectionFilter)parent.getLocalProperty("projection");				
					if (projectionFilter.containsProperty("consistsOfTunnelPart", tunnelModule))
						parent.addConsistsOfTunnelPart(new TunnelPartProperty((TunnelPart)tunnel));
				}
			}

			// check whether lod filter is satisfied
			if (!lodFilter.preservesGeometry()) {
				for (AbstractTunnel tmp : result) {
					for (Iterator<TunnelPartProperty> iter = tmp.getConsistsOfTunnelPart().iterator(); iter.hasNext(); ) {
						if (!exporter.satisfiesLodFilter(iter.next().getTunnelPart()))
							iter.remove();
					}
				}
			}

			return result;
		}
	}

}
