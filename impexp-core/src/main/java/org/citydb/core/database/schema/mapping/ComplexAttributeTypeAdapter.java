/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ComplexAttributeTypeAdapter extends XmlAdapter<String, ComplexAttributeType> {
    private final SchemaMapping schemaMapping;

    protected ComplexAttributeTypeAdapter() {
        schemaMapping = null;
    }

    public ComplexAttributeTypeAdapter(SchemaMapping schemaMapping) {
        this.schemaMapping = schemaMapping;
    }

    @Override
    public ComplexAttributeType unmarshal(String id) throws Exception {
        if (id == null || id.isEmpty())
            throw new SchemaMappingException("The attribute 'id' is not set for the attribute type reference.");

        ComplexAttributeType type = null;
        if (schemaMapping != null)
            type = schemaMapping.getComplexAttributeTypeById(id);

        if (type == null) {
            type = new ComplexAttributeType();
            type.id = id;
            type.setLocalProperty(MappingConstants.IS_XLINK, true);
        }

        return type;
    }

    @Override
    public String marshal(ComplexAttributeType dataType) throws Exception {
        return dataType != null ? dataType.id : null;
    }

}
