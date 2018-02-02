package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="file")
public class FileReference {
	@XmlValue
	private String value;

	public FileReference() {
	}
	
	public FileReference(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isSetValue() {
		return value != null;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
