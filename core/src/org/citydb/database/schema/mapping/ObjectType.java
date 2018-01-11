package org.citydb.database.schema.mapping;

import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "objectType", propOrder = {
		"extension",
		"properties"
})
public class ObjectType extends AbstractObjectType<ObjectType> {
	protected ObjectTypeExtension extension;

	protected ObjectType() {
	}
    
    public ObjectType(String id, String path, String table, int objectClassId, AppSchema schema, SchemaMapping schemaMapping) {
    	super(id, path, table, objectClassId, schema, schemaMapping);
    }

	@Override
	public AbstractExtension<ObjectType> getExtension() {
		return extension;
	}

	@Override
	public boolean isSetExtension() {
		return extension != null;
	}
	
	@Override
	public void setExtension(AbstractExtension<ObjectType> extension) {
		this.extension = (ObjectTypeExtension)extension;
	}

	@Override
	public List<ObjectType> listSubTypes(boolean skipAbstractTypes) {
		return listSubTypes(schemaMapping.objectTypes, skipAbstractTypes);
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.OBJECT_TYPE;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		schema.addObjectType(this);

		if (isSetExtension())
			extension.validate(schemaMapping, this);
	}

}
