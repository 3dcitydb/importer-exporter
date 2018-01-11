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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import vcs.sqlbuilder.schema.Table;
import vcs.sqlbuilder.select.Select;

public class DBBuildingInstallation extends AbstractFeatureExporter<AbstractCityObject> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBThematicSurface thematicSurfaceExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private GMLConverter gmlConverter;
	
	private String buildingModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;
	private Set<String> adeHookTables;

	public DBBuildingInstallation(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractCityObject.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BUILDING_INSTALLATION.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		
		table = new Table(TableEnum.BUILDING_INSTALLATION.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", buildingModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", buildingModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", buildingModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));		
		if (projectionFilter.containsProperty("lod2Geometry", buildingModule)) select.addProjection(table.getColumn("lod2_brep_id"), exporter.getGeometryColumn(table.getColumn("lod2_other_geom")));
		if (projectionFilter.containsProperty("lod3Geometry", buildingModule)) select.addProjection(table.getColumn("lod3_brep_id"), exporter.getGeometryColumn(table.getColumn("lod3_other_geom")));
		if (projectionFilter.containsProperty("lod4Geometry", buildingModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
		if (projectionFilter.containsProperty("lod2ImplicitRepresentation", buildingModule))
			select.addProjection(table.getColumn("lod2_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod2_implicit_ref_point")), table.getColumn("lod2_implicit_transformation"));
		if (projectionFilter.containsProperty("lod3ImplicitRepresentation", buildingModule))
			select.addProjection(table.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point")), table.getColumn("lod3_implicit_transformation"));
		if (projectionFilter.containsProperty("lod4ImplicitRepresentation", buildingModule))
			select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));
		
		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.BUILDING_INSTALLATION);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}
		
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		thematicSurfaceExporter = exporter.getExporter(DBThematicSurface.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();			
		valueSplitter = exporter.getAttributeValueSplitter();
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
		
		PreparedStatement ps = null;
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
			List<AbstractCityObject> installations = new ArrayList<>();
			
			while (rs.next()) {
				long installationId = rs.getLong("id");				
				AbstractCityObject installation = null;
				FeatureType featureType = null;
				
				if (installationId == id && root != null) {
					installation = root;
					featureType = rootType;
				} else {
					int objectClassId = rs.getInt("objectclass_id");
					featureType = exporter.getFeatureType(objectClassId);
					if (featureType == null)
						continue;
					
					// create building installation object
					installation = exporter.createObject(objectClassId, AbstractCityObject.class);
					if (installation == null) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, installationId) + " as building installation object.");
						continue;
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
				
				// export city object information
				cityObjectExporter.doExport(installation, installationId, featureType, projectionFilter);

				boolean isExteriorInstallation = installation instanceof BuildingInstallation;				
				
				if (projectionFilter.containsProperty("class", buildingModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						
						if (isExteriorInstallation)
							((BuildingInstallation)installation).setClazz(code);
						else
							((IntBuildingInstallation)installation).setClazz(code);
					}
				}
				
				if (projectionFilter.containsProperty("function", buildingModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						
						if (isExteriorInstallation)
							((BuildingInstallation)installation).addFunction(function);
						else
							((IntBuildingInstallation)installation).addFunction(function);
					}
				}
				
				if (projectionFilter.containsProperty("usage", buildingModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						
						if (isExteriorInstallation)
							((BuildingInstallation)installation).addUsage(usage);
						else
							((IntBuildingInstallation)installation).addUsage(usage);
					}
				}
				
				// bldg:boundedBy
				if (projectionFilter.containsProperty("boundedBy", buildingModule)) {
					if (isExteriorInstallation && lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
						for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport((BuildingInstallation)installation, installationId))
							((BuildingInstallation)installation).addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
					} if (!isExteriorInstallation && lodFilter.containsLodGreaterThanOrEuqalTo(4)) {
						for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport((IntBuildingInstallation)installation, installationId))
							((IntBuildingInstallation)installation).addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
					}
				}
				
				LodIterator lodIterator = lodFilter.iterator(2, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("Geometry").toString(), buildingModule))
						continue;

					long geometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_brep_id").toString());
					Object geometryObj = rs.getObject(new StringBuilder("lod").append(lod).append("_other_geom").toString());
					if (geometryId == 0 && geometryObj == null)
						continue;

					GeometryProperty<AbstractGeometry> geometryProperty = null;
					if (geometryId != 0) {
						SurfaceGeometry geometry = geometryExporter.doExport(geometryId);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<>();
							if (geometry.isSetGeometry())
								geometryProperty.setGeometry(geometry.getGeometry());
							else
								geometryProperty.setHref(geometry.getReference());
						}
					} else {
						GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
						if (geometry != null)
							geometryProperty = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
					}

					if (geometryProperty != null) {
						switch (lod) {
						case 2:
							if (isExteriorInstallation)
								((BuildingInstallation)installation).setLod2Geometry(geometryProperty);
							break;
						case 3:
							if (isExteriorInstallation)
								((BuildingInstallation)installation).setLod3Geometry(geometryProperty);
							break;
						case 4:
							if (isExteriorInstallation)
								((BuildingInstallation)installation).setLod4Geometry(geometryProperty);
							else
								((IntBuildingInstallation)installation).setLod4Geometry(geometryProperty);
							break;
						}
					}
				}
				
				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("ImplicitRepresentation").toString(), buildingModule))
						continue;

					// get implicit geometry details
					long implicitGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_implicit_rep_id").toString());
					if (rs.wasNull())
						continue;

					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(new StringBuilder("lod").append(lod).append("_implicit_ref_point").toString());
					if (!rs.wasNull())
						referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(new StringBuilder("lod").append(lod).append("_implicit_transformation").toString());

					ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 2:
							if (isExteriorInstallation)
								((BuildingInstallation)installation).setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							if (isExteriorInstallation)
								((BuildingInstallation)installation).setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							if (isExteriorInstallation)
								((BuildingInstallation)installation).setLod4ImplicitRepresentation(implicitProperty);
							else
								((IntBuildingInstallation)installation).setLod4ImplicitRepresentation(implicitProperty);
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
				
				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(installation))
					continue;
				
				installations.add(installation);
			}

			return installations;
		} 
	}

}
