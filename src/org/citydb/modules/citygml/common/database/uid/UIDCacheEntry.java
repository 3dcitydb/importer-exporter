/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
