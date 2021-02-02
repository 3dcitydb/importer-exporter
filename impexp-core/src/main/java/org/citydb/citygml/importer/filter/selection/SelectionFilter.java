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
package org.citydb.citygml.importer.filter.selection;

import org.citydb.citygml.importer.filter.selection.comparison.LikeFilter;
import org.citydb.citygml.importer.filter.selection.id.ResourceIdFilter;
import org.citydb.citygml.importer.filter.selection.spatial.SimpleBBOXFilter;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.gml.feature.AbstractFeature;

public class SelectionFilter {
	private ResourceIdFilter resourceIdFilter;
	private LikeFilter nameFilter;
	private SimpleBBOXFilter bboxFilter;

	public ResourceIdFilter getResourceIdFilter() {
		return resourceIdFilter;
	}

	public boolean isSetResourceIdFilter() {
		return resourceIdFilter != null;
	}

	public void setResourceIdFilter(ResourceIdFilter resourceIdFilter) {
		this.resourceIdFilter = resourceIdFilter;
	}

	public LikeFilter getNameFilter() {
		return nameFilter;
	}

	public boolean isSetNameFilter() {
		return nameFilter != null;
	}

	public void setNameFilter(LikeFilter nameFilter) {
		this.nameFilter = nameFilter;
	}

	public SimpleBBOXFilter getBboxFilter() {
		return bboxFilter;
	}

	public boolean isSetBboxFilter() {
		return bboxFilter != null;
	}

	public void setBboxFilter(SimpleBBOXFilter bboxFilter) {
		this.bboxFilter = bboxFilter;
	}

	public boolean isSatisfiedBy(AbstractFeature feature) throws FilterException {
		if (resourceIdFilter != null && !resourceIdFilter.isSatisfiedBy(feature))
			return false;

		if (nameFilter != null && !nameFilter.isSatisfiedBy(feature))
			return false;

		if (bboxFilter != null && !bboxFilter.isSatisfiedBy(feature))
			return false;			

		return true;
	}

}
