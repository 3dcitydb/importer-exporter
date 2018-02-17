/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
import java.util.List;
import java.util.Set;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;

public class DBWaterBoundarySurface extends AbstractFeatureExporter<AbstractWaterBoundarySurface> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;

	private String waterBodyModule;
	private LodFilter lodFilter;
	private Set<String> adeHookTables;

	public DBWaterBoundarySurface(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractWaterBoundarySurface.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.WATERBOUNDARY_SURFACE.getName());
		waterBodyModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.WATER_BODY).getNamespaceURI();
		lodFilter = exporter.getLodFilter();

		table = new Table(TableEnum.WATERBOUNDARY_SURFACE.getName());
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("waterLevel", waterBodyModule)) select.addProjection(table.getColumn("water_level"), table.getColumn("water_level_codespace"));
		if (projectionFilter.containsProperty("lod2Surface", waterBodyModule)) select.addProjection(table.getColumn("lod2_surface_id"));
		if (projectionFilter.containsProperty("lod3Surface", waterBodyModule)) select.addProjection(table.getColumn("lod3_surface_id"));
		if (projectionFilter.containsProperty("lod4Surface", waterBodyModule)) select.addProjection(table.getColumn("lod4_surface_id"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.WATERBOUNDARY_SURFACE);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
	}

	@Override
	protected Collection<AbstractWaterBoundarySurface> doExport(long id, AbstractWaterBoundarySurface root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<AbstractWaterBoundarySurface> waterBoundarySurfaces = new ArrayList<>();

			while (rs.next()) {
				long waterBoundarySurfaceId = rs.getLong("id");
				AbstractWaterBoundarySurface waterBoundarySurface = null;
				FeatureType featureType = null;						

				if (waterBoundarySurfaceId == id & root != null) {
					waterBoundarySurface = root;
					featureType = rootType;
				} else {
					int objectClassId = rs.getInt("objectclass_id");
					featureType = exporter.getFeatureType(objectClassId);
					if (featureType == null)
						continue;

					// create water boundary surface object
					waterBoundarySurface = exporter.createObject(featureType.getObjectClassId(), AbstractWaterBoundarySurface.class);						
					if (waterBoundarySurface == null) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, waterBoundarySurfaceId) + " as water boundary surface object.");
						continue;
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				// export city object information
				cityObjectExporter.doExport(waterBoundarySurface, waterBoundarySurfaceId, featureType, projectionFilter);

				if (waterBoundarySurface instanceof WaterSurface
						&& projectionFilter.containsProperty("waterLevel", waterBodyModule)) {
					String waterLevel = rs.getString("water_level");
					if (!rs.wasNull()) {
						Code code = new Code(waterLevel);
						code.setCodeSpace(rs.getString("water_level_codespace"));
						((WaterSurface)waterBoundarySurface).setWaterLevel(code);
					}
				}

				LodIterator lodIterator = lodFilter.iterator(2, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("Surface").toString(), waterBodyModule))
						continue;

					long surfaceGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_surface_id").toString());
					if (rs.wasNull())
						continue;

					SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
					if (geometry != null && geometry.getType().isInstance(GMLClass.ABSTRACT_SURFACE)) {
						SurfaceProperty surfaceProperty = new SurfaceProperty();
						if (geometry.isSetGeometry())
							surfaceProperty.setSurface((AbstractSurface)geometry.getGeometry());
						else
							surfaceProperty.setHref(geometry.getReference());

						switch (lod) {
						case 2:
							waterBoundarySurface.setLod2Surface(surfaceProperty);
							break;
						case 3:
							waterBoundarySurface.setLod3Surface(surfaceProperty);
							break;
						case 4:
							waterBoundarySurface.setLod4Surface(surfaceProperty);
							break;
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, waterBoundarySurface, waterBoundarySurfaceId, featureType, projectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(waterBoundarySurface))
					continue;

				waterBoundarySurfaces.add(waterBoundarySurface);
			}

			return waterBoundarySurfaces;
		}
	}

}
