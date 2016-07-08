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
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.AbstractOpening;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.CeilingSurface;
import org.citygml4j.model.citygml.bridge.ClosureSurface;
import org.citygml4j.model.citygml.bridge.Door;
import org.citygml4j.model.citygml.bridge.FloorSurface;
import org.citygml4j.model.citygml.bridge.GroundSurface;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.bridge.InteriorWallSurface;
import org.citygml4j.model.citygml.bridge.OpeningProperty;
import org.citygml4j.model.citygml.bridge.OuterCeilingSurface;
import org.citygml4j.model.citygml.bridge.OuterFloorSurface;
import org.citygml4j.model.citygml.bridge.RoofSurface;
import org.citygml4j.model.citygml.bridge.WallSurface;
import org.citygml4j.model.citygml.bridge.Window;
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

public class DBBridgeThematicSurface implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBridgeThematicSurface;
	private PreparedStatement psBridgeInstallationThematicSurface;
	private PreparedStatement psBridgeRoomThematicSurface;
	private PreparedStatement psBridgeConstrElementThematicSurface;

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

	public DBBridgeThematicSurface(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
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
					.append("from BRIDGE_THEMATIC_SURFACE ts left join BRIDGE_OPEN_TO_THEM_SRF o2t on ts.ID = o2t.BRIDGE_THEMATIC_SURFACE_ID left join BRIDGE_OPENING op on op.ID = o2t.BRIDGE_OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ");

			psBridgeThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_ID = ?");
			psBridgeInstallationThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_INSTALLATION_ID = ?");
			psBridgeRoomThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_ROOM_ID = ?");
			psBridgeConstrElementThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_CONSTR_ELEMENT_ID = ?");
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
					.append("from BRIDGE_THEMATIC_SURFACE ts left join BRIDGE_OPEN_TO_THEM_SRF o2t on ts.ID = o2t.BRIDGE_THEMATIC_SURFACE_ID left join BRIDGE_OPENING op on op.ID = o2t.BRIDGE_OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ");

			psBridgeThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_ID = ?");
			psBridgeInstallationThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_INSTALLATION_ID = ?");
			psBridgeRoomThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_ROOM_ID = ?");
			psBridgeConstrElementThematicSurface = connection.prepareStatement(query.toString() + "ts.BRIDGE_CONSTR_ELEMENT_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public void read(AbstractBridge bridge, long parentId) throws SQLException {
		read((AbstractCityObject)bridge, parentId);
	}

	public void read(BridgeInstallation bridgeInstallation, long parentId) throws SQLException {
		read((AbstractCityObject)bridgeInstallation, parentId);
	}

	public void read(IntBridgeInstallation intBridgeInstallation, long parentId) throws SQLException {
		read((AbstractCityObject)intBridgeInstallation, parentId);
	}

	public void read(BridgeRoom room, long parentId) throws SQLException {
		read((AbstractCityObject)room, parentId);
	}

	public void read(BridgeConstructionElement constructionElement, long parentId) throws SQLException {
		read((AbstractCityObject)constructionElement, parentId);
	}

	private void read(AbstractCityObject cityObject, long parentId) throws SQLException {
		final List<Long> boundarySurfaceIds = new ArrayList<Long>();
		ResultSet rs = null;

		try {
			switch (cityObject.getCityGMLClass()) {
			case BRIDGE:
			case BRIDGE_PART:
				psBridgeThematicSurface.setLong(1, parentId);
				rs = psBridgeThematicSurface.executeQuery();
				break;
			case BRIDGE_CONSTRUCTION_ELEMENT:
				psBridgeConstrElementThematicSurface.setLong(1, parentId);
				rs = psBridgeConstrElementThematicSurface.executeQuery();
				break;
			case BRIDGE_INSTALLATION:
			case INT_BRIDGE_INSTALLATION:
				psBridgeInstallationThematicSurface.setLong(1, parentId);
				rs = psBridgeInstallationThematicSurface.executeQuery();
				break;
			case BRIDGE_ROOM:
				psBridgeRoomThematicSurface.setLong(1, parentId);
				rs = psBridgeRoomThematicSurface.executeQuery();
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
						case BRIDGE_WALL_SURFACE:
							boundarySurface = new WallSurface();
							break;
						case BRIDGE_ROOF_SURFACE:
							boundarySurface = new RoofSurface();
							break;
						case INTERIOR_BRIDGE_WALL_SURFACE:
							boundarySurface = new InteriorWallSurface();
							break;
						case BRIDGE_GROUND_SURFACE:
							boundarySurface = new GroundSurface();
							break;
						case BRIDGE_FLOOR_SURFACE:
							boundarySurface = new FloorSurface();
							break;
						case BRIDGE_CLOSURE_SURFACE:
							boundarySurface = new ClosureSurface();
							break;
						case BRIDGE_CEILING_SURFACE:
							boundarySurface = new CeilingSurface();
							break;
						case OUTER_BRIDGE_FLOOR_SURFACE:
							boundarySurface = new OuterFloorSurface();
							break;
						case OUTER_BRIDGE_CEILING_SURFACE:
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
						case BRIDGE:
						case BRIDGE_PART:
							((AbstractBridge)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						case BRIDGE_CONSTRUCTION_ELEMENT:
							((BridgeConstructionElement)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						case BRIDGE_INSTALLATION:
							((BridgeInstallation)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						case INT_BRIDGE_INSTALLATION:
							((IntBridgeInstallation)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						case BRIDGE_ROOM:
							((BridgeRoom)cityObject).addBoundedBySurface(boundarySurfaceProperty);
							break;
						default:
							continue;
						}							

						boundarySurfaceIds.add(boundarySurfaceId);
					} else {
						switch (cityObject.getCityGMLClass()) {
						case BRIDGE:
						case BRIDGE_PART:
							boundarySurface = ((AbstractBridge)cityObject).getBoundedBySurface().get(index).getBoundarySurface();
							break;
						case BRIDGE_CONSTRUCTION_ELEMENT:
							boundarySurface = ((BridgeConstructionElement)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						case BRIDGE_INSTALLATION:
							boundarySurface = ((BridgeInstallation)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						case INT_BRIDGE_INSTALLATION:
							boundarySurface = ((IntBridgeInstallation)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
							break;
						case BRIDGE_ROOM:
							boundarySurface = ((BridgeRoom)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
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
				case BRIDGE_WINDOW:
					opening = new Window();
					break;
				case BRIDGE_DOOR:
					opening = new Door();
					break;
				default:
					continue;
				}

				// cityobject stuff
				cityObjectExporter.read(opening, openingId);

				if (opening.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(opening.getId(), openingId, CityGMLClass.ABSTRACT_BRIDGE_OPENING)) {
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
				if (!rs.wasNull() && opening.getCityGMLClass() == CityGMLClass.BRIDGE_DOOR) {
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
		psBridgeThematicSurface.close();
		psBridgeInstallationThematicSurface.close();
		psBridgeRoomThematicSurface.close();
		psBridgeConstrElementThematicSurface.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BRIDGE_THEMATIC_SURFACE;
	}

}
