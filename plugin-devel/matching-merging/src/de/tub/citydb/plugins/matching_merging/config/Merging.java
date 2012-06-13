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
package de.tub.citydb.plugins.matching_merging.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MergingType", propOrder={
		"gmlNameMode",
		"deleteMode",
		"lineage"
		})		
public class Merging {
	@XmlElement(required=true)
	private MatchingGmlNameMode gmlNameMode = MatchingGmlNameMode.APPEND;
	@XmlElement(required=true)
	private MatchingDeleteMode deleteMode = MatchingDeleteMode.MERGE;
	@XmlElement(required=true)
	private String lineage = "";

	@XmlTransient
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
			
	public boolean isGmlNameModeAppend() {
		return gmlNameMode == MatchingGmlNameMode.APPEND;
	}	
	
	public boolean isGmlNameModeIgnore() {
		return gmlNameMode == MatchingGmlNameMode.IGNORE;
	}	
	
	public boolean isGmlNameModeReplace() {
		return gmlNameMode == MatchingGmlNameMode.REPLACE;
	}	
	
	public MatchingGmlNameMode getGmlNameMode() {
		return gmlNameMode;
	}
	
	public void setGmlNameMode(MatchingGmlNameMode gmlNameMode) {
		MatchingGmlNameMode oldGmlMadeMode = this.gmlNameMode;
		this.gmlNameMode = gmlNameMode;
		changes.firePropertyChange("matchPref.gmlNameMode", oldGmlMadeMode, gmlNameMode);
	}
		
	public boolean isDeleteModeMerge() {
		return deleteMode == MatchingDeleteMode.MERGE;
	}	
	
	public boolean isDeleteModeDelAll() {
		return deleteMode == MatchingDeleteMode.DELALL;
	}	
	
	public boolean isDeleteModeRename() {
		return deleteMode == MatchingDeleteMode.RENAME;
	}	
	
	public MatchingDeleteMode getDeleteMode() {
		return deleteMode;
	}

	public void setDeleteMode(MatchingDeleteMode deleteMode) {
		MatchingDeleteMode oldDeleteMode = this.deleteMode;
		this.deleteMode = deleteMode;
		changes.firePropertyChange("matchPref.deleteMode", oldDeleteMode, deleteMode);
	}
	
	public String getLineage() {
		return lineage;
	}
	
	public void setLineage(String lineage) {
		this.lineage = lineage;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}
}

