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
package org.citydb.modules.citygml.common.database.cache.model;

public enum CacheTableModelEnum {
	// provide a unique id for the tmp table
	// that does not extend 6 chars!
	GMLID_FEATURE("IDF"),
	GMLID_GEOMETRY("IDG"),
	SURFACE_GEOMETRY("SG"),
	SOLID_GEOMETRY("SOG"),
	BASIC("BA"),
	LINEAR_RING("LR"),
	TEXTURE_COORD_LIST("TC"),
	TEXTUREPARAM("TP"),
	TEXTUREASSOCIATION("TA"),
	TEXTUREASSOCIATION_TARGET("TAT"),
	TEXTURE_FILE_ID("IDT"),
	TEXTURE_FILE("TF"),
	SURFACE_DATA_TO_TEX_IMAGE("STT"),
	LIBRARY_OBJECT("LO"),
	DEPRECATED_MATERIAL("DP"),
	GROUP_TO_CITYOBJECT("GTC"),
	GLOBAL_APPEARANCE("GA");

	private final String value;

	CacheTableModelEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}
