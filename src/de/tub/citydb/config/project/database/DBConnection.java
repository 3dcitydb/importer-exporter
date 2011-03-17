package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.util.UUIDManager;

@XmlType(name="ConnectionType", propOrder={
		"description",
		"server",
		"port",
		"sid",
		"user",
		"password",
		"savePassword"
		})
public class DBConnection implements Comparable<DBConnection> {
	@XmlAttribute(required=true)
	@XmlID
	private String id;
	private String description = "";
	@XmlSchemaType(name="anyURI")
	private String server = "";
	@XmlSchemaType(name="positiveInteger")
	private Integer port = 1521;
	private String sid = "";
	private String user = "";
	private String password = "";
	private Boolean savePassword = false;
	@XmlTransient
	private DBMetaData metaData;
		
	public DBConnection() {
		id = UUIDManager.randomUUID();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isSetSavePassword() {
		if (savePassword != null)
			return savePassword.booleanValue();
		
		return false;
	}

	public Boolean getSavePassword() {
		return savePassword;
	}

	public void setSavePassword(Boolean savePassword) {
		this.savePassword = savePassword;
	}
	
	public String toString() {
		return description;
	}
	
	public DBMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(DBMetaData metaData) {
		this.metaData = metaData;
	}

	@Override
	public int compareTo(DBConnection o) {
		return description.toUpperCase().compareTo(o.getDescription().toUpperCase());
	}
	
	public String toConnectString() {
		return user + "@" + server + ":" + port + "/" + sid;
	}
	
}
