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
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractOpening;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.OpeningProperty;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DBTunnelThematicSurface extends AbstractFeatureExporter<AbstractBoundarySurface> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBImplicitGeometry implicitGeometryExporter;

	private final String tunnelModule;
	private final LodFilter lodFilter;
	private final boolean useXLink;
	private Set<String> surfaceADEHookTables;
	private Set<String> openingADEHookTables;

	public DBTunnelThematicSurface(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractBoundarySurface.class, connection, exporter);

		CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_THEMATIC_SURFACE.getName());
		CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_OPENING.getName());
		tunnelModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TUNNEL).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		useXLink = exporter.getExportConfig().getXlink().getFeature().isModeXLink();

		table = new Table(TableEnum.TUNNEL_THEMATIC_SURFACE.getName(), schema);
		Table opening = new Table(TableEnum.TUNNEL_OPENING.getName(), schema);

		select = new Select().addProjection(table.getColumn("id", "tsid"), table.getColumn("objectclass_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("lod2MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("lod3MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("lod4MultiSurface", tunnelModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("opening", tunnelModule)) {
			Table openingToThemSurface = new Table(TableEnum.TUNNEL_OPEN_TO_THEM_SRF.getName(), schema);
			select.addJoin(JoinFactory.left(openingToThemSurface, "tunnel_thematic_surface_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
			.addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("tunnel_opening_id")))
			.addProjection(opening.getColumn("id", "opid"), opening.getColumn("objectclass_id", "opobjectclass_id"));
			if (openingProjectionFilter.containsProperty("lod3MultiSurface", tunnelModule)) select.addProjection(opening.getColumn("lod3_multi_surface_id", "oplod3_multi_surface_id"));
			if (openingProjectionFilter.containsProperty("lod4MultiSurface", tunnelModule)) select.addProjection(opening.getColumn("lod4_multi_surface_id", "oplod4_multi_surface_id"));
			if (openingProjectionFilter.containsProperty("lod3ImplicitRepresentation", tunnelModule)) 
				select.addProjection(opening.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(opening.getColumn("lod3_implicit_ref_point")), opening.getColumn("lod3_implicit_transformation"));
			if (openingProjectionFilter.containsProperty("lod4ImplicitRepresentation", tunnelModule)) 
				select.addProjection(opening.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(opening.getColumn("lod4_implicit_ref_point")), opening.getColumn("lod4_implicit_transformation"));
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			surfaceADEHookTables = exporter.getADEHookTables(TableEnum.TUNNEL_THEMATIC_SURFACE);
			openingADEHookTables = exporter.getADEHookTables(TableEnum.TUNNEL_OPENING);
			if (surfaceADEHookTables != null) addJoinsToADEHookTables(surfaceADEHookTables, table);
			if (openingADEHookTables != null) addJoinsToADEHookTables(openingADEHookTables, opening);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);	
	}

	protected Collection<AbstractBoundarySurface> doExport(AbstractTunnel parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("tunnel_id"));
	}

	protected Collection<AbstractBoundarySurface> doExport(TunnelInstallation parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("tunnel_installation_id"));
	}

	protected Collection<AbstractBoundarySurface> doExport(IntTunnelInstallation parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("tunnel_installation_id"));
	}

	protected Collection<AbstractBoundarySurface> doExport(HollowSpace parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("tunnel_hollow_space_id"));
	}

	@Override
	protected Collection<AbstractBoundarySurface> doExport(long id, AbstractBoundarySurface root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			long currentBoundarySurfaceId = 0;
			AbstractBoundarySurface boundarySurface = null;
			ProjectionFilter boundarySurfaceProjectionFilter = null;
			HashMap<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

			while (rs.next()) {
				long boundarySurfaceId = rs.getLong("tsid");

				if (boundarySurfaceId != currentBoundarySurfaceId || boundarySurface == null) {
					currentBoundarySurfaceId = boundarySurfaceId;

					boundarySurface = boundarySurfaces.get(boundarySurfaceId);
					if (boundarySurface == null) {
						FeatureType featureType;
						if (boundarySurfaceId == id && root != null) {
							boundarySurface = root;
							featureType = rootType;						
						} else {
							int objectClassId = rs.getInt("objectclass_id");
							featureType = exporter.getFeatureType(objectClassId);
							if (featureType == null)
								continue;

							// create boundary surface object
							boundarySurface = exporter.createObject(featureType.getObjectClassId(), AbstractBoundarySurface.class);
							if (boundarySurface == null) {
								exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, boundarySurfaceId) + " as boundary surface object.");
								continue;
							}
						}

						// get projection filter
						boundarySurfaceProjectionFilter = exporter.getProjectionFilter(featureType);

						// export city object information
						cityObjectExporter.doExport(boundarySurface, boundarySurfaceId, featureType, boundarySurfaceProjectionFilter);

						LodIterator lodIterator = lodFilter.iterator(2, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!boundarySurfaceProjectionFilter.containsProperty("lod" + lod + "MultiSurface", tunnelModule))
								continue;

							long lodMultiSurfaceId = rs.getLong("lod" + lod + "_multi_surface_id");
							if (rs.wasNull())
								continue;

							SurfaceGeometry geometry = geometryExporter.doExport(lodMultiSurfaceId);
							if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
								if (geometry.isSetGeometry())
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getReference());

								switch (lod) {
								case 2:
									boundarySurface.setLod2MultiSurface(multiSurfaceProperty);
									break;
								case 3:
									boundarySurface.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									boundarySurface.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}

						// delegate export of generic ADE properties
						if (surfaceADEHookTables != null) {
							List<String> adeHookTables = retrieveADEHookTables(surfaceADEHookTables, rs);
							if (adeHookTables != null)
								exporter.delegateToADEExporter(adeHookTables, boundarySurface, boundarySurfaceId, featureType, boundarySurfaceProjectionFilter);
						}

						boundarySurface.setLocalProperty("projection", boundarySurfaceProjectionFilter);
						boundarySurfaces.put(boundarySurfaceId, boundarySurface);
					} else
						boundarySurfaceProjectionFilter = (ProjectionFilter)boundarySurface.getLocalProperty("projection");
				}

				// continue if openings shall not be exported
				if (!lodFilter.containsLodGreaterThanOrEuqalTo(3)
						|| !boundarySurfaceProjectionFilter.containsProperty("opening", tunnelModule))
					continue;

				long openingId = rs.getLong("opid");
				if (rs.wasNull())
					continue;

				// create new opening object
				int objectClassId = rs.getInt("opobjectclass_id");
				AbstractOpening opening = exporter.createObject(objectClassId, AbstractOpening.class);
				if (opening == null) {
					exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, openingId) + " as opening object.");
					continue;
				}

				// get projection filter
				FeatureType openingType = exporter.getFeatureType(objectClassId);
				ProjectionFilter openingProjectionFilter = exporter.getProjectionFilter(openingType);

				// export city object information
				cityObjectExporter.doExport(opening, openingId, openingType, openingProjectionFilter);

				if (opening.isSetId()) {
					// process xlink
					if (exporter.lookupAndPutObjectUID(opening.getId(), openingId, objectClassId)) {
						if (useXLink) {
							OpeningProperty openingProperty = new OpeningProperty();
							openingProperty.setHref("#" + opening.getId());
							boundarySurface.addOpening(openingProperty);
							continue;
						} else
							opening.setId(exporter.generateNewGmlId(opening));
					}
				}

				LodIterator lodIterator = lodFilter.iterator(3, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!openingProjectionFilter.containsProperty("lod" + lod + "MultiSurface", tunnelModule))
						continue;

					long lodMultiSurfaceId = rs.getLong("oplod" + lod + "_multi_surface_id");
					if (rs.wasNull()) 
						continue;

					SurfaceGeometry geometry = geometryExporter.doExport(lodMultiSurfaceId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.isSetGeometry())
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getReference());

						switch (lod) {
						case 3:
							opening.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 4:
							opening.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!openingProjectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", tunnelModule))
						continue;

					// get implicit geometry details
					long implicitGeometryId = rs.getLong("lod" + lod + "_implicit_rep_id");
					if (rs.wasNull())
						continue;

					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject("lod" + lod + "_implicit_ref_point");
					if (!rs.wasNull())
						referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString("lod" + lod + "_implicit_transformation");

					ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 3:
							opening.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							opening.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}

				// delegate export of generic ADE properties
				if (openingADEHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(openingADEHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, opening, openingId, openingType, openingProjectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(opening))
					continue;

				OpeningProperty openingProperty = new OpeningProperty(opening);
				boundarySurface.addOpening(openingProperty);
			}

			// check whether lod filter is satisfied
			if (!lodFilter.preservesGeometry()) {
				for (Iterator<Entry<Long, AbstractBoundarySurface>> iter = boundarySurfaces.entrySet().iterator(); iter.hasNext(); ) {
					boundarySurface = iter.next().getValue();
					if (!exporter.satisfiesLodFilter(boundarySurface))
						iter.remove();
				}
			}

			return boundarySurfaces.values();
		}
	}

}
