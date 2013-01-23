/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.config.project.general.FeatureClassMode;

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
