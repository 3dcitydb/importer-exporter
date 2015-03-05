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
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="TexturePathType", propOrder={
		"relativePath",
		"absolutePath",
		"noOfBuckets"
})
public class TexturePath {
	@XmlAttribute
	private TexturePathMode mode = TexturePathMode.RELATIVE;
	@XmlAttribute
	private boolean useBuckets = false;
	private String relativePath = "appearance";
	private String absolutePath = "";
	private Integer noOfBuckets;

	public boolean isAbsolute() {
		return mode == TexturePathMode.ABSOLUTE;
	}

	public boolean isRelative() {
		return mode == TexturePathMode.RELATIVE;
	}

	public TexturePathMode getMode() {
		return mode;
	}

	public void setMode(TexturePathMode mode) {
		this.mode = mode;
	}

	public boolean isUseBuckets() {
		return useBuckets;
	}

	public void setUseBuckets(boolean useBuckets) {
		this.useBuckets = useBuckets;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public int getNoOfBuckets() {
		if (noOfBuckets == null)
			return 0;

		return noOfBuckets.intValue();
	}

	public void setNoOfBuckets(Integer noOfBuckets) {
		if (noOfBuckets != null)
			this.noOfBuckets = noOfBuckets;
	}

}
