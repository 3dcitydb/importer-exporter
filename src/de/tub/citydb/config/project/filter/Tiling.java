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
package de.tub.citydb.config.project.filter;

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
