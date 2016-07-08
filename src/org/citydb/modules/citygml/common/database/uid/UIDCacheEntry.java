/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.common.database.uid;

import java.util.concurrent.atomic.AtomicBoolean;

import org.citygml4j.model.citygml.CityGMLClass;

public class UIDCacheEntry {
	private long id;
	private long rootId;
	private boolean reverse;
	private String mapping;
	private CityGMLClass type;
	private AtomicBoolean isRegistered = new AtomicBoolean(false);
	private AtomicBoolean isRequested = new AtomicBoolean(false);

	public UIDCacheEntry(long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		this.id = id;
		this.rootId = rootId;
		this.reverse = reverse;
		this.mapping = mapping;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public long getRootId() {
		return rootId;
	}

	public boolean isReverse() {
		return reverse;
	}

	public String getMapping() {
		return mapping;
	}

	public boolean isRequested() {
		return isRequested.get();
	}
	
	protected boolean getAndSetRequested(boolean value) {
		return isRequested.getAndSet(value);
	}
	
	protected boolean isRegistered() {
		return isRegistered.get();
	}
	
	protected boolean getAndSetRegistered(boolean value) {
		return isRegistered.getAndSet(value);
	}
	
	public CityGMLClass getType() {
		return type;
	}

}
