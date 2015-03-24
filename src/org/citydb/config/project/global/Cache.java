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
package org.citydb.config.project.global;

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="CacheType", propOrder={
		"mode",
		"localPath"
})
public class Cache {
	@XmlElement(required=true)
	private CacheMode mode = CacheMode.DATABASE;
	private String localPath;

	public Cache() {
		File tmp = new File(System.getProperty("java.io.tmpdir") + "3dcitydb.tmp");
		if ((tmp.exists() || tmp.mkdir()) && tmp.canWrite())
			localPath = tmp.getAbsolutePath();
	}

	public boolean isUseDatabase() {
		return mode == CacheMode.DATABASE;
	}

	public boolean isUseLocal() {
		return mode == CacheMode.LOCAL;
	}

	public CacheMode getCacheMode() {
		return mode;
	}

	public void setCacheMode(CacheMode mode) {
		this.mode = mode;
	}

	public String getLocalCachePath() {	
		return localPath;
	}

	public void setLocalCachePath(String localPath) {
		this.localPath = localPath;
	}

}
