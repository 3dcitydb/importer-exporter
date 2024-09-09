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
package org.citydb.core.operation.importer.database.xlink.resolver;

import org.citydb.core.database.adapter.BlobImportAdapter;
import org.citydb.core.database.adapter.BlobType;
import org.citydb.core.operation.common.xlink.DBXlinkTextureFile;
import org.citydb.util.event.global.CounterEvent;
import org.citydb.util.event.global.CounterType;
import org.citydb.util.log.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class XlinkTextureImage implements DBXlinkResolver {
    private final Logger log = Logger.getInstance();
    private final DBXlinkResolverManager manager;
    private final BlobImportAdapter textureImportAdapter;
    private final CounterEvent counter;

    public XlinkTextureImage(Connection connection, DBXlinkResolverManager manager) throws SQLException {
        this.manager = manager;

        counter = new CounterEvent(CounterType.TEXTURE_IMAGE, 1);
        textureImportAdapter = manager.getDatabaseAdapter().getSQLAdapter().getBlobImportAdapter(
                connection, BlobType.TEXTURE_IMAGE);
    }

    public boolean insert(DBXlinkTextureFile xlink) throws SQLException {
        manager.propagateEvent(counter);
        String fileURI = xlink.getFileURI();

        try (InputStream stream = new BufferedInputStream(manager.openStream(fileURI))) {
            textureImportAdapter.insert(xlink.getId(), stream);
            return true;
        } catch (IOException e) {
            log.error("Failed to read texture file '" + fileURI + "'.", e);
            return false;
        }
    }

    @Override
    public void executeBatch() throws SQLException {
        // we do not have any action here
    }

    @Override
    public void close() throws SQLException {
        textureImportAdapter.close();
    }

    @Override
    public DBXlinkResolverEnum getDBXlinkResolverType() {
        return DBXlinkResolverEnum.TEXTURE_IMAGE;
    }

}
