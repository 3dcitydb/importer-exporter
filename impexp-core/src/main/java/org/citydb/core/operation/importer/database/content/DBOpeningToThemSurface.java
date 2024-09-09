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
package org.citydb.core.operation.importer.database.content;

import org.citydb.config.Config;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.importer.CityGMLImportException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBOpeningToThemSurface implements DBImporter {
    private final CityGMLImportManager importer;

    private PreparedStatement psOpeningToThemSurface;
    private int batchCounter;

    public DBOpeningToThemSurface(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException {
        this.importer = importer;

        String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

        String stmt = "insert into " + schema + ".opening_to_them_surface (opening_id, thematic_surface_id) values " +
                "(?, ?)";
        psOpeningToThemSurface = batchConn.prepareStatement(stmt);
    }

    protected void doImport(long openingId, long thematicSurfaceId) throws CityGMLImportException, SQLException {
        psOpeningToThemSurface.setLong(1, openingId);
        psOpeningToThemSurface.setLong(2, thematicSurfaceId);

        psOpeningToThemSurface.addBatch();
        if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
            importer.executeBatch(TableEnum.OPENING_TO_THEM_SURFACE);
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            psOpeningToThemSurface.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        psOpeningToThemSurface.close();
    }

}
