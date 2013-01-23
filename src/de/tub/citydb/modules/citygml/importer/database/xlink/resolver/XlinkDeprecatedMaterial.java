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
import java.sql.SQLException;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkDeprecatedMaterial;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencerEnum;

public class XlinkDeprecatedMaterial implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSurfaceData;
	private PreparedStatement psTextureParam;
	
	private int batchCounter;

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
		
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			executeBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psSurfaceData.executeBatch();
		psTextureParam.executeBatch();
		batchCounter = 0;
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
