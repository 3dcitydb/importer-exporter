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

@XmlType(name = "ExportBatchingType", propOrder = {
		"featureBatchSize",
		"geometryBatchSize",
		"blobBatchSize"
})
public class ExportBatching {
	public static final int MAX_BATCH_SIZE = 1000;
	public static final int DEFAULT_BATCH_SIZE = 30;

	@XmlElement(defaultValue = "30")
	@XmlSchemaType(name = "positiveInteger")
	private int featureBatchSize = DEFAULT_BATCH_SIZE;
	@XmlElement(defaultValue = "30")
	@XmlSchemaType(name = "positiveInteger")
	private int geometryBatchSize = DEFAULT_BATCH_SIZE;
	@XmlElement(defaultValue = "30")
	@XmlSchemaType(name = "positiveInteger")
	private int blobBatchSize = DEFAULT_BATCH_SIZE;

	public int getFeatureBatchSize() {
		return featureBatchSize > 0 ? featureBatchSize : DEFAULT_BATCH_SIZE;
	}

	public void setFeatureBatchSize(int featureBatchSize) {
		if (featureBatchSize > 0 && featureBatchSize <= MAX_BATCH_SIZE)
			this.featureBatchSize = featureBatchSize;
	}

	public int getGeometryBatchSize() {
		return geometryBatchSize > 0 ? geometryBatchSize : DEFAULT_BATCH_SIZE;
	}

	public void setGeometryBatchSize(int geometryBatchSize) {
		if (geometryBatchSize > 0 && geometryBatchSize <= MAX_BATCH_SIZE)
			this.geometryBatchSize = geometryBatchSize;
	}

	public int getBlobBatchSize() {
		return blobBatchSize > 0 ? blobBatchSize : DEFAULT_BATCH_SIZE;
	}

	public void setBlobBatchSize(int blobBatchSize) {
		if (blobBatchSize > 0 && blobBatchSize <= MAX_BATCH_SIZE)
			this.blobBatchSize = blobBatchSize;
	}
}
