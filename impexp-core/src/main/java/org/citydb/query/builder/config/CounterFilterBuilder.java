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

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.counter.CounterFilter;

public class CounterFilterBuilder {

	protected CounterFilterBuilder() {
		
	}

	protected CounterFilter buildCounterFilter(org.citydb.config.project.query.filter.counter.CounterFilter counterFilterConfig) throws FilterException {
		if (counterFilterConfig.isSetCount() && counterFilterConfig.isSetStartIndex())
			return new CounterFilter(counterFilterConfig.getCount(), counterFilterConfig.getStartIndex());
		else if (counterFilterConfig.isSetCount())
			return CounterFilter.ofCount(counterFilterConfig.getCount());
		else if (counterFilterConfig.isSetStartIndex())
			return CounterFilter.ofStartIndex(counterFilterConfig.getStartIndex());

		throw new FilterException("Either count or startIndex must be defined for a counter filter.");
	}
}
