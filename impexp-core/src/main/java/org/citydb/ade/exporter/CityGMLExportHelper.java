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
	<T extends AbstractGML> T exportObject(long objectId, int objectClassId, Class<T> type) throws CityGMLExportException, SQLException;
	<T extends AbstractGML> T createObject(long objectId, int objectClassId, Class<T> type) throws CityGMLExportException, SQLException;
	<T extends AbstractFeature> Collection<T> exportNestedCityGMLObjects(FeatureProperty featureProperty, long parentId, Class<T> featureClass) throws CityGMLExportException, SQLException;
	SurfaceGeometry exportSurfaceGeometry(long surfaceGeometryId) throws CityGMLExportException, SQLException;
	ImplicitGeometry exportImplicitGeometry(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException;
	boolean exportAsGlobalFeature(AbstractFeature feature) throws CityGMLExportException;
	boolean supportsExportOfGlobalFeatures();
	GMLConverter getGMLConverter();
	
	AbstractDatabaseAdapter getDatabaseAdapter();
	CityGMLVersion getTargetCityGMLVersion();
	ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType);
	CombinedProjectionFilter getCombinedProjectionFilter(String tableName);
	LodFilter getLodFilter();
	AttributeValueSplitter getAttributeValueSplitter();

	boolean isFailOnError();
	Exporter getExportConfig();
	
	String getTableNameWithSchema(String tableName);
	ProjectionToken getGeometryColumn(Column column);
	ProjectionToken getGeometryColumn(Column column, String asName);
	String getGeometryColumn(String columnName);
	String getGeometryColumn(String columnName, String asName);
	
	void logOrThrowErrorMessage(String message) throws CityGMLExportException;
	String getObjectSignature(int objectClassId, long id);
	String getObjectSignature(AbstractObjectType<?> objectType, long id);
	
	FeatureType getFeatureType(AbstractFeature feature);
	ObjectType getObjectType(AbstractGML object);
	AbstractObjectType<?> getAbstractObjectType(AbstractGML object);
	FeatureType getFeatureType(int objectClassId);
	ObjectType getObjectType(int objectClassId);
	AbstractObjectType<?> getAbstractObjectType(int objectClassId);
	
	boolean lookupAndPutObjectUID(String gmlId, long id, int objectClassId);
	boolean lookupObjectUID(String gmlId);
	
	boolean satisfiesLodFilter(AbstractCityObject cityObject);
}
