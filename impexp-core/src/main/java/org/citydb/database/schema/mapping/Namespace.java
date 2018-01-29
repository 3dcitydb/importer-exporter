package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "namespace")
public class Namespace {
    @XmlAttribute(required = true)
    protected CityGMLContext context;
    @XmlAttribute
    protected String schemaLocation;
    @XmlValue
    protected String uri;
    
    protected Namespace() {
    }
    
    public Namespace(String uri, CityGMLContext context) {
    	this.uri = uri;
    	this.context = context;
    }

	public CityGMLContext getContext() {
		return context;
	}
	
	public boolean isSetContext() {
		return context != null;
	}
	
	public void setContext(CityGMLContext context) {
		this.context = context;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}
	
	public boolean isSetSchemaLocation() {
		return schemaLocation != null && !schemaLocation.isEmpty();
	}

	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}

	public String getURI() {
		return isSetURI() ? uri : "";
	}

	public boolean isSetURI() {
		return uri != null && !uri.isEmpty();
	}
	
	public void setURI(String uri) {
		this.uri = uri;
	}
	
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (!(parent instanceof AppSchema))
			throw new SchemaMappingException("Unexpected parent element for namespace: " + parent);
		
		AppSchema schema = (AppSchema)parent;
		if (!isSetURI())
			throw new SchemaMappingException("The application schema '" + schema.id + "' lacks a namespace URI declaration.");
		else if (!isSetContext())
			throw new SchemaMappingException("The application schema '" + schema.id + "' lacks a CityGML context definition.");
	}

}
