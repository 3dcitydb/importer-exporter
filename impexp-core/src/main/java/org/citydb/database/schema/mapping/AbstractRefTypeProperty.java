/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "abstractRefTypeProperty")
public abstract class AbstractRefTypeProperty<T extends AbstractType<T>> extends AbstractTypeProperty<T> {
    @XmlAttribute
    protected RelationType relationType;

	protected AbstractRefTypeProperty() {
	}
    
    public AbstractRefTypeProperty(String path, AppSchema schema) {
    	super(path, schema);
    }
    
    public abstract void setType(T type);

    @Override
    public RelationType getRelationType() {
        return relationType != null ? relationType : RelationType.ASSOCIATION;
    }

    public boolean isSetRelationType() {
        return relationType != null;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }
}
