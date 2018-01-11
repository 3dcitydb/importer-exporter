package org.citydb.database.schema.mapping;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlType(name = "abstractPathElement")
public abstract class AbstractPathElement {
    @XmlAttribute(required = true)
    protected String path;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(AppSchemaAdapter.class)
    protected AppSchema schema;
    @XmlAttribute
    protected Boolean queryable;
    
    protected AbstractPathElement() {
	}
    
    public AbstractPathElement(String path, AppSchema schema) {
    	this.path = path;
    	this.schema = schema;
    }
    
    @XmlTransient
    private HashMap<String, Object> localProperties;

	public abstract PathElementType getElementType();

    public String getPath() {
        return path;
    }

    public boolean isSetPath() {
        return path != null && !path.isEmpty();
    }
    
    public void setPath(String path) {
    	this.path = path;
    }

    public AppSchema getSchema() {
        return schema;
    }

    public boolean isSetSchema() {
        return schema != null;
    }
    
    public void setSchema(AppSchema schema) {
    	this.schema = schema;
    }

    public boolean isQueryable() {
        return queryable == null ? true : queryable.booleanValue();
    }

    public boolean isSetQueryable() {
        return queryable != null;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable ? null : false;
    }
    
    public Object getLocalProperty(String name) {
		if (localProperties != null)
			return localProperties.get(name);
			
		return null;
	}

	public void setLocalProperty(String name, Object value) {
		if (localProperties == null)
			localProperties = new HashMap<String, Object>();
		
		localProperties.put(name, value);
	}

	public boolean hasLocalProperty(String name) {
		return localProperties != null && localProperties.containsKey(name);
	}

	public Object unsetLocalProperty(String name) {
		if (localProperties != null)
			return localProperties.remove(name);
		
		return null;
	}
	
	public boolean matchesName(String name, String namespaceURI) {
		return schema.matchesNamespaceURI(namespaceURI) && path.equals(name);
	}
	
	public boolean matchesName(QName name) {
		return matchesName(name.getLocalPart(), name.getNamespaceURI());
	}
	
	public boolean isAvailableForCityGML(CityGMLVersion version) {
		return schema.isAvailableForCityGML(version);
	}
	
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (!isSetPath())
			throw new SchemaMappingException("A path element of type " + getElementType() + " lacks a path name.");
		
		if (!isSetSchema())
			throw new SchemaMappingException("The path element of name '" + path + "' and type " + getElementType() + " lacks an application schema.");
		
		if (schema.hasLocalProperty(MappingConstants.IS_XLINK)) {
			AppSchema ref = schemaMapping.getSchemaById(schema.id);
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve application schema reference '" + schema.id + "'.");
			
			schema = ref;
		}
	}

	@Override
	public String toString() {
		if (!schema.isSetXMLPrefix())
			schema.generateXMLPrefix();
		
		return new StringBuilder(schema.getXMLPrefix()).append(":").append(path).toString();
	}
	
}
