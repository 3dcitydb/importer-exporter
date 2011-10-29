package de.tub.citydb.api.config;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DatabaseSrsType", propOrder={
		"srid",
		"srsName",
		"description"
		})
public final class DatabaseSrs implements Comparable<DatabaseSrs> {
	public static final DatabaseSrs DEFAULT = new DatabaseSrs("", 0, "n/a", "", false);

	@XmlAttribute
	@XmlID
	protected String id;
	protected int srid = 0;
	protected String srsName = "";
	protected String description = "";
	@XmlTransient
	protected boolean isSupported = true;
	@XmlTransient
	protected boolean is3D;
	
	protected DatabaseSrs() {
		id = generateUUID();
	}
	
	public DatabaseSrs(int srid, String srsName, String description, boolean isSupported) {
		this(generateUUID(), srid, srsName, description, isSupported);
	}

	public DatabaseSrs(DatabaseSrs other) {
		this(other.srid, other.srsName, other.description, other.isSupported);
	}

	public DatabaseSrs(String id, int srid, String srsName, String description, boolean isSupported) {
		this.id = id;
		this.srid = srid;
		this.srsName = srsName;
		this.description = description;
		this.isSupported = isSupported;
	}
	
	public int getSrid() {
		return srid;
	}
	
	public String getSrsName() {
		return srsName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isSupported() {
		return isSupported;
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

	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSupported(boolean isSupported) {
		this.isSupported = isSupported;
	}
	
	public boolean is3D() {
		return is3D;
	}
	
	public boolean is2D() {
		return !is3D;
	}

	public void setIs3D(boolean is3d) {
		is3D = is3d;
	}

	@Override
	public int compareTo(DatabaseSrs o) {
		return getDescription().toUpperCase().compareTo(o.getDescription().toUpperCase());
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
	
	private static String generateUUID() {
		return new StringBuilder("UUID_").append(UUID.randomUUID().toString()).toString();
	}
	
}
