package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "complexProperty")
public class ComplexProperty extends AbstractRefOrInlineTypeProperty<ComplexType> {
	@XmlAttribute
	@XmlJavaTypeAdapter(ComplexTypeAdapter.class)
	protected ComplexType refType;
	@XmlElement(name="type", required=false)
	protected ComplexType inlineType;
	
	protected ComplexProperty() {
	}
    
    public ComplexProperty(String path, AppSchema schema) {
    	super(path, schema);
    }

	@Override
	public ComplexType getType() {
		return refType != null ? refType : inlineType;
	}

	@Override
	public boolean isSetType() {
		return refType != null || inlineType != null;
	}
	
	@Override
	public void setRefType(ComplexType refType) {
		this.refType = refType;
		inlineType = null;
	}
	
	@Override
	public void setInlineType(ComplexType inlineType) {
		this.inlineType = inlineType;
		refType = null;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.COMPLEX_PROPERTY;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {		
		if (inlineType != null && refType != null)
			throw new SchemaMappingException("The type of a complex property must either be given by reference or inline but not both.");
		
		if (inlineType != null) {
			if (parent instanceof AbstractType<?>)
				inlineType.transitiveTable = ((AbstractType<?>)parent).getTable();
			else if (parent instanceof PropertyInjection)
				inlineType.transitiveTable = ((PropertyInjection)parent).getTable();
			
			inlineType.validate(schemaMapping, this);
		} else if (refType != null && refType.hasLocalProperty(MappingConstants.IS_XLINK)) {
			ComplexType ref = schemaMapping.getComplexTypeById(refType.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve complex type reference '" + refType.getId() + "'.");

			refType = ref;
		}
		
		super.validate(schemaMapping, parent);
	}

}
