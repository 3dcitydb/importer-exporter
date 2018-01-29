package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "objectProperty")
public class ObjectProperty extends AbstractRefTypeProperty<ObjectType> {
	@XmlAttribute(name = "target", required = true)
	@XmlJavaTypeAdapter(ObjectTypeAdapter.class)
	protected ObjectType type;

	protected ObjectProperty() {
	}
    
    public ObjectProperty(String path, ObjectType type, AppSchema schema) {
    	super(path, schema);
    	this.type = type;
    }

	@Override
	public ObjectType getType() {
		return type;
	}

	@Override
	public boolean isSetType() {
		return type != null;
	}

	@Override
	public void setType(ObjectType type) {
		this.type = type;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.OBJECT_PROPERTY;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (type.hasLocalProperty(MappingConstants.IS_XLINK)) {
			ObjectType ref = schemaMapping.getObjectTypeById(type.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve object type reference '" + type.getId() + "'.");

			type = ref;
		}
		
		super.validate(schemaMapping, parent);
	}

}
