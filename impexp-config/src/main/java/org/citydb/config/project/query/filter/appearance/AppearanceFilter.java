package org.citydb.config.project.query.filter.appearance;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AppearanceFilterType", propOrder={
		"includeNullTheme",
		"themes"
})
public class AppearanceFilter {
	@XmlElement(name="nullTheme")
	private Boolean includeNullTheme;
	@XmlElement(name="theme")
	private List<String> themes;
		
	public AppearanceFilter() {
		themes = new ArrayList<>();
	}
		
	public boolean isIncludeNullTheme() {
		return includeNullTheme != null ? includeNullTheme.booleanValue() : false;
	}

	public void setIncludeNullTheme(boolean includeNullTheme) {
		this.includeNullTheme = includeNullTheme;
	}

	public boolean isSetThemes() {
		return !themes.isEmpty();
	}

	public List<String> getThemes() {
		return themes;
	}

	public void setThemes(List<String> themes) {
		if (themes != null && !themes.isEmpty())
			this.themes = themes;
	}
}
