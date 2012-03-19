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
package de.tub.citydb.config.project.filter;

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
