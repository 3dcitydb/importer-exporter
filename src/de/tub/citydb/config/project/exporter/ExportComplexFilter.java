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
package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.AbstractComplexFilter;
import de.tub.citydb.config.project.filter.FilterBoundingBox;
import de.tub.citydb.config.project.filter.TiledBoundingBox;

@XmlType(name="ExportComplexFilterType", propOrder={
		"boundingBox"
})
public class ExportComplexFilter extends AbstractComplexFilter {
	private TiledBoundingBox boundingBox;

	public ExportComplexFilter() {
		boundingBox = new TiledBoundingBox();
	}

	@Override
	public FilterBoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public void setBoundingBox(FilterBoundingBox boundingBox) {
		if (boundingBox instanceof TiledBoundingBox) 
			this.boundingBox = (TiledBoundingBox)boundingBox;
		else {
			this.boundingBox.setActive(boundingBox.getActive());
			this.boundingBox.setLowerLeftCorner(boundingBox.getLowerLeftCorner());
			this.boundingBox.setUpperRightCorner(boundingBox.getUpperRightCorner());
			this.boundingBox.setMode(boundingBox.getMode());
			this.boundingBox.setSrs(boundingBox.getSrs());
		}
	}

	public TiledBoundingBox getTiledBoundingBox() {
		return boundingBox;
	}

	public void setTiledBoundingBox(TiledBoundingBox boundingBox) {
		if (boundingBox != null)
			this.boundingBox = boundingBox;
	}

}
