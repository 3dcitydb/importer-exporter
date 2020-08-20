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
package org.citydb.ade.exporter;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.database.content.GMLConverter;
import org.citydb.citygml.exporter.database.content.SurfaceGeometry;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.exporter.Exporter;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import java.sql.SQLException;
import java.util.Collection;

public interface CityGMLExportHelper {
	public <T extends AbstractGML> T createObject(long objectId, int objectClassId, Class<T> type) throws CityGMLExportException, SQLException;
	public <T extends AbstractFeature> Collection<T> exportNestedCityGMLObjects(FeatureProperty featureProperty, long parentId, Class<T> featureClass) throws CityGMLExportException, SQLException;
	public SurfaceGeometry exportSurfaceGeometry(long surfaceGeometryId) throws CityGMLExportException, SQLException;
	public ImplicitGeometry exportImplicitGeometry(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException;
	public boolean exportAsGlobalFeature(AbstractFeature feature) throws CityGMLExportException;
	public boolean supportsExportOfGlobalFeatures();
	public GMLConverter getGMLConverter();
	
	public AbstractDatabaseAdapter getDatabaseAdapter();
	public CityGMLVersion getTargetCityGMLVersion();
	public ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType);
	public CombinedProjectionFilter getCombinedProjectionFilter(String tableName);
	public LodFilter getLodFilter();
	public AttributeValueSplitter getAttributeValueSplitter();

	public boolean isFailOnError();
	public Exporter getExportConfig();
	
	public String getTableNameWithSchema(String tableName);
	public ProjectionToken getGeometryColumn(Column column);
	public ProjectionToken getGeometryColumn(Column column, String asName);
	public String getGeometryColumn(String columnName);
	public String getGeometryColumn(String columnName, String asName);
	
	public void logOrThrowErrorMessage(String message) throws CityGMLExportException;
	public String getObjectSignature(int objectClassId, long id);
	public String getObjectSignature(AbstractObjectType<?> objectType, long id);
	
	public FeatureType getFeatureType(AbstractFeature feature);	
	public ObjectType getObjectType(AbstractGML object);
	public AbstractObjectType<?> getAbstractObjectType(AbstractGML object);
	public FeatureType getFeatureType(int objectClassId);	
	public ObjectType getObjectType(int objectClassId);
	public AbstractObjectType<?> getAbstractObjectType(int objectClassId);
	
	public boolean lookupAndPutObjectUID(String gmlId, long id, int objectClassId);
	public boolean lookupObjectUID(String gmlId);
	
	public boolean satisfiesLodFilter(AbstractCityObject cityObject);
}
