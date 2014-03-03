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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.CeilingSurface;
import org.citygml4j.model.citygml.tunnel.ClosureSurface;
import org.citygml4j.model.citygml.tunnel.FloorSurface;
import org.citygml4j.model.citygml.tunnel.GroundSurface;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.InteriorWallSurface;
import org.citygml4j.model.citygml.tunnel.OuterCeilingSurface;
import org.citygml4j.model.citygml.tunnel.OuterFloorSurface;
import org.citygml4j.model.citygml.tunnel.RoofSurface;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.tunnel.WallSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import de.tub.citydb.util.Util;

public class DBTunnelThematicSurface implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Connection connection;

	private PreparedStatement psTunnelThematicSurface;
	private PreparedStatement psTunnelInstallationThematicSurface;
	private PreparedStatement psHollowSpaceThematicSurface;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;

	public DBTunnelThematicSurface(Connection connection, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		String query = "select ID, OBJECTCLASS_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID from TUNNEL_THEMATIC_SURFACE where ";

		psTunnelThematicSurface = connection.prepareStatement(query + "TUNNEL_ID = ?");
		psTunnelInstallationThematicSurface = connection.prepareStatement(query + "TUNNEL_INSTALLATION_ID = ?");
		psHollowSpaceThematicSurface = connection.prepareStatement(query + "TUNNEL_HOLLOW_SPACE_ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public void read(AbstractTunnel tunnel, long parentId) throws SQLException {
		read((AbstractCityObject)tunnel, parentId);
	}

	public void read(TunnelInstallation tunnelInstallation, long parentId) throws SQLException {
		read((AbstractCityObject)tunnelInstallation, parentId);
	}

	public void read(IntTunnelInstallation intTunnelInstallation, long parentId) throws SQLException {
		read((AbstractCityObject)intTunnelInstallation, parentId);
	}

	public void read(HollowSpace hollowSpace, long parentId) throws SQLException {
		read((AbstractCityObject)hollowSpace, parentId);
	}

	private void read(AbstractCityObject cityObject, long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			switch (cityObject.getCityGMLClass()) {
			case TUNNEL:
			case TUNNEL_PART:
				psTunnelThematicSurface.setLong(1, parentId);
				rs = psTunnelThematicSurface.executeQuery();
				break;
			case TUNNEL_INSTALLATION:
			case INT_TUNNEL_INSTALLATION:
				psTunnelInstallationThematicSurface.setLong(1, parentId);
				rs = psTunnelInstallationThematicSurface.executeQuery();
				break;
			case HOLLOW_SPACE:
				psHollowSpaceThematicSurface.setLong(1, parentId);
				rs = psHollowSpaceThematicSurface.executeQuery();
				break;
			default:
				return;
			}

			while (rs.next()) {
				// boundarySurface
				long boundarySurfaceId = rs.getLong(1);
				AbstractBoundarySurface boundarySurface = null;

				int classId = rs.getInt(2);
				if (rs.wasNull() || classId == 0)
					continue;

				CityGMLClass type = Util.classId2cityObject(classId);
				switch (type) {
				case TUNNEL_WALL_SURFACE:
					boundarySurface = new WallSurface();
					break;
				case TUNNEL_ROOF_SURFACE:
					boundarySurface = new RoofSurface();
					break;
				case INTERIOR_TUNNEL_WALL_SURFACE:
					boundarySurface = new InteriorWallSurface();
					break;
				case TUNNEL_GROUND_SURFACE:
					boundarySurface = new GroundSurface();
					break;
				case TUNNEL_FLOOR_SURFACE:
					boundarySurface = new FloorSurface();
					break;
				case TUNNEL_CLOSURE_SURFACE:
					boundarySurface = new ClosureSurface();
					break;
				case TUNNEL_CEILING_SURFACE:
					boundarySurface = new CeilingSurface();
					break;
				case OUTER_TUNNEL_FLOOR_SURFACE:
					boundarySurface = new OuterFloorSurface();
					break;
				case OUTER_TUNNEL_CEILING_SURFACE:
					boundarySurface = new OuterCeilingSurface();
					break;
				default:
					continue;
				}

				// cityobject stuff
				cityObjectExporter.read(boundarySurface, boundarySurfaceId);

				for (int lod = 0; lod < 3; lod++) {
					long lodMultiSurfaceId = rs.getLong(3 + lod);
					if (rs.wasNull() || lodMultiSurfaceId == 0)
						continue;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodMultiSurfaceId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.getAbstractGeometry() != null)
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 0:
							boundarySurface.setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 1:
							boundarySurface.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							boundarySurface.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}

				BoundarySurfaceProperty boundarySurfaceProperty = new BoundarySurfaceProperty();
				boundarySurfaceProperty.setObject(boundarySurface);

				switch (cityObject.getCityGMLClass()) {
				case TUNNEL:
				case TUNNEL_PART:
					((AbstractTunnel)cityObject).addBoundedBySurface(boundarySurfaceProperty);
					break;
				case TUNNEL_INSTALLATION:
					((TunnelInstallation)cityObject).addBoundedBySurface(boundarySurfaceProperty);
					break;
				case INT_TUNNEL_INSTALLATION:
					((IntTunnelInstallation)cityObject).addBoundedBySurface(boundarySurfaceProperty);
					break;
				case HOLLOW_SPACE:
					((HollowSpace)cityObject).addBoundedBySurface(boundarySurfaceProperty);
					break;
				default:
					continue;
				}
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTunnelThematicSurface.close();
		psTunnelInstallationThematicSurface.close();
		psHollowSpaceThematicSurface.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TUNNEL_THEMATIC_SURFACE;
	}

}
