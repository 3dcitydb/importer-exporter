package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractRefTypeProperty")
public abstract class AbstractRefTypeProperty<T extends AbstractType<T>> extends AbstractTypeProperty<T> {

	protected AbstractRefTypeProperty() {
	}
    
    public AbstractRefTypeProperty(String path, AppSchema schema) {
    	super(path, schema);
    }
    
    public abstract void setType(T type);    
}
