package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractRefOrInlineTypeProperty")
public abstract class AbstractRefOrInlineTypeProperty<T extends AbstractType<T>> extends AbstractTypeProperty<T> {

	protected AbstractRefOrInlineTypeProperty() {
	}
    
    public AbstractRefOrInlineTypeProperty(String path, AppSchema schema) {
    	super(path, schema);
    }
    
    public abstract void setRefType(T type);
    public abstract void setInlineType(T type);

    @Override
    public RelationType getRelationType() {
        return RelationType.COMPOSITION;
    }

    @Override
    public void setRelationType(RelationType relationType) {
        // nothing to do here
    }
}
