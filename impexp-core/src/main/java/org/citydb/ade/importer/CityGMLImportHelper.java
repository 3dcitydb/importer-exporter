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
package org.citydb.ade.importer;

import java.sql.SQLException;

import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.database.content.GeometryConverter;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.project.importer.Importer;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.ObjectType;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.AbstractGeometry;

public interface CityGMLImportHelper {
	public long importObject(AbstractGML object) throws CityGMLImportException, SQLException;
	public long importObject(AbstractGML object, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException;
	public long importGlobalAppearance(Appearance appearance) throws CityGMLImportException, SQLException;
	public long importSurfaceGeometry(AbstractGeometry surfaceGeometry, long cityObjectId) throws CityGMLImportException, SQLException;
	public long importImplicitGeometry(ImplicitGeometry implicitGeometry) throws CityGMLImportException, SQLException;
	public GeometryConverter getGeometryConverter();
	public String convertImplicitGeometryTransformationMatrix(TransformationMatrix4x4 matrix);
	public boolean isSurfaceGeometry(AbstractGeometry abstractGeometry);
	public boolean isPointOrLineGeometry(AbstractGeometry abstractGeometry);
	
	public AbstractDatabaseAdapter getDatabaseAdapter();
	public void executeBatch(String tableName) throws CityGMLImportException, SQLException;
	public void executeBatch(AbstractObjectType<?> type) throws CityGMLImportException, SQLException;
	public String getTableNameWithSchema(String tableName);
	public long getNextSequenceValue(String sequence) throws SQLException;
	public AttributeValueJoiner getAttributeValueJoiner();
	
	public boolean isFailOnError();
	public Importer getImportConfig();
	
	public void logOrThrowUnsupportedXLinkMessage(AbstractGML from, Class<? extends AbstractGML> to, String xlink) throws CityGMLImportException;
	public void logOrThrowUnsupportedGeometryMessage(AbstractGML from, AbstractGeometry geometry) throws CityGMLImportException;
	public void logOrThrowErrorMessage(String message) throws CityGMLImportException;
	public String getObjectSignature(AbstractGML object);
	
	public int getObjectClassId(AbstractGML object);
	public FeatureType getFeatureType(AbstractFeature feature);
	public ObjectType getObjectType(AbstractGML object);
	public AbstractObjectType<?> getAbstractObjectType(AbstractGML object);
	
	public void propagateObjectXlink(String table, long objectId, String xlink, String propertyColumn);
	public void propagateObjectXlink(String intermediateTable, long objectId, String fromColumn, String xlink, String toColumn);
	public void propagateReverseObjectXlink(String toTable, String gmlId, long objectId, String propertyColumn);
	public void propagateSurfaceGeometryXlink(String xlink, String table, long objectId, String propertyColumn);
}
