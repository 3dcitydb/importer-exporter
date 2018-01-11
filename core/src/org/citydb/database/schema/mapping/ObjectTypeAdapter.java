package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ObjectTypeAdapter extends XmlAdapter<String, ObjectType> {
	private final SchemaMapping schemaMapping;

	protected ObjectTypeAdapter() {
		schemaMapping = null;
	}

	public ObjectTypeAdapter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	@Override
	public ObjectType unmarshal(String id) throws Exception {
		if (id == null || id.isEmpty())
			throw new SchemaMappingException("The attribute 'id' is not set for the object property.");

		ObjectType type = null;
		if (schemaMapping != null)
			type = schemaMapping.getObjectTypeById(id);

		if (type == null) {
			type = new ObjectType();
			type.id = id;
			type.setLocalProperty(MappingConstants.IS_XLINK, true);
		}

		return type;
	}

	@Override
	public String marshal(ObjectType type) throws Exception {
		return type != null ? type.id : null;
	}

}
