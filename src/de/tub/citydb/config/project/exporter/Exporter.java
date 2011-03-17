package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.general.Path;
import de.tub.citydb.config.project.system.System;

@XmlType(name="ExportType", propOrder={
		"path",
		"targetSRS",
		"appearances",
		"filter",
		"moduleVersion",
		"xlink",
		"system"
})
public class Exporter {
	private Path path;
	@XmlIDREF
	private ReferenceSystem targetSRS = Internal.DEFAULT_DB_REF_SYS;
	private ExportAppearance appearances;
	private ExportFilterConfig filter;
	private ModuleVersion moduleVersion;
	private XLink xlink;
	private System system;

	public Exporter() {
		path = new Path();
		appearances = new ExportAppearance();
		filter = new ExportFilterConfig();
		moduleVersion = new ModuleVersion();
		xlink = new XLink();
		system = new System();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		if (path != null)
			this.path = path;
	}

	public ReferenceSystem getTargetSRS() {
		return targetSRS;
	}

	public void setTargetSRS(ReferenceSystem targetSRS) {
		if (targetSRS != null)
			this.targetSRS = targetSRS;
	}

	public ExportAppearance getAppearances() {
		return appearances;
	}

	public void setAppearances(ExportAppearance appearances) {
		if (appearances != null)
			this.appearances = appearances;
	}

	public ExportFilterConfig getFilter() {
		return filter;
	}

	public void setFilter(ExportFilterConfig filter) {
		if (filter != null)
			this.filter = filter;
	}

	public ModuleVersion getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(ModuleVersion moduleVersion) {
		if (moduleVersion != null)
			this.moduleVersion = moduleVersion;
	}

	public XLink getXlink() {
		return xlink;
	}

	public void setXlink(XLink xlink) {
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
