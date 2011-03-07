package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.tub.citydb.db.cache.GmlIdEntry;
import de.tub.citydb.db.temp.DBTempHeapTable;
import de.tub.citydb.db.xlink.DBXlinkTextureAssociation;
import de.tub.citydb.db.xlink.DBXlinkTextureParam;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.CityGMLClass;

public class XlinkTexCoordList implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBTempHeapTable textureParamHeapView;
	private final DBTempHeapTable linearRingHeapView;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psTexCoordList;
	private PreparedStatement psSelectLinearRing;
	private PreparedStatement psSelectInteriorLinearRing;
	private PreparedStatement psSelectTexCoord;

	public XlinkTexCoordList(Connection batchConn, DBTempHeapTable textureParamHeapView, DBTempHeapTable linearRingHeapView, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.textureParamHeapView = textureParamHeapView;
		this.linearRingHeapView = linearRingHeapView;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psTexCoordList = batchConn.prepareStatement("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE , TEXTURE_COORDINATES, SURFACE_DATA_ID) values " +
			"(?, 1, null, ?, ?)");

		Connection linearRingConn = linearRingHeapView.getConnection();
		String linearRingTableName = linearRingHeapView.getTableName();
		
		psSelectLinearRing = linearRingConn.prepareStatement("select RING_NO, PARENT_GMLID from " + linearRingTableName + " where GMLID=?");
		psSelectInteriorLinearRing = linearRingConn.prepareStatement("select GMLID, RING_NO from " + linearRingTableName +
				" where PARENT_GMLID=? and RING_NO<>0");
		psSelectTexCoord = textureParamHeapView.getConnection().prepareStatement("select GMLID, TEXTURE_COORDINATES from " + textureParamHeapView.getTableName() +
					" where TEXCOORDLIST_ID=? and not GMLID=?");
	}


	public boolean insert(DBXlinkTextureParam xlink) throws SQLException {
		String gmlId = xlink.getGmlId();
		ResultSet rs = null;

		// check whether we deal with a local gml:id
		// remote gml:ids are not supported so far...
		if (Util.isRemoteXlink(gmlId))
			return false;

		// replace leading #
		gmlId = gmlId.replaceAll("^#", "");

		try {
			// step 1: get the exterior linear ring element
			psSelectLinearRing.setString(1, gmlId);
			rs = psSelectLinearRing.executeQuery();

			if (!rs.next())
				return false;

			// if an interior ring is returned we silently discard it
			int exteriorRing = rs.getInt("RING_NO");
			if (exteriorRing != 0)
				return true;
			
			String parentGmlId = rs.getString("PARENT_GMLID");
			rs.close();

			// step 2: check whether parent geometry exists... we need to do this
			// since we require the database key for referencing
			GmlIdEntry surfaceGeometryEntry = resolverManager.getDBId(parentGmlId, CityGMLClass.GMLGEOMETRY);
			if (surfaceGeometryEntry == null || surfaceGeometryEntry.getId() == -1) {
				LogMessageEvent log = new LogMessageEvent(
						"Der gml:LinearRing \"" + gmlId + "\" konnte keinem Geometrieobjekt zugeordnet werden.",
						LogMessageEnum.ERROR);
				resolverManager.propagateEvent(log);

				return false;
			}

			// step 3: find all corresponding interior rings
			psSelectInteriorLinearRing.setString(1, parentGmlId);
			rs = psSelectInteriorLinearRing.executeQuery();

			HashMap<String, Integer> innerRingMap = new HashMap<String, Integer>();
			int maxRingNo = 0;
			while (rs.next()) {
				String innerGmlId = rs.getString("GMLID");
				if (innerGmlId == null)
					innerGmlId = parentGmlId;

				int ringNo = rs.getInt("RING_NO");

				innerRingMap.put(innerGmlId, ringNo);
				if (ringNo > maxRingNo)
					maxRingNo = ringNo;
			}

			rs.close();

			// step 4: find corresponding texture coordinates
			List<String> texCoordList = new ArrayList<String>();
			texCoordList.add(0, xlink.getTextureCoord());
			for (int i = 0; i < maxRingNo; i++)
				texCoordList.add("");

			psSelectTexCoord.setString(1, xlink.getTexCoordListId());
			psSelectTexCoord.setString(2, xlink.getGmlId());
			rs = psSelectTexCoord.executeQuery();

			while (rs.next()) {
				String innerGmlId = rs.getString("GMLID");
				String textureCoordinates = rs.getString("TEXTURE_COORDINATES");

				if (Util.isRemoteXlink(innerGmlId))
					continue;

				// replace leading #
				innerGmlId = innerGmlId.replaceAll("^#", "");
				if (innerRingMap.containsKey(innerGmlId))
					texCoordList.set(innerRingMap.get(innerGmlId), textureCoordinates);
			}

			// step 5: sanity check
			String texCoord = Util.collection2string(texCoordList, ";");
			if (texCoord.contains(";;") || texCoord.endsWith(";")) {
				LogMessageEvent log = new LogMessageEvent(
						"Fehlende Texturkoordinaten für Geometrieobjekt '" + parentGmlId + "'.",
						LogMessageEnum.WARN);
				resolverManager.propagateEvent(log);
			}

			psTexCoordList.setLong(1, surfaceGeometryEntry.getId());
			psTexCoordList.setString(2, texCoord);
			psTexCoordList.setLong(3, xlink.getId());

			psTexCoordList.addBatch();

			if (xlink.getTexParamGmlId() != null) {
				// propagate xlink...
				resolverManager.propagateXlink(new DBXlinkTextureAssociation(
						xlink.getId(),
						surfaceGeometryEntry.getId(),
						xlink.getTexParamGmlId()));
			}

			return true;
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

	@Override
	public void executeBatch() throws SQLException {
		psTexCoordList.executeBatch();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXCOORDLIST;
	}

}
