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
import org.citydb.citygml.exporter.util.GeometrySetter;
import org.citydb.citygml.exporter.util.GeometrySetterHandler;
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
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DBBuildingInstallation extends AbstractFeatureExporter<AbstractCityObject> {
	private final Map<Long, AbstractCityObject> batches;
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBThematicSurface thematicSurfaceExporter;
	private final DBOpening openingExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final DBAddress addressExporter;
	private final GMLConverter gmlConverter;
	
	private final String buildingModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean useXLink;
	private final List<Table> installationADEHookTables;
	private List<Table> surfaceADEHookTables;
	private List<Table> openingADEHookTables;
	private List<Table> addressADEHookTables;

	public DBBuildingInstallation(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractCityObject.class, connection, exporter);

		batches = new LinkedHashMap<>();
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		thematicSurfaceExporter = exporter.getExporter(DBThematicSurface.class);
		openingExporter = exporter.getExporter(DBOpening.class);
		addressExporter = exporter.getExporter(DBAddress.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BUILDING_INSTALLATION.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		useXLink = exporter.getExportConfig().getXlink().getFeature().isModeXLink();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		
		table = new Table(TableEnum.BUILDING_INSTALLATION.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", buildingModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", buildingModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", buildingModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2Geometry", buildingModule)) select.addProjection(table.getColumn("lod2_brep_id"), exporter.getGeometryColumn(table.getColumn("lod2_other_geom")));
			if (projectionFilter.containsProperty("lod2ImplicitRepresentation", buildingModule)) select.addProjection(table.getColumn("lod2_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod2_implicit_ref_point")), table.getColumn("lod2_implicit_transformation"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3Geometry", buildingModule)) select.addProjection(table.getColumn("lod3_brep_id"), exporter.getGeometryColumn(table.getColumn("lod3_other_geom")));
			if (projectionFilter.containsProperty("lod3ImplicitRepresentation", buildingModule)) select.addProjection(table.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point")), table.getColumn("lod3_implicit_transformation"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4Geometry", buildingModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", buildingModule)) select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));
		}
		if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
				&& projectionFilter.containsProperty("boundedBy", buildingModule)) {
			CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.THEMATIC_SURFACE.getName());
			Table thematicSurface = new Table(TableEnum.THEMATIC_SURFACE.getName(), schema);
			thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
					.addJoin(JoinFactory.left(thematicSurface, "building_installation_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
			if (lodFilter.containsLodGreaterThanOrEuqalTo(3)
					&& boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule)) {
				CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.OPENING.getName());
				Table opening = new Table(TableEnum.OPENING.getName(), schema);
				Table openingToThemSurface = new Table(TableEnum.OPENING_TO_THEM_SURFACE.getName(), schema);
				Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
				openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
						.addProjection(cityObject.getColumn("gmlid", "opgmlid"))
						.addJoin(JoinFactory.left(openingToThemSurface, "thematic_surface_id", ComparisonName.EQUAL_TO, thematicSurface.getColumn("id")))
						.addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("opening_id")))
						.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
				if (openingProjectionFilter.containsProperty("address", buildingModule)) {
					Table address = new Table(TableEnum.ADDRESS.getName(), schema);
					addressExporter.addProjection(select, address, "oa")
							.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")));
					addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
				}
				openingADEHookTables = addJoinsToADEHookTables(TableEnum.OPENING, opening);
			}
			surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.THEMATIC_SURFACE, table);
		}
		installationADEHookTables = addJoinsToADEHookTables(TableEnum.BUILDING_INSTALLATION, table);
	}

	protected void addBatch(long id, AbstractCityObject parent) {
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
				Map<Long, AbstractCityObject> installations = doExport(0, null, null, rs);
				for (Map.Entry<Long, AbstractCityObject> entry : installations.entrySet()) {
					AbstractCityObject parent = batches.get(entry.getKey());
					if (parent == null) {
						exporter.logOrThrowErrorMessage("Failed to assign installation with id " + entry.getKey() + " to a city object.");
						continue;
					}

					if (entry.getValue() instanceof BuildingInstallation && parent instanceof AbstractBuilding) {
						((AbstractBuilding) parent).addOuterBuildingInstallation(new BuildingInstallationProperty((BuildingInstallation) entry.getValue()));
					} else if (entry.getValue() instanceof IntBuildingInstallation) {
						if (parent instanceof AbstractBuilding)
							((AbstractBuilding) parent).addInteriorBuildingInstallation(new IntBuildingInstallationProperty((IntBuildingInstallation) entry.getValue()));
						else if (parent instanceof Room)
							((Room) parent).addRoomInstallation(new IntBuildingInstallationProperty((IntBuildingInstallation) entry.getValue()));
					}
				}
			}
		} finally {
			batches.clear();
		}
	}

	protected boolean doExport(BuildingInstallation installation, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		return doExport((AbstractCityObject)installation, id, featureType);
	}
	
	protected boolean doExport(IntBuildingInstallation installation, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		return doExport((AbstractCityObject)installation, id, featureType);
	}

	protected Collection<AbstractCityObject> doExport(AbstractBuilding parent, long parentId, ProjectionFilter parentProjectionFilter) throws CityGMLExportException, SQLException {
		boolean exterior = parentProjectionFilter.containsProperty("outerBuildingInstallation", buildingModule);
		boolean interior = parentProjectionFilter.containsProperty("interiorBuildingInstallation", buildingModule);
		if (!exterior && !interior)
			return Collections.emptyList();
		
		PreparedStatement ps;
		if (!exterior)
			ps = getOrCreateStatement("building_id", IntBuildingInstallation.class);
		else if (!interior)
			ps = getOrCreateStatement("building_id", BuildingInstallation.class);
		else
			ps = getOrCreateStatement("building_id");
		
		return doExport(parentId, null, null, ps);
	}
	
	protected Collection<AbstractCityObject> doExport(Room parent, long parentId, ProjectionFilter parentProjectionFilter) throws CityGMLExportException, SQLException {
		if (!parentProjectionFilter.containsProperty("roomInstallation", buildingModule)) 
			return Collections.emptyList();
		
		return doExport(parentId, null, null, getOrCreateStatement("room_id", IntBuildingInstallation.class));
	}
	
	@Override
	protected Collection<AbstractCityObject> doExport(long id, AbstractCityObject root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);
		
		try (ResultSet rs = ps.executeQuery()) {
			return doExport(id, root, rootType, rs).values();
		} 
	}

	private Map<Long, AbstractCityObject> doExport(long id, AbstractCityObject root, FeatureType rootType, ResultSet rs) throws CityGMLExportException, SQLException {
		long currentInstallationId = 0;
		AbstractCityObject installation = null;
		ProjectionFilter projectionFilter = null;
		boolean isExteriorInstallation = false;
		Map<Long, AbstractCityObject> installations = new HashMap<>();
		Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
		Map<Long, List<String>> adeHookTables = installationADEHookTables != null ? new HashMap<>() : null;

		long currentBoundarySurfaceId = 0;
		AbstractBoundarySurface boundarySurface = null;
		ProjectionFilter boundarySurfaceProjectionFilter = null;
		Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

		while (rs.next()) {
			long installationId = rs.getLong("id");

			if (installationId != currentInstallationId || installation == null) {
				currentInstallationId = installationId;

				installation = installations.get(installationId);
				if (installation == null) {
					FeatureType featureType;
					if (installationId == id && root != null) {
						installation = root;
						featureType = rootType;
					} else {
						// create building installation object
						int objectClassId = rs.getInt("objectclass_id");
						installation = exporter.createObject(objectClassId, AbstractCityObject.class);
						if (installation == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, installationId) + " as building installation object.");
							continue;
						}

						featureType = exporter.getFeatureType(objectClassId);
					}

					// get projection filter
					projectionFilter = exporter.getProjectionFilter(featureType);

					// export city object information
					cityObjectExporter.addBatch(installation, installationId, featureType, projectionFilter);

					isExteriorInstallation = installation instanceof BuildingInstallation;

					if (projectionFilter.containsProperty("class", buildingModule)) {
						String clazz = rs.getString("class");
						if (!rs.wasNull()) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString("class_codespace"));

							if (isExteriorInstallation)
								((BuildingInstallation) installation).setClazz(code);
							else
								((IntBuildingInstallation) installation).setClazz(code);
						}
					}

					if (projectionFilter.containsProperty("function", buildingModule)) {
						for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
							Code function = new Code(splitValue.result(0));
							function.setCodeSpace(splitValue.result(1));

							if (isExteriorInstallation)
								((BuildingInstallation) installation).addFunction(function);
							else
								((IntBuildingInstallation) installation).addFunction(function);
						}
					}

					if (projectionFilter.containsProperty("usage", buildingModule)) {
						for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
							Code usage = new Code(splitValue.result(0));
							usage.setCodeSpace(splitValue.result(1));

							if (isExteriorInstallation)
								((BuildingInstallation) installation).addUsage(usage);
							else
								((IntBuildingInstallation) installation).addUsage(usage);
						}
					}

					LodIterator lodIterator = lodFilter.iterator(2, 4);
					while (lodIterator.hasNext()) {
						int lod = lodIterator.next();

						if (!projectionFilter.containsProperty("lod" + lod + "Geometry", buildingModule))
							continue;

						long geometryId = rs.getLong("lod" + lod + "_brep_id");
						if (!rs.wasNull()) {
							if (isExteriorInstallation) {
								BuildingInstallation exterior = (BuildingInstallation) installation;
								switch (lod) {
									case 2:
										geometries.put(geometryId, new DefaultGeometrySetterHandler((GeometrySetter.AbstractGeometry) exterior::setLod2Geometry));
										break;
									case 3:
										geometries.put(geometryId, new DefaultGeometrySetterHandler((GeometrySetter.AbstractGeometry) exterior::setLod3Geometry));
										break;
									case 4:
										geometries.put(geometryId, new DefaultGeometrySetterHandler((GeometrySetter.AbstractGeometry) exterior::setLod4Geometry));
										break;
								}
							} else {
								IntBuildingInstallation interior = (IntBuildingInstallation) installation;
								geometries.put(geometryId, new DefaultGeometrySetterHandler((GeometrySetter.AbstractGeometry) interior::setLod4Geometry));
							}
						} else {
							Object geometryObj = rs.getObject("lod" + lod + "_other_geom");
							if (rs.wasNull())
								continue;

							GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
							if (geometry != null) {
								GeometryProperty<AbstractGeometry> property = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
								if (isExteriorInstallation) {
									BuildingInstallation exterior = (BuildingInstallation) installation;
									switch (lod) {
										case 2:
											exterior.setLod2Geometry(property);
											break;
										case 3:
											exterior.setLod3Geometry(property);
											break;
										case 4:
											exterior.setLod4Geometry(property);
											break;
									}
								} else
									((IntBuildingInstallation) installation).setLod4Geometry(property);
							}
						}
					}

					lodIterator.reset();
					while (lodIterator.hasNext()) {
						int lod = lodIterator.next();

						if (!projectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", buildingModule))
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
								case 2:
									if (isExteriorInstallation)
										((BuildingInstallation) installation).setLod2ImplicitRepresentation(implicitProperty);
									break;
								case 3:
									if (isExteriorInstallation)
										((BuildingInstallation) installation).setLod3ImplicitRepresentation(implicitProperty);
									break;
								case 4:
									if (isExteriorInstallation)
										((BuildingInstallation) installation).setLod4ImplicitRepresentation(implicitProperty);
									else
										((IntBuildingInstallation) installation).setLod4ImplicitRepresentation(implicitProperty);
									break;
							}
						}
					}

					// get tables of ADE hook properties
					if (installationADEHookTables != null) {
						List<String> tables = retrieveADEHookTables(installationADEHookTables, rs);
						if (tables != null) {
							adeHookTables.put(installationId, tables);
							installation.setLocalProperty("type", featureType);
						}
					}

					installation.setLocalProperty("projection", projectionFilter);
					installations.put(installationId, installation);
				} else
					projectionFilter = (ProjectionFilter) installation.getLocalProperty("projection");
			}

			if (!lodFilter.containsLodGreaterThanOrEuqalTo(2)
					|| !projectionFilter.containsProperty("boundedBy", buildingModule))
				continue;

			// bldg:boundedBy
			long boundarySurfaceId = rs.getLong("tsid");
			if (rs.wasNull())
				continue;

			if (boundarySurfaceId != currentBoundarySurfaceId || boundarySurface == null) {
				currentBoundarySurfaceId = boundarySurfaceId;

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

					if (isExteriorInstallation)
						((BuildingInstallation) installation).getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
					else
						((IntBuildingInstallation) installation).getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));

					boundarySurfaces.put(boundarySurfaceId, boundarySurface);
				} else
					boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
			}

			// continue if openings shall not be exported
			if (!lodFilter.containsLodGreaterThanOrEuqalTo(3)
					|| !boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule))
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

			if (opening instanceof Door
					&& openingProjectionFilter.containsProperty("address", buildingModule)) {
				long addressId = rs.getLong("oaid");
				if (!rs.wasNull()) {
					AddressProperty addressProperty = addressExporter.doExport(addressId, "oa", addressADEHookTables, rs);
					if (addressProperty != null)
						((Door) opening).addAddress(addressProperty);
				}
			}

			boundarySurface.getOpening().add(new OpeningProperty(opening));
		}

		// export postponed geometries
		for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
			geometryExporter.addBatch(entry.getKey(), entry.getValue());

		// delegate export of generic ADE properties
		if (adeHookTables != null) {
			for (Map.Entry<Long, List<String>> entry : adeHookTables.entrySet()) {
				long installationId = entry.getKey();
				installation = installations.get(installationId);
				exporter.delegateToADEExporter(entry.getValue(), installation, installationId,
						(FeatureType) installation.getLocalProperty("type"),
						(ProjectionFilter) installation.getLocalProperty("projection"));
			}
		}

		return installations;
	}
}
