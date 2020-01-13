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
package org.citydb.citygml.common.database.xlink;

public class DBXlinkSurfaceGeometry implements DBXlink {
	private long id;
	private long parentId;
	private long rootId;
	private boolean reverse;
	private long cityObjectId;
	private String table;
	private String fromColumn;
	private String gmlId;
	
	public DBXlinkSurfaceGeometry(long id, long parentId, long rootId, boolean reverse, String gmlId, long cityObjectId, String table, String fromColumn) {
		this.id = id;
		this.parentId = parentId;
		this.rootId = rootId;
		this.reverse = reverse;
		this.gmlId = gmlId;
		this.cityObjectId = cityObjectId;
		this.table = table;
		this.fromColumn = fromColumn;
	}
	
	public DBXlinkSurfaceGeometry(long id, long parentId, long rootId, boolean reverse, String gmlId, long cityObjectId) {
		this(id, parentId, rootId, reverse, gmlId, cityObjectId, null, null);
	}
	
	public DBXlinkSurfaceGeometry(String table, long cityObjectId, String gmlId, String fromColumn) {
		this(0, 0, 0, false, gmlId, cityObjectId, table, fromColumn);
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

	public String getTable() {
		return table;
	}

	public String getFromColumn() {
		return fromColumn;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.SURFACE_GEOMETRY;
	}

}
