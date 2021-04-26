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
package org.citydb.config.project.query.filter.selection.sql;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.PredicateName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "sql")
@XmlType(name = "SelectOperatorType", propOrder = {
        "value"
})
public class SelectOperator extends AbstractPredicate {
    @XmlElement(name = "select", required = true)
    private String value;

    public String getValue() {
        return value;
    }

    public boolean isSetValue() {
        return value != null && !value.trim().isEmpty();
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void reset() {
        value = null;
    }

    @Override
    public PredicateName getPredicateName() {
        return PredicateName.SQL_OPERATOR;
    }
}
