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
package org.citydb.query.filter.lod;

public class LodFilter {
	private boolean[] lods;
	private LodFilterMode mode;
	private Integer searchDepth;	
		
	public LodFilter(LodFilterMode mode) {
		this(true, mode);
	}
	
	public LodFilter(boolean defaultValue, LodFilterMode mode) {
		lods = new boolean[5];
		for (int i = 0; i < lods.length; i++)
			lods[i] = defaultValue;
		
		this.mode = mode;
	}

	public boolean isSetSearchDepth() {
		return searchDepth != null && searchDepth >= 0;
	}
	
	public int getSearchDepth() {
		return isSetSearchDepth() ? searchDepth : Integer.MAX_VALUE;
	}

	public void setSearchDepth(int searchDepth) {
		if (searchDepth >= 0)
			this.searchDepth = searchDepth;
	}

	public void setEnabled(int lod, boolean enabled) {
		if (lod >= 0 && lod < 5)
			lods[lod] = enabled;
	}
		
	public boolean isEnabled(int lod) {
		return (lod >= 0 && lod < 5) && lods[lod];
	}

	public void setEnabledAll(boolean enabled) {
		for (int i = 0; i < lods.length; i++)
			lods[i] = enabled;
	}
	
	public boolean isAnyEnabled() {
		for (int i = 0; i < lods.length; i++) {
			if (lods[i])
				return true;
		}
		
		return false;
	}
	
	public boolean areAllEnabled() {
		for (int i = 0; i < lods.length; i++) {
			if (!lods[i])
				return false;
		}
		
		return true;
	}
	
	public int getMaximumLod() {
		for (int i = lods.length - 1; i >= 0; i--) {
			if (lods[i])
				return i;
		}
		
		return -1;
	}
	
	public int getMinimumLod() {
		for (int i = 0; i < lods.length; i++) {
			if (lods[i])
				return i;
		}
		
		return -1;
	}
	
	public boolean containsLodGreaterThanOrEuqalTo(int lod) {
		for (int i = lod; i < lods.length; i++) {
			if (lods[i])
				return true;
		}
		
		return false;
	}
	
	public LodFilterMode getFilterMode() {
		return mode;
	}
	
	public void setFilterMode(LodFilterMode mode) {
		this.mode = mode;
	}
	
	public boolean preservesGeometry() {
		return areAllEnabled();
	}
	
	public LodIterator iterator(int fromLod, int toLod, boolean reverse) {
		if (fromLod < 0)
			throw new IllegalArgumentException("Lower boundary must be greater than or equal to 0.");

		if (toLod > 4)
			throw new IllegalArgumentException("Upper boundary must be less than or equal to 4.");
		
		if (fromLod > toLod)
			throw new IllegalArgumentException("Lower boundary must not be greater than upper boundary.");
		
		return new LodIterator(this, fromLod, toLod, reverse);
	}
	
	public LodIterator iterator(int fromLod, int toLod) {
		return iterator(fromLod, toLod, false);
	}
	
}
