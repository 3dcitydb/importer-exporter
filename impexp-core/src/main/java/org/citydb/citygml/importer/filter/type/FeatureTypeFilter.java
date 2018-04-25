package org.citydb.citygml.importer.filter.type;

import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.FeatureProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.filter.FilterException;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

public class FeatureTypeFilter {
	private final SchemaMapping schemaMapping;
	private final Set<QName> typeNames = new HashSet<>();
	private final Set<FeatureType> featureTypes = new HashSet<>();
	
	public FeatureTypeFilter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}
	
	public FeatureTypeFilter(org.citydb.config.project.query.filter.type.FeatureTypeFilter typeFilter, SchemaMapping schemaMapping) throws FilterException {
		if (typeFilter == null)
			throw new FilterException("The feature type filter must not be null.");

		this.schemaMapping = schemaMapping;
		typeNames.addAll(typeFilter.getTypeNames());

		for (QName typeName : typeNames) {
			FeatureType featureType = schemaMapping.getFeatureType(typeName);
			if (featureType != null)
				featureTypes.add(featureType);
		}
	}
	
	public boolean isSatisfiedBy(QName name, boolean alllowFlatHierarchies) {
		if (typeNames.isEmpty() || typeNames.contains(name))
			return true;

		if (alllowFlatHierarchies) {
			// if flat hierarchies shall be supported, we check whether the
			// feature to be tested can be represented as nested feature
			// of at least one of the features given in the filter settings.
			// if so, the nested feature passes this filter.
			FeatureType candidate = schemaMapping.getFeatureType(name);
			if (candidate != null) {
				Set<FeatureType> visitedFeatures = new HashSet<>();
				Set<FeatureProperty> visitedProperties = new HashSet<>();

				for (FeatureType parent : featureTypes) {
					if (isPartOf(parent, candidate, visitedFeatures, visitedProperties)) {
						typeNames.add(name);
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean isPartOf(FeatureType parent, FeatureType candidate, Set<FeatureType> visitedFeatures, Set<FeatureProperty> visitedProperties) {
		visitedFeatures.add(parent);

		for (AbstractProperty property : parent.listProperties(false, true)) {
			if (property.getElementType() != PathElementType.FEATURE_PROPERTY)
				continue;

			FeatureProperty featureProperty = (FeatureProperty) property;
			if (!visitedProperties.add(featureProperty))
				continue;

			FeatureType target = featureProperty.getType();

			// we do not accept the feature property if it may contain top-level features;
			// otherwise we would allow any feature to bypass the given filter settings
			if (target.isAbstract() && target.listSubTypes(true).stream().anyMatch(FeatureType::isTopLevel))
				continue;

			if (candidate.isEqualToOrSubTypeOf(target))
				return true;

			if (visitedFeatures.add(target) && isPartOf(target, candidate, visitedFeatures, visitedProperties))
				return true;

			for (FeatureType subType : target.listSubTypes(true)) {
				if (visitedFeatures.add(subType) && isPartOf(subType, candidate, visitedFeatures, visitedProperties))
					return true;
			}
		}

		return false;
	}
}
