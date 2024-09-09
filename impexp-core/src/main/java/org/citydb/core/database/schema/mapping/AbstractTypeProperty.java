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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractTypeProperty", propOrder = {
        "join"
})
public abstract class AbstractTypeProperty<T extends AbstractType<T>> extends AbstractProperty {
    @XmlElements({
            @XmlElement(type = Join.class),
            @XmlElement(name = "joinTable", type = JoinTable.class)
    })
    protected AbstractJoin join;

    protected AbstractTypeProperty() {
    }

    public AbstractTypeProperty(String path, AppSchema schema) {
        super(path, schema);
    }

    public abstract T getType();

    public abstract boolean isSetType();

    public abstract RelationType getRelationType();

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

    public void setJoin(JoinTable join) {
        this.join = join;
    }

    @Override
    protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
        super.validate(schemaMapping, parent);

        if (!isSetType())
            throw new SchemaMappingException("A type property requires a target type.");
    }

}
