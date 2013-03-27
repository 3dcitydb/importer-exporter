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

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.citygml.building.BoundarySurfacePropertyImpl;
import org.citygml4j.impl.citygml.building.CeilingSurfaceImpl;
import org.citygml4j.impl.citygml.building.ClosureSurfaceImpl;
import org.citygml4j.impl.citygml.building.DoorImpl;
import org.citygml4j.impl.citygml.building.FloorSurfaceImpl;
import org.citygml4j.impl.citygml.building.GroundSurfaceImpl;
import org.citygml4j.impl.citygml.building.InteriorWallSurfaceImpl;
import org.citygml4j.impl.citygml.building.OpeningPropertyImpl;
import org.citygml4j.impl.citygml.building.RoofSurfaceImpl;
import org.citygml4j.impl.citygml.building.WallSurfaceImpl;
import org.citygml4j.impl.citygml.building.WindowImpl;
import org.citygml4j.impl.citygml.core.ExternalObjectImpl;
import org.citygml4j.impl.citygml.core.ExternalReferenceImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiSurfacePropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.exporter.AddressMode;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.modules.citygml.common.xal.AddressExportFactory;
import de.tub.citydb.modules.citygml.common.xal.AddressObject;
import de.tub.citydb.util.Util;

public class DBThematicSurface implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBuildingThematicSurface;
	private PreparedStatement psRoomThematicSurface;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBSdoGeometry sdoGeometry;

	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private boolean transformCoords;
	private String gmlIdPrefix;
	private String infoSys;

	public DBThematicSurface(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
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

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {
			String query = "select ts.ID as TSID, ts.NAME, ts.NAME_CODESPACE, ts.DESCRIPTION, upper(ts.TYPE) as TYPE, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, "+
					"op.ID as OPID, op.NAME as OPNAME, op.NAME_CODESPACE as OPNAME_CODESPACE, op.DESCRIPTION as OPDESCRIPTION, upper(op.TYPE) as OPTYPE, op.ADDRESS_ID as OPADDR, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, " +
					"a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT, a.XAL_SOURCE " +
					"from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ";

			psBuildingThematicSurface = connection.prepareStatement(query + "ts.BUILDING_ID = ?");
			psRoomThematicSurface = connection.prepareStatement(query + "ts.ROOM_ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String query = "select ts.ID as TSID, ts.NAME, ts.NAME_CODESPACE, ts.DESCRIPTION, upper(ts.TYPE) as TYPE, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, "+
					"op.ID as OPID, op.NAME as OPNAME, op.NAME_CODESPACE as OPNAME_CODESPACE, op.DESCRIPTION as OPDESCRIPTION, upper(op.TYPE) as OPTYPE, op.ADDRESS_ID as OPADDR, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, " +
					"a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, " +
					"geodb_util.transform_or_null(a.MULTI_POINT, " + srid + ") AS MULTI_POINT, a.XAL_SOURCE " +
					"from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ";

			psBuildingThematicSurface = connection.prepareStatement(query + "ts.BUILDING_ID = ?");
			psRoomThematicSurface = connection.prepareStatement(query + "ts.ROOM_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public void read(AbstractBuilding building, long parentId) throws SQLException {
		read(building, parentId, true);
	}
	
	public void read(Room room, long parentId) throws SQLException {
		read(room, parentId, false);
	}

	private void read(AbstractCityObject cityObject, long parentId, boolean isBuilding) throws SQLException {
		final List<Long> boundarySurfaceIds = new ArrayList<Long>();
		ResultSet rs = null;

		try {
			if (isBuilding) {
				psBuildingThematicSurface.setLong(1, parentId);
				rs = psBuildingThematicSurface.executeQuery();
			} else {
				psRoomThematicSurface.setLong(1, parentId);
				rs = psRoomThematicSurface.executeQuery();
			}

			long currentBoundarySurfaceId = 0;
			AbstractBoundarySurface boundarySurface = null;

			while (rs.next()) {
				// boundarySurface
				long boundarySurfaceId = rs.getLong("TSID");

				if (boundarySurfaceId != currentBoundarySurfaceId) {
					currentBoundarySurfaceId = boundarySurfaceId;
					
					int index = boundarySurfaceIds.indexOf(boundarySurfaceId);
					if (index == -1) {
						String type = rs.getString("TYPE");
						if (rs.wasNull() || type == null || type.length() == 0)
							continue;

						if (type.equals(TypeAttributeValueEnum.WALL_SURFACE.toString().toUpperCase()))
							boundarySurface = new WallSurfaceImpl();
						else if (type.equals(TypeAttributeValueEnum.ROOF_SURFACE.toString().toUpperCase()))
							boundarySurface = new RoofSurfaceImpl();
						else if (type.equals(TypeAttributeValueEnum.INTERIOR_WALL_SURFACE.toString().toUpperCase()))
							boundarySurface = new InteriorWallSurfaceImpl();
						else if (type.equals(TypeAttributeValueEnum.GROUND_SURFACE.toString().toUpperCase()))
							boundarySurface = new GroundSurfaceImpl();
						else if (type.equals(TypeAttributeValueEnum.FLOOR_SURFACE.toString().toUpperCase()))
							boundarySurface = new FloorSurfaceImpl();
						else if (type.equals(TypeAttributeValueEnum.CLOSURE_SURFACE.toString().toUpperCase()))
							boundarySurface = new ClosureSurfaceImpl();
						else if (type.equals(TypeAttributeValueEnum.CEILING_SURFACE.toString().toUpperCase()))
							boundarySurface = new CeilingSurfaceImpl();

						if (boundarySurface == null)
							continue;

						String gmlName = rs.getString("NAME");
						String gmlNameCodespace = rs.getString("NAME_CODESPACE");

						Util.dbGmlName2featureName(boundarySurface, gmlName, gmlNameCodespace);

						String description = rs.getString("DESCRIPTION");
						if (description != null) {
							StringOrRef stringOrRef = new StringOrRefImpl();
							stringOrRef.setValue(description);
							boundarySurface.setDescription(stringOrRef);
						}

						for (int lod = 2; lod < 5 ; lod++) {
							long lodMultiSurfaceId = rs.getLong("LOD" + lod + "_MULTI_SURFACE_ID");

							if (!rs.wasNull() && lodMultiSurfaceId != 0) {
								DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodMultiSurfaceId);

								if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
									MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

									if (geometry.getAbstractGeometry() != null)
										multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
									else
										multiSurfaceProperty.setHref(geometry.getTarget());

									switch (lod) {
									case 2:
										boundarySurface.setLod2MultiSurface(multiSurfaceProperty);
										break;
									case 3:
										boundarySurface.setLod3MultiSurface(multiSurfaceProperty);
										break;
									case 4:
										boundarySurface.setLod4MultiSurface(multiSurfaceProperty);
										break;
									}
								}
							}
						}

						// cityobject stuff
						cityObjectExporter.read(boundarySurface, boundarySurfaceId);

						BoundarySurfaceProperty boundarySurfaceProperty = new BoundarySurfacePropertyImpl();
						boundarySurfaceProperty.setObject(boundarySurface);

						if (isBuilding)
							((AbstractBuilding)cityObject).addBoundedBySurface(boundarySurfaceProperty);
						else
							((Room)cityObject).addBoundedBySurface(boundarySurfaceProperty);
						
						boundarySurfaceIds.add(boundarySurfaceId);
					} else {
						if (isBuilding)
							boundarySurface = ((AbstractBuilding)cityObject).getBoundedBySurface().get(index).getBoundarySurface();
						else
							boundarySurface = ((Room)cityObject).getBoundedBySurface().get(index).getBoundarySurface();							
					}
				}

				// continue if we could not interpret the boundary surface
				if (boundarySurface == null)
					continue;

				long openingId = rs.getLong("OPID");
				if (rs.wasNull())
					continue;

				// create new opening object
				AbstractOpening opening = null;
				String type = rs.getString("OPTYPE");
				if (rs.wasNull() || type == null || type.length() == 0)
					continue;

				if (type.equals(TypeAttributeValueEnum.WINDOW.toString().toUpperCase()))
					opening = new WindowImpl();
				else if (type.equals(TypeAttributeValueEnum.DOOR.toString().toUpperCase()))
					opening = new DoorImpl();

				if (opening == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(opening, openingId);

				if (opening.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(opening.getId(), openingId, CityGMLClass.ABSTRACT_OPENING)) {
						if (useXLink) {
							OpeningProperty openingProperty = new OpeningPropertyImpl();
							openingProperty.setHref("#" + opening.getId());

							boundarySurface.addOpening(openingProperty);
							continue;
						} else {
							String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + opening.getId();

							if (keepOldGmlId) {
								ExternalReference externalReference = new ExternalReferenceImpl();
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = new ExternalObjectImpl();
								externalObject.setName(opening.getId());

								externalReference.setExternalObject(externalObject);
								opening.addExternalReference(externalReference);
							}

							opening.setId(newGmlId);	
						}	
					}
				}

				String gmlName = rs.getString("OPNAME");
				String gmlNameCodespace = rs.getString("OPNAME_CODESPACE");

				Util.dbGmlName2featureName(opening, gmlName, gmlNameCodespace);

				String description = rs.getString("OPDESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);

					opening.setDescription(stringOrRef);
				}

				for (int lod = 3; lod < 5 ; lod++) {
					long lodMultiSurfaceId = rs.getLong("OPLOD" + lod + "_MULTI_SURFACE_ID");

					if (!rs.wasNull() && lodMultiSurfaceId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodMultiSurfaceId);

						if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
							MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
							else
								multiSurfaceProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 3:
								opening.setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 4:
								opening.setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}
				}

				rs.getLong("OPADDR");
				if (!rs.wasNull() && opening.getCityGMLClass() == CityGMLClass.DOOR) {
					AddressExportFactory factory = dbExporterManager.getAddressExportFactory();					
					AddressObject addressObject = factory.newAddressObject();

					fillAddressObject(addressObject, factory.getPrimaryMode(), rs);
					if (!addressObject.canCreate(factory.getPrimaryMode()) && factory.isUseFallback())
						fillAddressObject(addressObject, factory.getFallbackMode(), rs);

					if (addressObject.canCreate()) {
						// multiPointGeometry
						STRUCT multiPointObj = (STRUCT)rs.getObject("MULTI_POINT");
						if (!rs.wasNull() && multiPointObj != null) {
							JGeometry multiPoint = JGeometry.load(multiPointObj);

							MultiPointProperty multiPointProperty = sdoGeometry.getMultiPointProperty(multiPoint, false);
							if (multiPointProperty != null) {
								addressObject.setMultiPointProperty(multiPointProperty);
							}
						}

						// create xAL address
						AddressProperty addressProperty = factory.create(addressObject);
						if (addressProperty != null)
							((Door)opening).addAddress(addressProperty);
					}
				}

				OpeningProperty openingProperty = new OpeningPropertyImpl();
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
			addressObject.setStreet(rs.getString("STREET"));
			addressObject.setHouseNumber(rs.getString("HOUSE_NUMBER"));
			addressObject.setPOBox(rs.getString("PO_BOX"));
			addressObject.setZipCode(rs.getString("ZIP_CODE"));
			addressObject.setCity(rs.getString("CITY"));
			addressObject.setCountry(rs.getString("COUNTRY"));
			addressObject.setState(rs.getString("STATE"));
		} else {
			Clob clob = rs.getClob("XAL_SOURCE");
			if (!rs.wasNull()) {
				Object object = dbExporterManager.unmarshal(clob.getCharacterStream());
				if (object instanceof AddressDetails)
					addressObject.setAddressDetails((AddressDetails)object);
			}
		}
	}

	@Override
	public void close() throws SQLException {
		psBuildingThematicSurface.close();
		psRoomThematicSurface.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.THEMATIC_SURFACE;
	}

}
