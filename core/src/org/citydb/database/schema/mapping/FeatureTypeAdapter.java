package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class FeatureTypeAdapter extends XmlAdapter<String, FeatureType> {
	private final SchemaMapping schemaMapping;

	protected FeatureTypeAdapter() {
		schemaMapping = null;
	}

	public FeatureTypeAdapter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	@Override
	public FeatureType unmarshal(String id) throws Exception {
		if (id == null || id.isEmpty())
			throw new SchemaMappingException("The attribute 'id' is not set for the feature property.");

		FeatureType type = null;
		if (schemaMapping != null)
			type = schemaMapping.getFeatureTypeById(id);
		
		if (type == null) {
			type = new FeatureType();
			type.id = id;
			type.setLocalProperty(MappingConstants.IS_XLINK, true);
		}
		
		return type;
	}

	@Override
	public String marshal(FeatureType type) throws Exception {
		return type != null ? type.id : null;
	}

}
