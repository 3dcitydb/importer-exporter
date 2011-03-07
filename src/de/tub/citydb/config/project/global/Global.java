package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GlobalType", propOrder={
		"language"
		})
public class Global {
	private LanguageType language = LanguageType.SYSTEM;

	public Global() {
	}
	
	public LanguageType getLanguage() {
		return language;
	}

	public void setLanguage(LanguageType language) {
		this.language = language;
	}
}
