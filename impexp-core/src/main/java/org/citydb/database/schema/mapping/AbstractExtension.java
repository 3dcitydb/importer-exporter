package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class AbstractExtension<T extends AbstractType<T>> implements Joinable {
    protected Join join;
    
    @Override
    public Join getJoin() {
        return join;
    }

    @Override
    public boolean isSetJoin() {
        return join != null;
    }
    
    public void setJoin(Join join) {
    	this.join = join;
    }

    public abstract T getBase();
    public abstract boolean isSetBase();
    public abstract void setBase(T base);

    protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
    	if (!isSetBase())
    		throw new SchemaMappingException("An extension requires a base type.");
    	
    	if (isSetJoin())
    		join.validate(schemaMapping, this, parent);
    }
}
