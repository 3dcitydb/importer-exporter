package org.citydb.database.schema.mapping;

import javax.xml.bind.Unmarshaller.Listener;

public class UnmarshalListener extends Listener {

	@Override
	public void afterUnmarshal(Object target, Object parent) {
		if (target instanceof SchemaMapping) {
			try {
				((SchemaMapping)target).validate();
			} catch (SchemaMappingException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
