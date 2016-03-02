/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
package org.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractComplexFilterType", propOrder={
		"featureCount",
		"gmlName",
		"featureClass"
})
public abstract class AbstractComplexFilter {
	private FeatureCount featureCount;
	private GmlName gmlName;
	private FeatureClass featureClass;

	public AbstractComplexFilter() {
		featureCount = new FeatureCount();
		gmlName = new GmlName();
		featureClass = new FeatureClass();
	}

	public abstract FilterBoundingBox getBoundingBox();
	public abstract void setBoundingBox(FilterBoundingBox boundingBox);
	
	public GmlName getGmlName() {
		return gmlName;
	}

	public void setGmlName(GmlName gmlName) {
		if (gmlName != null)
			this.gmlName = gmlName;
	}

	public FeatureCount getFeatureCount() {
		return featureCount;
	}

	public void setFeatureCount(FeatureCount featureCount) {
		if (featureCount != null)
			this.featureCount = featureCount;
	}

	public FeatureClass getFeatureClass() {
		return featureClass;
	}

	public void setFeatureClass(FeatureClass featureClass) {
		if (featureClass != null)
			this.featureClass = featureClass;
	}

}
