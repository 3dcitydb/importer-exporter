/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface;
import org.citygml4j.model.citygml.tunnel.AbstractOpening;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.tunnel.CeilingSurface;
import org.citygml4j.model.citygml.tunnel.ClosureSurface;
import org.citygml4j.model.citygml.tunnel.Door;
import org.citygml4j.model.citygml.tunnel.FloorSurface;
import org.citygml4j.model.citygml.tunnel.GroundSurface;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.InteriorWallSurface;
import org.citygml4j.model.citygml.tunnel.OpeningProperty;
import org.citygml4j.model.citygml.tunnel.OuterCeilingSurface;
import org.citygml4j.model.citygml.tunnel.OuterFloorSurface;
import org.citygml4j.model.citygml.tunnel.RoofSurface;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.tunnel.WallSurface;
import org.citygml4j.model.citygml.tunnel.Window;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBTunnelThematicSurface implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTunnelThematicSurface;
	private PreparedStatement psTunnelInstallationThematicSurface;
	private PreparedStatement psHollowSpaceThematicSurface;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;

	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private String gmlIdPrefix;
	private String infoSys;

	public DBTunnelThematicSurface(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();

		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			keepOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetKeepGmlIdAsExternalReference();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
			infoSys = config.getInternal().getExportFileName();
		}	

		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select ts.ID as TSID, ts.OBJECTCLASS_ID, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, ")
			.append("op.ID as OPID, op.OBJECTCLASS_ID as OPOBJECTCLASS_ID, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, ")
			.append("op.LOD3_IMPLICIT_REP_ID, op.LOD4_IMPLICIT_REP_ID, op.LOD3_IMPLICIT_REF_POINT, op.LOD4_IMPLICIT_REF_POINT, op.LOD3_IMPLICIT_TRANSFORMATION, op.LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from TUNNEL_THEMATIC_SURFACE ts left join TUNNEL_OPEN_TO_THEM_SRF o2t on ts.ID = o2t.TUNNEL_THEMATIC_SURFACE_ID left join TUNNEL_OPENING op on op.ID = o2t.TUNNEL_OPENING_ID where ");

			psTunnelThematicSurface = connection.prepareStatement(query.toString() + "ts.TUNNEL_ID = ?");
			psTunnelInstallationThematicSurface = connection.prepareStatement(query.toString() + "ts.TUNNEL_INSTALLATION_ID = ?");
			psHollowSpaceThematicSurface = connection.prepareStatement(query.toString() + "ts.TUNNEL_HOLLOW_SPACE_ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select ts.ID as TSID, ts.OBJECTCLASS_ID, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, ")
			.append("op.ID as OPID, op.OBJECTCLASS_ID as OPOBJECTCLASS_ID, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, ")
			.append("op.LOD3_IMPLICIT_REP_ID, op.LOD4_IMPLICIT_REP_ID, ")
			.append(transformOrNull).append("(LOD3_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD3_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD4_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD4_IMPLICIT_REF_POINT, ")
			.append("op.LOD3_IMPLICIT_TRANSFORMATION, op.LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from TUNNEL_THEMATIC_SURFACE ts left join TUNNEL_OPEN_TO_THEM_SRF o2t on ts.ID = o2t.TUNNEL_THEMATIC_SURFACE_ID left join TUNNEL_OPENING op on op.ID = o2t.TUNNEL_OPENING_ID where ");

			psTunnelThematicSurface = connection.prepareStatement(query.toString() + "ts.TUNNEL_ID = ?");
			psTunnelInstallationThematicSurface = connection.prepareStatement(query.toString() + "ts.TUNNEL_INSTALLATION_ID = ?");
			psHollowSpaceThematicSurface = connection.prepareStatement(query.toString() + "ts.TUNNEL_HOLLOW_SPACE_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
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
		final List<Long> boundarySurfaceIds = new ArrayList<Long>();
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

			long currentBoundarySurfaceId = 0;
			AbstractBoundarySurface boundarySurface = null;

			while (rs.next()) {
				// boundarySurface
				long boundarySurfaceId = rs.getLong(1);

				if (boundarySurfaceId != currentBoundarySurfaceId) {
					currentBoundarySurfaceId = boundarySurfaceId;

					int index = boundarySurfaceIds.indexOf(boundarySurfaceId);
					if (index == -1) {
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

						boundarySurfaceIds.add(boundarySurfaceId);
					} else {
						switch (cityObject.getCityGMLClass()) {
						case TUNNEL:
						case TUNNEL_PART:
							boundarySurface = ((AbstractTunnel)cityObject).getBoundedBySurface().get(index).getBoundarySurface();
							break;
						case TUNNEL_INSTALLATION:
							boundarySurface = ((TunnelInstallation)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						case INT_TUNNEL_INSTALLATION:
							boundarySurface = ((IntTunnelInstallation)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						case HOLLOW_SPACE:
							boundarySurface = ((HollowSpace)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						default:
							continue;
						}
					}
				}

				// continue if we could not interpret the boundary surface
				if (boundarySurface == null)
					continue;

				long openingId = rs.getLong(6);
				if (rs.wasNull())
					continue;

				// create new opening object
				AbstractOpening opening = null;
				int classId = rs.getInt(7);
				if (rs.wasNull() || classId == 0)
					continue;

				CityGMLClass type = Util.classId2cityObject(classId);
				switch (type) {
				case TUNNEL_WINDOW:
					opening = new Window();
					break;
				case TUNNEL_DOOR:
					opening = new Door();
					break;
				default:
					continue;
				}

				// cityobject stuff
				cityObjectExporter.read(opening, openingId);

				if (opening.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(opening.getId(), openingId, CityGMLClass.ABSTRACT_TUNNEL_OPENING)) {
						if (useXLink) {
							OpeningProperty openingProperty = new OpeningProperty();
							openingProperty.setHref("#" + opening.getId());
							boundarySurface.addOpening(openingProperty);
							continue;
						} else {
							String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + opening.getId();

							if (keepOldGmlId) {
								ExternalReference externalReference = new ExternalReference();
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = new ExternalObject();
								externalObject.setName(opening.getId());

								externalReference.setExternalObject(externalObject);
								opening.addExternalReference(externalReference);
							}

							opening.setId(newGmlId);	
						}	
					}
				}

				for (int lod = 0; lod < 2; lod++) {
					long lodMultiSurfaceId = rs.getLong(8 + lod);
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
							opening.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 1:
							opening.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}

				for (int lod = 0; lod < 2; lod++) {
					// get implicit geometry details
					long implicitGeometryId = rs.getLong(10 + lod);
					if (rs.wasNull())
						continue;

					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(13 + lod);
					if (!rs.wasNull() && referencePointObj != null)
						referencePoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(15 + lod);

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 0:
							opening.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 1:
							opening.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}

				OpeningProperty openingProperty = new OpeningProperty();
				openingProperty.setObject(opening);
				boundarySurface.addOpening(openingProperty);
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
