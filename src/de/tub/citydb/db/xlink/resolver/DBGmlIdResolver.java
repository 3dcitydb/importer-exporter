package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerManager;
import de.tub.citydb.db.gmlId.GmlIdEntry;
import de.tub.citydb.db.gmlId.GmlIdLookupServer;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBGmlIdResolver {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection commitConn;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final Config config;

	private PreparedStatement psSurfaceGeometryId;
	private PreparedStatement psCityObjectId;
	private String gmlIdCodespace;

	public DBGmlIdResolver(Connection commitConn, DBGmlIdLookupServerManager lookupServerManager, Config config) throws SQLException {
		this.commitConn = commitConn;
		this.lookupServerManager = lookupServerManager;
		this.config = config;

		init();
	}

	private void init() throws SQLException {
		gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();

		String sqlGeometryFromClause = "from SURFACE_GEOMETRY where ROOT_ID=? and GMLID=?";
		if (gmlIdCodespace != null && gmlIdCodespace.length() != 0)
			sqlGeometryFromClause += " and GMLID_CODESPACE='" + gmlIdCodespace + "'";
		
		String sqlCityObjectFromClause = "from CITYOBJECT where GMLID=?";
		if (gmlIdCodespace != null && gmlIdCodespace.length() != 0)
			sqlCityObjectFromClause += " and GMLID_CODESPACE='" + gmlIdCodespace + "'";

		psSurfaceGeometryId = commitConn.prepareStatement("select ID " + sqlGeometryFromClause);
		psCityObjectId = commitConn.prepareStatement("select ID, CLASS_ID " + sqlCityObjectFromClause);
	}
	
	public GmlIdEntry getDBId(String gmlId, CityGMLClass type, boolean forceCityObjectDatabaseLookup) {
		GmlIdLookupServer server = lookupServerManager.getLookupServer(type);
		if (server == null)
			return null;

		// replace leading #
		gmlId = gmlId.replaceAll("^#", "");
		GmlIdEntry entry = serverLookup(gmlId, null, server);

		if (entry == null || entry.getId() == -1) {
			
			if (type == CityGMLClass.GMLGEOMETRY) {
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

	private GmlIdEntry dbGeometryLookup(GmlIdEntry entry) {
		// init database search
		long id;
		ResultSet rs = null;

		try {
			psSurfaceGeometryId.setLong(1, entry.getRootId());
			psSurfaceGeometryId.setString(2, entry.getMapping());
			rs = psSurfaceGeometryId.executeQuery();

			if (rs.next()) {
				id = rs.getLong(1);
				
				return new GmlIdEntry(id, entry.getRootId(), entry.isReverse(), entry.getMapping(), CityGMLClass.GMLGEOMETRY);
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

	private GmlIdEntry dbCityObjectLookup(String gmlId) {
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

				return new GmlIdEntry(id, 0, false, gmlId, Util.classId2cityObject(classId));
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
	
	private GmlIdEntry serverLookup(String gmlId, GmlIdEntry oldEntry, GmlIdLookupServer lookupServer) {
		// this is a recursive server request since we might have mapped gml:ids!
		GmlIdEntry entry = lookupServer.get(gmlId);

		// we get an answer and it has got some meaningful content. so we are done
		if (entry != null && entry.getId() != -1) {

			// flip reverse attribute if necessary. since we do not want to
			// change the entry in the gmlId cache we create a new one to do so
			if (oldEntry != null)
				entry = new GmlIdEntry(
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
				entry = new GmlIdEntry(
						entry.getId(),
						entry.getRootId(),
						entry.isReverse() ^ oldEntry.isReverse(),
						entry.getMapping(),
						entry.getType());
			
			if (entry.getRootId() == -1)
				entry = serverLookup(entry.getMapping(), entry, lookupServer);
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
