package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.AbstractComplexFilter;
import de.tub.citydb.config.project.filter.BoundingBox;
import de.tub.citydb.config.project.filter.TiledBoundingBox;

@XmlType(name="ExportComplexFilterType", propOrder={
		"boundingBox"
})
public class ExportComplexFilter extends AbstractComplexFilter {
	private TiledBoundingBox boundingBox;

	public ExportComplexFilter() {
		boundingBox = new TiledBoundingBox();
	}

	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void setBoundingBox(BoundingBox boundingBox) {
		if (boundingBox instanceof TiledBoundingBox) 
			this.boundingBox = (TiledBoundingBox)boundingBox;
		else {
			this.boundingBox.setActive(boundingBox.getActive());
			this.boundingBox.setLowerLeftCorner(boundingBox.getLowerLeftCorner());
			this.boundingBox.setUpperRightCorner(boundingBox.getUpperRightCorner());
			this.boundingBox.setMode(boundingBox.getMode());
			this.boundingBox.setSRS(boundingBox.getSRS());
		}
	}

	public TiledBoundingBox getTiledBoundingBox() {
		return boundingBox;
	}

	public void setTiledBoundingBox(TiledBoundingBox boundingBox) {
		if (boundingBox != null)
			this.boundingBox = boundingBox;
	}

}
