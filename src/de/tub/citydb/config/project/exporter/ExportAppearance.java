/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportAppearanceType", propOrder={
		"exportAppearances",
		"exportTextureFiles",
		"overwriteTextureFiles",
		"uniqueTextureFileNames",
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
	private Boolean overwriteTextureFiles = true;
	private Boolean uniqueTextureFileNames = false;
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
	
	public boolean isSetUniqueTextureFileNames() {
		if (uniqueTextureFileNames != null)
			return uniqueTextureFileNames.booleanValue();
		
		return false;
	}
	
	public Boolean getUniqueTextureFileNames() {
		return uniqueTextureFileNames;
	}

	public void setUniqueTextureFileNames(Boolean uniqueTextureFileNames) {
		this.uniqueTextureFileNames = uniqueTextureFileNames;
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
