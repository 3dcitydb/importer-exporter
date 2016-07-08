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
