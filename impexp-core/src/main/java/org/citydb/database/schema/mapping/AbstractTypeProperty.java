package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractTypeProperty", propOrder = {
	    "join"
})
public abstract class AbstractTypeProperty<T extends AbstractType<T>> extends AbstractProperty {
	@XmlElements({
        @XmlElement(type = Join.class),
        @XmlElement(name = "joinTable", type = JoinTable.class)
    })
    protected AbstractJoin join;
	
	protected AbstractTypeProperty() {
	}
    
    public AbstractTypeProperty(String path, AppSchema schema) {
    	super(path, schema);
    }
	
    public abstract T getType();
    public abstract boolean isSetType();
    
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
    
    public void setJoin(JoinTable join) {
    	this.join = join;
    }
    
	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		
		if (!isSetType())
    		throw new SchemaMappingException("A type property requires a target type.");
	}
    
}
