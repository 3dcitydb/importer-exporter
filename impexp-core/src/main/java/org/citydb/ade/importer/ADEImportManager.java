package org.citydb.ade.importer;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.gml.feature.AbstractFeature;

public interface ADEImportManager {
	public void init(Connection connection, CityGMLImportHelper helper) throws CityGMLImportException, SQLException;	
	public void importObject(ADEModelObject object, long objectId, AbstractObjectType<?> objectType, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException;
	public void importGenericApplicationProperties(ADEPropertyCollection properties, AbstractFeature parent, long parentId, FeatureType parentType) throws CityGMLImportException, SQLException;
	public void executeBatch(String tableName) throws CityGMLImportException, SQLException;
	public void close() throws CityGMLImportException, SQLException;
}
