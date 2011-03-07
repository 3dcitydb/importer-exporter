package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportGmlIdType", propOrder={
		"uuidMode",
		"keepGmlIdAsExternalReference",
		"codeSpaceMode",
		"codeSpace"
})
public class ImpGmlId {
	@XmlElement(required=true)
	private ImpUUIDMode uuidMode = ImpUUIDMode.REPLACE;
	@XmlElement(defaultValue="true")
	private Boolean keepGmlIdAsExternalReference = true;
	@XmlElement(required=true)
	private ImpGmlIdCodeSpaceMode codeSpaceMode = ImpGmlIdCodeSpaceMode.USER;
	@XmlElement(defaultValue="UUID")
	private String codeSpace = "UUID";

	public ImpGmlId() {
	}

	public boolean isUUIDModeReplace() {
		return uuidMode == ImpUUIDMode.REPLACE;
	}

	public boolean isUUIDModeComplement() {
		return uuidMode == ImpUUIDMode.COMPLEMENT;
	}

	public ImpUUIDMode getUuidMode() {
		return uuidMode;
	}

	public void setUuidMode(ImpUUIDMode uuidMode) {
		this.uuidMode = uuidMode;
	}

	public boolean isSetKeepGmlIdAsExternalReference() {
		if (keepGmlIdAsExternalReference != null)
			return keepGmlIdAsExternalReference.booleanValue();

		return false;
	}

	public Boolean getKeepGmlIdAsExternalReference() {
		return keepGmlIdAsExternalReference;
	}

	public void setKeepGmlIdAsExternalReference(Boolean keepGmlIdAsExternalReference) {
		this.keepGmlIdAsExternalReference = keepGmlIdAsExternalReference;
	}

	public boolean isSetRelativeCodeSpaceMode() {
		return codeSpaceMode == ImpGmlIdCodeSpaceMode.RELATIVE;
	}

	public boolean isSetAbsoluteCodeSpaceMode() {
		return codeSpaceMode == ImpGmlIdCodeSpaceMode.ABSOLUTE;
	}

	public boolean isSetUserCodeSpaceMode() {
		return codeSpaceMode == ImpGmlIdCodeSpaceMode.USER;
	}

	public ImpGmlIdCodeSpaceMode getCodeSpaceMode() {
		return codeSpaceMode;
	}

	public void setCodeSpaceMode(ImpGmlIdCodeSpaceMode codeSpaceMode) {
		this.codeSpaceMode = codeSpaceMode;
	}

	public String getCodeSpace() {
		return codeSpace;
	}

	public void setCodeSpace(String codeSpace) {
		this.codeSpace = codeSpace;
	}

}
