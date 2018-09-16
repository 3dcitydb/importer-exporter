/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.tiling.AbstractTilingOptions;

@XmlType(name="KmlTilingOptionsType", propOrder={
		"rows",
		"columns",
		"autoTileSideLength"
})
public class KmlTilingOptions extends AbstractTilingOptions {
	@XmlAttribute
	private KmlTilingMode mode = KmlTilingMode.NO_TILING;
	@XmlElement(defaultValue="1")
	private Integer rows = 1;
	@XmlElement(defaultValue="1")
	private Integer columns = 1;
	private double autoTileSideLength = 125.0;
	
	public KmlTilingMode getMode() {
		return mode;
	}

	public void setMode(KmlTilingMode mode) {
		this.mode = mode;
	}
	
	public int getRows() {
		return rows != null ? rows : 1;
	}
	
	public void setRows(int rows) {
		this.rows = rows;
	}
	
	public int getColumns() {
		return columns != null ? columns : 1;
	}
	
	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	public void setAutoTileSideLength(double autoTileSideLength) {
		this.autoTileSideLength = autoTileSideLength;
	}

	public double getAutoTileSideLength() {
		return autoTileSideLength;
	}
	
}
