package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="IndexType", propOrder={
		"spatial",
		"normal"
		})
public class Index {
	private IndexMode spatial = IndexMode.UNCHANGED;
	private IndexMode normal = IndexMode.UNCHANGED;
	
	public Index() {
	}

	public IndexMode getSpatial() {
		return spatial;
	}

	public void setSpatial(IndexMode spatial) {
		this.spatial = spatial;
	}

	public IndexMode getNormal() {
		return normal;
	}

	public void setNormal(IndexMode normal) {
		this.normal = normal;
	}
	
	public boolean isSpatialIndexModeUnchanged() {
		return spatial == IndexMode.UNCHANGED;
	}
	
	public boolean isSpatialIndexModeDeactivate() {
		return spatial == IndexMode.DEACTIVATE;
	}
	
	public boolean isSpatialIndexModeDeactivateActivate() {
		return spatial == IndexMode.DEACTIVATE_ACTIVATE;
	}
	
	public boolean isNormalIndexModeUnchanged() {
		return normal == IndexMode.UNCHANGED;
	}
	
	public boolean isNormalIndexModeDeactivate() {
		return normal == IndexMode.DEACTIVATE;
	}
	
	public boolean isNormalIndexModeDeactivateActivate() {
		return normal == IndexMode.DEACTIVATE_ACTIVATE;
	}
}
