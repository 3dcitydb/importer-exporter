package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SimpleFilterType", propOrder={
		"gmlIdFilter"
		})
public class SimpleFilter {
	@XmlElement(name="gmlIds")
	private GmlId gmlIdFilter;
	
	public SimpleFilter() {
		gmlIdFilter = new GmlId();
	}
	
	public GmlId getGmlIdFilter() {
		return gmlIdFilter;
	}

	public void setGmlIdFilter(GmlId gmlIdFilter) {
		if (gmlIdFilter != null)
			this.gmlIdFilter = gmlIdFilter;
	}
}
