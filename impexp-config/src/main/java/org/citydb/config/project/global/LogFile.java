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
package org.citydb.config.project.global;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LogFileType", propOrder={
		"logLevel",
		"useAlternativeLogFile",
		"alternativeLogFile"
		})
public class LogFile {
	@XmlAttribute
	private boolean active = false;
	private LogLevel logLevel = LogLevel.INFO;
	private Boolean useAlternativeLogFile = false;
	private String alternativeLogFile = "";

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		if (logLevel != null)
			this.logLevel = logLevel;
	}
	
	public boolean isUseAlternativeLogFile() {
		return useAlternativeLogFile != null ? useAlternativeLogFile : false;
	}

	public void setUseAlternativeLogFile(Boolean useAlternativeLogFile) {
		this.useAlternativeLogFile = useAlternativeLogFile;
	}

	public String getAlternativeLogFile() {
		return alternativeLogFile;
	}

	public void setAlternativeLogFile(String alternativeLogFile) {
		if (alternativeLogFile != null)
			this.alternativeLogFile = alternativeLogFile;
	}

}
