package org.citydb.query.filter.apperance;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.citydb.query.filter.FilterException;

public class AppearanceFilter {
	private boolean includeNullTheme;
	private HashSet<String> themes;
	
	public AppearanceFilter() {
		themes = new HashSet<>();
	}
	
	public AppearanceFilter(Collection<String> themes) throws FilterException {
		if (themes == null)
			throw new FilterException("List of themes may not be null.");
		
		this.themes = new HashSet<>(themes);
	}
	
	public AppearanceFilter(String... themes) throws FilterException {
		this(Arrays.asList(themes));
	}
	
	public boolean isIncludeNullTheme() {
		return includeNullTheme;
	}

	public void setIncludeNullTheme(boolean includeNullTheme) {
		this.includeNullTheme = includeNullTheme;
	}

	public boolean containsThemes() {
		return includeNullTheme || !themes.isEmpty();
	}
	
	public boolean addTheme(String theme) {
		return themes.add(theme);
	}
	
	public HashSet<String> getThemes() {
		return themes;
	}
	
}
