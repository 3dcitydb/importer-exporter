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

import org.citydb.config.project.resources.Resources;
import org.citydb.config.project.resources.UIDCacheConfig;

@XmlType(name="ImportResourcesType", propOrder={
		"texImageCache"
})
public class ImportResources extends Resources {
	@XmlElement(required=true)
	private UIDCacheConfig texImageCache;
	
	public ImportResources() {
		texImageCache = new UIDCacheConfig();
	}

	public UIDCacheConfig getTexImageCache() {
		return texImageCache;
	}

	public void setTexImageCache(UIDCacheConfig texImageCache) {
		this.texImageCache = texImageCache;
	}
}
