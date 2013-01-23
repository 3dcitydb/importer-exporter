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
package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ContinuationType", propOrder={
		"lineage",
		"updatingPersonMode",
		"updatingPerson",
		"reasonForUpdate"
		})
public class Continuation {
	@XmlElement(required=true)
	private String lineage = "";
	@XmlElement(required=true)
	private UpdatingPersonMode updatingPersonMode = UpdatingPersonMode.DATABASE;
	private String updatingPerson = "";
	@XmlElement(required=true)
	private String reasonForUpdate = "";
	
	public Continuation() {
		
	}
	
	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public boolean isUpdatingPersonModeDatabase() {
		return updatingPersonMode == UpdatingPersonMode.DATABASE;
	}
	
	public boolean isUpdatingPersonModeUser() {
		return updatingPersonMode == UpdatingPersonMode.USER;
	}
	
	public UpdatingPersonMode getUpdatingPersonMode() {
		return updatingPersonMode;
	}

	public void setUpdatingPersonMode(UpdatingPersonMode updatingPersonMode) {
		this.updatingPersonMode = updatingPersonMode;
	}

	public String getUpdatingPerson() {
		return updatingPerson;
	}

	public void setUpdatingPerson(String updatingPerson) {
		this.updatingPerson = updatingPerson;
	}

	public String getReasonForUpdate() {
		return reasonForUpdate;
	}

	public void setReasonForUpdate(String reasonForUpdate) {
		this.reasonForUpdate = reasonForUpdate;
	}
	
}
