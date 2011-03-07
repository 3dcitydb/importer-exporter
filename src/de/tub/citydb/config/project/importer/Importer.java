package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.general.Path;
import de.tub.citydb.config.project.system.System;

@XmlType(name="ImportType", propOrder={
		"continuation",
		"path",
		"gmlId",
		"appearances",
		"indexes",
		"filter",
		"xmlValidation",
		"system"
})
public class Importer {
	private Continuation continuation;
	private Path path;
	private ImportGmlId gmlId;
	private ImportAppearance appearances;
	private Index indexes;
	private ImportFilterConfig filter;
	private XMLValidation xmlValidation;
	private System system;

	public Importer() {
		continuation = new Continuation();
		path = new Path();
		gmlId = new ImportGmlId();
		appearances = new ImportAppearance();
		indexes = new Index();
		filter = new ImportFilterConfig();
		xmlValidation = new XMLValidation();
		system = new System();
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

	public ImportAppearance getAppearances() {
		return appearances;
	}

	public void setAppearances(ImportAppearance appearances) {
		if (appearances != null)
			this.appearances = appearances;
	}

	public Index getIndexes() {
		return indexes;
	}

	public void setIndexes(Index indexes) {
		if (indexes != null)
			this.indexes = indexes;
	}

	public ImportFilterConfig getFilter() {
		return filter;
	}

	public void setFilter(ImportFilterConfig filter) {
		if (filter != null)
			this.filter = filter;
	}

	public XMLValidation getXMLValidation() {
		return xmlValidation;
	}

	public void setXMLValidation(XMLValidation xmlValidation) {
		if (xmlValidation != null)
			this.xmlValidation = xmlValidation;
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(System system) {
		if (system != null)
			this.system = system;
	}	

}
