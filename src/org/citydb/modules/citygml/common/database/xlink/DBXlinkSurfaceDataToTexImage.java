/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.modules.citygml.common.database.xlink;


public class DBXlinkSurfaceDataToTexImage implements DBXlink {
	private long fromId;
	private long toId;

	public DBXlinkSurfaceDataToTexImage(long fromId, long toId) {
		this.fromId = fromId;
		this.toId = toId;
	}
	
	public long getFromId() {
		return fromId;
	}


	public void setFromId(long fromId) {
		this.fromId = fromId;
	}


	public long getToId() {
		return toId;
	}


	public void setToId(long toId) {
		this.toId = toId;
	}

	public String getGmlId() {
		// no need for gml:ids
		return null;
	}

	public void setGmlId(String gmlId) {
		// no need for gml:ids
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.SURFACE_DATA_TO_TEX_IMAGE;
	}
}
