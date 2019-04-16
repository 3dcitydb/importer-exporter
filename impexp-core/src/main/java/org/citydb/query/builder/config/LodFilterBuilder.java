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

import org.citydb.config.project.query.filter.lod.LodSearchMode;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodFilterMode;

public class LodFilterBuilder {

	public LodFilterBuilder() {

	}

	public LodFilter buildLodFilter(org.citydb.config.project.query.filter.lod.LodFilter lodFilterConfig) throws QueryBuildException {
		if (!lodFilterConfig.isSetAnyLod())
			throw new QueryBuildException("No LoD level provided for LoD filter.");

		LodFilter lodFilter = new LodFilter(LodFilterMode.OR);

		switch (lodFilterConfig.getMode()) {
		case OR:
			lodFilter.setFilterMode(LodFilterMode.OR);
			break;
		case AND:
			lodFilter.setFilterMode(LodFilterMode.AND);
			break;
		}

		for (int lod = 0; lod < 5; lod++)
			lodFilter.setEnabled(lod, lodFilterConfig.isSetLod(lod));
		
		if (lodFilterConfig.getSearchMode() == LodSearchMode.DEPTH && lodFilterConfig.isSetSearchDepth())
			lodFilter.setSearchDepth(lodFilterConfig.getSearchDepth());
		else 
			lodFilter.setSearchDepth(Integer.MAX_VALUE);
		
		return lodFilter;
	}

}
