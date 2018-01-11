package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AppSchemaAdapter extends XmlAdapter<String, AppSchema> {
	private final SchemaMapping schemaMapping;

	protected AppSchemaAdapter() {
		schemaMapping = null;
	}

	public AppSchemaAdapter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	@Override
	public AppSchema unmarshal(String id) throws Exception {
		if (id == null || id.isEmpty())
			throw new SchemaMappingException("The attribute 'id' is not set for the application schema reference.");

		AppSchema schema = null;
		if (schemaMapping != null)
			schema = schemaMapping.getSchemaById(id);

		if (schema == null) {
			schema = new AppSchema();
			schema.id = id;
			schema.setLocalProperty(MappingConstants.IS_XLINK, true);
		}

		return schema;
	}

	@Override
	public String marshal(AppSchema appSchema) throws Exception {
		return appSchema != null ? appSchema.id : null;
	}

}
