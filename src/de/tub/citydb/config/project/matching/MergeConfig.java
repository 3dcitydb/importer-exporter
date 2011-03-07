package de.tub.citydb.config.project.matching;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MergeConfigType", propOrder={
		"gmlNameMode",
		"deleteMode",
		"lineage"
		})		
public class MergeConfig {
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

