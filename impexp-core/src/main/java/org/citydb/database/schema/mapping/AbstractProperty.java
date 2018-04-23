package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractProperty")
public abstract class AbstractProperty extends AbstractPathElement implements Joinable {
	@XmlAttribute
	protected Integer minOccurs = null;
	@XmlAttribute
	protected Integer maxOccurs = null;

	protected AbstractProperty() {
	}

	public AbstractProperty(String path, AppSchema schema) {
		super(path, schema);
	}

	public int getMinOccurs() {
		return minOccurs != null ? minOccurs : 0;
	}

	public boolean isSetMinOccurs() {
		return minOccurs != null;
	}

	public void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs > 0 ? minOccurs : null;
	}

	public Integer getMaxOccurs() {
		return maxOccurs;
	}

	public boolean isSetMaxOccurs() {
		return maxOccurs != null;
	}

	public void setMaxOccurs(Integer maxOccurs) {
		this.maxOccurs = maxOccurs > 0 ? maxOccurs : null;
	}

	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		
		if (isSetJoin())
			getJoin().validate(schemaMapping, this, parent);

		if (isSetMaxOccurs() && getMaxOccurs() < getMinOccurs())
			throw new SchemaMappingException("Invalid occurrence constraint: " +
					"'minOccurs' ('" + getMinOccurs() + "') must not be greater than 'maxOccurs ('" + getMaxOccurs() + "').");
	}
}
