package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.FilterConfig;
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
	private ImpContinuation continuation;
	private Path path;
	private ImpGmlId gmlId;
	private ImpAppearance appearances;
	private ImpIndex indexes;
	private FilterConfig filter;
	private ImpXMLValidation xmlValidation;
	private System system;

	public Importer() {
		continuation = new ImpContinuation();
		path = new Path();
		gmlId = new ImpGmlId();
		appearances = new ImpAppearance();
		indexes = new ImpIndex();
		filter = new FilterConfig();
		xmlValidation = new ImpXMLValidation();
		system = new System();
	}

	public ImpContinuation getContinuation() {
		return continuation;
	}

	public void setContinuation(ImpContinuation continuation) {
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

	public ImpGmlId getGmlId() {
		return gmlId;
	}

	public void setGmlId(ImpGmlId gmlId) {
		if (gmlId != null)
			this.gmlId = gmlId;
	}

	public ImpAppearance getAppearances() {
		return appearances;
	}

	public void setAppearances(ImpAppearance appearances) {
		if (appearances != null)
			this.appearances = appearances;
	}

	public ImpIndex getIndexes() {
		return indexes;
	}

	public void setIndexes(ImpIndex indexes) {
		if (indexes != null)
			this.indexes = indexes;
	}

	public FilterConfig getFilter() {
		return filter;
	}

	public void setFilter(FilterConfig filter) {
		if (filter != null)
			this.filter = filter;
	}

	public ImpXMLValidation getXMLValidation() {
		return xmlValidation;
	}

	public void setXMLValidation(ImpXMLValidation xmlValidation) {
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
