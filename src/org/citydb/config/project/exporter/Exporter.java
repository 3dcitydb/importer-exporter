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
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.config.project.general.Path;
import org.citydb.config.project.resources.Resources;

@XmlType(name="ExportType", propOrder={
		"path",
		"targetSRS",
		"cityObjectGroup",
		"address",
		"appearances",
		"filter",
		"cityGMLVersion",
		"xlink",
		"resources"
})
public class Exporter {
	private Path path;
	@XmlIDREF
	private DatabaseSrs targetSRS = DatabaseSrs.createDefaultSrs();
	private ExportCityObjectGroup cityObjectGroup;
	private ExportAddress address;
	private ExportAppearance appearances;
	private ExportFilterConfig filter;
	private CityGMLVersionType cityGMLVersion = CityGMLVersionType.v2_0_0;
	private XLink xlink;
	private Resources resources;

	public Exporter() {
		path = new Path();
		cityObjectGroup = new ExportCityObjectGroup();
		address = new ExportAddress();
		appearances = new ExportAppearance();
		filter = new ExportFilterConfig();
		xlink = new XLink();
		resources = new Resources();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		if (path != null)
			this.path = path;
	}

	public DatabaseSrs getTargetSRS() {
		return targetSRS;
	}

	public void setTargetSRS(DatabaseSrs targetSRS) {
		if (targetSRS != null)
			this.targetSRS = targetSRS;
	}

	public ExportAddress getAddress() {
		return address;
	}

	public void setAddress(ExportAddress address) {
		if (address != null)
			this.address = address;
	}

	public ExportAppearance getAppearances() {
		return appearances;
	}

	public void setAppearances(ExportAppearance appearances) {
		if (appearances != null)
			this.appearances = appearances;
	}

	public ExportCityObjectGroup getCityObjectGroup() {
		return cityObjectGroup;
	}

	public void setCityObjectGroup(ExportCityObjectGroup cityObjectGroup) {
		if (cityObjectGroup != null)
			this.cityObjectGroup = cityObjectGroup;
	}

	public ExportFilterConfig getFilter() {
		return filter;
	}

	public void setFilter(ExportFilterConfig filter) {
		if (filter != null)
			this.filter = filter;
	}

	public CityGMLVersionType getCityGMLVersion() {
		return cityGMLVersion;
	}

	public void setCityGMLVersion(CityGMLVersionType cityGMLVersion) {
		this.cityGMLVersion = cityGMLVersion;
	}

	public XLink getXlink() {
		return xlink;
	}

	public void setXlink(XLink xlink) {
		if (xlink != null)
			this.xlink = xlink;
	}

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources system) {
		if (system != null)
			this.resources = system;
	}

}
