/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.uid.UIDCache;
import org.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

public class DBGmlIdResolver {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection conn;
	private final UIDCacheManager uidCacheManager;

	private PreparedStatement psSurfaceGeometryId;
	private PreparedStatement psCityObjectId;

	public DBGmlIdResolver(Connection commitConn, UIDCacheManager uidCacheManager) throws SQLException {
		this.conn = commitConn;
		this.uidCacheManager = uidCacheManager;

		init();
	}

	private void init() throws SQLException {
		psSurfaceGeometryId = conn.prepareStatement("select ID from SURFACE_GEOMETRY where ROOT_ID=? and GMLID=?");
		psCityObjectId = conn.prepareStatement("select ID, OBJECTCLASS_ID from CITYOBJECT where GMLID=?");
	}
	
	public UIDCacheEntry getDBId(String gmlId, CityGMLClass type, boolean forceCityObjectDatabaseLookup) {
		UIDCache cache = uidCacheManager.getCache(type);
		if (cache == null)
			return null;

		// replace leading #
		gmlId = gmlId.replaceAll("^#", "");
		UIDCacheEntry entry = chacheLookup(gmlId, null, cache);

		if (entry == null || entry.getId() == -1) {
			
			if (type == CityGMLClass.ABSTRACT_GML_GEOMETRY) {
				if (entry == null)
					return null;

				entry = dbGeometryLookup(entry);			
			}
			
			else if (forceCityObjectDatabaseLookup) {
				if (entry != null)
					gmlId = entry.getMapping();
				
				entry = dbCityObjectLookup(gmlId);
			}
		}

		return entry;
	}

	private UIDCacheEntry dbGeometryLookup(UIDCacheEntry entry) {
		// init database search
		long id;
		ResultSet rs = null;

		try {
			psSurfaceGeometryId.setLong(1, entry.getRootId());
			psSurfaceGeometryId.setString(2, entry.getMapping());
			rs = psSurfaceGeometryId.executeQuery();

			if (rs.next()) {
				id = rs.getLong(1);
				
				return new UIDCacheEntry(id, entry.getRootId(), entry.isReverse(), entry.getMapping(), CityGMLClass.ABSTRACT_GML_GEOMETRY);
			}

		} catch (SQLException sqlEx) {
			LOG.error("SQL error while querying the gml:id cache: " + sqlEx.getMessage());
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

		return null;
	}

	private UIDCacheEntry dbCityObjectLookup(String gmlId) {
		// init database search
		long id;
		int classId;
		ResultSet rs = null;

		try {
			psCityObjectId.setString(1, gmlId);
			rs = psCityObjectId.executeQuery();

			if (rs.next()) {
				id = rs.getLong(1);
				classId = rs.getInt(2);

				return new UIDCacheEntry(id, 0, false, gmlId, Util.classId2cityObject(classId));
			}

		} catch (SQLException sqlEx) {
			LOG.error("SQL error while querying the gml:id cache: " + sqlEx.getMessage());
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

		return null;
	}
	
	private UIDCacheEntry chacheLookup(String gmlId, UIDCacheEntry oldEntry, UIDCache cache) {
		// this is a recursive server request since we might have mapped gml:ids!
		UIDCacheEntry entry = cache.get(gmlId);

		// we get an answer and it has got some meaningful content. so we are done
		if (entry != null && entry.getId() != -1) {

			// flip reverse attribute if necessary. since we do not want to
			// change the entry in the gmlId cache we create a new one to do so
			if (oldEntry != null)
				entry = new UIDCacheEntry(
						entry.getId(),
						entry.getRootId(),
						entry.isReverse() ^ oldEntry.isReverse(),
						entry.getMapping(),
						entry.getType());

			return entry;
		}

		// if we get an answer but it just contains some mapping (be careful on how to correctly
		// mark a mapping!) we have to ask the server again. we can have long recursive chains
		// here, e.g. if we want to correctly resolve an orientable surface
		if (entry != null && entry.getId() == -1 && entry.getMapping() != null) {

			// flip reverse attribute if necessary. since we do not want to
			// change the entry in the gmlId cache we create a new one to do so
			if (oldEntry != null)
				entry = new UIDCacheEntry(
						entry.getId(),
						entry.getRootId(),
						entry.isReverse() ^ oldEntry.isReverse(),
						entry.getMapping(),
						entry.getType());
			
			if (entry.getRootId() == -1)
				entry = chacheLookup(entry.getMapping(), entry, cache);
		}

		// finally we did not get an answer on a mapping request. we return the mapping
		// to enable another query...
		if (entry == null && oldEntry != null)
			entry = oldEntry;

		return entry;
	}
	
	public void close() throws SQLException {
		psSurfaceGeometryId.close();
		psCityObjectId.close();
	}
}
