/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.citygml.common.database.cache.HeapCacheTable;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import de.tub.citydb.modules.common.filter.ImportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;

public class XlinkGroupToCityObject implements DBXlinkResolver {
	private final Connection batchConn;
	private final HeapCacheTable heapTable;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectTmp;
	private PreparedStatement psGroupMemberToCityObject;
	private PreparedStatement psGroupParentToCityObject;
	
	private int parentBatchCounter;
	private int memberBatchCounter;

	private FeatureClassFilter featureClassFilter;

	public XlinkGroupToCityObject(Connection batchConn, HeapCacheTable heapTable, ImportFilter importFilter, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.heapTable = heapTable;
		this.resolverManager = resolverManager;
		this.featureClassFilter = importFilter.getFeatureClassFilter();
		
		init();
	}

	private void init() throws SQLException {
		psSelectTmp = heapTable.getConnection().prepareStatement("select GROUP_ID from " + heapTable.getTableName() + " where GROUP_ID=? and IS_PARENT=?");
		psGroupMemberToCityObject = batchConn.prepareStatement("insert into GROUP_TO_CITYOBJECT (CITYOBJECT_ID, CITYOBJECTGROUP_ID, ROLE) values " +
		"(?, ?, ?)");
		psGroupParentToCityObject = batchConn.prepareStatement("update CITYOBJECTGROUP set PARENT_CITYOBJECT_ID=? where ID=?");
	}

	public boolean insert(DBXlinkGroupToCityObject xlink) throws SQLException {
		// for groupMembers, we do not only lookup gmlIds within the document, but also within
		// the whole database!
		GmlIdEntry cityObjectEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.ABSTRACT_CITY_OBJECT, true);
		if (cityObjectEntry == null || cityObjectEntry.getId() == -1)
			return false;

		if (featureClassFilter.filter(cityObjectEntry.getType()))
			return true;

		// be careful with cyclic groupings!
		if (cityObjectEntry.getType() == CityGMLClass.CITY_OBJECT_GROUP) {
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
					} catch (SQLException sqlEx) {
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
			if (++parentBatchCounter == Internal.ORACLE_MAX_BATCH_SIZE) {
				psGroupParentToCityObject.executeBatch();
				parentBatchCounter = 0;
			}
		} else {
			psGroupMemberToCityObject.setLong(1, cityObjectEntry.getId());
			psGroupMemberToCityObject.setLong(2, xlink.getGroupId());
			psGroupMemberToCityObject.setString(3, xlink.getRole());

			psGroupMemberToCityObject.addBatch();
			if (++memberBatchCounter == Internal.ORACLE_MAX_BATCH_SIZE) {
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
