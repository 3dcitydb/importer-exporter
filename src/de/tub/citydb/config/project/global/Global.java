package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GlobalType", propOrder={
		"logging",
		"language"
		})
public class Global {
	private Logging logging;
	private LanguageType language = LanguageType.fromValue(System.getProperty("user.language"));

	public Global() {
		logging = new Logging();
	}
	
	public Logging getLogging() {
		return logging;
	}

	public void setLogging(Logging logging) {
		if (logging != null)
			this.logging = logging;
	}

	public LanguageType getLanguage() {
		return language;
	}

	public void setLanguage(LanguageType language) {
		this.language = language;
	}
}
