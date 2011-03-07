package de.tub.citydb.config.project.general;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="PathType", propOrder={
		"mode",
		"standardPath",
		"lastUsedPath"
})
public class Path {
	@XmlElement(required=true)
	private PathMode mode = PathMode.LASTUSED;
	private String standardPath = "";
	private String lastUsedPath = "";
	
	public Path() {
	}
	
	public boolean isSetLastUsedMode() {
		return mode == PathMode.LASTUSED;
	}

	public boolean isSetStandardMode() {
		return mode == PathMode.STANDARD;
	}
	
	public PathMode getPathMode() {
		return mode;
	}

	public void setPathMode(PathMode mode) {
		this.mode = mode;
	}

	public String getStandardPath() {	
		return standardPath;
	}

	public void setStandardPath(String standardPath) {
		this.standardPath = standardPath;
	}

	public String getLastUsedPath() {
		return lastUsedPath;
	}

	public void setLastUsedPath(String lastUsedPath) {
		this.lastUsedPath = lastUsedPath;
	}
	
}
