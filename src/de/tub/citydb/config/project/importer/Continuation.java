package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ContinuationType", propOrder={
		"lineage",
		"updatingPersonMode",
		"updatingPerson",
		"reasonForUpdate"
		})
public class Continuation {
	@XmlElement(required=true)
	private String lineage = "";
	@XmlElement(required=true)
	private UpdatingPersonMode updatingPersonMode = UpdatingPersonMode.DATABASE;
	private String updatingPerson = "";
	@XmlElement(required=true)
	private String reasonForUpdate = "";
	
	public Continuation() {
		
	}
	
	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public boolean isUpdatingPersonModeDatabase() {
		return updatingPersonMode == UpdatingPersonMode.DATABASE;
	}
	
	public boolean isUpdatingPersonModeUser() {
		return updatingPersonMode == UpdatingPersonMode.USER;
	}
	
	public UpdatingPersonMode getUpdatingPersonMode() {
		return updatingPersonMode;
	}

	public void setUpdatingPersonMode(UpdatingPersonMode updatingPersonMode) {
		this.updatingPersonMode = updatingPersonMode;
	}

	public String getUpdatingPerson() {
		return updatingPerson;
	}

	public void setUpdatingPerson(String updatingPerson) {
		this.updatingPerson = updatingPerson;
	}

	public String getReasonForUpdate() {
		return reasonForUpdate;
	}

	public void setReasonForUpdate(String reasonForUpdate) {
		this.reasonForUpdate = reasonForUpdate;
	}
	
}
