package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "objectTypeExtension", propOrder={
		"join"
})
public class ObjectTypeExtension extends AbstractExtension<ObjectType> {
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(ObjectTypeAdapter.class)
	private ObjectType base;
	
	protected ObjectTypeExtension() {
    }
	
	public ObjectTypeExtension(ObjectType base) {
		this.base = base;
	}
	
	@Override
	public ObjectType getBase() {
		return base;
	}

	@Override
	public boolean isSetBase() {
		return base != null;
	}
	
	@Override
	public void setBase(ObjectType base) {
		this.base = base;
	}
	
	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {		
		if (base.hasLocalProperty(MappingConstants.IS_XLINK)) {
			ObjectType ref = schemaMapping.getObjectTypeById(base.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve object type reference '" + base.getId() + "'.");

			base = ref;
		}
		
		super.validate(schemaMapping, parent);
	}
	
}
