/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.query.builder.sql;

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

		Set<Integer> ids = new HashSet<>(featureTypes.size());
		for (FeatureType featureType : featureTypes)
			ids.add(featureType.getObjectClassId());

		return ids;
	}

	protected Set<Integer> buildFeatureTypeFilter(FeatureTypeFilter typeFilter) {
		return buildFeatureTypeFilter(typeFilter, CityGMLVersion.DEFAULT);
	}

}
