package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.db.cache.HeapCacheTable;
import de.tub.citydb.db.gmlId.GmlIdEntry;
import de.tub.citydb.db.xlink.DBXlinkTextureAssociation;
import de.tub.citydb.db.xlink.DBXlinkTextureParam;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class XlinkTextureAssociation implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final HeapCacheTable heapTable;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psTextureParam;
	private PreparedStatement psSelectParts;
	private PreparedStatement psSelectContent;

	public XlinkTextureAssociation(Connection batchConn, HeapCacheTable heapTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.heapTable = heapTable;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psTextureParam = batchConn.prepareStatement("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, SURFACE_DATA_ID) values " +
			"(?, 1, ?, ?, ?)");
		
		psSelectParts = heapTable.getConnection().prepareStatement("select SURFACE_DATA_ID, SURFACE_GEOMETRY_ID from " + heapTable.getTableName() + " where GMLID=?");
		psSelectContent = batchConn.prepareStatement("select WORLD_TO_TEXTURE, TEXTURE_COORDINATES from TEXTUREPARAM where SURFACE_DATA_ID=? " +
				"and SURFACE_GEOMETRY_ID=?");
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
			// one gml:id pointing to a texture association may consist of
			// different parts. so firstly retrieve these parts
			psSelectParts.setString(1, gmlId);
			rs = psSelectParts.executeQuery();

			List<DBXlinkTextureAssociation> texAssList = new ArrayList<DBXlinkTextureAssociation>();
			while (rs.next()) {
				long surfaceDataId = rs.getLong("SURFACE_DATA_ID");
				long surfaceGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");

				texAssList.add(new DBXlinkTextureAssociation(surfaceDataId, surfaceGeometryId, null));
			}

			rs.close();

			for (DBXlinkTextureAssociation texAss : texAssList) {
				psSelectContent.setLong(1, texAss.getSurfaceDataId());
				psSelectContent.setLong(2, texAss.getSurfaceGeometryId());
				rs = psSelectContent.executeQuery();

				if (rs.next()) {
					String worldToTexture = rs.getString("WORLD_TO_TEXTURE");
					String texCoord = rs.getString("TEXTURE_COORDINATES");

					if (worldToTexture != null) {
						GmlIdEntry idEntry = resolverManager.getDBId(xlink.getTargetURI(), CityGMLClass.GMLGEOMETRY);

						if (idEntry == null || idEntry.getId() == -1) {
							LOG.error("Failed to resolve XLink reference '" + xlink.getTargetURI() + "'.");
							continue;
						}

						psTextureParam.setLong(1, idEntry.getId());
						psTextureParam.setString(2, worldToTexture);
						psTextureParam.setNull(3, Types.VARCHAR);
						psTextureParam.setLong(4, xlink.getId());

					} else {
						// ok, if we deal with texture coordinates we can ignore the
						// uri attribute of the <target> element. it must be the same as
						// in the referenced <target> element. so we let the new entry point
						// to the same surface_geometry entry...

						psTextureParam.setLong(1, texAss.getSurfaceGeometryId());
						psTextureParam.setNull(2, Types.VARCHAR);
						psTextureParam.setString(3, texCoord);
						psTextureParam.setLong(4, xlink.getId());
					}

					psTextureParam.addBatch();

				} else {
					LOG.warn("Failed to completely resolve XLink reference '" + gmlId + "' to TextureAssociation.");
				}

				rs.close();
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
	}

	@Override
	public void close() throws SQLException {
		psTextureParam.close();
		psSelectParts.close();
		psSelectContent.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.XLINK_TEXTUREASSOCIATION;
	}

}
