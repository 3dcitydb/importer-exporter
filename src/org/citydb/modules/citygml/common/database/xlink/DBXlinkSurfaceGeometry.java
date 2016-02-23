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
package org.citydb.modules.citygml.common.database.xlink;

import org.citydb.database.TableEnum;

public class DBXlinkSurfaceGeometry implements DBXlink {
	private long id;
	private long parentId;
	private long rootId;
	private boolean reverse;
	private long cityObjectId;
	private TableEnum fromTable;
	private String fromTableAttributeName;
	private String gmlId;
	
	public DBXlinkSurfaceGeometry(long id, long parentId, long rootId, boolean reverse, String gmlId, long cityObjectId, TableEnum fromTable, String fromTableAttributeName) {
		this.id = id;
		this.parentId = parentId;
		this.rootId = rootId;
		this.reverse = reverse;
		this.gmlId = gmlId;
		this.cityObjectId = cityObjectId;
		this.fromTable = fromTable;
		this.fromTableAttributeName = fromTableAttributeName;
	}
	
	public DBXlinkSurfaceGeometry(long id, long parentId, long rootId, boolean reverse, String gmlId, long cityObjectId) {
		this(id, parentId, rootId, reverse, gmlId, cityObjectId, null, null);
	}
	
	public DBXlinkSurfaceGeometry(String gmlId, long cityObjectId, TableEnum fromTable, String fromTableAttributeName) {
		this(0, 0, 0, false, gmlId, cityObjectId, fromTable, fromTableAttributeName);
	}
	
	public long getId() {
		return id;
	}

	public long getParentId() {
		return parentId;
	}

	public long getRootId() {
		return rootId;
	}

	public boolean isReverse() {
		return reverse;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public long getCityObjectId() {
		return cityObjectId;
	}

	public TableEnum getFromTable() {
		return fromTable;
	}

	public String getFromTableAttributeName() {
		return fromTableAttributeName;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.SURFACE_GEOMETRY;
	}

}
