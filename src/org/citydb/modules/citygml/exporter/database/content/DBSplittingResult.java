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
package org.citydb.modules.citygml.exporter.database.content;

import org.citygml4j.model.citygml.CityGMLClass;

public class DBSplittingResult {
	private final long primaryKey;
	private final CityGMLClass cityObjectType;
	private final String gmlId;
	
	private boolean checkIfAlreadyExported = false;

	public DBSplittingResult(long primaryKey, CityGMLClass cityObjectType) {
		this(null, primaryKey, cityObjectType);
	}

	public DBSplittingResult(String gmlId, long primaryKey, CityGMLClass cityObjectType) {
		this.gmlId = gmlId;
		this.primaryKey = primaryKey;
		this.cityObjectType = cityObjectType;
	}

	public String getGmlId() {
		return gmlId;
	}

	public long getPrimaryKey() {
		return primaryKey;
	}

	public CityGMLClass getCityObjectType() {
		return cityObjectType;
	}

	public boolean isCheckIfAlreadyExported() {
		return checkIfAlreadyExported;
	}

	public void setCheckIfAlreadyExported(boolean checkIfAlreadyExported) {
		this.checkIfAlreadyExported = checkIfAlreadyExported;
	}
	
}
