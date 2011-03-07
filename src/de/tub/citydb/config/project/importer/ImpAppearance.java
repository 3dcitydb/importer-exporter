package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportAppearanceType", propOrder={
		"importAppearances",
		"importTextureFiles",
		"themeForTexturedSurface"
		})
public class ImpAppearance {
	@XmlElement(name="import", required=true, defaultValue="true")
	private Boolean importAppearances = true;
	@XmlElement(required=true, defaultValue="true")
	private Boolean importTextureFiles = true;
	@XmlElement(required=true, defaultValue="rgbTexture")
	private String themeForTexturedSurface = "rgbTexture";
	
	public ImpAppearance() {
	}

	public boolean isSetImportAppearance() {
		return importAppearances == true;
	}
	
	public Boolean getImportAppearances() {
		return importAppearances;
	}

	public void setImportAppearances(Boolean importAppearances) {
		this.importAppearances = importAppearances;
	}

	public boolean isSetImportTextureFiles() {
		return importTextureFiles == true;
	}
	
	public Boolean getImportTextureFiles() {
		return importTextureFiles;
	}

	public void setImportTextureFiles(Boolean importTextureFiles) {
		this.importTextureFiles = importTextureFiles;
	}

	public String getThemeForTexturedSurface() {
		return themeForTexturedSurface;
	}

	public void setThemeForTexturedSurface(String themeForTexturedSurface) {
		this.themeForTexturedSurface = themeForTexturedSurface;
	}
	
}
