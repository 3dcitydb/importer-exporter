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
package org.citydb.query.filter.selection.operator.sql;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.PredicateName;
import org.citydb.query.filter.selection.operator.Operator;

public class SelectOperator implements Operator {
    private String select;

    public SelectOperator(String select) {
        this.select = select;
    }

    public boolean isSetSelect() {
        return select != null;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    @Override
    public SQLOperatorName getOperatorName() {
        return SQLOperatorName.SELECT;
    }

    @Override
    public PredicateName getPredicateName() {
        return PredicateName.SQL_OPERATOR;
    }

    @Override
    public SelectOperator copy() throws FilterException {
        return new SelectOperator(select);
    }
}
