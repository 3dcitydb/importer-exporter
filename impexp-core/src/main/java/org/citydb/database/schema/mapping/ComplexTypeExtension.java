package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "complexTypeExtension", propOrder={
		"join"
})
public class ComplexTypeExtension extends AbstractExtension<ComplexType> {
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(ComplexTypeAdapter.class)
	private ComplexType base;
	
	protected ComplexTypeExtension() {
	}
	
	public ComplexTypeExtension(ComplexType base) {
		this.base = base;
	}
	
	@Override
	public ComplexType getBase() {
		return base;
	}

	@Override
	public boolean isSetBase() {
		return base != null;
	}
	
	@Override
	public void setBase(ComplexType base) {
		this.base = base;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {		
		if (base.hasLocalProperty(MappingConstants.IS_XLINK)) {
			ComplexType ref = schemaMapping.getComplexTypeById(base.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve complex type reference '" + base.getId() + "'.");

			base = ref;
		}
		
		super.validate(schemaMapping, parent);
	}
	
}
