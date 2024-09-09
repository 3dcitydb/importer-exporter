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

package org.citydb.config.project.importer;

import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportListType")
public class ImportList extends IdList {
    private ImportListMode mode = ImportListMode.IMPORT;

    @Override
    public ImportList withDefaultCommentCharacter(Character commentCharacter) {
        return (ImportList) super.withDefaultCommentCharacter(commentCharacter);
    }

    public ImportListMode getMode() {
        return mode != null ? mode : ImportListMode.IMPORT;
    }

    public void setMode(ImportListMode mode) {
        this.mode = mode;
    }

    @Override
    public IdColumnType getIdColumnType() {
        return IdColumnType.RESOURCE_ID;
    }

    @Override
    public void setIdColumnType(IdColumnType idColumnType) {
        super.setIdColumnType(IdColumnType.RESOURCE_ID);
    }
}
