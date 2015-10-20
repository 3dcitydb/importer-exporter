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
	private CodeSpaceMode codeSpaceMode = CodeSpaceMode.USER;
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
