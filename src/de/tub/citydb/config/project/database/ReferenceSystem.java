package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.util.UUIDManager;

@XmlType(name="ReferenceSystemType", propOrder={
		"srid",
		"srsName",
		"description"
		})
public class ReferenceSystem implements Comparable<ReferenceSystem> {	
	@XmlAttribute
	@XmlID
	private String id;
	private int srid = 0;
	private String srsName = "";
	private String description = "";
	@XmlTransient
	private boolean isSupported = true;

	public ReferenceSystem() {
		id = UUIDManager.randomUUID();
	}

	public ReferenceSystem(int srid, String srsName, String description, boolean isSupported) {
		this(UUIDManager.randomUUID(), srid, srsName, description, isSupported);
	}
	
	public ReferenceSystem(ReferenceSystem other) {
		this(other.getSrid(), other.getSrsName(), other.getDescription(), other.isSupported);
	}
	
	public ReferenceSystem(String id, int srid, String srsName, String description, boolean isSupported) {
		this.id = id;
		this.srid = srid;
		this.srsName = srsName;
		this.description = description;
		this.isSupported = isSupported;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public int getSrid() {
		return srid;
	}

	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}

	public String getSrsName() {
		return srsName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return getDescription();
	}

	public boolean isSupported() {
		return isSupported;
	}

	public void setSupported(boolean isSupported) {
		this.isSupported = isSupported;
	}

	@Override
	public int compareTo(ReferenceSystem o) {
		return getDescription().toUpperCase().compareTo(o.getDescription().toUpperCase());
	}
	
}
