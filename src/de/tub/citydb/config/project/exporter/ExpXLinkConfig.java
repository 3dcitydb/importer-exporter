package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.util.UUIDManager;

@XmlType(name="ExportXLinkConfigType", propOrder={
		"mode",
		"idPrefix",
		"appendId"
		})
public class ExpXLinkConfig {
	@XmlElement(name="multipleRepresentationMode", required=true)
	private ExpXLinkMode mode = ExpXLinkMode.XLINK;
	@XmlElement(defaultValue="UUID_")
	private String idPrefix = UUIDManager.UUIDPrefix;
	@XmlElement(defaultValue="false")
	private Boolean appendId = false;
	
	public ExpXLinkConfig() {
	}

	public boolean isModeXLink() {
		return mode == ExpXLinkMode.XLINK;
	}
	
	public boolean isModeCopy() {
		return mode == ExpXLinkMode.COPY;
	}
	
	public ExpXLinkMode getMode() {
		return mode;
	}

	public void setMode(ExpXLinkMode mode) {
		this.mode = mode;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	public boolean isSetAppendId() {
		if (appendId != null)
			return appendId.booleanValue();
		
		return false;
	}

	public Boolean getAppendId() {
		return appendId;
	}

	public void setAppendId(Boolean appendId) {
		this.appendId = appendId;
	}
	
}
