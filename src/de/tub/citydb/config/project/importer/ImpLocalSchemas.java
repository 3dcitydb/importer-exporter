package de.tub.citydb.config.project.importer;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportLocalSchemasType")
public class ImpLocalSchemas {
	@XmlAttribute(required=true)
	private Boolean active = true;
	@XmlElement(name="schema")
	private Set<ImpSchemaType> schemas;
	
	public ImpLocalSchemas() {
		schemas = new HashSet<ImpSchemaType>();
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

	public Set<ImpSchemaType> getSchemas() {
		return schemas;
	}
	
	public void addSchema(ImpSchemaType schema) {
		schemas.add(schema);
	}

	public void setSchemas(Set<ImpSchemaType> schemas) {
		if (schemas != null)
			this.schemas = schemas;
	}
	
}
