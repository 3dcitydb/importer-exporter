package org.citydb.database.schema.mapping;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.Marshaller.Listener;

public class MarshalListener extends Listener {
	private SchemaMapping previous;

	@Override
	public void beforeMarshal(Object source) {
		if (source instanceof SchemaMapping) {
			SchemaMapping schemaMapping = (SchemaMapping)source;

			// beforeMarshal is invoked twice due to a JAXB bug
			if (schemaMapping == previous)
				return;

			previous = schemaMapping;

			if (schemaMapping.attributeTypes.isEmpty())
				schemaMapping.attributeTypes = null;

			if (schemaMapping.complexTypes.isEmpty())
				schemaMapping.complexTypes = null;

			if (schemaMapping.objectTypes.isEmpty())
				schemaMapping.objectTypes = null;
			
			if (schemaMapping.propertyInjections.isEmpty())
				schemaMapping.propertyInjections = null;

			if (schemaMapping.featureTypes.isEmpty())
				schemaMapping.featureTypes = null;
			else {
				// remove injected properties from feature before marshalling
				for (FeatureType featureType : schemaMapping.featureTypes) {			
					Iterator<AbstractProperty> iter = featureType.properties.iterator();
					while (iter.hasNext()) {
						if (iter.next() instanceof InjectedProperty)
							iter.remove();
					}
				}
			}
			
			// unset generated XML prefixes
			for (AppSchema schema : schemaMapping.getSchemas()) {
				if (schema.isGeneratedXMLPrefix())
					schema.setXMLPrefix(null);
			}
		}
	}

	@Override
	public void afterMarshal(Object source) {
		if (source instanceof SchemaMapping) {
			SchemaMapping schemaMapping = (SchemaMapping)source;

			if (schemaMapping.attributeTypes == null)
				schemaMapping.attributeTypes = new ArrayList<>();

			if (schemaMapping.complexTypes == null)
				schemaMapping.complexTypes = new ArrayList<>();

			if (schemaMapping.objectTypes == null)
				schemaMapping.objectTypes = new ArrayList<>();
			
			if (schemaMapping.featureTypes == null)
				schemaMapping.featureTypes = new ArrayList<>();

			if (schemaMapping.propertyInjections == null)
				schemaMapping.propertyInjections = new ArrayList<>();
			else {
				try {
					// re-add injected properties after marshalling
					for (PropertyInjection propertyInjection : schemaMapping.propertyInjections)
						propertyInjection.validate(schemaMapping, schemaMapping);
				} catch (SchemaMappingException e) {
					throw new RuntimeException(e);
				}
			}
			
			// re-generate XML prefixes
			for (AppSchema schema : schemaMapping.getSchemas()) {
				if (!schema.isSetXMLPrefix())
					schema.generateXMLPrefix();
			}
		}
	}

}
