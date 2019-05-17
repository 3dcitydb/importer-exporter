/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.config.project.deleter;

import org.citydb.config.project.global.UpdatingPersonMode;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;

@XmlType(name="DeleteContinuationType", propOrder={
		"lineage",
		"updatingPersonMode",
		"updatingPerson",
		"reasonForUpdate"
})
public class Continuation {
	private String lineage;
	private UpdatingPersonMode updatingPersonMode = UpdatingPersonMode.DATABASE;
	private String updatingPerson;
	private String reasonForUpdate;

	@XmlTransient
	LocalDateTime terminationDate;

	public boolean isSetLineage() {
		return lineage != null;
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

	public boolean isSetUpdatingPerson() {
		return updatingPerson != null;
	}

	public String getUpdatingPerson() {
		return updatingPerson;
	}

	public void setUpdatingPerson(String updatingPerson) {
		this.updatingPerson = updatingPerson;
	}

	public boolean isSetReasonForUpdate() {
		return reasonForUpdate != null;
	}

	public String getReasonForUpdate() {
		return reasonForUpdate;
	}

	public void setReasonForUpdate(String reasonForUpdate) {
		this.reasonForUpdate = reasonForUpdate;
	}

	public boolean isSetTerminationDate() {
		return terminationDate != null;
	}

	public LocalDateTime getTerminationDate() {
		return terminationDate;
	}

	public void setTerminationDate(LocalDateTime terminationDate) {
		this.terminationDate = terminationDate;
	}
  
}
