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
package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="IndexType", propOrder={
		"spatial",
		"normal"
		})
public class Index {
	private IndexMode spatial = IndexMode.UNCHANGED;
	private IndexMode normal = IndexMode.UNCHANGED;
	
	public Index() {
	}

	public IndexMode getSpatial() {
		return spatial;
	}

	public void setSpatial(IndexMode spatial) {
		this.spatial = spatial;
	}

	public IndexMode getNormal() {
		return normal;
	}

	public void setNormal(IndexMode normal) {
		this.normal = normal;
	}
	
	public boolean isSpatialIndexModeUnchanged() {
		return spatial == IndexMode.UNCHANGED;
	}
	
	public boolean isSpatialIndexModeDeactivate() {
		return spatial == IndexMode.DEACTIVATE;
	}
	
	public boolean isSpatialIndexModeDeactivateActivate() {
		return spatial == IndexMode.DEACTIVATE_ACTIVATE;
	}
	
	public boolean isNormalIndexModeUnchanged() {
		return normal == IndexMode.UNCHANGED;
	}
	
	public boolean isNormalIndexModeDeactivate() {
		return normal == IndexMode.DEACTIVATE;
	}
	
	public boolean isNormalIndexModeDeactivateActivate() {
		return normal == IndexMode.DEACTIVATE_ACTIVATE;
	}
}
