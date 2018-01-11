package org.citydb.config.project.query.filter.tiling;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.exporter.TilingOptions;
import org.citydb.config.project.kmlExporter.KmlTilingOptions;

@XmlType(name="TilingType", propOrder={
		"extent",
		"rows",
		"columns",
		"tilingOptions"
})
public class Tiling {
	@XmlElement(required=true)
	private BoundingBox extent;
	@XmlElement(required=true, defaultValue="1")
	private int rows = 1;
	@XmlElement(required=true, defaultValue="1")
	private int columns = 1;
	@XmlElements({
		@XmlElement(name="cityGMLTilingOptions", type=TilingOptions.class),
		@XmlElement(name="kmlTilingOptions", type=KmlTilingOptions.class)
	})
	private AbstractTilingOptions tilingOptions;
	
	public Tiling() {
		extent = new BoundingBox();
	}

	public BoundingBox getExtent() {
		return extent;
	}

	public void setExtent(BoundingBox extent) {
		this.extent = extent;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public AbstractTilingOptions getTilingOptions() {
		return tilingOptions;
	}
	
	public boolean isSetTilingOptions() {
		return tilingOptions != null;
	}

	public void setTilingOptions(AbstractTilingOptions tilingOptions) {
		this.tilingOptions = tilingOptions;
	}
	
}
