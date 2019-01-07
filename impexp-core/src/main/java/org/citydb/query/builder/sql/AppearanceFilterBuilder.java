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
package org.citydb.query.builder.sql;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.apperance.AppearanceFilter;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class AppearanceFilterBuilder {
	private final AbstractDatabaseAdapter databaseAdapter;
	
	public AppearanceFilterBuilder(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	@SuppressWarnings("unchecked")
	public PredicateToken buildAppearanceFilter(AppearanceFilter appearanceFilter, Column themeColumn) throws QueryBuildException {
		if (!appearanceFilter.containsThemes())
			throw new QueryBuildException("The appearance filter does not contain themes.");

		List<PredicateToken> predicates = new ArrayList<>();
		
		// add null theme predicate
		if (appearanceFilter.isIncludeNullTheme())
			predicates.add(ComparisonFactory.isNull(themeColumn));

		// add in operator for themes
		HashSet<String> themes = appearanceFilter.getThemes();
		if (themes.size() == 1) {
			predicates.add(ComparisonFactory.equalTo(themeColumn, new PlaceHolder<>(themes.iterator().next())));
		} else {
			List<PlaceHolder<String>> placeHolders = new ArrayList<>();
			int maxItems = databaseAdapter.getSQLAdapter().getMaximumNumberOfItemsForInOperator();
			int i = 0;
			
			Iterator<String> iter = themes.iterator();
			while (iter.hasNext()) {
				placeHolders.add(new PlaceHolder<>(iter.next()));

				if (++i == maxItems || !iter.hasNext()) {
					predicates.add(ComparisonFactory.in(themeColumn, new LiteralList(placeHolders.toArray(new PlaceHolder[0]))));
					placeHolders.clear();
					i = 0;
				}
			}
		}

		return LogicalOperationFactory.OR(predicates);
	}

}
