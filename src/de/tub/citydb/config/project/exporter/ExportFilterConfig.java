package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.AbstractComplexFilter;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;

@XmlType(name="ExportFilterType", propOrder={
		"complexFilter"
		})
public class ExportFilterConfig extends AbstractFilterConfig {
	@XmlElement(name="complex", required=true)
	private ExportComplexFilter complexFilter;
	
	public ExportFilterConfig() {
		complexFilter = new ExportComplexFilter();
	}

	public ExportComplexFilter getComplexFilter() {
		return complexFilter;
	}

	public void setComplexFilter(ExportComplexFilter complexFilter) {
		if (complexFilter != null)
			this.complexFilter = complexFilter;
	}

	@Override
	public void setComplexFilter(AbstractComplexFilter complexFilter) {
		if (complexFilter instanceof ExportComplexFilter)
			this.complexFilter = (ExportComplexFilter)complexFilter;
		else {
			this.complexFilter.setBoundingBox(complexFilter.getBoundingBox());
			this.complexFilter.setFeatureClass(complexFilter.getFeatureClass());
			this.complexFilter.setFeatureCount(complexFilter.getFeatureCount());
			this.complexFilter.setGmlName(complexFilter.getGmlName());
		}
	}
	
}
