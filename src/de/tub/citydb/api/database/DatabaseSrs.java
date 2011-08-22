package de.tub.citydb.api.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DatabaseSrsType", propOrder={
		"srid",
		"srsName",
		"description"
		})
public class DatabaseSrs implements Comparable<DatabaseSrs> {
	@XmlAttribute
	@XmlID
	protected String id;
	protected int srid = 0;
	protected String srsName = "";
	protected String description = "";
	@XmlTransient
	protected boolean isSupported = true;
	
	protected DatabaseSrs() {
		
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
	
	@Override
	public int compareTo(DatabaseSrs o) {
		return getDescription().toUpperCase().compareTo(o.getDescription().toUpperCase());
	}
	
}
