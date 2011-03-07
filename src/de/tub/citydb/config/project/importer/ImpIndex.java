package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportIndexType", propOrder={
		"spatial",
		"normal"
		})
public class ImpIndex {
	private ImpIndexMode spatial = ImpIndexMode.UNCHANGED;
	private ImpIndexMode normal = ImpIndexMode.UNCHANGED;
	
	public ImpIndex() {
	}

	public ImpIndexMode getSpatial() {
		return spatial;
	}

	public void setSpatial(ImpIndexMode spatial) {
		this.spatial = spatial;
	}

	public ImpIndexMode getNormal() {
		return normal;
	}

	public void setNormal(ImpIndexMode normal) {
		this.normal = normal;
	}
	
	public boolean isSpatialIndexModeUnchanged() {
		return spatial == ImpIndexMode.UNCHANGED;
	}
	
	public boolean isSpatialIndexModeDeactivate() {
		return spatial == ImpIndexMode.DEACTIVATE;
	}
	
	public boolean isSpatialIndexModeDeactivateActivate() {
		return spatial == ImpIndexMode.DEACTIVATE_ACTIVATE;
	}
	
	public boolean isNormalIndexModeUnchanged() {
		return normal == ImpIndexMode.UNCHANGED;
	}
	
	public boolean isNormalIndexModeDeactivate() {
		return normal == ImpIndexMode.DEACTIVATE;
	}
	
	public boolean isNormalIndexModeDeactivateActivate() {
		return normal == ImpIndexMode.DEACTIVATE_ACTIVATE;
	}
}
