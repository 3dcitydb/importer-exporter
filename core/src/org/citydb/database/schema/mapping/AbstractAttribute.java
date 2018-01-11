package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractAttribute")
public abstract class AbstractAttribute extends AbstractProperty {

	protected AbstractAttribute() {
	}
    
    public AbstractAttribute(String path, AppSchema schema) {
    	super(path, schema);
    }
    
}
