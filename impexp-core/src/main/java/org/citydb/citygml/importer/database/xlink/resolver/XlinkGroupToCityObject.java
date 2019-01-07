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
package org.citydb.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import org.citydb.database.schema.mapping.FeatureType;

public class XlinkGroupToCityObject implements DBXlinkResolver {
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectTmp;
	private PreparedStatement psGroupMemberToCityObject;
	private PreparedStatement psGroupParentToCityObject;
	private int parentBatchCounter;
	private int memberBatchCounter;
	private FeatureType cityObjectGroupType;
	
	public XlinkGroupToCityObject(Connection batchConn, CacheTable cacheTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.resolverManager = resolverManager;

		String schema = resolverManager.getDatabaseAdapter().getConnectionDetails().getSchema();
		cityObjectGroupType = resolverManager.getFeatureType(23);

		StringBuilder selectStmt = new StringBuilder("select GROUP_ID from ").append(cacheTable.getTableName()).append(" where GROUP_ID=? and IS_PARENT=?");
		psSelectTmp = cacheTable.getConnection().prepareStatement(selectStmt.toString());
		
		StringBuilder insertStmt = new StringBuilder("insert into ").append(schema).append(".GROUP_TO_CITYOBJECT (CITYOBJECT_ID, CITYOBJECTGROUP_ID, ROLE) values (?, ?, ?)");
		psGroupMemberToCityObject = batchConn.prepareStatement(insertStmt.toString());
		
		StringBuilder updateStmt = new StringBuilder("update ").append(schema).append(".CITYOBJECTGROUP set PARENT_CITYOBJECT_ID=? where ID=?");
		psGroupParentToCityObject = batchConn.prepareStatement(updateStmt.toString());
	}

	public boolean insert(DBXlinkGroupToCityObject xlink) throws SQLException {
		// for groupMembers, we do not only lookup gml:ids within the document
		// but within the whole database
		UIDCacheEntry cityObjectEntry = resolverManager.getObjectId(xlink.getGmlId(), true);
		if (cityObjectEntry == null || cityObjectEntry.getId() == -1)
			return false;		
		
		FeatureType featureType = resolverManager.getFeatureType(cityObjectEntry.getObjectClassId());
		if (featureType == null)
			return false;

		// be careful with cyclic groupings
		if (featureType.isEqualToOrSubTypeOf(cityObjectGroupType)) {
			ResultSet rs = null;

			try {
				psSelectTmp.setLong(1, cityObjectEntry.getId());
				psSelectTmp.setLong(2, xlink.isParent() ? 1 : 0);
				rs = psSelectTmp.executeQuery();			

				if (rs.next()) {
					resolverManager.propagateXlink(xlink);
					return true;
				}

			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {
						//
					}

					rs = null;
				}
			}
		}

		if (xlink.isParent()) {
			psGroupParentToCityObject.setLong(1, cityObjectEntry.getId());
			psGroupParentToCityObject.setLong(2, xlink.getGroupId());
			
			psGroupParentToCityObject.addBatch();
			if (++parentBatchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize()) {
				psGroupParentToCityObject.executeBatch();
				parentBatchCounter = 0;
			}
		} else {
			psGroupMemberToCityObject.setLong(1, cityObjectEntry.getId());
			psGroupMemberToCityObject.setLong(2, xlink.getGroupId());
			psGroupMemberToCityObject.setString(3, xlink.getRole());

			psGroupMemberToCityObject.addBatch();
			if (++memberBatchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize()) {
				psGroupMemberToCityObject.executeBatch();
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
