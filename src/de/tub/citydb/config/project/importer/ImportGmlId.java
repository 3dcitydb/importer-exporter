/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.config.project.importer;

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
	private UUIDMode uuidMode = UUIDMode.REPLACE;
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
