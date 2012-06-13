/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.common.database.gmlid;

import java.util.concurrent.atomic.AtomicBoolean;

import org.citygml4j.model.citygml.CityGMLClass;

public class GmlIdEntry {
	private long id;
	private long rootId;
	private boolean reverse;
	private String mapping;
	private CityGMLClass type;
	private AtomicBoolean isRegistered = new AtomicBoolean(false);
	private AtomicBoolean isRequested = new AtomicBoolean(false);

	public GmlIdEntry(long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
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
