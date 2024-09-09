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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "complexProperty")
public class ComplexProperty extends AbstractRefOrInlineTypeProperty<ComplexType> {
    @XmlAttribute
    @XmlJavaTypeAdapter(ComplexTypeAdapter.class)
    protected ComplexType refType;
    @XmlElement(name = "type", required = false)
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
                inlineType.transitiveTable = ((AbstractType<?>) parent).getTable();
            else if (parent instanceof PropertyInjection)
                inlineType.transitiveTable = ((PropertyInjection) parent).getTable();

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
