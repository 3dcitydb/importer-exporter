package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "reverseJoin")
public class ReverseJoin extends AbstractJoin {

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent, Object transitiveParent) throws SchemaMappingException {
		// nothing to do here...
	}
	
}
