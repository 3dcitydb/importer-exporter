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
package org.citydb.config.project.query.filter.lod;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.LinkedHashSet;

@XmlType(name="LodFilterType", propOrder={
		"lods"
})
public class LodFilter {
	@XmlAttribute
	private LodFilterMode mode = LodFilterMode.OR;
	@XmlAttribute
	private LodSearchMode searchMode = LodSearchMode.DEPTH;	
	@XmlAttribute
	private Integer searchDepth = 1;
	@XmlElement(name="lod", required = true)
	private LinkedHashSet<Integer> lods;
	
	public LodFilter() {
		this(false);
	}
	
	public LodFilter(boolean defaultValue) {
		lods = new LinkedHashSet<>();
		if (defaultValue) {
			for (int lod = 0; lod < 5; lod++)
				setLod(lod);
		}
	}
	
	public LodFilterMode getMode() {
		return mode;
	}

	public void setMode(LodFilterMode mode) {
		this.mode = mode;
	}
	
	public LodSearchMode getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(LodSearchMode searchMode) {
		this.searchMode = searchMode;
	}

	public boolean isSetSearchDepth() {
		return searchDepth != null && searchDepth >= 0;
	}
	
	public int getSearchDepth() {
		return isSetSearchDepth() ? searchDepth : 1;
	}
	
	public void setSearchDepth(int searchDepth) {
		if (searchDepth < 0)
			throw new IllegalArgumentException("The LoD search depth must be greater or equal to 0.");
		
		this.searchDepth = searchDepth;
	}

	public void unsetSearchDepth() {
		searchDepth = null;
	}
	
	public boolean isSetLod(int lod) {
		return lods.contains(lod);
	}
	
	public void setLod(int lod) {
		if (lod < 0 || lod > 4)
			throw new IllegalArgumentException("LoD value must be between 0 and 4.");
		
		lods.add(lod);
	}
	
	public void setLod(int lod, boolean enable) {
		if (lod < 0 || lod > 4)
			throw new IllegalArgumentException("LoD value must be between 0 and 4.");
		
		if (enable)
			lods.add(lod);
		else
			lods.remove(lod);
	}
	
	public boolean isSetAnyLod() {
		for (int lod = 0; lod < 5; lod++) {
			if (lods.contains(lod))
				return true;
		}
		
		return false;
	}
	
	public boolean areAllEnabled() {
		for (int lod = 0; lod < 5; lod++) {
			if (!lods.contains(lod))
				return false;
		}
		
		return true;
	}
	
}
