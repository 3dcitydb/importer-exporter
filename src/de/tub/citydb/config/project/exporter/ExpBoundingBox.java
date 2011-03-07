package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.FilterBoundingBoxMode;

@XmlType(name="ExportBoundingBoxType", propOrder={
		"mode"
		})
public class ExpBoundingBox {
	@XmlElement(required=true)
	private FilterBoundingBoxMode mode = FilterBoundingBoxMode.CONTAIN;
	
	public ExpBoundingBox() {
	}

	public FilterBoundingBoxMode getMode() {
		return mode;
	}

	public void setMode(FilterBoundingBoxMode mode) {
		this.mode = mode;
	}
	
}
