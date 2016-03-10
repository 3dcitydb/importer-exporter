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

@XmlType(name="ContinuationType", propOrder={
		"lineage",
		"updatingPersonMode",
		"updatingPerson",
		"reasonForUpdate",
		"creationDateMode",
		"terminationDateMode"
		})
public class Continuation {
	@XmlElement(required=true)
	private String lineage = "";
	@XmlElement(required=true)
	private UpdatingPersonMode updatingPersonMode = UpdatingPersonMode.DATABASE;
	private String updatingPerson = "";
	@XmlElement(required=true)
	private String reasonForUpdate = "";
	@XmlElement(required=true)
	private CreationDateMode creationDateMode = CreationDateMode.REPLACE;
	@XmlElement(required=true)
	private TerminationDateMode terminationDateMode = TerminationDateMode.REPLACE;
	
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

	public boolean isCreationDateModeInherit() {
		return creationDateMode == CreationDateMode.INHERIT;
	}

	public boolean isCreationDateModeComplement() {
		return creationDateMode == CreationDateMode.COMPLEMENT;
	}

	public boolean isCreationDateModeReplace() {
		return creationDateMode == CreationDateMode.REPLACE;
	}

	public CreationDateMode getCreationDateMode() {
		return creationDateMode;
	}

	public void setCreationDateMode(CreationDateMode creationDateMode) {
		this.creationDateMode = creationDateMode;
	}

	public boolean isTerminationDateModeInherit() {
		return terminationDateMode == TerminationDateMode.INHERIT;
	}

	public boolean isTerminationDateModeComplement() {
		return terminationDateMode == TerminationDateMode.COMPLEMENT;
	}

	public boolean isTerminationDateModeReplace() {
		return terminationDateMode == TerminationDateMode.REPLACE;
	}

	public TerminationDateMode getTerminationDateMode() {
		return terminationDateMode;
	}

	public void setTerminationDateMode(TerminationDateMode terminationDateMode) {
		this.terminationDateMode = terminationDateMode;
	}
  
}
