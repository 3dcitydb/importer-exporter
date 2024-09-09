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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlType(name = "complexAttribute", propOrder = {
        "join",
        "attributes"
})
public class ComplexAttribute extends AbstractAttribute {
    @XmlElements({
            @XmlElement(type = Join.class),
            @XmlElement(name = "reverseJoin", type = ReverseJoin.class)
    })
    protected AbstractJoin join;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(ComplexAttributeTypeAdapter.class)
    protected ComplexAttributeType refType;
    @XmlElements({
            @XmlElement(name = "attribute", type = SimpleAttribute.class),
            @XmlElement(name = "complexAttribute", type = ComplexAttribute.class)
    })
    protected List<AbstractAttribute> attributes;

    @XmlTransient
    protected ComplexAttributeType inlineType;

    protected ComplexAttribute() {
        attributes = new ArrayList<>();
    }

    public ComplexAttribute(String path, AppSchema schema) {
        super(path, schema);
        attributes = new ArrayList<>();
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

    public ComplexAttributeType getType() {
        return refType != null ? refType : inlineType;
    }

    public boolean isSetType() {
        return refType != null || inlineType != null;
    }

    public void setRefType(ComplexAttributeType refType) {
        this.refType = refType;
        inlineType = null;
    }

    public void setInlineType(ComplexAttributeType inlineType) {
        this.inlineType = inlineType;
        attributes = inlineType.attributes;
        refType = null;
    }

    @Override
    public PathElementType getElementType() {
        return PathElementType.COMPLEX_ATTRIBUTE;
    }

    @Override
    protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
        if (refType == null && (attributes == null || attributes.isEmpty()))
            throw new SchemaMappingException("The type of a complex attribute must either be given by reference or inline.");
        else if (refType != null && attributes != null && !attributes.isEmpty())
            throw new SchemaMappingException("The type of a complex attribute must either be given by reference or inline but not both.");

        if (inlineType != null) {
            inlineType.validate(schemaMapping, this);
        } else if (refType == null) {
            inlineType = new ComplexAttributeType();
            inlineType.attributes = attributes;
            inlineType.validate(schemaMapping, this);
        } else if (refType.hasLocalProperty(MappingConstants.IS_XLINK)) {
            ComplexAttributeType ref = schemaMapping.getComplexAttributeTypeById(refType.getId());
            if (ref == null)
                throw new SchemaMappingException("Failed to resolve the attribute type reference '" + refType.getId() + "'.");

            refType = ref;
        }

        super.validate(schemaMapping, parent);

        for (AbstractAttribute attribute : getType().getAttributes()) {
            if (attribute.getElementType() == PathElementType.SIMPLE_ATTRIBUTE && attribute.getPath().equals(".")) {
                ((SimpleAttribute) attribute).name = path;
                break;
            }
        }
    }

}
