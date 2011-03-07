package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.util.UUIDManager;

@XmlType(name="XLinkConfigType", propOrder={
		"mode",
		"idPrefix",
		"appendId"
		})
public class XLinkConfig {
	@XmlElement(name="multipleRepresentationMode", required=true)
	private XLinkMode mode = XLinkMode.XLINK;
	@XmlElement(defaultValue="UUID_")
	private String idPrefix = UUIDManager.UUIDPrefix;
	@XmlElement(defaultValue="false")
	private Boolean appendId = false;
	
	public XLinkConfig() {
	}

	public boolean isModeXLink() {
		return mode == XLinkMode.XLINK;
	}
	
	public boolean isModeCopy() {
		return mode == XLinkMode.COPY;
	}
	
	public XLinkMode getMode() {
		return mode;
	}

	public void setMode(XLinkMode mode) {
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
