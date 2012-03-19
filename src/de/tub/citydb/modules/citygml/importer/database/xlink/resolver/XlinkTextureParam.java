/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParamEnum;

public class XlinkTextureParam implements DBXlinkResolver {
	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psTextureParam;
	private int batchCounter;

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
		GmlIdEntry surfaceGeometryEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.ABSTRACT_GML_GEOMETRY);
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
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			executeBatch();

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
		batchCounter = 0;
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
