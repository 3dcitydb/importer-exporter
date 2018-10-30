/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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

import org.citydb.config.project.general.Path;
import org.citydb.config.project.general.XSLTransformation;
import org.citydb.config.project.query.Query;
import org.citydb.config.project.resources.Resources;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportType", propOrder={
		"query",
		"genericQuery",
		"path",
		"continuation",
		"cityObjectGroup",
		"address",
		"appearances",
		"xlink",
		"xslTransformation",
		"resources"
})
public class Exporter {
	private SimpleQuery query;
	private Query genericQuery;
	private Path path;
	private Continuation continuation;
	private ExportCityObjectGroup cityObjectGroup;
	private ExportAddress address;
	private ExportAppearance appearances;
	private XLink xlink;
	private XSLTransformation xslTransformation;
	private Resources resources;

	public Exporter() {
		query = new SimpleQuery();
		path = new Path();
		cityObjectGroup = new ExportCityObjectGroup();
		address = new ExportAddress();
		appearances = new ExportAppearance();
		xlink = new XLink();
		xslTransformation = new XSLTransformation();
		resources = new Resources();
		continuation = new Continuation();
	}

	public SimpleQuery getQuery() {
		return query;
	}

	public void setQuery(SimpleQuery query) {
		if (query != null)
			this.query = query;
	}

	public Query getGenericQuery() {
		return genericQuery;
	}
	
	public boolean isSetGenericQuery() {
		return genericQuery != null;
	}

	public void setGenericQuery(Query genericQuery) {
		this.genericQuery = genericQuery;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		if (path != null)
			this.path = path;
	}

	public Continuation getContinuation() {
		return continuation;
	}

	public void setContinuation(Continuation continuation) {
		if (continuation != null)
			this.continuation = continuation;
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

	public XLink getXlink() {
		return xlink;
	}

	public void setXlink(XLink xlink) {
		if (xlink != null)
			this.xlink = xlink;
	}

	public XSLTransformation getXSLTransformation() {
		return xslTransformation;
	}

	public void setXSLTransformation(XSLTransformation xslTransformation) {
		if (xslTransformation != null)
			this.xslTransformation = xslTransformation;
	}

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources system) {
		if (system != null)
			this.resources = system;
	}

}
