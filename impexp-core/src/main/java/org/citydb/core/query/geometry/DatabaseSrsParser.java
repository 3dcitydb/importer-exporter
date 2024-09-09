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
package org.citydb.core.query.geometry;

import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;

public class DatabaseSrsParser extends SrsNameParser {
    private final AbstractDatabaseAdapter databaseAdapter;
    private final Config config;

    public DatabaseSrsParser(AbstractDatabaseAdapter databaseAdapter, Config config) {
        this.databaseAdapter = databaseAdapter;
        this.config = config;
    }

    public DatabaseSrs getDefaultSrs() {
        return databaseAdapter.getConnectionMetaData().getReferenceSystem();
    }

    public DatabaseSrs getDatabaseSrs(String srsName) throws SrsParseException {
        if (srsName.equals(databaseAdapter.getConnectionMetaData().getReferenceSystem().getGMLSrsName()))
            return databaseAdapter.getConnectionMetaData().getReferenceSystem();

        int epsgCode = getEPSGCode(srsName);
        if (epsgCode == databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid())
            return databaseAdapter.getConnectionMetaData().getReferenceSystem();

        // check whether SRS is supported by database
        DatabaseSrs targetSrs = null;
        for (DatabaseSrs srs : config.getDatabaseConfig().getReferenceSystems()) {
            if (srs.getSrid() == epsgCode) {
                if (!srs.isSupported())
                    throw new SrsParseException("The CRS '" + srsName + "' is advertised but not supported by the database.");

                targetSrs = srs;
                break;
            }
        }

        if (targetSrs == null)
            throw new SrsParseException("The CRS '" + srsName + "' is not advertised.");

        return targetSrs;
    }

}
