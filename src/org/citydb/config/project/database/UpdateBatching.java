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
