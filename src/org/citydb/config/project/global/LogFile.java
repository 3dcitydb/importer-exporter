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
package org.citydb.config.project.global;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.log.LogLevel;

@XmlType(name="LogFileType", propOrder={
		"logLevel",
		"useAlternativeLogPath",
		"alternativeLogPath"
		})
public class LogFile {
	@XmlAttribute(required=false)
	private Boolean active = false;
	private LogLevel logLevel = LogLevel.INFO;
	private Boolean useAlternativeLogPath = false;
	private String alternativeLogPath = "";
	
	public LogFile() {
	}
	
	public boolean isSet() {
		if (active != null)
			return active.booleanValue();
		
		return false;
	}
	
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		if (logLevel != null)
			this.logLevel = logLevel;
	}

	public Boolean getUseAlternativeLogPath() {
		return useAlternativeLogPath;
	}
	
	public boolean isSetUseAlternativeLogPath() {
		if (useAlternativeLogPath != null)
			return useAlternativeLogPath.booleanValue();
		
		return false;
	}

	public void setUseAlternativeLogPath(Boolean useAlternativeLogPath) {
		if (useAlternativeLogPath != null)
			this.useAlternativeLogPath = useAlternativeLogPath;
	}

	public String getAlternativeLogPath() {
		return alternativeLogPath;
	}

	public void setAlternativeLogPath(String alternativeLogPath) {
		if (alternativeLogPath != null)
			this.alternativeLogPath = alternativeLogPath;
	}

}
