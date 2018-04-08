package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlType(name = "complexType", propOrder = {
		"extension",
		"properties"
})
public class ComplexType extends AbstractType<ComplexType> {
	@XmlAttribute
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;
	@XmlAttribute
	protected String table;
	@XmlAttribute
	protected Integer objectClassId;
	protected ComplexTypeExtension extension;
	
	@XmlTransient
	protected String transitiveTable;

	protected ComplexType() {
	}
    
    public ComplexType(String path, AppSchema schema, SchemaMapping schemaMapping) {
    	super(path, schema, schemaMapping);
    }
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isSetId() {
		return id != null && !id.isEmpty();
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getTable() {
		return table != null ? table : transitiveTable;
	}

	@Override
	public boolean isSetTable() {
		return (table != null && !table.isEmpty()) || (transitiveTable != null && !transitiveTable.isEmpty());
	}
	
	@Override
	public void setTable(String table) {
		this.table = table;
		transitiveTable = null;
	}

	@Override
	public int getObjectClassId() {
		return objectClassId != null ? objectClassId.intValue() : 0;
	}

	@Override
	public boolean isSetObjectClass() {
		return objectClassId != null;
	}

	@Override
	public void setObjectClassId(int objectClassId) {
		if (objectClassId >= 0)
			this.objectClassId = objectClassId;
	}

	@Override
	public AbstractExtension<ComplexType> getExtension() {
		return extension;
	}

	@Override
	public boolean isSetExtension() {
		return extension != null;
	}

	@Override
	public void setExtension(AbstractExtension<ComplexType> extension) {
		this.extension = (ComplexTypeExtension)extension;
	}

	@Override
	public List<ComplexType> listSubTypes(boolean skipAbstractTypes) {
		return listSubTypes(schemaMapping.getComplexTypes(), skipAbstractTypes);
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.COMPLEX_TYPE;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		schema.addComplexType(this);
		
		boolean isInline = parent instanceof ComplexProperty;
		
		if (!isInline) {
			if (!isSetId())
				throw new SchemaMappingException("A global complex type must be assigned an id value.");
			else if (!isSetObjectClass())
				throw new SchemaMappingException("An object type requires an objectClassId.");
		} else {
			if (isSetId())
				throw new SchemaMappingException("The attribute 'id' is not allowed for a complex type that is given inline.");
			else if (table != null)
				throw new SchemaMappingException("The attribute 'table' is not allowed for a complex type that is given inline.");
			else if (isSetObjectClass())
				throw new SchemaMappingException("The attribute 'objectClassId' is not allowed for a complex type that is given inline.");
		}
		
		if (isSetExtension())
			extension.validate(schemaMapping, this);
	}

}
