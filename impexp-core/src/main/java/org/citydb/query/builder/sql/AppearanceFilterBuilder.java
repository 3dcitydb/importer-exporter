package org.citydb.query.builder.sql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.apperance.AppearanceFilter;

import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;

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
			List<PlaceHolder<String>> placeHolders = new ArrayList<PlaceHolder<String>>();
			int maxItems = databaseAdapter.getSQLAdapter().getMaximumNumberOfItemsForInOperator();
			int i = 0;
			
			Iterator<String> iter = themes.iterator();
			while (iter.hasNext()) {
				placeHolders.add(new PlaceHolder<String>(iter.next()));

				if (++i == maxItems || !iter.hasNext()) {
					predicates.add(ComparisonFactory.in(themeColumn, new LiteralList(placeHolders.toArray(new PlaceHolder[placeHolders.size()]))));
					placeHolders.clear();
					i = 0;
				}
			}
		}

		return LogicalOperationFactory.OR(predicates);
	}

}
