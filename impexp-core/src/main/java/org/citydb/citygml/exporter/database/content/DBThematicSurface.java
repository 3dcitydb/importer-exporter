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
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBThematicSurface extends AbstractFeatureExporter<AbstractBoundarySurface> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBOpening openingExporter;
	private final DBAddress addressExporter;

	private final String buildingModule;
	private final LodFilter lodFilter;
	private final boolean useXLink;
	private final List<Table> surfaceADEHookTables;
	private List<Table> openingADEHookTables;
	private List<Table> addressADEHookTables;

	public DBThematicSurface(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractBoundarySurface.class, connection, exporter);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		openingExporter = exporter.getExporter(DBOpening.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		addressExporter = exporter.getExporter(DBAddress.class);

		CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.THEMATIC_SURFACE.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		useXLink = exporter.getExportConfig().getXlink().getFeature().isModeXLink();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.THEMATIC_SURFACE.getName(), schema);
		Table opening = new Table(TableEnum.OPENING.getName(), schema);
		Table address = new Table(TableEnum.ADDRESS.getName(), schema);

		select = addProjection(new Select(), table, boundarySurfaceProjectionFilter, "");
		if (boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule)
				&& lodFilter.containsLodGreaterThanOrEuqalTo(3)) {
			CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.OPENING.getName());
			Table openingToThemSurface = new Table(TableEnum.OPENING_TO_THEM_SURFACE.getName(), schema);
			Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
			openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
					.addProjection(cityObject.getColumn("gmlid", "opgmlid"))
					.addJoin(JoinFactory.left(openingToThemSurface, "thematic_surface_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
					.addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("opening_id")))
					.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
			if (openingProjectionFilter.containsProperty("address", buildingModule)) {
				addressExporter.addProjection(select, address, "oa")
						.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")));
				addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
			}
			openingADEHookTables = addJoinsToADEHookTables(TableEnum.OPENING, opening);
		}
		surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.THEMATIC_SURFACE, table);
	}

	protected Select addProjection(Select select, Table table, CombinedProjectionFilter projectionFilter, String prefix) {
		select.addProjection(table.getColumn("id", prefix + "id"), table.getColumn("objectclass_id", prefix + "objectclass_id"));
		if (lodFilter.isEnabled(2) && projectionFilter.containsProperty("lod2MultiSurface", buildingModule))
			select.addProjection(table.getColumn("lod2_multi_surface_id", prefix + "lod2_multi_surface_id"));
		if (lodFilter.isEnabled(3) && projectionFilter.containsProperty("lod3MultiSurface", buildingModule))
			select.addProjection(table.getColumn("lod3_multi_surface_id", prefix + "lod3_multi_surface_id"));
		if (lodFilter.isEnabled(4) && projectionFilter.containsProperty("lod4MultiSurface", buildingModule))
			select.addProjection(table.getColumn("lod4_multi_surface_id", prefix + "lod4_multi_surface_id"));

		return select;
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
			ProjectionFilter projectionFilter = null;
			Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

			while (rs.next()) {
				long boundarySurfaceId = rs.getLong("id");

				if (boundarySurfaceId != currentBoundarySurfaceId || boundarySurface == null) {
					currentBoundarySurfaceId = boundarySurfaceId;

					boundarySurface = boundarySurfaces.get(boundarySurfaceId);
					if (boundarySurface == null) {
						FeatureType featureType;
						if (boundarySurfaceId == id && root != null) {
							boundarySurface = root;
							featureType = rootType;						
						} else {
							// create boundary surface object
							int objectClassId = rs.getInt("objectclass_id");
							boundarySurface = exporter.createObject(objectClassId, AbstractBoundarySurface.class);
							if (boundarySurface == null) {
								exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, boundarySurfaceId) + " as boundary surface object.");
								continue;
							}

							featureType = exporter.getFeatureType(objectClassId);
						}

						// get projection filter
						projectionFilter = exporter.getProjectionFilter(featureType);
						boundarySurface.setLocalProperty("projection", projectionFilter);

						doExport(boundarySurface, boundarySurfaceId, featureType, projectionFilter, "", surfaceADEHookTables, rs);
						boundarySurfaces.put(boundarySurfaceId, boundarySurface);
					} else 
						projectionFilter = (ProjectionFilter)boundarySurface.getLocalProperty("projection");
				}

				// continue if openings shall not be exported
				if (!lodFilter.containsLodGreaterThanOrEuqalTo(3)
						|| !projectionFilter.containsProperty("opening", buildingModule))
					continue;

				long openingId = rs.getLong("opid");
				if (rs.wasNull())
					continue;

				int objectClassId = rs.getInt("opobjectclass_id");

				// check whether we need an XLink
				String gmlId = rs.getString("opgmlid");
				boolean generateNewGmlId = false;
				if (!rs.wasNull()) {
					if (exporter.lookupAndPutObjectUID(gmlId, openingId, objectClassId)) {
						if (useXLink) {
							OpeningProperty openingProperty = new OpeningProperty();
							openingProperty.setHref("#" + gmlId);
							boundarySurface.addOpening(openingProperty);
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
				ProjectionFilter openingProjectionFilter = exporter.getProjectionFilter(featureType);

				if (opening instanceof Door && openingProjectionFilter.containsProperty("address", buildingModule)) {
					long addressId = rs.getLong("oaid");
					if (!rs.wasNull()) {
						AddressProperty addressProperty = addressExporter.doExport(addressId, "oa", addressADEHookTables, rs);
						if (addressProperty != null)
							((Door) opening).addAddress(addressProperty);
					}
				}

				boundarySurface.addOpening(new OpeningProperty(opening));
			}

			return boundarySurfaces.values();
		}
	}

	protected AbstractBoundarySurface doExport(long id, FeatureType featureType, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		AbstractBoundarySurface boundarySurface = null;
		if (featureType != null) {
			boundarySurface = exporter.createObject(featureType.getObjectClassId(), AbstractBoundarySurface.class);
			if (boundarySurface != null)
				doExport(boundarySurface, id, featureType, exporter.getProjectionFilter(featureType), prefix, adeHookTables, rs);
		}

		return boundarySurface;
	}

	private void doExport(AbstractBoundarySurface object, long id, FeatureType featureType, ProjectionFilter projectionFilter, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
		// export city object information
		cityObjectExporter.addBatch(object, id, featureType, projectionFilter);

		LodIterator lodIterator = lodFilter.iterator(2, 4);
		while (lodIterator.hasNext()) {
			int lod = lodIterator.next();

			if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", buildingModule))
				continue;

			long geometryId = rs.getLong(prefix + "lod" + lod + "_multi_surface_id");
			if (rs.wasNull())
				continue;

			switch (lod) {
				case 2:
					geometryExporter.addBatch(geometryId, object::setLod2MultiSurface);
					break;
				case 3:
					geometryExporter.addBatch(geometryId, object::setLod3MultiSurface);
					break;
				case 4:
					geometryExporter.addBatch(geometryId, object::setLod4MultiSurface);
					break;
			}
		}

		// delegate export of generic ADE properties
		if (adeHookTables != null) {
			List<String> tableNames = retrieveADEHookTables(adeHookTables, rs);
			if (tableNames != null)
				exporter.delegateToADEExporter(tableNames, object, id, featureType, projectionFilter);
		}
	}
}
