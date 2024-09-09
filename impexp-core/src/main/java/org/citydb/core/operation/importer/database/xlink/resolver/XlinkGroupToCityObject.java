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

import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.cache.IdCacheEntry;
import org.citydb.core.operation.common.xlink.DBXlinkGroupToCityObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class XlinkGroupToCityObject implements DBXlinkResolver {
    private final DBXlinkResolverManager manager;
    private final PreparedStatement psSelectTmp;
    private final PreparedStatement psGroupMemberToCityObject;
    private final PreparedStatement psGroupParentToCityObject;
    private final FeatureType cityObjectGroupType;

    private int parentBatchCounter;
    private int memberBatchCounter;

    public XlinkGroupToCityObject(Connection connection, CacheTable cacheTable, DBXlinkResolverManager manager) throws SQLException {
        this.manager = manager;

        String schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();
        cityObjectGroupType = manager.getFeatureType(23);

        psSelectTmp = cacheTable.getConnection().prepareStatement("select GROUP_ID from " + cacheTable.getTableName()
                + " where GROUP_ID=? and IS_PARENT=?");

        psGroupMemberToCityObject = connection.prepareStatement("insert into " + schema + ".GROUP_TO_CITYOBJECT " +
                "(CITYOBJECT_ID, CITYOBJECTGROUP_ID, ROLE) " +
                "values (?, ?, ?)");

        psGroupParentToCityObject = connection.prepareStatement("update " + schema + ".CITYOBJECTGROUP " +
                "set PARENT_CITYOBJECT_ID=? where ID=?");
    }

    public boolean insert(DBXlinkGroupToCityObject xlink) throws SQLException {
        // for groupMembers, we do not only lookup gml:ids within the document
        // but within the whole database
        IdCacheEntry cityObjectEntry = manager.getObjectId(xlink.getGmlId(), true);
        if (cityObjectEntry == null || cityObjectEntry.getId() == -1)
            return false;

        FeatureType featureType = manager.getFeatureType(cityObjectEntry.getObjectClassId());
        if (featureType == null)
            return false;

        // be careful with cyclic groupings
        if (featureType.isEqualToOrSubTypeOf(cityObjectGroupType)) {
            psSelectTmp.setLong(1, cityObjectEntry.getId());
            psSelectTmp.setLong(2, xlink.isParent() ? 1 : 0);

            try (ResultSet rs = psSelectTmp.executeQuery()) {
                if (rs.next()) {
                    manager.propagateXlink(xlink);
                    return true;
                }
            }
        }

        if (xlink.isParent()) {
            psGroupParentToCityObject.setLong(1, cityObjectEntry.getId());
            psGroupParentToCityObject.setLong(2, xlink.getGroupId());

            psGroupParentToCityObject.addBatch();
            if (++parentBatchCounter == manager.getDatabaseAdapter().getMaxBatchSize()) {
                manager.executeBatchWithLock(psGroupParentToCityObject, this);
                parentBatchCounter = 0;
            }
        } else {
            psGroupMemberToCityObject.setLong(1, cityObjectEntry.getId());
            psGroupMemberToCityObject.setLong(2, xlink.getGroupId());
            psGroupMemberToCityObject.setString(3, xlink.getRole());

            psGroupMemberToCityObject.addBatch();
            if (++memberBatchCounter == manager.getDatabaseAdapter().getMaxBatchSize()) {
                manager.executeBatchWithLock(psGroupMemberToCityObject, this);
                memberBatchCounter = 0;
            }
        }

        return true;
    }

    @Override
    public void executeBatch() throws SQLException {
        psGroupMemberToCityObject.executeBatch();
        psGroupParentToCityObject.executeBatch();
        parentBatchCounter = 0;
        memberBatchCounter = 0;
    }

    @Override
    public void close() throws SQLException {
        psGroupMemberToCityObject.close();
        psGroupParentToCityObject.close();
        psSelectTmp.close();
    }

    @Override
    public DBXlinkResolverEnum getDBXlinkResolverType() {
        return DBXlinkResolverEnum.GROUP_TO_CITYOBJECT;
    }
}
