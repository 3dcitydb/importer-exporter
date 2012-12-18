package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportXALType", propOrder={
		"importXAL"
})
public class ImportAddress {
	@XmlElement(defaultValue="true")
	private Boolean importXAL = true;
	
	public boolean isSetImportXAL() {
		if (importXAL != null)
			return importXAL.booleanValue();

		return false;
	}

	public Boolean getImportXAL() {
		return importXAL;
	}

	public void setImportXAL(Boolean importXAL) {
		this.importXAL = importXAL;
	}
}
