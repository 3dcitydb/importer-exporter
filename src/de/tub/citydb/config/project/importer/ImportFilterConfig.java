package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.AbstractComplexFilter;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;

@XmlType(name="ImportFilterType", propOrder={
		"complexFilter"
		})
public class ImportFilterConfig extends AbstractFilterConfig {
	@XmlElement(name="complex", required=true)
	private ImportComplexFilter complexFilter;
	
	public ImportFilterConfig() {
		complexFilter = new ImportComplexFilter();
	}

	public ImportComplexFilter getComplexFilter() {
		return complexFilter;
	}

	public void setComplexFilter(ImportComplexFilter complexFilter) {
		if (complexFilter != null)
			this.complexFilter = complexFilter;
	}

	@Override
	public void setComplexFilter(AbstractComplexFilter complexFilter) {
		if (complexFilter instanceof ImportComplexFilter)
			this.complexFilter = (ImportComplexFilter)complexFilter;
		else {
			this.complexFilter.setBoundingBox(complexFilter.getBoundingBox());
			this.complexFilter.setFeatureClass(complexFilter.getFeatureClass());
			this.complexFilter.setFeatureCount(complexFilter.getFeatureCount());
			this.complexFilter.setGmlName(complexFilter.getGmlName());
		}
	}
	
}
