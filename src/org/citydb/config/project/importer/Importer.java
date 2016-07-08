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
package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.general.AffineTransformation;
import org.citydb.config.project.general.Path;

@XmlType(name="ImportType", propOrder={
		"continuation",
		"path",
		"gmlId",
		"address",
		"appearances",
		"filter",
		"affineTransformation",
		"indexes",
		"xmlValidation",
		"importLog",
		"resources"
})
public class Importer {
	private Continuation continuation;
	private Path path;
	private ImportGmlId gmlId;
	private ImportAddress address;
	private ImportAppearance appearances;
	private ImportFilterConfig filter;
	private AffineTransformation affineTransformation;
	private Index indexes;
	private XMLValidation xmlValidation;
	private ImportLog importLog;
	private ImportResources resources;

	public Importer() {
		continuation = new Continuation();
		path = new Path();
		gmlId = new ImportGmlId();
		address = new ImportAddress();
		appearances = new ImportAppearance();
		filter = new ImportFilterConfig();
		affineTransformation = new AffineTransformation();
		indexes = new Index();
		xmlValidation = new XMLValidation();
		importLog = new ImportLog();
		resources = new ImportResources();
	}

	public Continuation getContinuation() {
		return continuation;
	}

	public void setContinuation(Continuation continuation) {
		if (continuation != null)
			this.continuation = continuation;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		if (path != null)
			this.path = path;
	}

	public ImportGmlId getGmlId() {
		return gmlId;
	}

	public void setGmlId(ImportGmlId gmlId) {
		if (gmlId != null)
			this.gmlId = gmlId;
	}

	public ImportAddress getAddress() {
		return address;
	}

	public void setAddress(ImportAddress address) {
		if (address != null)
			this.address = address;
	}

	public ImportAppearance getAppearances() {
		return appearances;
	}

	public void setAppearances(ImportAppearance appearances) {
		if (appearances != null)
			this.appearances = appearances;
	}

	public ImportFilterConfig getFilter() {
		return filter;
	}

	public void setFilter(ImportFilterConfig filter) {
		if (filter != null)
			this.filter = filter;
	}

	public Index getIndexes() {
		return indexes;
	}

	public void setIndexes(Index indexes) {
		if (indexes != null)
			this.indexes = indexes;
	}

	public XMLValidation getXMLValidation() {
		return xmlValidation;
	}

	public void setXMLValidation(XMLValidation xmlValidation) {
		if (xmlValidation != null)
			this.xmlValidation = xmlValidation;
	}

	public AffineTransformation getAffineTransformation() {
		return affineTransformation;
	}

	public void setAffineTransformation(AffineTransformation affineTransformation) {
		if (affineTransformation != null)
			this.affineTransformation = affineTransformation;
	}

	public ImportLog getImportLog() {
		return importLog;
	}

	public void setImportLog(ImportLog importLog) {
		if (importLog != null)
			this.importLog = importLog;
	}

	public ImportResources getResources() {
		return resources;
	}

	public void setResources(ImportResources resources) {
		if (resources != null)
			this.resources = resources;
	}	

}
