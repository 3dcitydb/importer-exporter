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

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.core.operation.common.xlink.DBXlinkTextureCoordList;
import org.citydb.core.util.Util;
import org.citydb.util.log.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

public class XlinkTexCoordList implements DBXlinkResolver {
    private final Logger log = Logger.getInstance();
    private final Connection connection;
    private final DBXlinkResolverManager manager;

    private final PreparedStatement psSelectTexCoords;
    private final PreparedStatement psSelectTexCoordsByGmlId;
    private final PreparedStatement psSelectLinearRings;
    private final PreparedStatement psTextureParam;

    private int batchCounter;

    public XlinkTexCoordList(Connection connection, CacheTable texCoords, CacheTable linearRings, DBXlinkResolverManager manager) throws SQLException {
        this.connection = connection;
        this.manager = manager;
        String schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();

        psSelectTexCoords = texCoords.getConnection().prepareStatement("select GMLID, TEXTURE_COORDINATES from " +
                texCoords.getTableName() + " where TARGET_ID=? and ID=?");

        psSelectTexCoordsByGmlId = texCoords.getConnection().prepareStatement("select GMLID, TEXTURE_COORDINATES from " +
                texCoords.getTableName() + " where GMLID=?");

        psSelectLinearRings = linearRings.getConnection().prepareStatement("select GMLID, RING_NO from " +
                linearRings.getTableName() + " where PARENT_ID = ?");

        psTextureParam = connection.prepareStatement("insert into " + schema + ".TEXTUREPARAM (SURFACE_GEOMETRY_ID, " +
                "IS_TEXTURE_PARAMETRIZATION, TEXTURE_COORDINATES, SURFACE_DATA_ID) " +
                "values (?, 1, ?, ?)");
    }

    public boolean insert(DBXlinkTextureCoordList xlink) throws SQLException {
        // check whether we deal with a local gml:id
        // remote gml:ids are not supported so far...
        if (Util.isRemoteXlink(xlink.getGmlId()))
            return false;

        ResultSet rs = null;

        try {
            // step 1: get linear rings
            psSelectLinearRings.setLong(1, xlink.getSurfaceGeometryId());
            rs = psSelectLinearRings.executeQuery();

            long surfaceGeometryId = xlink.getSurfaceGeometryId();
            boolean reverse = xlink.isReverse();

            HashMap<String, Integer> ringNos = new HashMap<String, Integer>();
            while (rs.next()) {
                String ringId = rs.getString(1);
                int ringNo = rs.getInt(2);
                ringNos.put(ringId, ringNo);
            }

            rs.close();

            if (surfaceGeometryId == 0)
                return false;

            // step 2: get texture coordinates
            if (ringNos.size() == 1) {
                psSelectTexCoordsByGmlId.setString(1, ringNos.keySet().iterator().next());
                rs = psSelectTexCoordsByGmlId.executeQuery();
            } else {
                psSelectTexCoords.setLong(1, xlink.getTargetId());
                psSelectTexCoords.setLong(2, xlink.getId());
                rs = psSelectTexCoords.executeQuery();
            }

            double[][] texCoords = new double[ringNos.size()][];
            while (rs.next()) {
                String ringId = rs.getString(1);
                GeometryObject texCoord = manager.getCacheAdapter().getGeometryConverter().getPolygon(rs.getObject(2));
                if (texCoord != null && ringNos.containsKey(ringId))
                    texCoords[ringNos.get(ringId)] = texCoord.getCoordinates(0);
            }

            rs.close();

            // step 3: sanity check
            for (int i = 0; i < texCoords.length; i++) {
                if (texCoords[i] == null) {
                    for (Entry<String, Integer> entry : ringNos.entrySet()) {
                        if (entry.getValue() == i) {
                            log.warn("Missing texture coordinates for ring '" + entry.getValue() + "'.");
                            return false;
                        }
                    }
                }
            }

            // step 4: reverse texture coordinates if required
            if (reverse) {
                for (int i = 0; i < texCoords.length; i++) {
                    double[] tmp = new double[texCoords[i].length];
                    for (int j = texCoords[i].length - 2, n = 0; j >= 0; j -= 2) {
                        tmp[n++] = texCoords[i][j];
                        tmp[n++] = texCoords[i][j + 1];
                    }

                    texCoords[i] = tmp;
                }
            }

            // step 5: update textureparam
            psTextureParam.setLong(1, surfaceGeometryId);
            psTextureParam.setObject(2, manager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(GeometryObject.createPolygon(texCoords, 2, 0), connection));
            psTextureParam.setLong(3, xlink.getId());

            psTextureParam.addBatch();
            if (++batchCounter == manager.getDatabaseAdapter().getMaxBatchSize())
                executeBatch();

            if (xlink.getTexParamGmlId() != null) {
                // make sure xlinks to the corresponding texture parameterization can be resolved
                manager.propagateXlink(new DBXlinkTextureAssociationTarget(
                        xlink.getId(),
                        surfaceGeometryId,
                        xlink.getTexParamGmlId()));
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    //
                }

                rs = null;
            }
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
        psSelectTexCoords.close();
        psSelectTexCoordsByGmlId.close();
        psSelectLinearRings.close();
        psTextureParam.close();
    }

    @Override
    public DBXlinkResolverEnum getDBXlinkResolverType() {
        return DBXlinkResolverEnum.TEXCOORDLIST;
    }

}
