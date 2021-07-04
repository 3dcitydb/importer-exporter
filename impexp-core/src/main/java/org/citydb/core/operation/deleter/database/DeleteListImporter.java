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

package org.citydb.core.operation.deleter.database;

import org.citydb.config.project.deleter.DeleteListIdType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.csv.IdListException;
import org.citydb.core.operation.common.csv.IdListParser;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteListImporter {
    private final CacheTable cacheTable;
    private final int maxBatchSize;

    private int batchCounter;

    public DeleteListImporter(CacheTable cacheTable, int maxBatchSize) {
        this.cacheTable = cacheTable;
        this.maxBatchSize = maxBatchSize;
    }

    public void doImport(IdListParser parser, DeleteListIdType idType) throws IdListException, SQLException {
        String sql = "insert into " + cacheTable.getTableName() + " " +
                (idType == DeleteListIdType.DATABASE_ID ?
                        "(" + MappingConstants.ID + ") values (?)" :
                        "(" + MappingConstants.GMLID + ") values (?)");

        try (PreparedStatement ps = cacheTable.getConnection().prepareStatement(sql)) {
            while (parser.hasNext()) {
                long lineNumber = parser.getCurrentLineNumber();
                String id = parser.nextId();

                if (id != null) {
                    if (idType == DeleteListIdType.DATABASE_ID) {
                        try {
                            ps.setLong(1, Long.parseLong(id));
                        } catch (NumberFormatException e) {
                            throw new IdListException("Invalid database ID: '" + id + "' (line " +
                                    lineNumber + ") is not an integer.");
                        }
                    } else {
                        ps.setString(1, id);
                    }

                    ps.addBatch();
                    if (++batchCounter == maxBatchSize) {
                        ps.executeBatch();
                        batchCounter = 0;
                    }
                }
            }

            if (batchCounter > 0) {
                ps.executeBatch();
            }
        }
    }
}
