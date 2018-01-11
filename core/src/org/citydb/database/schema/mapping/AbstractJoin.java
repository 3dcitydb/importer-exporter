package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractJoin")
public abstract class AbstractJoin {
	protected abstract void validate(SchemaMapping schemaMapping, Object parent, Object transitiveParent) throws SchemaMappingException;
}
