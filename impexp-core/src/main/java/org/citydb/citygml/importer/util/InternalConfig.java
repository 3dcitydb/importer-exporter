/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.citygml.importer.util;

import org.citydb.config.project.global.UpdatingPersonMode;
import org.citydb.config.project.importer.Continuation;
import org.citydb.file.InputFile;

public class InternalConfig {
	private InputFile inputFile;
	private String currentGmlIdCodespace;
	private UpdatingPersonMode updatingPersonMode;
	private String updatingPerson;
	private String reasonForUpdate;
	private String lineage;

	public InputFile getInputFile() {
		return inputFile;
	}

	public void setInputFile(InputFile inputFile) {
		this.inputFile = inputFile;
	}

	public String getCurrentGmlIdCodespace() {
		return currentGmlIdCodespace;
	}

	public void setCurrentGmlIdCodespace(String currentGmlIdCodespace) {
		if (currentGmlIdCodespace != null && !currentGmlIdCodespace.trim().isEmpty()) {
			this.currentGmlIdCodespace = currentGmlIdCodespace.trim();
		}
	}

	public UpdatingPersonMode getUpdatingPersonMode() {
		return updatingPersonMode != null ? updatingPersonMode : UpdatingPersonMode.DATABASE;
	}

	public void setUpdatingPersonMode(UpdatingPersonMode updatingPersonMode) {
		this.updatingPersonMode = updatingPersonMode;
	}

	public String getUpdatingPerson() {
		return updatingPerson;
	}

	public void setUpdatingPerson(String updatingPerson) {
		if (updatingPerson != null && !updatingPerson.trim().isEmpty()) {
			this.updatingPerson = updatingPerson.trim();
		}
	}

	public String getReasonForUpdate() {
		return reasonForUpdate;
	}

	public void setReasonForUpdate(String reasonForUpdate) {
		if (reasonForUpdate != null && !reasonForUpdate.trim().isEmpty()) {
			this.reasonForUpdate = reasonForUpdate.trim();
		}
	}

	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		if (lineage != null && !lineage.trim().isEmpty()) {
			this.lineage = lineage.trim();
		}
	}

	public void setMetadata(Continuation continuation) {
		setUpdatingPersonMode(continuation.getUpdatingPersonMode());
		setUpdatingPerson(continuation.getUpdatingPerson());
		setReasonForUpdate(continuation.getReasonForUpdate());
		setLineage(continuation.getLineage());
	}
}
