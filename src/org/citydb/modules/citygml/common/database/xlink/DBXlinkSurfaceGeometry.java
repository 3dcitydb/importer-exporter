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
