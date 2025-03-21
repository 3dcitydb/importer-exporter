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
package org.citydb.core.operation.importer.database.xlink.resolver;

import org.citydb.core.operation.common.cache.IdCacheEntry;
import org.citydb.core.operation.common.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.core.operation.common.xlink.DBXlinkTextureParam;
import org.citydb.core.operation.common.xlink.DBXlinkTextureParamEnum;
import org.citydb.core.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class XlinkTextureParam implements DBXlinkResolver {
    private final DBXlinkResolverManager manager;
    private final PreparedStatement psTextureParam;

    private int batchCounter;

    public XlinkTextureParam(Connection connection, DBXlinkResolverManager manager) throws SQLException {
        this.manager = manager;

        String schemaName = manager.getDatabaseAdapter().getConnectionDetails().getSchema();
        psTextureParam = connection.prepareStatement("insert into " + schemaName + ".TEXTUREPARAM (SURFACE_GEOMETRY_ID, " +
                "IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, SURFACE_DATA_ID) " +
                "values (?, ?, ?, ?)");
    }

    public boolean insert(DBXlinkTextureParam xlink) throws SQLException {
        // check whether we deal with a local gml:id
        // remote gml:ids are not supported so far...
        if (Util.isRemoteXlink(xlink.getGmlId()))
            return false;

        IdCacheEntry geometryEntry = manager.getGeometryId(xlink.getGmlId());
        if (geometryEntry == null || geometryEntry.getId() == -1)
            return false;

        psTextureParam.setLong(1, geometryEntry.getId());
        psTextureParam.setInt(2, xlink.isTextureParameterization() ? 1 : 0);
        psTextureParam.setLong(4, xlink.getId());

        // worldToTexture
        if (xlink.getWorldToTexture() != null && xlink.getWorldToTexture().length() != 0)
            psTextureParam.setString(3, xlink.getWorldToTexture());
        else
            psTextureParam.setNull(3, Types.VARCHAR);

        psTextureParam.addBatch();
        if (++batchCounter == manager.getDatabaseAdapter().getMaxBatchSize())
            manager.executeBatch(this);

        if (xlink.getType() == DBXlinkTextureParamEnum.TEXCOORDGEN && xlink.getTexParamGmlId() != null) {
            // make sure xlinks to the corresponding texture parameterization can be resolved
            manager.propagateXlink(new DBXlinkTextureAssociationTarget(
                    xlink.getId(),
                    geometryEntry.getId(),
                    xlink.getTexParamGmlId()));
        }

        return true;
    }

    @Override
    public void executeBatch() throws SQLException {
        psTextureParam.executeBatch();
        batchCounter = 0;
    }

    @Override
    public void close() throws SQLException {
        psTextureParam.close();
    }

    @Override
    public DBXlinkResolverEnum getDBXlinkResolverType() {
        return DBXlinkResolverEnum.TEXTUREPARAM;
    }

}
