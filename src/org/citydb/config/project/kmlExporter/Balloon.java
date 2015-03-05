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
package org.citydb.config.project.kmlExporter;


import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.general.Path;

@XmlType(name="BalloonType", propOrder={
		"includeDescription",
		"balloonContentMode",
		"balloonContentPath",
		"balloonContentTemplateFile",
		"balloonContentInSeparateFile"
})
public class Balloon {
	private boolean includeDescription;
	private BalloonContentMode balloonContentMode;
	private Path balloonContentPath;
	private String balloonContentTemplateFile;
	private boolean balloonContentInSeparateFile;

	public Balloon() {
		includeDescription = false;
		setBalloonContentMode(BalloonContentMode.GEN_ATTRIB);
		balloonContentPath = new Path();
		balloonContentTemplateFile = "";
		balloonContentInSeparateFile = false;
	}


	public void setIncludeDescription(boolean includeDescription) {
		this.includeDescription = includeDescription;
	}

	public boolean isIncludeDescription() {
		return includeDescription;
	}

	public void setBalloonContentMode(BalloonContentMode balloonContentMode) {
		this.balloonContentMode = balloonContentMode;
	}

	public BalloonContentMode getBalloonContentMode() {
		return balloonContentMode;
	}

	public Path getBalloonContentPath() {
		return balloonContentPath;
	}

	public void setBalloonContentPath(Path balloonContentPath) {
		if (balloonContentPath != null)
			this.balloonContentPath = balloonContentPath;
	}

	public void setBalloonContentTemplateFile(String balloonContentTemplateFile) {
		this.balloonContentTemplateFile = balloonContentTemplateFile;
	}

	public String getBalloonContentTemplateFile() {
		return balloonContentTemplateFile;
	}

	public void setBalloonContentInSeparateFile(boolean balloonContentInSeparateFile) {
		this.balloonContentInSeparateFile = balloonContentInSeparateFile;
	}

	public boolean isBalloonContentInSeparateFile() {
		return balloonContentInSeparateFile;
	}
	
	public boolean equals (Object o) {
		try {
			Balloon balloonToCompare = (Balloon) o;
			if (balloonToCompare.getBalloonContentMode() != this.getBalloonContentMode()) return false;
			if (!balloonToCompare.getBalloonContentTemplateFile().equals(this.getBalloonContentTemplateFile())) return false;
			if (balloonToCompare.isBalloonContentInSeparateFile() != this.isBalloonContentInSeparateFile()) return false;
			if (balloonToCompare.isIncludeDescription() != this.isIncludeDescription()) return false;
			if (balloonToCompare.getBalloonContentPath().getPathMode() != this.getBalloonContentPath().getPathMode()) return false;
			if (!balloonToCompare.getBalloonContentPath().getLastUsedPath().equals(this.getBalloonContentPath().getLastUsedPath())) return false;
			if (!balloonToCompare.getBalloonContentPath().getStandardPath().equals(this.getBalloonContentPath().getStandardPath())) return false;
		}
		catch (ClassCastException e) {
			return false;
		}
		return true;
	}

}
