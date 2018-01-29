package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "geometryProperty", propOrder = {
	    "join"
	})
public class GeometryProperty extends AbstractProperty {
	@XmlElements({
        @XmlElement(type = Join.class),
        @XmlElement(name = "reverseJoin", type = ReverseJoin.class)
    })
    protected AbstractJoin join;
	@XmlAttribute(required = false)
	protected Integer lod;
	@XmlAttribute(required = false)
	protected String refColumn;
	@XmlAttribute(required = false)
	protected String inlineColumn;
	@XmlAttribute(required = true)
	protected GeometryType type;
	@XmlAttribute
	protected Integer srsDimension;

	protected GeometryProperty() {
	}
    
    public GeometryProperty(String path, GeometryType type, AppSchema schema) {
    	super(path, schema);
    	this.type = type;
    }
	
	@Override
    public AbstractJoin getJoin() {
        return join;
    }

	@Override
    public boolean isSetJoin() {
        return join != null;
    }
	
	public void setJoin(Join join) {
		this.join = join;
	}
	
	public void setJoin(ReverseJoin join) {
		this.join = join;
	}
	
	public int getLod() {
		return lod != null ? lod.intValue() : -1;
	}
	
	public boolean isSetLod() {
		return lod != null;
	}
	
	public void setLod(int lod) {
		if (lod < 0 || lod > 4)
			throw new IllegalArgumentException("LOD level of a geometry property must be between 0 and 4.");
		
		this.lod = lod;
	}
	
	public String getRefColumn() {
		return refColumn;
	}

	public boolean isSetRefColumn() {
		return refColumn != null && !refColumn.isEmpty();
	}
	
	public void setRefColumn(String refColumn) {
		this.refColumn = refColumn;
	}

	public String getInlineColumn() {
		return inlineColumn;
	}

	public boolean isSetInlineColumn() {
		return inlineColumn != null;
	}
	
	public void setInlineColumn(String inlineColumn) {
		this.inlineColumn = inlineColumn;
	}

	public GeometryType getType() {
		return type;
	}

	public boolean isSetType() {
		return type != null;
	}
	
	public void setType(GeometryType type) {
		this.type = type;
	}

	public int getSrsDimension() {
		return srsDimension == null ? 3 : srsDimension.intValue();
	}
	
	public void setSrsDimension(int srsDimension) {
		if (srsDimension > 1 && srsDimension < 4)
			this.srsDimension = srsDimension;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.GEOMETRY_PROPERTY;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		
		if (refColumn == null && inlineColumn == null)
			throw new SchemaMappingException("A geometry property must provide a reference column or an inline column.");
		else if (getSrsDimension() < 2 || getSrsDimension() > 3)
			throw new SchemaMappingException("The SRS dimension of a geometry property must be between 2 and 3.");
		else if (refColumn != null && getSrsDimension() == 2)
			throw new SchemaMappingException("The SRS dimension of a surface geometry property must be 3.");
		
		if (isSetLod() && (lod < 0 || lod > 4))
			throw new IllegalArgumentException("LOD level of a geometry property must be between 0 and 4.");
	}
	
}
