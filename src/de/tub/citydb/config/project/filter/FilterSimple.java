package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SimpleFilterType", propOrder={
		"gmlIdFilter"
		})
public class FilterSimple {
	@XmlElement(name="gmlIds")
	private FilterGmlId gmlIdFilter;
	
	public FilterSimple() {
		gmlIdFilter = new FilterGmlId();
	}
	
	public FilterGmlId getGmlIdFilter() {
		return gmlIdFilter;
	}

	public void setGmlIdFilter(FilterGmlId gmlIdFilter) {
		if (gmlIdFilter != null)
			this.gmlIdFilter = gmlIdFilter;
	}
}
