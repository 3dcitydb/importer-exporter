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
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DBThematicSurface extends AbstractFeatureExporter<AbstractBoundarySurface> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final DBAddress addressExporter;

	private final String buildingModule;
	private final LodFilter lodFilter;
	private final boolean useXLink;
	private Set<String> surfaceADEHookTables;
	private Set<String> openingADEHookTables;
	private Set<String> addressADEHookTables;

	public DBThematicSurface(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractBoundarySurface.class, connection, exporter);

		CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.THEMATIC_SURFACE.getName());
		CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.OPENING.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();		
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		useXLink = exporter.getExportConfig().getXlink().getFeature().isModeXLink();

		table = new Table(TableEnum.THEMATIC_SURFACE.getName(), schema);
		Table opening = new Table(TableEnum.OPENING.getName(), schema);
		Table address = new Table(TableEnum.ADDRESS.getName(), schema);

		select = new Select().addProjection(table.getColumn("id", "tsid"), table.getColumn("objectclass_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("lod2MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("lod3MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("lod4MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule)) {
			Table openingToThemSurface = new Table(TableEnum.OPENING_TO_THEM_SURFACE.getName(), schema);
			select.addJoin(JoinFactory.left(openingToThemSurface, "thematic_surface_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
			.addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("opening_id")))
			.addProjection(opening.getColumn("id", "opid"), opening.getColumn("objectclass_id", "opobjectclass_id"));
			if (openingProjectionFilter.containsProperty("lod3MultiSurface", buildingModule)) select.addProjection(opening.getColumn("lod3_multi_surface_id", "oplod3_multi_surface_id"));
			if (openingProjectionFilter.containsProperty("lod4MultiSurface", buildingModule)) select.addProjection(opening.getColumn("lod4_multi_surface_id", "oplod4_multi_surface_id"));
			if (openingProjectionFilter.containsProperty("lod3ImplicitRepresentation", buildingModule)) 
				select.addProjection(opening.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(opening.getColumn("lod3_implicit_ref_point")), opening.getColumn("lod3_implicit_transformation"));
			if (openingProjectionFilter.containsProperty("lod4ImplicitRepresentation", buildingModule)) 
				select.addProjection(opening.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(opening.getColumn("lod4_implicit_ref_point")), opening.getColumn("lod4_implicit_transformation"));
			if (openingProjectionFilter.containsProperty("address", buildingModule)) {
				select.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")))
				.addProjection(opening.getColumn("address_id"), address.getColumn("street"), address.getColumn("house_number"), address.getColumn("po_box"), address.getColumn("zip_code"), address.getColumn("city"),
						address.getColumn("state"), address.getColumn("country"), address.getColumn("xal_source"), exporter.getGeometryColumn(address.getColumn("multi_point")));
			}
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			surfaceADEHookTables = exporter.getADEHookTables(TableEnum.THEMATIC_SURFACE);
			openingADEHookTables = exporter.getADEHookTables(TableEnum.OPENING);
			addressADEHookTables = exporter.getADEHookTables(TableEnum.ADDRESS);
			if (surfaceADEHookTables != null) addJoinsToADEHookTables(surfaceADEHookTables, table);
			if (openingADEHookTables != null) addJoinsToADEHookTables(openingADEHookTables, opening);
			if (addressADEHookTables != null) addJoinsToADEHookTables(addressADEHookTables, address);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		addressExporter = exporter.getExporter(DBAddress.class);
	}

	protected Collection<AbstractBoundarySurface> doExport(AbstractBuilding parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("building_id"));
	}

	protected Collection<AbstractBoundarySurface> doExport(BuildingInstallation parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("building_installation_id"));
	}

	protected Collection<AbstractBoundarySurface> doExport(IntBuildingInstallation parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("building_installation_id"));
	}

	protected Collection<AbstractBoundarySurface> doExport(Room parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("room_id"));
	}

	@Override
	protected Collection<AbstractBoundarySurface> doExport(long id, AbstractBoundarySurface root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {	
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			long currentBoundarySurfaceId = 0;
			AbstractBoundarySurface boundarySurface = null;
			ProjectionFilter boundarySurfaceProjectionFilter = null;
			Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

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

							if (!boundarySurfaceProjectionFilter.containsProperty("lod" + lod + "MultiSurface", buildingModule))
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
						|| !boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule))
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

					if (!openingProjectionFilter.containsProperty("lod" + lod + "MultiSurface", buildingModule))
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

					if (!openingProjectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", buildingModule))
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

				if (opening instanceof Door && openingProjectionFilter.containsProperty("address", buildingModule)) {
					long addressId = rs.getLong("address_id");
					if (!rs.wasNull()) {
						AddressProperty addressProperty = addressExporter.doExport(rs);
						if (addressProperty != null) {
							((Door)opening).addAddress(addressProperty);

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
