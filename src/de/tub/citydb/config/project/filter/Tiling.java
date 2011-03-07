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
