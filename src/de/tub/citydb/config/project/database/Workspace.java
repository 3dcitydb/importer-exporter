package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="WorkspaceType", propOrder={
		"name",
		"timestamp"
})
public class Workspace {
	@XmlElement(required=true, defaultValue="LIVE")
	private String name = "LIVE";
	private String timestamp = "";

	public Workspace() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
