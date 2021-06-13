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
package org.citydb.operation.importer.filter;

import org.citydb.operation.importer.filter.selection.SelectionFilter;
import org.citydb.operation.importer.filter.selection.counter.CounterFilter;
import org.citydb.operation.importer.filter.type.FeatureTypeFilter;
import org.citydb.database.schema.mapping.SchemaMapping;

public class CityGMLFilter {
	private FeatureTypeFilter featureTypeFilter;
	private SelectionFilter selectionFilter;
	private CounterFilter counterFilter;
	
	public CityGMLFilter(SchemaMapping schemaMapping) {
		featureTypeFilter = new FeatureTypeFilter(schemaMapping);
		selectionFilter = new SelectionFilter();
	}

	public FeatureTypeFilter getFeatureTypeFilter() {
		return featureTypeFilter;
	}
	
	public boolean isSetFeatureTypeFilter() {
		return featureTypeFilter != null;
	}
	
	public void setFeatureTypeFilter(FeatureTypeFilter featureTypeFilter) {
		if (featureTypeFilter != null)
			this.featureTypeFilter = featureTypeFilter;
	}

	public SelectionFilter getSelectionFilter() {
		return selectionFilter;
	}
	
	public boolean isSetSelectionFilter() {
		return selectionFilter != null;
	}

	public void setSelectionFilter(SelectionFilter selectionFilter) {
		if (selectionFilter != null)
			this.selectionFilter = selectionFilter;
	}

	public CounterFilter getCounterFilter() {
		return counterFilter;
	}
	
	public boolean isSetCounterFilter() {
		return counterFilter != null;
	}

	public void setCounterFilter(CounterFilter counterFilter) {
		this.counterFilter = counterFilter;
	}
	
}
