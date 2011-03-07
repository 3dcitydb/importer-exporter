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
public class ExportAppearance {
	@XmlElement(name="export", required=true, defaultValue="true")
	private Boolean exportAppearances = true;
	@XmlElement(required=true, defaultValue="true")
	private Boolean exportTextureFiles = true;
	@XmlElement(defaultValue="false")
	private Boolean overwriteTextureFiles = false;
	private TexturePathMode texturePathMode = TexturePathMode.RELATIVE;
	private String relativeTexturePath = "appearance";
	private String absoluteTexturePath = "";
	
	public ExportAppearance() {
	}

	public boolean isSetExportAppearance() {
		if (exportAppearances != null)
			return exportAppearances.booleanValue();
		
		return false;
	}

	public Boolean getExportAppearances() {
		return exportAppearances;
	}

	public void setExportAppearances(Boolean exportAppearances) {
		this.exportAppearances = exportAppearances;
	}
	
	public boolean isSetExportTextureFiles() {
		if (exportTextureFiles != null)
			return exportTextureFiles.booleanValue();
		
		return false;
	}

	public Boolean getExportTextureFiles() {
		return exportTextureFiles;
	}

	public void setExportTextureFiles(Boolean exportTextureFiles) {
		this.exportTextureFiles = exportTextureFiles;
	}

	public boolean isSetOverwriteTextureFiles() {
		if (overwriteTextureFiles != null)
			return overwriteTextureFiles.booleanValue();
		
		return false;
	}
	
	public Boolean getOverwriteTextureFiles() {
		return overwriteTextureFiles;
	}

	public void setOverwriteTextureFiles(Boolean overwriteTextureFiles) {
		this.overwriteTextureFiles = overwriteTextureFiles;
	}

	public boolean isTexturePathAbsolute() {
		return texturePathMode == TexturePathMode.ABSOLUTE;
	}
	
	public boolean isTexturePathRealtive() {
		return texturePathMode == TexturePathMode.RELATIVE;
	}
	
	public TexturePathMode getTexturePathMode() {
		return texturePathMode;
	}

	public void setTexturePathMode(TexturePathMode texturePathMode) {
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
