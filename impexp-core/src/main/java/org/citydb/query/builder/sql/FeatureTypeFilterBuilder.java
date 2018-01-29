package org.citydb.query.builder.sql;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.CityGMLVersion;

public class FeatureTypeFilterBuilder {

	protected FeatureTypeFilterBuilder() {

	}

	protected Set<Integer> buildFeatureTypeFilter(FeatureTypeFilter typeFilter, CityGMLVersion targetVersion) {
		List<FeatureType> featureTypes = typeFilter.getFeatureTypes(targetVersion);		
		if (featureTypes.isEmpty())
			return Collections.emptySet();

		if (featureTypes.size() == 1) {
			FeatureType featureType = featureTypes.iterator().next();
			if (!featureType.isAbstract() && !featureType.hasSharedTable(true))
				return Collections.emptySet();
		}

		Set<Integer> ids = new HashSet<Integer>(featureTypes.size());
		for (FeatureType featureType : featureTypes)
			ids.add(featureType.getObjectClassId());

		return ids;
	}

	protected Set<Integer> buildFeatureTypeFilter(FeatureTypeFilter typeFilter) {
		return buildFeatureTypeFilter(typeFilter, CityGMLVersion.DEFAULT);
	}

}
