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
package org.citydb.query.filter.apperance;

import org.citydb.query.filter.FilterException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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
