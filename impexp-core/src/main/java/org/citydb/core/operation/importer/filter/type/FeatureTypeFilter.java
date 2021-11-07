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
package org.citydb.core.operation.importer.filter.type;

import org.citydb.core.database.schema.mapping.*;
import org.citydb.core.query.filter.FilterException;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureTypeFilter {
	private final SchemaMapping schemaMapping;
	private final Set<QName> typeNames = new HashSet<>();
	private final Set<FeatureType> featureTypes = new HashSet<>();

	public FeatureTypeFilter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	public FeatureTypeFilter(QName typeName, SchemaMapping schemaMapping) {
		this(schemaMapping);
		addFeatureType(typeName);
	}

	public FeatureTypeFilter(List<QName> typeNames, SchemaMapping schemaMapping) {
		this(schemaMapping);
		typeNames.forEach(this::addFeatureType);
	}
	
	public FeatureTypeFilter(org.citydb.config.project.query.filter.type.FeatureTypeFilter typeFilter, SchemaMapping schemaMapping) throws FilterException {
		if (typeFilter == null)
			throw new FilterException("The feature type filter must not be null.");

		this.schemaMapping = schemaMapping;
		typeFilter.getTypeNames().forEach(this::addFeatureType);
	}

	public void addFeatureType(QName typeName) {
		typeNames.add(typeName);

		FeatureType featureType = schemaMapping.getFeatureType(typeName);
		if (featureType != null)
			featureTypes.add(featureType);
	}
	
	public boolean isSatisfiedBy(QName name, boolean allowFlatHierarchies) {
		if (typeNames.isEmpty() || typeNames.contains(name))
			return true;

		if (allowFlatHierarchies) {
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
