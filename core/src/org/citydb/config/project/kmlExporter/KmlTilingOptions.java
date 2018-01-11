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
