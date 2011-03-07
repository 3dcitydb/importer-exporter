package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractFilterType", propOrder={
		"mode",
		"simpleFilter"
})
public abstract class AbstractFilterConfig {
	@XmlElement(required=true)
	private FilterMode mode = FilterMode.COMPLEX;
	@XmlElement(name="simple", required=true)
	private SimpleFilter simpleFilter;
	
	public abstract AbstractComplexFilter getComplexFilter();
	public abstract void setComplexFilter(AbstractComplexFilter complexFilter);
	
	public AbstractFilterConfig() {
		simpleFilter = new SimpleFilter();
	}

	public FilterMode getMode() {
		return mode;
	}

	public boolean isSetSimpleFilter() {
		return mode == FilterMode.SIMPLE;
	}
	
	public boolean isSetComplexFilter() {
		return mode == FilterMode.COMPLEX;
	}
	
	public void setMode(FilterMode mode) {
		this.mode = mode;
	}

	public SimpleFilter getSimpleFilter() {
		return simpleFilter;
	}

	public void setSimpleFilter(SimpleFilter simpleFilter) {
		if (simpleFilter != null)
			this.simpleFilter = simpleFilter;
	}

}
