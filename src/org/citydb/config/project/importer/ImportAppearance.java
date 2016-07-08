/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
