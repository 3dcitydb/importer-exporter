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
package org.citydb.config.project.query.filter.appearance;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

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
		return includeNullTheme != null && includeNullTheme;
	}

	public void setIncludeNullTheme(boolean includeNullTheme) {
		this.includeNullTheme = includeNullTheme;
	}

	public boolean containsThemes() {
		return isIncludeNullTheme() || !themes.isEmpty();
	}

	public List<String> getThemes() {
		return themes;
	}

	public void setThemes(List<String> themes) {
		if (themes != null && !themes.isEmpty())
			this.themes = themes;
	}
}
