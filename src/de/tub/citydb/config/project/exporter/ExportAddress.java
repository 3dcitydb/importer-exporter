package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportAddressType", propOrder={
		"mode",
		"useFallback"
})
public class ExportAddress {
	@XmlElement(name="exportMode", required=true)
	private AddressMode mode = AddressMode.DB;
	private Boolean useFallback = false;

	public AddressMode getMode() {
		return mode;
	}

	public void setMode(AddressMode mode) {
		this.mode = mode;
	}

	public boolean isSetUseFallback() {
		if (useFallback != null)
			return useFallback.booleanValue();

		return false;
	}

	public Boolean getUseFallback() {
		return useFallback;
	}

	public void setUseFallback(Boolean useFallback) {
		this.useFallback = useFallback;
	}
}
