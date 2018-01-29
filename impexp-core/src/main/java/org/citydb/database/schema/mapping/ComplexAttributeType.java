package org.citydb.database.schema.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "complexAttributeType", propOrder = {
		"attributes"
})
public class ComplexAttributeType {
	@XmlElements({
		@XmlElement(name = "attribute", type = SimpleAttribute.class),
		@XmlElement(name = "complexAttribute", type = ComplexAttribute.class)
	})
	protected List<AbstractAttribute> attributes;
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;
	@XmlAttribute
	protected String table;

	@XmlTransient
	private HashMap<String, Object> localProperties;
	@XmlTransient
	protected SchemaMapping schemaMapping;

	protected ComplexAttributeType() {
		attributes = new ArrayList<>();
	}
	
	public ComplexAttributeType(SchemaMapping schemaMapping) {
		this();
		this.schemaMapping = schemaMapping;
	}

	public String getTable() {
		return table;
	}

	public boolean isSetTable() {
		return table != null && !table.isEmpty();
	}
	
	public void setTable(String table) {
		this.table = table;
	}

	public List<AbstractAttribute> getAttributes() {
		return new ArrayList<>(attributes);
	}

	public AbstractAttribute getAttribute(String name, String namespaceURI) {
		for (AbstractAttribute attribute : attributes) {
			if (attribute.getSchema().matchesNamespaceURI(namespaceURI)) {
				String path = attribute.getPath();
				if (path.startsWith("@"))
					path = path.substring(1, path.length());

				if (path.equals(name))
					return attribute;
			}
		}

		return null;
	}

	public boolean isSetAttributes() {
		return attributes != null && !attributes.isEmpty();
	}

	public void addAttribute(AbstractAttribute attribute) {
		if (attribute == null)
			return;
		
		if (attribute instanceof SimpleAttribute)
			((SimpleAttribute)attribute).setParentAttributeType(this);

		attributes.add(attribute);
	}

	public String getId() {
		return id;
	}

	public boolean isSetId() {
		return id != null && !id.isEmpty();
	}
	
	public void setId(String id) {
		this.id = id;
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

	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		this.schemaMapping = schemaMapping;
		boolean isInline = parent instanceof ComplexAttribute;

		if (!isInline) {
			if (!isSetId())
				throw new SchemaMappingException("A global attribute type must be assigned an id value.");
		} else {
			if (isSetId())
				throw new SchemaMappingException("The attribute 'id' is not allowed for an attribute type that is given inline.");
			else if (isSetTable())
				throw new SchemaMappingException("The attribute 'table' is not allowed for an attribute type that is given inline.");
		}

		for (AbstractAttribute attribute : getAttributes())
			attribute.validate(schemaMapping, this);
	}

}
