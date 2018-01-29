package org.citydb.ade.exporter;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.gml.feature.AbstractFeature;

public interface ADEExportManager {
	public void init(Connection connection, CityGMLExportHelper helper) throws CityGMLExportException, SQLException;
	public void exportObject(ADEModelObject object, long objectId, AbstractObjectType<?> objectType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException;
	public void exportGenericApplicationProperties(String adeHookTable, AbstractFeature parent, long parentId, FeatureType parentType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException;
	public void close() throws CityGMLExportException, SQLException;
}
