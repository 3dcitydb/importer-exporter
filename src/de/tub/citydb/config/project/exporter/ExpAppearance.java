package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportAppearanceType", propOrder={
		"exportAppearances",
		"exportTextureFiles",
		"overwriteTextureFiles",
		"texturePathMode",
		"relativeTexturePath",
		"absoluteTexturePath"
		})
public class ExpAppearance {
	@XmlElement(name="export", required=true, defaultValue="true")
	private Boolean exportAppearances = true;
	@XmlElement(required=true, defaultValue="true")
	private Boolean exportTextureFiles = true;
	@XmlElement(defaultValue="false")
	private Boolean overwriteTextureFiles = false;
	private ExpTexturePathMode texturePathMode = ExpTexturePathMode.RELATIVE;
	private String relativeTexturePath = "appearance";
	private String absoluteTexturePath = "";
	
	public ExpAppearance() {
	}

	public boolean isSetExportAppearance() {
		return exportAppearances == true;
	}

	public Boolean getExportAppearances() {
		return exportAppearances;
	}

	public void setExportAppearances(Boolean exportAppearances) {
		this.exportAppearances = exportAppearances;
	}
	
	public boolean isSetExportTextureFiles() {
		return exportTextureFiles == true;
	}

	public Boolean getExportTextureFiles() {
		return exportTextureFiles;
	}

	public void setExportTextureFiles(Boolean exportTextureFiles) {
		this.exportTextureFiles = exportTextureFiles;
	}

	public boolean isSetOverwriteTextureFiles() {
		return overwriteTextureFiles== true;
	}
	
	public Boolean getOverwriteTextureFiles() {
		return overwriteTextureFiles;
	}

	public void setOverwriteTextureFiles(Boolean overwriteTextureFiles) {
		this.overwriteTextureFiles = overwriteTextureFiles;
	}

	public boolean isTexturePathAbsolute() {
		return texturePathMode == ExpTexturePathMode.ABSOLUTE;
	}
	
	public boolean isTexturePathRealtive() {
		return texturePathMode == ExpTexturePathMode.RELATIVE;
	}
	
	public ExpTexturePathMode getTexturePathMode() {
		return texturePathMode;
	}

	public void setTexturePathMode(ExpTexturePathMode texturePathMode) {
		this.texturePathMode = texturePathMode;
	}

	public String getRelativeTexturePath() {
		return relativeTexturePath;
	}

	public void setRelativeTexturePath(String relativeTexturePath) {
		this.relativeTexturePath = relativeTexturePath;
	}

	public String getAbsoluteTexturePath() {
		return absoluteTexturePath;
	}

	public void setAbsoluteTexturePath(String absoluteTexturePath) {
		this.absoluteTexturePath = absoluteTexturePath;
	}
	
}
