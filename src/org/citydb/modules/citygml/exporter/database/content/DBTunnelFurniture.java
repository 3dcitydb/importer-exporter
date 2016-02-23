/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBTunnelFurniture implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTunnelFurniture;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBOtherGeometry geometryExporter;

	public DBTunnelFurniture(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
			.append("LOD4_BREP_ID, LOD4_OTHER_GEOM, ")
			.append("LOD4_IMPLICIT_REP_ID, LOD4_IMPLICIT_REF_POINT, LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from TUNNEL_FURNITURE where TUNNEL_HOLLOW_SPACE_ID = ?");
			psTunnelFurniture = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
			.append("LOD4_BREP_ID, ")
			.append(transformOrNull).append("(LOD4_OTHER_GEOM, ").append(srid).append(") AS LOD4_OTHER_GEOM, ")
			.append("LOD4_IMPLICIT_REP_ID, ")
			.append(transformOrNull).append("(LOD4_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from TUNNEL_FURNITURE where TUNNEL_HOLLOW_SPACE_ID = ?");
			psTunnelFurniture = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public void read(HollowSpace hollowSpace, long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			psTunnelFurniture.setLong(1, parentId);
			rs = psTunnelFurniture.executeQuery();

			while (rs.next()) {
				long tunnelFurnitureId = rs.getLong(1);
				TunnelFurniture hollowSpaceFurniture = new TunnelFurniture();

				String clazz = rs.getString(2);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(3));
					hollowSpaceFurniture.setClazz(code);
				}

				String function = rs.getString(4);
				String functionCodeSpace = rs.getString(5);
				if (function != null)
					hollowSpaceFurniture.setFunction(Util.string2codeList(function, functionCodeSpace));

				String usage = rs.getString(6);
				String usageCodeSpace = rs.getString(7);
				if (usage != null)
					hollowSpaceFurniture.setUsage(Util.string2codeList(usage, usageCodeSpace));

				// geometry
				long surfaceGeometryId = rs.getLong(8);
				Object geomObj = rs.getObject(9);
				if (surfaceGeometryId != 0 || geomObj != null) {
					GeometryProperty<AbstractGeometry> geometryProperty = null;
					if (surfaceGeometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());
						}
					} else {
						GeometryObject geometry = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(geomObj);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							geometryProperty.setGeometry(geometryExporter.getPointOrCurveGeometry(geometry, true));
						}	
					}

					if (geometryProperty != null)
						hollowSpaceFurniture.setLod4Geometry(geometryProperty);
				}

				// implicit geometry
				long implicitGeometryId = rs.getLong(10);
				if (implicitGeometryId != 0) {
					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(11);
					if (!rs.wasNull() && referencePointObj != null)
						referencePoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(12);

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);
						hollowSpaceFurniture.setLod4ImplicitRepresentation(implicitProperty);
					}
				}

				// cityObject stuff
				cityObjectExporter.read(hollowSpaceFurniture, tunnelFurnitureId);

				InteriorFurnitureProperty tunnelFurnitureProp = new InteriorFurnitureProperty();
				tunnelFurnitureProp.setObject(hollowSpaceFurniture);
				hollowSpace.addInteriorFurniture(tunnelFurnitureProp);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTunnelFurniture.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TUNNEL_FURNITURE;
	}

}
