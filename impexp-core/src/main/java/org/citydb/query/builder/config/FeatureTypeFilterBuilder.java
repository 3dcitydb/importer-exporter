package org.citydb.query.builder.config;

import javax.xml.namespace.QName;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.CityGMLVersion;

public class FeatureTypeFilterBuilder {
	private final Query query;
	private final SchemaMapping schemaMapping;
	private final ADEExtensionManager adeManager; 

	protected FeatureTypeFilterBuilder(Query query, SchemaMapping schemaMapping) {
		this.query = query;
		this.schemaMapping = schemaMapping;

		adeManager = ADEExtensionManager.getInstance();
	}

	protected FeatureTypeFilter buildFeatureTypeFilter(org.citydb.config.project.query.filter.type.FeatureTypeFilter featureTypeFilterConfig) throws QueryBuildException {
		FeatureTypeFilter featureTypeFilter = new FeatureTypeFilter();
		CityGMLVersion version = null;

		try {
			for (QName typeName : featureTypeFilterConfig.getTypeNames()) {
				if (typeName == null)
					throw new QueryBuildException("Failed to parse the qualified names of the feature types.");

				FeatureType featureType = schemaMapping.getFeatureType(typeName);
				if (featureType == null)
					throw new QueryBuildException("'" + typeName + "' is not a valid feature type.");

				// check whether all feature types share the same CityGML version
				CityGMLVersion featureVersion = featureType.getSchema().getCityGMLVersion(typeName.getNamespaceURI());
				if (featureVersion == null)
					throw new QueryBuildException("Failed to find CityGML version of the feature type '" + typeName + "'.");

				if (version == null)
					version = featureVersion;
				else if (version != featureVersion) 
					throw new QueryBuildException("Mixing feature types from different CityGML versions is not supported.");

				featureTypeFilter.addFeatureType(featureType);
			}
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build the feature type filter.", e);
		}

		// set the CityGML target version
		query.setTargetVersion(version);

		return featureTypeFilter;
	}

	protected FeatureTypeFilter buildFeatureTypeFilter(org.citydb.config.project.query.filter.type.FeatureTypeFilter featureTypeFilterConfig, CityGMLVersion version) throws QueryBuildException {
		FeatureTypeFilter featureTypeFilter = new FeatureTypeFilter();

		try {
			for (QName typeName : featureTypeFilterConfig.getTypeNames()) {
				if (typeName == null)
					continue;

				FeatureType featureType = schemaMapping.getFeatureType(typeName);
				if (featureType == null)
					continue;

				if (!featureType.isAvailableForCityGML(version))
					continue;

				ADEExtension extension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
				if (extension != null && !extension.isEnabled())
					continue;

				featureTypeFilter.addFeatureType(featureType);
			}
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build the feature type filter.", e);
		}

		return featureTypeFilter;
	}

}
