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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportBatchingType", propOrder = {
		"featureBatchSize",
		"gmlIdCacheBatchSize",
		"tempBatchSize"
})
public class ImportBatching {
	@XmlElement(required = true, defaultValue = "20")
	@XmlSchemaType(name = "positiveInteger")
	private Integer featureBatchSize = 20;
	@XmlElement(required = true, defaultValue = "1000")
	@XmlSchemaType(name = "positiveInteger")
	private Integer gmlIdCacheBatchSize = 1000;
	@XmlElement(required = true, defaultValue = "1000")
	@XmlSchemaType(name = "positiveInteger")
	private Integer tempBatchSize = 1000;

	public Integer getFeatureBatchSize() {
		return featureBatchSize;
	}

	public void setFeatureBatchSize(Integer featureBatchSize) {
		if (featureBatchSize != null && featureBatchSize > 0 &&
				featureBatchSize <= Database.MAX_BATCH_SIZE)
			this.featureBatchSize = featureBatchSize;
	}

	public Integer getGmlIdCacheBatchSize() {
		return gmlIdCacheBatchSize;
	}

	public void setGmlIdCacheBatchSize(Integer gmlIdCacheBatchSize) {
		if (gmlIdCacheBatchSize != null && gmlIdCacheBatchSize > 0 &&
				gmlIdCacheBatchSize <= Database.MAX_BATCH_SIZE)
			this.gmlIdCacheBatchSize = gmlIdCacheBatchSize;
	}

	public Integer getTempBatchSize() {
		return tempBatchSize;
	}

	public void setTempBatchSize(Integer tempBatchSize) {
		if (tempBatchSize != null && tempBatchSize > 0 &&
				tempBatchSize <= Database.MAX_BATCH_SIZE)
			this.tempBatchSize = tempBatchSize;
	}
	
}
