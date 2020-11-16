/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.modules.kml.database;

import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.modules.kml.util.CityObject4JSON;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

public class KmlSplittingResult {
	private final long id;
	private final String gmlId;
	private final int objectClassId;
	private final CityGMLClass cityGMLClass;
	private DisplayForm displayForm;
	private CityObject4JSON json;

	public KmlSplittingResult(long id, String gmlId, int objectClassId, CityObject4JSON json, DisplayForm displayForm) {
		this.id = id;
		this.gmlId = gmlId;
		this.objectClassId = objectClassId;
		this.displayForm = displayForm;
		this.json = json;
		
		cityGMLClass = Util.getCityGMLClass(objectClassId);
	}

	public long getId() {
		return id;
	}

	public String getGmlId() {
		return gmlId;
	}
	
	public int getObjectClassId() {
		return objectClassId;
	}
	
	public CityGMLClass getCityGMLClass() {
		return cityGMLClass;
	}

	public void setDisplayForm(DisplayForm displayForm) {
		this.displayForm = displayForm;
	}

	public DisplayForm getDisplayForm() {
		return displayForm;
	}

	public CityObject4JSON getJson() {
		return json;
	}

	public void setJson(CityObject4JSON json) {
		this.json = json;
	}

}
