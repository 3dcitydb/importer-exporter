/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportAppearanceType", propOrder={
		"exportAppearances",
		"exportTextureFiles",
		"overwriteTextureFiles",
		"uniqueTextureFileNames",
		"texturePath"
})
public class ExportAppearance {
	@XmlElement(name="export", required=true, defaultValue="true")
	private Boolean exportAppearances = true;
	@XmlElement(required=true, defaultValue="true")
	private Boolean exportTextureFiles = true;
	@XmlElement(defaultValue="false")
	private Boolean overwriteTextureFiles = true;
	private Boolean uniqueTextureFileNames = false;
	private TexturePath texturePath;

	public ExportAppearance() {
		texturePath = new TexturePath();
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

	public TexturePath getTexturePath() {
		return texturePath;
	}

	public void setTexturePath(TexturePath texturePath) {
		if (texturePath != null)
			this.texturePath = texturePath;
	}

}
