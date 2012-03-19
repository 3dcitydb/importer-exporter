/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;

@XmlType(name="UpdateBatchingType", propOrder={
		"featureBatchValue",
		"gmlIdLookupServerBatchValue",
		"tempBatchValue"
		})
public class UpdateBatching {
	@XmlElement(required=true, defaultValue="20")
	@XmlSchemaType(name="positiveInteger")
	private Integer featureBatchValue = 20;
	@XmlElement(required=true, defaultValue="1000")
	@XmlSchemaType(name="positiveInteger")
	private Integer gmlIdLookupServerBatchValue = 1000;
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
				featureBatchValue <= Internal.ORACLE_MAX_BATCH_SIZE)
			this.featureBatchValue = featureBatchValue;
	}

	public Integer getGmlIdLookupServerBatchValue() {
		return gmlIdLookupServerBatchValue;
	}

	public void setGmlIdLookupServerBatchValue(Integer gmlIdLookupServerBatchValue) {
		if (gmlIdLookupServerBatchValue != null && gmlIdLookupServerBatchValue > 0 &&
				gmlIdLookupServerBatchValue <= Internal.ORACLE_MAX_BATCH_SIZE)
			this.gmlIdLookupServerBatchValue = gmlIdLookupServerBatchValue;
	}

	public Integer getTempBatchValue() {
		return tempBatchValue;
	}

	public void setTempBatchValue(Integer tempBatchValue) {
		if (tempBatchValue != null && tempBatchValue > 0 && 
				tempBatchValue <= Internal.ORACLE_MAX_BATCH_SIZE)
			this.tempBatchValue = tempBatchValue;
	}
	
}
