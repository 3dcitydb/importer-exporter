package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "featureTypeExtension", propOrder={
		"join"
})
public class FeatureTypeExtension extends AbstractExtension<FeatureType> {	
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(FeatureTypeAdapter.class)
	private FeatureType base;
	
	protected FeatureTypeExtension() {
	}
	
	public FeatureTypeExtension(FeatureType base) {
		this.base = base;
	}
	
	@Override
	public FeatureType getBase() {
		return base;
	}

	@Override
	public boolean isSetBase() {
		return base != null;
	}

	@Override
	public void setBase(FeatureType base) {
		this.base = base;
	}
	
	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {	
		if (base.hasLocalProperty(MappingConstants.IS_XLINK)) {
			FeatureType ref = schemaMapping.getFeatureTypeById(base.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve feature type reference '" + base.getId() + "'.");

			base = ref;
		}
		
		super.validate(schemaMapping, parent);
	}
	
}
