/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.common.database.cache.model;

import org.citydb.database.adapter.AbstractSQLAdapter;

public class CacheTableBasic extends AbstractCacheTableModel {
    public static CacheTableBasic instance = null;

    public synchronized static CacheTableBasic getInstance() {
        if (instance == null)
            instance = new CacheTableBasic();

        return instance;
    }

    @Override
    public CacheTableModel getType() {
        return CacheTableModel.BASIC;
    }

    @Override
    protected String getColumns(AbstractSQLAdapter sqlAdapter) {
        return "(" +
                "ID " + sqlAdapter.getInteger() + ", " +
                "TABLE_NAME " + sqlAdapter.getCharacterVarying(64) + ", " +
                "FROM_COLUMN " + sqlAdapter.getCharacterVarying(64) + ", " +
                "TO_COLUMN " + sqlAdapter.getCharacterVarying(64) + ", " +
                "GMLID " + sqlAdapter.getCharacterVarying(256) +
                ")";
    }

}
