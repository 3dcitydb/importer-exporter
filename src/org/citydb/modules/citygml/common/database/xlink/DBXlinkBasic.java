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

public class DBXlinkBasic implements DBXlink {
	private long id;
	private TableEnum fromTable;
	private String gmlId;
	private TableEnum toTable;
	private String attrName;

	public DBXlinkBasic(long id, TableEnum fromTable, String gmlId, TableEnum toTable) {
		this.id = id;
		this.fromTable = fromTable;
		this.gmlId = gmlId;
		this.toTable = toTable;
	}

	public long getId() {
		return id;
	}

	public TableEnum getFromTable() {
		return fromTable;
	}

	public void setFromTable(TableEnum fromTable) {
		this.fromTable = fromTable;
	}

	public TableEnum getToTable() {
		return toTable;
	}

	public void setToTable(TableEnum toTable) {
		this.toTable = toTable;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.BASIC;
	}
}
