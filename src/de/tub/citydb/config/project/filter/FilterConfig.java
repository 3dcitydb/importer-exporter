package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="FilterType", propOrder={
		"mode",
		"simpleFilter",
		"complexFilter"
		})
public class FilterConfig {
	@XmlElement(required=true)
	private FilterMode mode = FilterMode.COMPLEX;
	@XmlElement(name="simple", required=true)
	private FilterSimple simpleFilter;
	@XmlElement(name="complex", required=true)
	private FilterComplex complexFilter;
	
	public FilterConfig() {
		simpleFilter = new FilterSimple();
		complexFilter = new FilterComplex();
	}

	public FilterMode getMode() {
		return mode;
	}

	public boolean isSetSimple() {
		return mode == FilterMode.SIMPLE;
	}
	
	public boolean isSetComplex() {
		return mode == FilterMode.COMPLEX;
	}
	
	public void setMode(FilterMode mode) {
		this.mode = mode;
	}

	public FilterSimple getSimpleFilter() {
		return simpleFilter;
	}

	public void setSimpleFilter(FilterSimple simpleFilter) {
		if (simpleFilter != null)
			this.simpleFilter = simpleFilter;
	}

	public FilterComplex getComplexFilter() {
		return complexFilter;
	}

	public void setComplexFilter(FilterComplex complexFilter) {
		if (complexFilter != null)
			this.complexFilter = complexFilter;
	}
	
}
