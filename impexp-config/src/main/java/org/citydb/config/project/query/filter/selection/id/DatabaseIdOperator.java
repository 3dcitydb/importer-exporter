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

package org.citydb.config.project.query.filter.selection.id;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "databaseIds")
@XmlType(name = "DatabaseIdType", propOrder = {
        "ids"
})
public class DatabaseIdOperator extends AbstractIdOperator {
    @XmlElement(name = "id")
    private List<Long> ids;

    public DatabaseIdOperator() {
        ids = new ArrayList<>();
    }

    public boolean isSetDatabaseIds() {
        return !ids.isEmpty();
    }

    public List<Long> getDatabaseIds() {
        return ids;
    }

    public void addDatabaseId(Long id) {
        ids.add(id);
    }

    public void setDatabaseIds(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.ids = ids;
        }
    }

    @Override
    public void reset() {
        ids.clear();
    }

    @Override
    public IdOperatorName getOperatorName() {
        return IdOperatorName.DATABASE_ID;
    }
}
