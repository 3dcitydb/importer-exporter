/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.ade.exporter;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.exporter.ExportConfig;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.FeatureProperty;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.ObjectType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.database.content.GMLConverter;
import org.citydb.core.operation.exporter.database.content.SurfaceGeometryExporter;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import java.sql.SQLException;
import java.util.Collection;

public interface CityGMLExportHelper {
	<T extends AbstractGML> T createObject(long objectId, int objectClassId, Class<T> type) throws CityGMLExportException, SQLException;
	<T extends AbstractFeature> Collection<T> exportNestedFeatures(FeatureProperty featureProperty, long parentId, Class<T> featureClass) throws CityGMLExportException, SQLException;
	ImplicitGeometry createImplicitGeometry(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException;
	SurfaceGeometryExporter getSurfaceGeometryExporter() throws CityGMLExportException, SQLException;
	AttributeValueSplitter getAttributeValueSplitter();
	GMLConverter getGMLConverter();

	void executeBatch() throws CityGMLExportException, SQLException;
	boolean exportAsGlobalFeature(AbstractFeature feature) throws CityGMLExportException, SQLException;
	boolean supportsExportOfGlobalFeatures();

	AbstractDatabaseAdapter getDatabaseAdapter();
	CityGMLVersion getTargetCityGMLVersion();
	ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType);
	CombinedProjectionFilter getCombinedProjectionFilter(String tableName);
	LodFilter getLodFilter();

	boolean isFailOnError();
	ExportConfig getExportConfig();
	
	String getTableNameWithSchema(String tableName);
	ProjectionToken getGeometryColumn(Column column);
	ProjectionToken getGeometryColumn(Column column, String asName);
	String getGeometryColumn(String columnName);
	String getGeometryColumn(String columnName, String asName);

	void logOrThrowErrorMessage(String message) throws CityGMLExportException;
	void logOrThrowErrorMessage(String message, Throwable cause) throws CityGMLExportException;
	String getObjectSignature(int objectClassId, long id);
	String getObjectSignature(AbstractObjectType<?> objectType, long id);
	
	FeatureType getFeatureType(AbstractFeature feature);	
	ObjectType getObjectType(AbstractGML object);
	AbstractObjectType<?> getAbstractObjectType(AbstractGML object);
	FeatureType getFeatureType(int objectClassId);	
	ObjectType getObjectType(int objectClassId);
	AbstractObjectType<?> getAbstractObjectType(int objectClassId);
	
	boolean lookupAndPutObjectId(String gmlId, long id, int objectClassId);
	boolean lookupObjectId(String gmlId);
	String replaceObjectId(String gmlId);
}
