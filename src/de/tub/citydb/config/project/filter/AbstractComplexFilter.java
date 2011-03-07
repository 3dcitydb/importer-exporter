package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractComplexFilterType", propOrder={
		"featureCount",
		"gmlName",
		"featureClass"
})
public abstract class AbstractComplexFilter {
	private FeatureCount featureCount;
	private GmlName gmlName;
	private FeatureClass featureClass;

	public AbstractComplexFilter() {
		featureCount = new FeatureCount();
		gmlName = new GmlName();
		featureClass = new FeatureClass();
	}

	public abstract BoundingBox getBoundingBox();
	public abstract void setBoundingBox(BoundingBox boundingBox);
	
	public GmlName getGmlName() {
		return gmlName;
	}

	public void setGmlName(GmlName gmlName) {
		if (gmlName != null)
			this.gmlName = gmlName;
	}

	public FeatureCount getFeatureCount() {
		return featureCount;
	}

	public void setFeatureCount(FeatureCount featureCount) {
		if (featureCount != null)
			this.featureCount = featureCount;
	}

	public FeatureClass getFeatureClass() {
		return featureClass;
	}

	public void setFeatureClass(FeatureClass featureClass) {
		if (featureClass != null)
			this.featureClass = featureClass;
	}

}
