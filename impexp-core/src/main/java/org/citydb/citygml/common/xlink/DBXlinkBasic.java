/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.citygml.common.xlink;

public class DBXlinkBasic implements DBXlink {
	private long id;
	private String table;
	private String fromColumn;
	private String toColumn;
	private String gmlId;
	
	public DBXlinkBasic(long id, String table, String fromColumn, String toColumn, String gmlId) {
		this.id = id;
		this.table = table;
		this.fromColumn = fromColumn;
		this.toColumn = toColumn;
		this.gmlId = gmlId;
	}

	public DBXlinkBasic(String table, long id, String gmlId, String fromColumn) {
		this(id, table, fromColumn, null, gmlId);
	}

	public DBXlinkBasic(String table, String gmlId, long id, String toColumn) {
		this(id, table, null, toColumn, gmlId);
	}
	
	public DBXlinkBasic(String table, long id, String fromColumn, String gmlId, String toColumn) {
		this(id, table, fromColumn, toColumn, gmlId);
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getFromColumn() {
		return fromColumn;
	}

	public void setFromColumn(String fromColumn) {
		this.fromColumn = fromColumn;
	}

	public String getToColumn() {
		return toColumn;
	}

	public void setToColumn(String toColumn) {
		this.toColumn = toColumn;
	}

	public boolean isForward() {
		return fromColumn != null && toColumn == null;
	}

	public boolean isReverse() {
		return fromColumn == null && toColumn != null;
	}

	public boolean isBidirectional() {
		return fromColumn != null && toColumn != null;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.BASIC;
	}
}
