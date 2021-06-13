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

import org.citydb.operation.common.cache.IdCacheEntry;
import org.citydb.operation.common.xlink.DBXlinkDeprecatedMaterial;
import org.citydb.database.schema.SequenceEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class XlinkDeprecatedMaterial implements DBXlinkResolver {
	private final DBXlinkResolverManager manager;
	private final PreparedStatement psSurfaceData;
	private final PreparedStatement psTextureParam;

	private int batchCounter;

	public XlinkDeprecatedMaterial(Connection batchConn, DBXlinkResolverManager manager) throws SQLException {
		this.manager = manager;
		String schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();

		psSurfaceData = batchConn.prepareStatement("insert into " + schema + ".SURFACE_DATA (select ?, GMLID, " +
				"GMLID_CODESPACE, NAME, NAME_CODESPACE, DESCRIPTION, IS_FRONT, TYPE, X3D_SHININESS, X3D_TRANSPARENCY, " +
				"X3D_AMBIENT_INTENSITY, X3D_SPECULAR_COLOR, X3D_DIFFUSE_COLOR, X3D_EMISSIVE_COLOR, X3D_IS_SMOOTH, " +
				"TEX_IMAGE_URI, TEX_IMAGE, TEX_MIME_TYPE, TEX_TEXTURE_TYPE, TEX_WRAP_MODE, TEX_BORDER_COLOR, " +
				"GT_PREFER_WORLDFILE, GT_ORIENTATION, GT_REFERENCE_POINT from " + schema + ".SURFACE_DATA where ID=?)");

		psTextureParam = batchConn.prepareStatement("insert into " + schema + ".TEXTUREPARAM (select ?, " +
				"IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, ? " +
				"from " + schema + ".TEXTUREPARAM where SURFACE_DATA_ID=?)");
	}

	public boolean insert(DBXlinkDeprecatedMaterial xlink) throws SQLException {
		IdCacheEntry surfaceDataEntry = manager.getObjectId(xlink.getGmlId());
		if (surfaceDataEntry == null || surfaceDataEntry.getId() == -1)
			return false;

		long newSurfaceDataId = manager.getDBId(SequenceEnum.SURFACE_DATA_ID_SEQ.getName());

		psSurfaceData.setLong(1, newSurfaceDataId);
		psSurfaceData.setLong(2, surfaceDataEntry.getId());
		psSurfaceData.addBatch();

		psTextureParam.setLong(1, xlink.getSurfaceGeometryId());
		psTextureParam.setLong(2, newSurfaceDataId);
		psTextureParam.setLong(3, surfaceDataEntry.getId());
		psTextureParam.addBatch();
		
		if (++batchCounter == manager.getDatabaseAdapter().getMaxBatchSize())
			manager.executeBatch(this);

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
