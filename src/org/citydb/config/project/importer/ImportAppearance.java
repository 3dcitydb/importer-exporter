/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportAppearanceType", propOrder={
		"importAppearances",
		"importTextureFiles",
		"themeForTexturedSurface"
})
public class ImportAppearance {
	@XmlElement(name="import", required=true, defaultValue="true")
	private Boolean importAppearances = true;
	@XmlElement(required=true, defaultValue="true")
	private Boolean importTextureFiles = true;
	@XmlElement(required=true, defaultValue="rgbTexture")
	private String themeForTexturedSurface = "rgbTexture";

	public ImportAppearance() {
	}

	public boolean isSetImportAppearance() {
		if (importAppearances != null)
			return importAppearances.booleanValue();

		return false;
	}

	public Boolean getImportAppearances() {
		return importAppearances;
	}

	public void setImportAppearances(Boolean importAppearances) {
		this.importAppearances = importAppearances;
	}

	public boolean isSetImportTextureFiles() {
		if (importTextureFiles != null)
			return importTextureFiles.booleanValue();

		return false;
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
