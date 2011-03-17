/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
