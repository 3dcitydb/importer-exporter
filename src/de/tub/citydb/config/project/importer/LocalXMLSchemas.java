package de.tub.citydb.config.project.importer;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LocalXMLSchemasType")
public class LocalXMLSchemas {
	@XmlAttribute(required=true)
	private Boolean active = true;
	@XmlElement(name="schema")
	private Set<LocalXMLSchemaType> schemas;
	
	public LocalXMLSchemas() {
		schemas = new HashSet<LocalXMLSchemaType>();
	}

	public boolean isSet() {
		if (active != null)
			return active.booleanValue();
		
		return false;
	}
	
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Set<LocalXMLSchemaType> getSchemas() {
		return schemas;
	}
	
	public void addSchema(LocalXMLSchemaType schema) {
		schemas.add(schema);
	}

	public void setSchemas(Set<LocalXMLSchemaType> schemas) {
		if (schemas != null)
			this.schemas = schemas;
	}
	
}
