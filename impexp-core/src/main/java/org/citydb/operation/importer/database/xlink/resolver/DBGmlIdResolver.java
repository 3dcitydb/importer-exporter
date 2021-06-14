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
package org.citydb.operation.importer.database.xlink.resolver;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.log.Logger;
import org.citydb.operation.common.cache.IdCache;
import org.citydb.operation.common.cache.IdCacheEntry;
import org.citydb.operation.common.cache.IdCacheManager;
import org.citydb.operation.common.cache.IdCacheType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBGmlIdResolver {
	private final Logger log = Logger.getInstance();
	private final IdCacheManager idCacheManager;
	private final PreparedStatement psSurfaceGeometryId;
	private final PreparedStatement psCityObjectId;
	
	public DBGmlIdResolver(Connection connection, AbstractDatabaseAdapter databaseAdapter, IdCacheManager idCacheManager) throws SQLException {
		this.idCacheManager = idCacheManager;
		String schema = databaseAdapter.getConnectionDetails().getSchema();
		psSurfaceGeometryId = connection.prepareStatement("select ID from " + schema + ".SURFACE_GEOMETRY where ROOT_ID=? and GMLID=?");
		psCityObjectId = connection.prepareStatement("select ID, OBJECTCLASS_ID from " + schema + ".CITYOBJECT where GMLID=?");
	}
	
	public IdCacheEntry getDBId(String gmlId, IdCacheType type, boolean forceCityObjectDatabaseLookup) {
		IdCache cache = idCacheManager.getCache(type);
		if (cache == null)
			return null;

		// replace leading #
		gmlId = gmlId.replaceAll("^#", "");
		IdCacheEntry entry = cacheLookup(gmlId, null, cache);

		if (entry == null || entry.getId() == -1) {
			try {
				if (type == IdCacheType.GEOMETRY) {
					if (entry == null)
						return null;

					entry = geometryLookup(entry);
				} else if (forceCityObjectDatabaseLookup) {
					if (entry != null)
						gmlId = entry.getMapping();

					entry = cityObjectLookup(gmlId);
				}
			} catch (SQLException e) {
				log.error("SQL error while querying the gml:id cache.", e);
			}
		}

		return entry;
	}

	private IdCacheEntry geometryLookup(IdCacheEntry entry) throws SQLException {
		psSurfaceGeometryId.setLong(1, entry.getRootId());
		psSurfaceGeometryId.setString(2, entry.getMapping());

		try (ResultSet rs = psSurfaceGeometryId.executeQuery()) {
			return rs.next() ?
					new IdCacheEntry(rs.getLong(1), entry.getRootId(), entry.isReverse(), entry.getMapping()) :
					null;
		}
	}

	private IdCacheEntry cityObjectLookup(String gmlId) throws SQLException {
		psCityObjectId.setString(1, gmlId);

		try (ResultSet rs = psCityObjectId.executeQuery()) {
			return rs.next() ?
					new IdCacheEntry(rs.getLong(1), 0, false, gmlId, rs.getInt(2)) :
					null;
		}
	}
	
	private IdCacheEntry cacheLookup(String gmlId, IdCacheEntry oldEntry, IdCache cache) {
		// this is a recursive server request since we might have mapped gml:ids!
		IdCacheEntry entry = cache.get(gmlId);

		// we get an answer and it has got some meaningful content. so we are done
		if (entry != null && entry.getId() != -1) {

			// flip reverse attribute if necessary. since we do not want to
			// change the entry in the gmlId cache we create a new one to do so
			if (oldEntry != null)
				entry = new IdCacheEntry(
						entry.getId(),
						entry.getRootId(),
						entry.isReverse() ^ oldEntry.isReverse(),
						entry.getMapping(),
						entry.getObjectClassId());

			return entry;
		}

		// if we get an answer but it just contains some mapping (be careful on how to correctly
		// mark a mapping!) we have to ask the server again. we can have long recursive chains
		// here, e.g. if we want to correctly resolve an orientable surface
		if (entry != null && entry.getId() == -1 && entry.getMapping() != null) {

			// flip reverse attribute if necessary. since we do not want to
			// change the entry in the gmlId cache we create a new one to do so
			if (oldEntry != null)
				entry = new IdCacheEntry(
						entry.getId(),
						entry.getRootId(),
						entry.isReverse() ^ oldEntry.isReverse(),
						entry.getMapping(),
						entry.getObjectClassId());
			
			if (entry.getRootId() == -1)
				entry = cacheLookup(entry.getMapping(), entry, cache);
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
