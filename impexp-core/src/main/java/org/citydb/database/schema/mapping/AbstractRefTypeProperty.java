package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractRefTypeProperty")
public abstract class AbstractRefTypeProperty<T extends AbstractType<T>> extends AbstractTypeProperty<T> {
    @XmlAttribute
    protected RelationType relationType;

	protected AbstractRefTypeProperty() {
	}
    
    public AbstractRefTypeProperty(String path, AppSchema schema) {
    	super(path, schema);
    }
    
    public abstract void setType(T type);

    @Override
    public RelationType getRelationType() {
        return relationType != null ? relationType : RelationType.ASSOCIATION;
    }

    @Override
    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }
}
