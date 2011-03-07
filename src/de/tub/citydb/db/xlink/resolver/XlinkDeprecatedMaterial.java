package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.db.gmlId.GmlIdEntry;
import de.tub.citydb.db.importer.DBSequencerEnum;
import de.tub.citydb.db.xlink.DBXlinkDeprecatedMaterial;

public class XlinkDeprecatedMaterial implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSurfaceData;
	private PreparedStatement psTextureParam;

	public XlinkDeprecatedMaterial(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psSurfaceData = batchConn.prepareStatement("insert into SURFACE_DATA (select ?, GMLID, GMLID_CODESPACE, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, TYPE, " +
				"X3D_SHININESS, X3D_TRANSPARENCY, X3D_AMBIENT_INTENSITY, X3D_SPECULAR_COLOR, X3D_DIFFUSE_COLOR, X3D_EMISSIVE_COLOR, X3D_IS_SMOOTH, " +
				"TEX_IMAGE_URI, TEX_IMAGE, TEX_MIME_TYPE, TEX_TEXTURE_TYPE, TEX_WRAP_MODE, TEX_BORDER_COLOR, " +
				"GT_PREFER_WORLDFILE, GT_ORIENTATION, GT_REFERENCE_POINT from SURFACE_DATA where ID=?)");

		psTextureParam = batchConn.prepareStatement("insert into TEXTUREPARAM (select ?, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, ? from TEXTUREPARAM where SURFACE_DATA_ID=?)");
	}

	public boolean insert(DBXlinkDeprecatedMaterial xlink) throws SQLException {
		GmlIdEntry surfaceDataEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.APPEARANCE);
		if (surfaceDataEntry == null || surfaceDataEntry.getId() == -1)
			return false;

		long newSurfaceDataId = resolverManager.getDBId(DBSequencerEnum.SURFACE_DATA_SEQ);

		psSurfaceData.setLong(1, newSurfaceDataId);
		psSurfaceData.setLong(2, surfaceDataEntry.getId());
		psSurfaceData.addBatch();

		psTextureParam.setLong(1, xlink.getSurfaceGeometryId());
		psTextureParam.setLong(2, newSurfaceDataId);
		psTextureParam.setLong(3, surfaceDataEntry.getId());
		psTextureParam.addBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psSurfaceData.executeBatch();
		psTextureParam.executeBatch();
	}

	@Override
	public void close() throws SQLException {
		psSurfaceData.close();
		psTextureParam.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.XLINK_DEPRECATED_MATERIAL;
	}

}
