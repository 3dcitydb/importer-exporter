package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ComplexFilterType", propOrder={
		"featureCountFilter",
		"gmlNameFilter",
		"boundingBoxFilter",
		"featureClassFilter"
})
public class FilterComplex {
	@XmlElement(name="featureCount")
	private FilterFeatureCount featureCountFilter;
	@XmlElement(name="gmlName")
	private FilterGmlName gmlNameFilter;
	@XmlElement(name="boundingBox")
	private FilterBoundingBox boundingBoxFilter;
	@XmlElement(name="featureClass")
	private FilterFeatureClass featureClassFilter;

	public FilterComplex() {
		featureCountFilter = new FilterFeatureCount();
		gmlNameFilter = new FilterGmlName();
		boundingBoxFilter = new FilterBoundingBox();
		featureClassFilter = new FilterFeatureClass();
	}

	public FilterBoundingBox getBoundingBoxFilter() {
		return boundingBoxFilter;
	}

	public void setBoundingBoxFilter(FilterBoundingBox boundingBoxFilter) {
		if (boundingBoxFilter != null)
			this.boundingBoxFilter = boundingBoxFilter;
	}

	public FilterGmlName getGmlNameFilter() {
		return gmlNameFilter;
	}

	public void setGmlNameFilter(FilterGmlName gmlNameFilter) {
		if (gmlNameFilter != null)
			this.gmlNameFilter = gmlNameFilter;
	}

	public FilterFeatureCount getFeatureCountFilter() {
		return featureCountFilter;
	}

	public void setFeatureCountFilter(FilterFeatureCount featureCountFilter) {
		if (featureCountFilter != null)
			this.featureCountFilter = featureCountFilter;
	}

	public FilterFeatureClass getFeatureClassFilter() {
		return featureClassFilter;
	}

	public void setFeatureClassFilter(FilterFeatureClass featureClassFilter) {
		if (featureClassFilter != null)
			this.featureClassFilter = featureClassFilter;
	}
}
