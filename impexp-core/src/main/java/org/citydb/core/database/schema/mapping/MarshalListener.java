/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.core.database.schema.mapping;

import javax.xml.bind.Marshaller.Listener;
import java.util.ArrayList;
import java.util.Iterator;

public class MarshalListener extends Listener {
    private SchemaMapping previous;

    @Override
    public void beforeMarshal(Object source) {
        if (source instanceof SchemaMapping) {
            SchemaMapping schemaMapping = (SchemaMapping) source;

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
            SchemaMapping schemaMapping = (SchemaMapping) source;

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
