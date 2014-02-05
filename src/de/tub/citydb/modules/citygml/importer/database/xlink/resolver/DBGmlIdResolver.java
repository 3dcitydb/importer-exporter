/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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

import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCache;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import de.tub.citydb.util.Util;

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
