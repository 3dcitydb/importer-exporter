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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.project.exporter.AddressMode;
import org.citydb.modules.citygml.common.xal.AddressExportFactory;
import org.citydb.modules.citygml.common.xal.AddressObject;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.CeilingSurface;
import org.citygml4j.model.citygml.building.ClosureSurface;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.FloorSurface;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.InteriorWallSurface;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.OuterCeilingSurface;
import org.citygml4j.model.citygml.building.OuterFloorSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.building.Window;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBThematicSurface implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBuildingThematicSurface;
	private PreparedStatement psBuildingInstallationThematicSurface;
	private PreparedStatement psRoomThematicSurface;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBOtherGeometry geometryExporter;

	private boolean handleAddressGmlId;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private String gmlIdPrefix;
	private String infoSys;

	public DBThematicSurface(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		handleAddressGmlId = dbExporterManager.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
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
					.append("op.ID as OPID, op.OBJECTCLASS_ID as OPOBJECTCLASS_ID, op.ADDRESS_ID, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, ")
					.append("op.LOD3_IMPLICIT_REP_ID, op.LOD4_IMPLICIT_REP_ID, op.LOD3_IMPLICIT_REF_POINT, op.LOD4_IMPLICIT_REF_POINT, op.LOD3_IMPLICIT_TRANSFORMATION, op.LOD4_IMPLICIT_TRANSFORMATION, ")
					.append("a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT, a.XAL_SOURCE").append(handleAddressGmlId ? ", a.GMLID " : " ")
					.append("from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ");

			psBuildingThematicSurface = connection.prepareStatement(query.toString() + "ts.BUILDING_ID = ?");
			psBuildingInstallationThematicSurface = connection.prepareStatement(query.toString() + "ts.BUILDING_INSTALLATION_ID = ?");
			psRoomThematicSurface = connection.prepareStatement(query.toString() + "ts.ROOM_ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
					.append("select ts.ID as TSID, ts.OBJECTCLASS_ID, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, ")
					.append("op.ID as OPID, op.OBJECTCLASS_ID as OPOBJECTCLASS_ID, op.ADDRESS_ID, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, ")
					.append("op.LOD3_IMPLICIT_REP_ID, op.LOD4_IMPLICIT_REP_ID, ")
					.append(transformOrNull).append("(LOD3_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD3_IMPLICIT_REF_POINT, ")
					.append(transformOrNull).append("(LOD4_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD4_IMPLICIT_REF_POINT, ")
					.append("op.LOD3_IMPLICIT_TRANSFORMATION, op.LOD4_IMPLICIT_TRANSFORMATION, ")
					.append("a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, ")
					.append(transformOrNull).append("(a.MULTI_POINT, ").append(srid).append(") AS MULTI_POINT, a.XAL_SOURCE").append(handleAddressGmlId ? ", a.GMLID " : " ")
					.append("from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ");

			psBuildingThematicSurface = connection.prepareStatement(query.toString() + "ts.BUILDING_ID = ?");
			psBuildingInstallationThematicSurface = connection.prepareStatement(query.toString() + "ts.BUILDING_INSTALLATION_ID = ?");
			psRoomThematicSurface = connection.prepareStatement(query.toString() + "ts.ROOM_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public void read(AbstractBuilding building, long parentId) throws SQLException {
		read((AbstractCityObject)building, parentId);
	}

	public void read(BuildingInstallation buildingInstallation, long parentId) throws SQLException {
		read((AbstractCityObject)buildingInstallation, parentId);
	}

	public void read(IntBuildingInstallation intBuildingInstallation, long parentId) throws SQLException {
		read((AbstractCityObject)intBuildingInstallation, parentId);
	}

	public void read(Room room, long parentId) throws SQLException {
		read((AbstractCityObject)room, parentId);
	}

	private void read(AbstractCityObject cityObject, long parentId) throws SQLException {
		final List<Long> boundarySurfaceIds = new ArrayList<Long>();
		ResultSet rs = null;

		try {
			switch (cityObject.getCityGMLClass()) {
			case BUILDING:
			case BUILDING_PART:
				psBuildingThematicSurface.setLong(1, parentId);
				rs = psBuildingThematicSurface.executeQuery();
				break;
			case BUILDING_INSTALLATION:
			case INT_BUILDING_INSTALLATION:
				psBuildingInstallationThematicSurface.setLong(1, parentId);
				rs = psBuildingInstallationThematicSurface.executeQuery();
				break;
			case BUILDING_ROOM:
				psRoomThematicSurface.setLong(1, parentId);
				rs = psRoomThematicSurface.executeQuery();
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
						case BUILDING_WALL_SURFACE:
							boundarySurface = new WallSurface();
							break;
						case BUILDING_ROOF_SURFACE:
							boundarySurface = new RoofSurface();
							break;
						case INTERIOR_BUILDING_WALL_SURFACE:
							boundarySurface = new InteriorWallSurface();
							break;
						case BUILDING_GROUND_SURFACE:
							boundarySurface = new GroundSurface();
							break;
						case BUILDING_FLOOR_SURFACE:
							boundarySurface = new FloorSurface();
							break;
						case BUILDING_CLOSURE_SURFACE:
							boundarySurface = new ClosureSurface();
							break;
						case BUILDING_CEILING_SURFACE:
							boundarySurface = new CeilingSurface();
							break;
						case OUTER_BUILDING_FLOOR_SURFACE:
							boundarySurface = new OuterFloorSurface();
							break;
						case OUTER_BUILDING_CEILING_SURFACE:
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
						case BUILDING:
						case BUILDING_PART:
							((AbstractBuilding)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						case BUILDING_INSTALLATION:
							((BuildingInstallation)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						case INT_BUILDING_INSTALLATION:
							((IntBuildingInstallation)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						case BUILDING_ROOM:
							((Room)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						default:
							continue;
						}							

						boundarySurfaceIds.add(boundarySurfaceId);
					} else {
						switch (cityObject.getCityGMLClass()) {
						case BUILDING:
						case BUILDING_PART:
							boundarySurface = ((AbstractBuilding)cityObject).getBoundedBySurface().get(index).getBoundarySurface();
							break;
						case BUILDING_INSTALLATION:
							boundarySurface = ((BuildingInstallation)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						case INT_BUILDING_INSTALLATION:
							boundarySurface = ((IntBuildingInstallation)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						case BUILDING_ROOM:
							boundarySurface = ((Room)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
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
				case BUILDING_WINDOW:
					opening = new Window();
					break;
				case BUILDING_DOOR:
					opening = new Door();
					break;
				default:
					continue;
				}

				// cityobject stuff
				cityObjectExporter.read(opening, openingId);

				if (opening.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(opening.getId(), openingId, CityGMLClass.ABSTRACT_BUILDING_OPENING)) {
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
					long lodMultiSurfaceId = rs.getLong(9 + lod);
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
					long implicitGeometryId = rs.getLong(11 + lod);
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

				long addressId = rs.getLong(8);
				if (!rs.wasNull() && opening.getCityGMLClass() == CityGMLClass.BUILDING_DOOR) {
					AddressExportFactory factory = dbExporterManager.getAddressExportFactory();					
					AddressObject addressObject = factory.newAddressObject();
					AddressProperty addressProperty = null;

					if (handleAddressGmlId) {
						String gmlId = rs.getString(26);
						if (dbExporterManager.lookupAndPutGmlId(gmlId, addressId, CityGMLClass.ADDRESS)) {
							if (useXLink) {
								addressProperty = new AddressProperty();
								addressProperty.setHref("#" + gmlId);
								((Door)opening).addAddress(addressProperty);
							} else {
								String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
								if (appendOldGmlId)
									newGmlId += '-' + gmlId;

								addressObject.setGmlId(newGmlId);	
							}
						} else
							addressObject.setGmlId(gmlId);							
					}

					if (addressProperty == null) {
						fillAddressObject(addressObject, factory.getPrimaryMode(), rs);
						if (!addressObject.canCreate(factory.getPrimaryMode()) && factory.isUseFallback())
							fillAddressObject(addressObject, factory.getFallbackMode(), rs);

						if (addressObject.canCreate()) {
							// multiPointGeometry
							Object multiPointObj = rs.getObject(24);
							if (!rs.wasNull() && multiPointObj != null) {
								GeometryObject multiPoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(multiPointObj);
								MultiPointProperty multiPointProperty = geometryExporter.getMultiPointProperty(multiPoint, false);
								if (multiPointProperty != null) {
									addressObject.setMultiPointProperty(multiPointProperty);
								}
							}

							// create xAL address
							addressProperty = factory.create(addressObject);
							if (addressProperty != null)
								((Door)opening).addAddress(addressProperty);
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

	private void fillAddressObject(AddressObject addressObject, AddressMode mode, ResultSet rs) throws SQLException {
		if (mode == AddressMode.DB) {
			addressObject.setStreet(rs.getString(17));
			addressObject.setHouseNumber(rs.getString(18));
			addressObject.setPOBox(rs.getString(19));
			addressObject.setZipCode(rs.getString(20));
			addressObject.setCity(rs.getString(21));
			addressObject.setState(rs.getString(22));
			addressObject.setCountry(rs.getString(23));
		} else {
			String xal = rs.getString(25);
			if (!rs.wasNull()) {
				Object object = dbExporterManager.unmarshal(new StringReader(xal));
				if (object instanceof AddressDetails)
					addressObject.setAddressDetails((AddressDetails)object);
			}
		}
	}

	@Override
	public void close() throws SQLException {
		psBuildingThematicSurface.close();
		psBuildingInstallationThematicSurface.close();
		psRoomThematicSurface.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.THEMATIC_SURFACE;
	}

}
