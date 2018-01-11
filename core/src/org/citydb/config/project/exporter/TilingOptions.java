package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.tiling.AbstractTilingOptions;

@XmlType(name="CityGMLTilingOptionsType", propOrder={
		"rows",
		"columns",
		"tilePath",
		"tilePathSuffix",
		"tileNameSuffix",
		"includeTileAsGenericAttribute",
		"genericAttributeValue"
})
public class TilingOptions extends AbstractTilingOptions {
	@XmlElement(defaultValue="1")
	private Integer rows = 1;
	@XmlElement(defaultValue="1")
	private Integer columns = 1;
	private String tilePath = "tile";
	private TileSuffixMode tilePathSuffix = TileSuffixMode.ROW_COLUMN;
	private TileNameSuffixMode tileNameSuffix = TileNameSuffixMode.NONE;
	@XmlElement(defaultValue="false")
	private Boolean includeTileAsGenericAttribute = false;
	private TileSuffixMode genericAttributeValue = TileSuffixMode.XMIN_YMIN_XMAX_YMAX;
	
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

	public Boolean getIncludeTileAsGenericAttribute() {
		return includeTileAsGenericAttribute;
	}
	
	public boolean isIncludeTileAsGenericAttribute() {
		return includeTileAsGenericAttribute != null ? includeTileAsGenericAttribute.booleanValue() : false;
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
	
}
