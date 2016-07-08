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
