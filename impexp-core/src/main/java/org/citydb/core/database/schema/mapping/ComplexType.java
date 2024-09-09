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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlType(name = "complexType", propOrder = {
        "extension",
        "properties"
})
public class ComplexType extends AbstractType<ComplexType> {
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute
    protected String table;
    @XmlAttribute
    protected Integer objectClassId;
    protected ComplexTypeExtension extension;

    @XmlTransient
    protected String transitiveTable;

    protected ComplexType() {
    }

    public ComplexType(String path, AppSchema schema, SchemaMapping schemaMapping) {
        super(path, schema, schemaMapping);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isSetId() {
        return id != null && !id.isEmpty();
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTable() {
        return table != null ? table : transitiveTable;
    }

    @Override
    public boolean isSetTable() {
        return (table != null && !table.isEmpty()) || (transitiveTable != null && !transitiveTable.isEmpty());
    }

    @Override
    public void setTable(String table) {
        this.table = table;
        transitiveTable = null;
    }

    @Override
    public int getObjectClassId() {
        return objectClassId != null ? objectClassId.intValue() : 0;
    }

    @Override
    public boolean isSetObjectClass() {
        return objectClassId != null;
    }

    @Override
    public void setObjectClassId(int objectClassId) {
        if (objectClassId >= 0)
            this.objectClassId = objectClassId;
    }

    @Override
    public AbstractExtension<ComplexType> getExtension() {
        return extension;
    }

    @Override
    public boolean isSetExtension() {
        return extension != null;
    }

    @Override
    public void setExtension(AbstractExtension<ComplexType> extension) {
        this.extension = (ComplexTypeExtension) extension;
    }

    @Override
    public List<ComplexType> listSubTypes(boolean skipAbstractTypes) {
        return listSubTypes(schemaMapping.getComplexTypes(), skipAbstractTypes);
    }

    @Override
    public PathElementType getElementType() {
        return PathElementType.COMPLEX_TYPE;
    }

    @Override
    protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
        super.validate(schemaMapping, parent);
        schema.addComplexType(this);

        boolean isInline = parent instanceof ComplexProperty;

        if (!isInline) {
            if (!isSetId())
                throw new SchemaMappingException("A global complex type must be assigned an id value.");
            else if (!isSetObjectClass())
                throw new SchemaMappingException("An object type requires an objectClassId.");
        } else {
            if (isSetId())
                throw new SchemaMappingException("The attribute 'id' is not allowed for a complex type that is given inline.");
            else if (table != null)
                throw new SchemaMappingException("The attribute 'table' is not allowed for a complex type that is given inline.");
            else if (isSetObjectClass())
                throw new SchemaMappingException("The attribute 'objectClassId' is not allowed for a complex type that is given inline.");
        }

        if (isSetExtension())
            extension.validate(schemaMapping, this);
    }

}
