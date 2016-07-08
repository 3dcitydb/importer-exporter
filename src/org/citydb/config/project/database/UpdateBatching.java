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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="UpdateBatchingType", propOrder={
		"featureBatchValue",
		"gmlIdCacheBatchValue",
		"tempBatchValue"
		})
public class UpdateBatching {
	@XmlElement(required=true, defaultValue="20")
	@XmlSchemaType(name="positiveInteger")
	private Integer featureBatchValue = 20;
	@XmlElement(required=true, defaultValue="1000")
	@XmlSchemaType(name="positiveInteger")
	private Integer gmlIdCacheBatchValue = 1000;
	@XmlElement(required=true, defaultValue="1000")
	@XmlSchemaType(name="positiveInteger")
	private Integer tempBatchValue = 1000;
	
	public UpdateBatching() {
	}

	public Integer getFeatureBatchValue() {
		return featureBatchValue;
	}

	public void setFeatureBatchValue(Integer featureBatchValue) {
		if (featureBatchValue != null && featureBatchValue > 0 &&
				featureBatchValue <= Database.MAX_BATCH_SIZE)
			this.featureBatchValue = featureBatchValue;
	}

	public Integer getGmlIdCacheBatchValue() {
		return gmlIdCacheBatchValue;
	}

	public void setGmlIdCacheBatchValue(Integer gmlIdCacheBatchValue) {
		if (gmlIdCacheBatchValue != null && gmlIdCacheBatchValue > 0 &&
				gmlIdCacheBatchValue <= Database.MAX_BATCH_SIZE)
			this.gmlIdCacheBatchValue = gmlIdCacheBatchValue;
	}

	public Integer getTempBatchValue() {
		return tempBatchValue;
	}

	public void setTempBatchValue(Integer tempBatchValue) {
		if (tempBatchValue != null && tempBatchValue > 0 && 
				tempBatchValue <= Database.MAX_BATCH_SIZE)
			this.tempBatchValue = tempBatchValue;
	}
	
}
