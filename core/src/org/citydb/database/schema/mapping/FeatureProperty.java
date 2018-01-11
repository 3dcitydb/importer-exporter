package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "featureProperty")
public class FeatureProperty extends AbstractRefTypeProperty<FeatureType> {
	@XmlAttribute(name = "target", required = true)
	@XmlJavaTypeAdapter(FeatureTypeAdapter.class)
	protected FeatureType type;

	protected FeatureProperty() {
	}
    
    public FeatureProperty(String path, FeatureType type, AppSchema schema) {
    	super(path, schema);
    	this.type = type;
    }

	@Override
	public FeatureType getType() {
		return type;
	}

	@Override
	public boolean isSetType() {
		return type != null;
	}

	@Override
	public void setType(FeatureType type) {
		this.type = type;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.FEATURE_PROPERTY;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (type.hasLocalProperty(MappingConstants.IS_XLINK)) {
			FeatureType ref = schemaMapping.getFeatureTypeById(type.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve feature type reference '" + type.getId() + "'.");

			type = ref;
		}
		
		super.validate(schemaMapping, parent);
	}

}
