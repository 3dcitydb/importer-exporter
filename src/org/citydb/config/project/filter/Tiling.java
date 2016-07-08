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
package org.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="TilingType", propOrder={
		"rows",
		"columns",
		"tilePath",
		"tilePathSuffix",
		"tileNameSuffix",
		"includeTileAsGenericAttribute",
		"genericAttributeValue"
})
public class Tiling {
	@XmlElement(required=true, defaultValue="1")
	private int rows = 1;
	@XmlElement(required=true, defaultValue="1")
	private int columns = 1;
	private String tilePath = "tile";
	private TileSuffixMode tilePathSuffix = TileSuffixMode.ROW_COLUMN;
	private TileNameSuffixMode tileNameSuffix = TileNameSuffixMode.NONE;
	@XmlElement(defaultValue="false")
	private Boolean includeTileAsGenericAttribute = false;
	private TileSuffixMode genericAttributeValue = TileSuffixMode.XMIN_YMIN_XMAX_YMAX;
	@XmlAttribute(required=true)
	private TilingMode mode = TilingMode.NO_TILING;
	
	public Tiling() {
	}
	
	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public Integer getColumns() {
		return columns;
	}

	public void setColumns(Integer columns) {
		this.columns = columns;
	}

	public String getTilePath() {
		return tilePath;
	}

	public void setTilePath(String tilePath) {
		this.tilePath = tilePath;
	}

	public TileSuffixMode getTilePathSuffix() {
		return tilePathSuffix;
	}

	public void setTilePathSuffix(TileSuffixMode tilePathSuffix) {
		this.tilePathSuffix = tilePathSuffix;
	}

	public TileNameSuffixMode getTileNameSuffix() {
		return tileNameSuffix;
	}

	public void setTileNameSuffix(TileNameSuffixMode tileNameSuffix) {
		this.tileNameSuffix = tileNameSuffix;
	}
	
	public boolean isIncludeTileAsGenericAttribute() {
		if (includeTileAsGenericAttribute != null)
			return includeTileAsGenericAttribute.booleanValue();
		
		return false;
	}

	public Boolean getIncludeTileAsGenericAttribute() {
		return includeTileAsGenericAttribute;
	}

	public void setIncludeTileAsGenericAttribute(Boolean includeTileAsGenericAttribute) {
		this.includeTileAsGenericAttribute = includeTileAsGenericAttribute;
	}

	public TileSuffixMode getGenericAttributeValue() {
		return genericAttributeValue;
	}

	public void setGenericAttributeValue(TileSuffixMode genericAttributeValue) {
		this.genericAttributeValue = genericAttributeValue;
	}

	public void setMode(TilingMode mode) {
		this.mode = mode;
	}

	public TilingMode getMode() {
		return mode;
	}
	
}
