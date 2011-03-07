package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="TiledBoundingBoxType", propOrder={
		"tiling"
})
public class TiledBoundingBox extends BoundingBox {
	@XmlElement(required=true)
	private Tiling tiling;

	public TiledBoundingBox() {
		tiling = new Tiling();
	}

	public Tiling getTiling() {
		return tiling;
	}

	public void setTiling(Tiling tiling) {
		if (tiling != null)
			this.tiling = tiling;
	}

}
