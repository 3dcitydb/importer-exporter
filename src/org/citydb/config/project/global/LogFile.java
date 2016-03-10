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
