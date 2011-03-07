package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.db.gmlId.GmlIdEntry;
import de.tub.citydb.db.xlink.DBXlinkTextureAssociation;
import de.tub.citydb.db.xlink.DBXlinkTextureParam;
import de.tub.citydb.db.xlink.DBXlinkTextureParamEnum;

public class XlinkTextureParam implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psTextureParam;

	public XlinkTextureParam(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psTextureParam = batchConn.prepareStatement("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, SURFACE_DATA_ID) values " +
			"(?, ?, ?, null, ?)");
	}

	public boolean insert(DBXlinkTextureParam xlink) throws SQLException {
		GmlIdEntry surfaceGeometryEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.GMLGEOMETRY);
		if (surfaceGeometryEntry == null || surfaceGeometryEntry.getId() == -1)
			return false;

		psTextureParam.setLong(1, surfaceGeometryEntry.getId());
		psTextureParam.setLong(4, xlink.getId());

		if (xlink.isTextureParameterization())
			psTextureParam.setInt(2, 1);
		else
			psTextureParam.setInt(2, 0);

		if (xlink.getWorldToTexture() != null && xlink.getWorldToTexture().length() != 0)
			psTextureParam.setString(3, xlink.getWorldToTexture());
		else
			psTextureParam.setNull(3, Types.VARCHAR);

		psTextureParam.addBatch();

		if (xlink.getType() == DBXlinkTextureParamEnum.TEXCOORDGEN && xlink.getTexParamGmlId() != null) {
			// propagate xlink...
			resolverManager.propagateXlink(new DBXlinkTextureAssociation(
					xlink.getId(),
					surfaceGeometryEntry.getId(),
					xlink.getTexParamGmlId()));
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
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXTUREPARAM;
	}

}
