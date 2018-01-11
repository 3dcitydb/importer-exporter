package org.citydb.ade.exporter;

import java.sql.SQLException;
import java.util.Collection;

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
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import vcs.sqlbuilder.schema.Column;
import vcs.sqlbuilder.select.ProjectionToken;

public interface CityGMLExportHelper {
	public AbstractGML exportObject(long objectId, int objectClassId) throws CityGMLExportException, SQLException;
	public <T extends AbstractGML> T createObjectStub(long objectId, int objectClassId, Class<T> type) throws CityGMLExportException, SQLException;
	public <T extends AbstractFeature> Collection<T> exportNestedCityGMLObjects(FeatureProperty featureProperty, long parentId, Class<T> featureClass) throws CityGMLExportException, SQLException;
	public SurfaceGeometry exportSurfaceGeometry(long surfaceGeometryId) throws CityGMLExportException, SQLException;
	public ImplicitGeometry exportImplicitGeometry(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException;
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
