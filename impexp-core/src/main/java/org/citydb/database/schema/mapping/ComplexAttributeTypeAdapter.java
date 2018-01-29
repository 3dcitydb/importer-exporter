package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ComplexAttributeTypeAdapter extends XmlAdapter<String, ComplexAttributeType> {
	private final SchemaMapping schemaMapping;

	protected ComplexAttributeTypeAdapter() {
		schemaMapping = null;
	}

	public ComplexAttributeTypeAdapter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	@Override
	public ComplexAttributeType unmarshal(String id) throws Exception {
		if (id == null || id.isEmpty())
			throw new SchemaMappingException("The attribute 'id' is not set for the attribute type reference.");

		ComplexAttributeType type = null;
		if (schemaMapping != null)
			type = schemaMapping.getComplexAttributeTypeById(id);

		if (type == null) {
			type = new ComplexAttributeType();
			type.id = id;
			type.setLocalProperty(MappingConstants.IS_XLINK, true);
		}

		return type;
	}

	@Override
	public String marshal(ComplexAttributeType dataType) throws Exception {
		return dataType != null ? dataType.id : null;
	}

}
