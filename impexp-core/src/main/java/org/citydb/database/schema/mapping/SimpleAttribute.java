package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "attribute", propOrder = {
	    "join"
	})
public class SimpleAttribute extends AbstractAttribute {
	@XmlElements({
        @XmlElement(type = Join.class),
        @XmlElement(name = "reverseJoin", type = ReverseJoin.class)
    })
    protected AbstractJoin join;
	@XmlAttribute(required = true)
	protected String column;
	@XmlAttribute(required = true)
	protected SimpleType type;
	
	@XmlTransient
	protected AbstractType<?> complexType;
	@XmlTransient
	protected ComplexAttributeType attributeType;
	@XmlTransient
	protected String name;

	protected SimpleAttribute() {
	}
    
    public SimpleAttribute(String path, String column, SimpleType type, AppSchema schema) {
    	super(path, schema);
    	this.column = column;
    	this.type = type;
    }
	
	public boolean hasParentType() {
		return complexType != null;
	}
	
	public AbstractType<?> getParentType() {
		return complexType;
	}

	public boolean hasParentAttributeType() {
		return attributeType != null;
	}
	
	public ComplexAttributeType getParentAttributeType() {
		return attributeType;
	}
	
	protected void setParentType(AbstractType<?> complexType) {
		this.complexType = complexType;
		this.attributeType = null;
	}
	
	protected void setParentAttributeType(ComplexAttributeType attributeType) {
		this.attributeType = attributeType;
		this.complexType = null;
	}
	
	@Override
    public AbstractJoin getJoin() {
        return join;
    }

    @Override
    public boolean isSetJoin() {
        return join != null;
    }
    
    public void setJoin(Join join) {
    	this.join = join;
    }
	
    public void setJoin(ReverseJoin join) {
    	this.join = join;
    }
    
	public String getColumn() {
		return column;
	}

	public boolean isSetColumn() {
		return column != null && !column.isEmpty();
	}
	
	public void setColumn(String column) {
		this.column = column;
	}

	public SimpleType getType() {
		return type;
	}

	public boolean isSetType() {
		return type != null;
	}
	
	public void setType(SimpleType type) {
		this.type = type;
	}
	
	public String getName() {
		if (name == null)
			name = path.startsWith("@") ? path.substring(1, path.length()) : path;

		return name;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.SIMPLE_ATTRIBUTE;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		
		if (parent instanceof AbstractType<?>)
			complexType = (AbstractType<?>)parent;
		else if (parent instanceof ComplexAttributeType)
			attributeType = (ComplexAttributeType)parent;
	}

}
