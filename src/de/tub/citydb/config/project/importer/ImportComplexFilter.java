package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.AbstractComplexFilter;
import de.tub.citydb.config.project.filter.BoundingBox;

@XmlType(name="ImportComplexFilterType", propOrder={
		"boundingBox"
})
public class ImportComplexFilter extends AbstractComplexFilter {
	private BoundingBox boundingBox;

	public ImportComplexFilter() {
		boundingBox = new BoundingBox();
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(BoundingBox boundingBox) {
		if (boundingBox != null)
			this.boundingBox = boundingBox;
	}

}
