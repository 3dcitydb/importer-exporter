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
package org.citydb.config.project.database;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.config.project.general.FeatureClassMode;

@XmlType(name="DBOperationType", propOrder={
		"lastUsed",
		"boundingBoxFeatureClass",
		"boundingBoxSrs",
		"spatialIndex",
		"normalIndex"
})
public class DBOperation {
	private DBOperationType lastUsed = DBOperationType.REPORT;
	private FeatureClassMode boundingBoxFeatureClass = FeatureClassMode.CITYOBJECT;
	@XmlIDREF
	private DatabaseSrs boundingBoxSrs = DatabaseSrs.createDefaultSrs();
	private boolean spatialIndex;
	private boolean normalIndex;
	
	public DBOperation() {
	}

	public DBOperationType lastUsed() {
		return lastUsed;
	}

	public void setLastUsed(DBOperationType mode) {
		this.lastUsed = mode;
	}

	public FeatureClassMode getBoundingBoxFeatureClass() {
		return boundingBoxFeatureClass;
	}

	public void setBoundingBoxFeatureClass(FeatureClassMode boundingBoxFeatureClass) {
		this.boundingBoxFeatureClass = boundingBoxFeatureClass;
	}

	public DatabaseSrs getBoundingBoxSRS() {
		return boundingBoxSrs;
	}

	public void setBoundingBoxSRS(DatabaseSrs boundingBoxSrs) {
		this.boundingBoxSrs = boundingBoxSrs;
	}

	public boolean isSetSpatialIndex() {
		return spatialIndex;
	}

	public void setSpatialIndex(boolean spatialIndex) {
		this.spatialIndex = spatialIndex;
	}

	public boolean isSetNormalIndex() {
		return normalIndex;
	}

	public void setNormalIndex(boolean normalIndex) {
		this.normalIndex = normalIndex;
	}
	
}
