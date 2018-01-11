package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ComplexTypeAdapter extends XmlAdapter<String, ComplexType> {
	private final SchemaMapping schemaMapping;

	protected ComplexTypeAdapter() {
		schemaMapping = null;
	}

	public ComplexTypeAdapter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	@Override
	public ComplexType unmarshal(String id) throws Exception {
		if (id == null || id.isEmpty())
			throw new SchemaMappingException("The attribute 'id' is not set for the complex property.");

		ComplexType type = null;
		if (schemaMapping != null)
			type = schemaMapping.getComplexTypeById(id);

		if (type == null) {
			type = new ComplexType();
			type.id = id;
			type.setLocalProperty(MappingConstants.IS_XLINK, true);
		}

		return type;
	}

	@Override
	public String marshal(ComplexType type) throws Exception {
		return type != null ? type.id : null;
	}

}
