package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.general.Path;
import de.tub.citydb.config.project.system.System;

@XmlType(name="ExportType", propOrder={
		"path",
		"appearances",
		"filter",
		"moduleVersion",
		"xlink",
		"system"
})
public class Exporter {
	private Path path;
	private ExpAppearance appearances;
	private FilterConfig filter;
	private ExpModuleVersion moduleVersion;
	private ExpXLink xlink;
	private System system;

	public Exporter() {
		path = new Path();
		appearances = new ExpAppearance();
		filter = new FilterConfig();
		moduleVersion = new ExpModuleVersion();
		xlink = new ExpXLink();
		system = new System();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		if (path != null)
			this.path = path;
	}

	public ExpAppearance getAppearances() {
		return appearances;
	}

	public void setAppearances(ExpAppearance appearances) {
		if (appearances != null)
			this.appearances = appearances;
	}

	public FilterConfig getFilter() {
		return filter;
	}

	public void setFilter(FilterConfig filter) {
		if (filter != null)
			this.filter = filter;
	}

	public ExpModuleVersion getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(ExpModuleVersion moduleVersion) {
		if (moduleVersion != null)
			this.moduleVersion = moduleVersion;
	}

	public ExpXLink getXlink() {
		return xlink;
	}

	public void setXlink(ExpXLink xlink) {
		if (xlink != null)
			this.xlink = xlink;
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(System system) {
		if (system != null)
			this.system = system;
	}

}
