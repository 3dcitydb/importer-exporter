/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
import javax.xml.namespace.QName;

import org.citygml4j.model.module.citygml.CoreModule;

@XmlType(name="DBOperationType", propOrder={
		"lastUsed",
		"boundingBoxTypeName",
		"boundingBoxSrs",
		"spatialIndex",
		"normalIndex"
})
public class DBOperation {
	private DBOperationType lastUsed = DBOperationType.REPORT;
	private QName boundingBoxTypeName = new QName(CoreModule.v2_0_0.getNamespaceURI(), "_CityObject");
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

	public QName getBoundingBoxTypeName() {
		return boundingBoxTypeName;
	}

	public void setBoundingBoxTypeName(QName boundingBoxTypeName) {
		this.boundingBoxTypeName = boundingBoxTypeName;
	}

	public DatabaseSrs getBoundingBoxSrs() {
		return boundingBoxSrs;
	}

	public void setBoundingBoxSrs(DatabaseSrs boundingBoxSrs) {
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
