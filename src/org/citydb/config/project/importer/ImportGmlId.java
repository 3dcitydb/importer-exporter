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

@XmlType(name="ImportGmlIdType", propOrder={
		"uuidMode",
		"keepGmlIdAsExternalReference",
		"codeSpaceMode",
		"codeSpace"
})
public class ImportGmlId {
	@XmlElement(required=true)
	private UUIDMode uuidMode = UUIDMode.COMPLEMENT;
	@XmlElement(defaultValue="true")
	private Boolean keepGmlIdAsExternalReference = true;
	@XmlElement(required=true)
	private CodeSpaceMode codeSpaceMode = CodeSpaceMode.NONE;
	@XmlElement(defaultValue="UUID")
	private String codeSpace = "UUID";

	public ImportGmlId() {
	}

	public boolean isUUIDModeReplace() {
		return uuidMode == UUIDMode.REPLACE;
	}

	public boolean isUUIDModeComplement() {
		return uuidMode == UUIDMode.COMPLEMENT;
	}

	public UUIDMode getUuidMode() {
		return uuidMode;
	}

	public void setUuidMode(UUIDMode uuidMode) {
		this.uuidMode = uuidMode;
	}

	public boolean isSetKeepGmlIdAsExternalReference() {
		if (keepGmlIdAsExternalReference != null)
			return keepGmlIdAsExternalReference.booleanValue();

		return false;
	}

	public Boolean getKeepGmlIdAsExternalReference() {
		return keepGmlIdAsExternalReference;
	}

	public void setKeepGmlIdAsExternalReference(Boolean keepGmlIdAsExternalReference) {
		this.keepGmlIdAsExternalReference = keepGmlIdAsExternalReference;
	}
	
	public boolean isSetNoneCodeSpaceMode() {
		return codeSpaceMode == CodeSpaceMode.NONE;
	}

	public boolean isSetRelativeCodeSpaceMode() {
		return codeSpaceMode == CodeSpaceMode.RELATIVE;
	}

	public boolean isSetAbsoluteCodeSpaceMode() {
		return codeSpaceMode == CodeSpaceMode.ABSOLUTE;
	}

	public boolean isSetUserCodeSpaceMode() {
		return codeSpaceMode == CodeSpaceMode.USER;
	}

	public CodeSpaceMode getCodeSpaceMode() {
		return codeSpaceMode;
	}

	public void setCodeSpaceMode(CodeSpaceMode codeSpaceMode) {
		this.codeSpaceMode = codeSpaceMode;
	}

	public String getCodeSpace() {
		return codeSpace;
	}

	public void setCodeSpace(String codeSpace) {
		this.codeSpace = codeSpace;
	}

}
