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
import org.citydb.citygml.exporter.util.GeometrySetter;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.core.AbstractCityObject;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DBBridgeInstallation extends AbstractFeatureExporter<AbstractCityObject> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectReader;
	private final DBBridgeThematicSurface thematicSurfaceExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final GMLConverter gmlConverter;

	private final String bridgeModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final List<Table> adeHookTables;
	
	public DBBridgeInstallation(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractCityObject.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_INSTALLATION.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.BRIDGE_INSTALLATION.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", bridgeModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", bridgeModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", bridgeModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2Geometry", bridgeModule)) select.addProjection(table.getColumn("lod2_brep_id"), exporter.getGeometryColumn(table.getColumn("lod2_other_geom")));
			if (projectionFilter.containsProperty("lod2ImplicitRepresentation", bridgeModule)) select.addProjection(table.getColumn("lod2_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod2_implicit_ref_point")), table.getColumn("lod2_implicit_transformation"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3Geometry", bridgeModule)) select.addProjection(table.getColumn("lod3_brep_id"), exporter.getGeometryColumn(table.getColumn("lod3_other_geom")));
			if (projectionFilter.containsProperty("lod3ImplicitRepresentation", bridgeModule)) select.addProjection(table.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point")), table.getColumn("lod3_implicit_transformation"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4Geometry", bridgeModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", bridgeModule)) select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));
		}

		adeHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_INSTALLATION, table);
		
		cityObjectReader = exporter.getExporter(DBCityObject.class);
		thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();			
		valueSplitter = exporter.getAttributeValueSplitter();		
	}

	protected boolean doExport(BridgeInstallation installation, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		return doExport((AbstractCityObject)installation, id, featureType);
	}
	
	protected boolean doExport(IntBridgeInstallation installation, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		return doExport((AbstractCityObject)installation, id, featureType);
	}
	
	protected Collection<AbstractCityObject> doExport(AbstractBridge parent, long parentId, ProjectionFilter parentProjectionFilter) throws CityGMLExportException, SQLException {
		boolean exterior = parentProjectionFilter.containsProperty("outerBridgeInstallation", bridgeModule);
		boolean interior = parentProjectionFilter.containsProperty("interiorBridgeInstallation", bridgeModule);
		if (!exterior && !interior)
			return Collections.emptyList();
		
		PreparedStatement ps;
		if (!exterior)
			ps = getOrCreateStatement("bridge_id", IntBridgeInstallation.class);
		else if (!interior)
			ps = getOrCreateStatement("bridge_id", BridgeInstallation.class);
		else
			ps = getOrCreateStatement("bridge_id");
		
		return doExport(parentId, null, null, ps);
	}
	
	protected Collection<AbstractCityObject> doExport(BridgeRoom parent, long parentId, ProjectionFilter parentProjectionFilter) throws CityGMLExportException, SQLException {
		if (!parentProjectionFilter.containsProperty("bridgeRoomInstallation", bridgeModule))
			return Collections.emptyList();
		
		return doExport(parentId, null, null, getOrCreateStatement("bridge_room_id", IntBridgeInstallation.class));
	}
	
	@Override
	protected Collection<AbstractCityObject> doExport(long id, AbstractCityObject root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<AbstractCityObject> installations = new ArrayList<>();

			while (rs.next()) {
				long installationId = rs.getLong("id");
				AbstractCityObject installation;
				FeatureType featureType;
				
				if (installationId == id && root != null) {
					installation = root;
					featureType = rootType;
				} else {
					// create bridge installation object
					int objectClassId = rs.getInt("objectclass_id");
					installation = exporter.createObject(objectClassId, AbstractCityObject.class);
					if (installation == null) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, installationId) + " as bridge installation object.");
						continue;
					}

					featureType = exporter.getFeatureType(objectClassId);
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
				
				// export city object information
				cityObjectReader.addBatch(installation, installationId, featureType, projectionFilter);

				boolean isExteriorInstallation = installation instanceof BridgeInstallation;

				if (projectionFilter.containsProperty("class", bridgeModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));

						if (isExteriorInstallation)
							((BridgeInstallation)installation).setClazz(code);
						else
							((IntBridgeInstallation)installation).setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));

						if (isExteriorInstallation)
							((BridgeInstallation)installation).addFunction(function);
						else
							((IntBridgeInstallation)installation).addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));

						if (isExteriorInstallation)
							((BridgeInstallation)installation).addUsage(usage);
						else
							((IntBridgeInstallation)installation).addUsage(usage);
					}
				}

				// brid:boundedBy
				if (projectionFilter.containsProperty("boundedBy", bridgeModule)) {
					if (isExteriorInstallation && lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
						for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport((BridgeInstallation)installation, installationId))
							((BridgeInstallation)installation).addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
					} else if (!isExteriorInstallation && lodFilter.containsLodGreaterThanOrEuqalTo(4)) {
						for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport((IntBridgeInstallation)installation, installationId))
							((IntBridgeInstallation)installation).addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
					}
				}

				LodIterator lodIterator = lodFilter.iterator(2, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "Geometry", bridgeModule))
						continue;

					long geometryId = rs.getLong("lod" + lod + "_brep_id");
					if (!rs.wasNull()) {
						if (isExteriorInstallation) {
							BridgeInstallation exterior = (BridgeInstallation) installation;
							switch (lod) {
								case 2:
									geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) exterior::setLod2Geometry);
									break;
								case 3:
									geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) exterior::setLod3Geometry);
									break;
								case 4:
									geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) exterior::setLod4Geometry);
									break;
							}
						} else {
							IntBridgeInstallation interior = (IntBridgeInstallation) installation;
							geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) interior::setLod4Geometry);
						}
					} else {
						Object geometryObj = rs.getObject("lod" + lod + "_other_geom");
						if (rs.wasNull())
							continue;

						GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
						if (geometry != null) {
							GeometryProperty<AbstractGeometry> property = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
							if (isExteriorInstallation) {
								BridgeInstallation exterior = (BridgeInstallation) installation;
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
								((IntBridgeInstallation) installation).setLod4Geometry(property);
						}
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", bridgeModule))
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
								((BridgeInstallation)installation).setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							if (isExteriorInstallation)
								((BridgeInstallation)installation).setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							if (isExteriorInstallation)
								((BridgeInstallation)installation).setLod4ImplicitRepresentation(implicitProperty);
							else
								((IntBridgeInstallation)installation).setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, installation, installationId, featureType, projectionFilter);
				}

				installations.add(installation);
			}
			
			return installations;
		}
	}

}
