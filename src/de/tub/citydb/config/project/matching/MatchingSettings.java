package de.tub.citydb.config.project.matching;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchingSettingsType", propOrder={
		"resultMode",
		"gmlNameMode",
		"deleteMode",
		"resultUser"
		})
public class MatchingSettings {
	@XmlElement(required=true)
	private MatchingResultMode resultMode = MatchingResultMode.FIX;
	@XmlElement(required=true)
	private MatchingGmlNameMode gmlNameMode = MatchingGmlNameMode.APPEND;
	@XmlElement(required=true)
	private MatchingDeleteMode deleteMode = MatchingDeleteMode.MERGE;
	@XmlElement(required=true)
	private int resultUser = 100;

	@XmlTransient
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	
	public boolean isResultModeFix() {
		return resultMode == MatchingResultMode.FIX;
	}	
	
	public boolean isResultModeUser() {
		return resultMode == MatchingResultMode.USER;
	}	
	
	public boolean isResultModeAll() {
		return resultMode == MatchingResultMode.ALL;
	}	
	
	public MatchingResultMode getResultMode() {
		return resultMode;
	}
	
	public void setResultMode(MatchingResultMode resultMode) {
		this.resultMode = resultMode;
	}
		
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
	
	public int getResultUser() {
		return resultUser;
	}	
	
	public void setResultUser(int resultUser) {
		this.resultUser = resultUser;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}
}

