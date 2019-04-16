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
package org.citydb.query.builder.config;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import javax.xml.namespace.QName;

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
