/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.citygml.common.database.xlink;

import de.tub.citydb.database.TableEnum;

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
