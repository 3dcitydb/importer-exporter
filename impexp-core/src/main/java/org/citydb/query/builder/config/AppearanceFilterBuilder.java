package org.citydb.query.builder.config;

import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.apperance.AppearanceFilter;

public class AppearanceFilterBuilder {
	
	protected AppearanceFilterBuilder() {
		
	}

	protected AppearanceFilter buildAppearanceFilter(org.citydb.config.project.query.filter.appearance.AppearanceFilter appearanceFilterConfig) throws QueryBuildException {	
		AppearanceFilter appearanceFilter = new AppearanceFilter();
		
		appearanceFilter.setIncludeNullTheme(appearanceFilterConfig.isIncludeNullTheme());
		
		for (String theme : appearanceFilterConfig.getThemes()) {
			if (theme != null && !theme.isEmpty())
				appearanceFilter.addTheme(theme);
		}
		
		if (!appearanceFilter.containsThemes())
			throw new QueryBuildException("No valid themes provided for appearance filter.");
		
		return appearanceFilter;
	}
	
}
