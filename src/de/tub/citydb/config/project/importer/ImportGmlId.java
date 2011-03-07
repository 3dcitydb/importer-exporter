package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportGmlIdType", propOrder={
		"uuidMode",
		"keepGmlIdAsExternalReference",
		"codeSpaceMode",
		"codeSpace"
})
public class ImportGmlId {
	@XmlElement(required=true)
	private UUIDMode uuidMode = UUIDMode.REPLACE;
	@XmlElement(defaultValue="true")
	private Boolean keepGmlIdAsExternalReference = true;
	@XmlElement(required=true)
	private CodeSpaceMode codeSpaceMode = CodeSpaceMode.USER;
	@XmlElement(defaultValue="UUID")
	private String codeSpace = "UUID";

	public ImportGmlId() {
	}

	public boolean isUUIDModeReplace() {
		return uuidMode == UUIDMode.REPLACE;
	}

	public boolean isUUIDModeComplement() {
		return uuidMode == UUIDMode.COMPLEMENT;
	}

	public UUIDMode getUuidMode() {
		return uuidMode;
	}

	public void setUuidMode(UUIDMode uuidMode) {
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
		return codeSpaceMode == CodeSpaceMode.RELATIVE;
	}

	public boolean isSetAbsoluteCodeSpaceMode() {
		return codeSpaceMode == CodeSpaceMode.ABSOLUTE;
	}

	public boolean isSetUserCodeSpaceMode() {
		return codeSpaceMode == CodeSpaceMode.USER;
	}

	public CodeSpaceMode getCodeSpaceMode() {
		return codeSpaceMode;
	}

	public void setCodeSpaceMode(CodeSpaceMode codeSpaceMode) {
		this.codeSpaceMode = codeSpaceMode;
	}

	public String getCodeSpace() {
		return codeSpace;
	}

	public void setCodeSpace(String codeSpace) {
		this.codeSpace = codeSpace;
	}

}
