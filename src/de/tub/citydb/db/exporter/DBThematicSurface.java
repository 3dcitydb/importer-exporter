package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiSurfacePropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.impl.jaxb.xal._2.AddressDetailsImpl;
import org.citygml4j.impl.jaxb.xal._2.CountryImpl;
import org.citygml4j.impl.jaxb.xal._2.CountryNameImpl;
import org.citygml4j.impl.jaxb.xal._2.LocalityImpl;
import org.citygml4j.impl.jaxb.xal._2.LocalityNameImpl;
import org.citygml4j.impl.jaxb.xal._2.PostBoxImpl;
import org.citygml4j.impl.jaxb.xal._2.PostBoxNumberImpl;
import org.citygml4j.impl.jaxb.xal._2.PostalCodeImpl;
import org.citygml4j.impl.jaxb.xal._2.PostalCodeNumberImpl;
import org.citygml4j.impl.jaxb.xal._2.ThoroughfareImpl;
import org.citygml4j.impl.jaxb.xal._2.ThoroughfareNameImpl;
import org.citygml4j.impl.jaxb.xal._2.ThoroughfareNumberImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurface;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingModule;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.Opening;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.MultiPointProperty;
import org.citygml4j.model.gml.MultiSurface;
import org.citygml4j.model.gml.MultiSurfaceProperty;
import org.citygml4j.model.gml.StringOrRef;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.model.xal.PostBox;
import org.citygml4j.model.xal.PostBoxNumber;
import org.citygml4j.model.xal.PostalCode;
import org.citygml4j.model.xal.PostalCodeNumber;
import org.citygml4j.model.xal.Thoroughfare;
import org.citygml4j.model.xal.ThoroughfareName;
import org.citygml4j.model.xal.ThoroughfareNumber;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.util.UUIDManager;
import de.tub.citydb.util.Util;

public class DBThematicSurface implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBuildingThematicSurface;
	private PreparedStatement psRoomThematicSurface;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBSdoGeometry sdoGeometry;

	private String gmlNameDelimiter;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private boolean transformCoords;
	private String gmlIdPrefix;
	private String infoSys;

	public DBThematicSurface(Connection connection, CityGMLFactory cityGMLFactory, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();

		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			keepOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetKeepGmlIdAsExternalReference();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
			infoSys = config.getInternal().getExportFileName();
		}	

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {
			psBuildingThematicSurface = connection.prepareStatement("select ts.ID as TSID, ts.NAME, ts.NAME_CODESPACE, ts.DESCRIPTION, upper(ts.TYPE) as TYPE, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, "+
					"op.ID as OPID, op.NAME as OPNAME, op.NAME_CODESPACE as OPNAME_CODESPACE, op.DESCRIPTION as OPDESCRIPTION, upper(op.TYPE) as OPTYPE, op.ADDRESS_ID as OPADDR, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, " +
					"a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT " +
			"from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ts.BUILDING_ID = ?");

			psRoomThematicSurface = connection.prepareStatement("select ts.ID as TSID, ts.NAME, ts.NAME_CODESPACE, ts.DESCRIPTION, upper(ts.TYPE) as TYPE, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, "+
					"op.ID as OPID, op.NAME as OPNAME, op.NAME_CODESPACE as OPNAME_CODESPACE, op.DESCRIPTION as OPDESCRIPTION, upper(op.TYPE) as OPTYPE, op.ADDRESS_ID as OPADDR, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, " +
					"a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT " +
			"from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ts.ROOM_ID = ?");
		} else {
			ReferenceSystem targetSRS = config.getInternal().getExportTargetSRS();
			int srid = targetSRS.getSrid();
			
			psBuildingThematicSurface = connection.prepareStatement("select ts.ID as TSID, ts.NAME, ts.NAME_CODESPACE, ts.DESCRIPTION, upper(ts.TYPE) as TYPE, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, "+
					"op.ID as OPID, op.NAME as OPNAME, op.NAME_CODESPACE as OPNAME_CODESPACE, op.DESCRIPTION as OPDESCRIPTION, upper(op.TYPE) as OPTYPE, op.ADDRESS_ID as OPADDR, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, " +
					"a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY," +
					"geodb_util.transform_or_null(a.MULTI_POINT, " + srid + ") AS MULTI_POINT " +
			"from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ts.BUILDING_ID = ?");

			psRoomThematicSurface = connection.prepareStatement("select ts.ID as TSID, ts.NAME, ts.NAME_CODESPACE, ts.DESCRIPTION, upper(ts.TYPE) as TYPE, ts.LOD2_MULTI_SURFACE_ID, ts.LOD3_MULTI_SURFACE_ID, ts.LOD4_MULTI_SURFACE_ID, "+
					"op.ID as OPID, op.NAME as OPNAME, op.NAME_CODESPACE as OPNAME_CODESPACE, op.DESCRIPTION as OPDESCRIPTION, upper(op.TYPE) as OPTYPE, op.ADDRESS_ID as OPADDR, op.LOD3_MULTI_SURFACE_ID as OPLOD3_MULTI_SURFACE_ID, op.LOD4_MULTI_SURFACE_ID as OPLOD4_MULTI_SURFACE_ID, " +
					"a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY," +
					"geodb_util.transform_or_null(a.MULTI_POINT, " + srid + ") AS MULTI_POINT " +
			"from THEMATIC_SURFACE ts left join OPENING_TO_THEM_SURFACE o2t on ts.ID = o2t.THEMATIC_SURFACE_ID left join OPENING op on op.ID = o2t.OPENING_ID left join ADDRESS a on op.ADDRESS_ID=a.ID where ts.ROOM_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public void read(AbstractBuilding building, long parentId, BuildingModule bldg) throws SQLException {
		ResultSet rs = null;

		try {
			psBuildingThematicSurface.setLong(1, parentId);
			rs = psBuildingThematicSurface.executeQuery();

			long currentBoundarySurfaceId = 0;
			BoundarySurface boundarySurface = null;

			while (rs.next()) {
				// boundarySurface
				long boundarySurfaceId = rs.getLong("TSID");

				if (boundarySurfaceId != currentBoundarySurfaceId) {
					currentBoundarySurfaceId = boundarySurfaceId;

					String type = rs.getString("TYPE");
					if (rs.wasNull() || type == null || type.length() == 0)
						continue;

					if (type.equals(CityGMLClass.WALLSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createWallSurface(bldg);
					else if (type.equals(CityGMLClass.ROOFSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createRoofSurface(bldg);
					else if (type.equals(CityGMLClass.INTERIORWALLSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createInteriorWallSurface(bldg);
					else if (type.equals(CityGMLClass.GROUNDSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createGroundSurface(bldg);
					else if (type.equals(CityGMLClass.FLOORSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createFloorSurface(bldg);
					else if (type.equals(CityGMLClass.CLOSURESURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createClosureSurface(bldg);
					else if (type.equals(CityGMLClass.CEILINGSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createCeilingSurface(bldg);

					if (boundarySurface == null)
						continue;

					String gmlName = rs.getString("NAME");
					String gmlNameCodespace = rs.getString("NAME_CODESPACE");

					Util.dbGmlName2featureName(boundarySurface, gmlName, gmlNameCodespace, gmlNameDelimiter);

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

							if (geometry != null && geometry.getType() == GMLClass.MULTISURFACE) {
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

					BoundarySurfaceProperty boundarySurfaceProperty = cityGMLFactory.createBoundarySurfaceProperty(bldg);
					boundarySurfaceProperty.setObject(boundarySurface);
					building.addBoundedBySurface(boundarySurfaceProperty);
				}

				// continue if we could not interpret the boundary surface
				if (boundarySurface == null)
					continue;
				
				long openingId = rs.getLong("OPID");
				if (rs.wasNull())
					continue;

				// create new opening object
				Opening opening = null;
				String type = rs.getString("OPTYPE");
				if (rs.wasNull() || type == null || type.length() == 0)
					continue;

				if (type.equals(CityGMLClass.WINDOW.toString().toUpperCase()))
					opening = cityGMLFactory.createWindow(bldg);
				else if (type.equals(CityGMLClass.DOOR.toString().toUpperCase()))
					opening = cityGMLFactory.createDoor(bldg);

				if (opening == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(opening, openingId);

				if (opening.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(opening.getId(), openingId, CityGMLClass.OPENING)) {
						if (useXLink) {
							OpeningProperty openingProperty = cityGMLFactory.createOpeningProperty(bldg);
							openingProperty.setHref("#" + opening.getId());

							boundarySurface.addOpening(openingProperty);
							continue;
						} else {
							String newGmlId = UUIDManager.randomUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + opening.getId();

							if (keepOldGmlId) {
								CoreModule core = (CoreModule)opening.getCityGMLModule().getModuleDependencies().getModule(CityGMLModuleType.CORE);

								ExternalReference externalReference = cityGMLFactory.createExternalReference(core);
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = cityGMLFactory.createExternalObject(core);
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

				Util.dbGmlName2featureName(opening, gmlName, gmlNameCodespace, gmlNameDelimiter);

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

						if (geometry != null && geometry.getType() == GMLClass.MULTISURFACE) {
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

				long addressId = rs.getLong("OPADDR");
				if (!rs.wasNull() && opening.getCityGMLClass() == CityGMLClass.DOOR) {
					HashMap<String, Object> properties = new HashMap<String, Object>();
					properties.put("street", rs.getString("STREET"));
					properties.put("houseNumber", rs.getString("HOUSE_NUMBER"));
					properties.put("poBox", rs.getString("PO_BOX"));
					properties.put("zipCode", rs.getString("ZIP_CODE"));
					properties.put("city", rs.getString("CITY"));
					properties.put("state", rs.getString("STATE"));
					properties.put("country", rs.getString("COUNTRY"));

					STRUCT multiPointObj = (STRUCT)rs.getObject("MULTI_POINT");
					if (!rs.wasNull() && multiPointObj != null) {
						JGeometry multiPoint = JGeometry.load(multiPointObj);
						properties.put("multiPoint", multiPoint);
					} else
						properties.put("multiPoint", null);	

					AddressProperty addressProperty = getDoorAddress(properties, 
							(CoreModule)bldg.getModuleDependencies().getModule(CityGMLModuleType.CORE));
					if (addressProperty != null)
						((Door)opening).addAddress(addressProperty);
				}

				OpeningProperty openingProperty = cityGMLFactory.createOpeningProperty(bldg);
				openingProperty.setObject(opening);
				boundarySurface.addOpening(openingProperty);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public void read(Room room, long parentId, BuildingModule bldg) throws SQLException {
		ResultSet rs = null;

		try {
			psRoomThematicSurface.setLong(1, parentId);
			rs = psRoomThematicSurface.executeQuery();

			long currentBoundarySurfaceId = 0;
			BoundarySurface boundarySurface = null;

			while (rs.next()) {
				// boundarySurface
				long boundarySurfaceId = rs.getLong("TSID");

				if (boundarySurfaceId != currentBoundarySurfaceId) {
					currentBoundarySurfaceId = boundarySurfaceId;

					String type = rs.getString("TYPE");
					if (rs.wasNull() || type == null || type.length() == 0)
						continue;

					if (type.equals(CityGMLClass.WALLSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createWallSurface(bldg);
					else if (type.equals(CityGMLClass.ROOFSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createRoofSurface(bldg);
					else if (type.equals(CityGMLClass.INTERIORWALLSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createInteriorWallSurface(bldg);
					else if (type.equals(CityGMLClass.GROUNDSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createGroundSurface(bldg);
					else if (type.equals(CityGMLClass.FLOORSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createFloorSurface(bldg);
					else if (type.equals(CityGMLClass.CLOSURESURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createClosureSurface(bldg);
					else if (type.equals(CityGMLClass.CEILINGSURFACE.toString().toUpperCase()))
						boundarySurface = cityGMLFactory.createCeilingSurface(bldg);

					if (boundarySurface == null)
						continue;

					String gmlName = rs.getString("NAME");
					String gmlNameCodespace = rs.getString("NAME_CODESPACE");

					Util.dbGmlName2featureName(boundarySurface, gmlName, gmlNameCodespace, gmlNameDelimiter);

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

							if (geometry != null && geometry.getType() == GMLClass.MULTISURFACE) {
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

					BoundarySurfaceProperty boundarySurfaceProperty = cityGMLFactory.createBoundarySurfaceProperty(bldg);
					boundarySurfaceProperty.setObject(boundarySurface);
					room.addBoundedBySurface(boundarySurfaceProperty);
				}

				// continue if we could not interpret the boundary surface
				if (boundarySurface == null)
					continue;
				
				long openingId = rs.getLong("OPID");
				if (rs.wasNull())
					continue;

				// create new opening object
				Opening opening = null;
				String type = rs.getString("OPTYPE");
				if (rs.wasNull() || type == null || type.length() == 0)
					continue;

				if (type.equals(CityGMLClass.WINDOW.toString().toUpperCase()))
					opening = cityGMLFactory.createWindow(bldg);
				else if (type.equals(CityGMLClass.DOOR.toString().toUpperCase()))
					opening = cityGMLFactory.createDoor(bldg);

				if (opening == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(opening, openingId);

				if (opening.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(opening.getId(), openingId, CityGMLClass.OPENING)) {
						if (useXLink) {
							OpeningProperty openingProperty = cityGMLFactory.createOpeningProperty(bldg);
							openingProperty.setHref("#" + opening.getId());

							boundarySurface.addOpening(openingProperty);
							continue;
						} else {
							String newGmlId = UUIDManager.randomUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + opening.getId();

							if (keepOldGmlId) {
								CoreModule core = (CoreModule)opening.getCityGMLModule().getModuleDependencies().getModule(CityGMLModuleType.CORE);

								ExternalReference externalReference = cityGMLFactory.createExternalReference(core);
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = cityGMLFactory.createExternalObject(core);
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

				Util.dbGmlName2featureName(opening, gmlName, gmlNameCodespace, gmlNameDelimiter);

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

						if (geometry != null && geometry.getType() == GMLClass.MULTISURFACE) {
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

				long addressId = rs.getLong("OPADDR");
				if (!rs.wasNull() && opening.getCityGMLClass() == CityGMLClass.DOOR) {
					HashMap<String, Object> properties = new HashMap<String, Object>();
					properties.put("street", rs.getString("STREET"));
					properties.put("houseNumber", rs.getString("HOUSE_NUMBER"));
					properties.put("poBox", rs.getString("PO_BOX"));
					properties.put("zipCode", rs.getString("ZIP_CODE"));
					properties.put("city", rs.getString("CITY"));
					properties.put("state", rs.getString("STATE"));
					properties.put("country", rs.getString("COUNTRY"));

					STRUCT multiPointObj = (STRUCT)rs.getObject("MULTI_POINT");
					if (!rs.wasNull() && multiPointObj != null) {
						JGeometry multiPoint = JGeometry.load(multiPointObj);
						properties.put("multiPoint", multiPoint);
					} else
						properties.put("multiPoint", null);				

					AddressProperty addressProperty = getDoorAddress(properties, 
							(CoreModule)bldg.getModuleDependencies().getModule(CityGMLModuleType.CORE));
					if (addressProperty != null)
						((Door)opening).addAddress(addressProperty);
				}

				OpeningProperty openingProperty = cityGMLFactory.createOpeningProperty(bldg);
				openingProperty.setObject(opening);
				boundarySurface.addOpening(openingProperty);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public AddressProperty getDoorAddress(HashMap<String, Object> properties, CoreModule core) {
		AddressProperty addressProperty = null;

		if (properties.get("country") != null) {
			AddressDetails addressDetails = new AddressDetailsImpl();
			Country country = new CountryImpl();

			// country name
			CountryName countryName = new CountryNameImpl();
			countryName.setContent((String)properties.get("country"));
			country.addCountryName(countryName);

			if (properties.get("city") != null) {
				Locality locality = new LocalityImpl();
				locality.setType("Town");

				LocalityName localityName = new LocalityNameImpl();
				localityName.setContent((String)properties.get("city"));
				locality.addLocalityName(localityName);

				if (properties.get("street") != null) {
					Thoroughfare thoroughfare = new ThoroughfareImpl();
					thoroughfare.setType("Street");

					ThoroughfareName name = new ThoroughfareNameImpl();
					name.setContent((String)properties.get("street"));
					thoroughfare.addThoroughfareName(name);

					if (properties.get("houseNumber") != null) {
						ThoroughfareNumber number = new ThoroughfareNumberImpl();
						number.setContent((String)properties.get("houseNumber"));

						thoroughfare.addThoroughfareNumber(number);
					}

					locality.setThoroughfare(thoroughfare);
				}				

				if (properties.get("zipCode") != null) {
					PostalCode postalCode = new PostalCodeImpl();
					PostalCodeNumber zipNumber = new PostalCodeNumberImpl();
					zipNumber.setContent((String)properties.get("zipCode"));

					postalCode.addPostalCodeNumber(zipNumber);
					locality.setPostalCode(postalCode);
				}

				if (properties.get("poBox") != null) {
					PostBox postBox = new PostBoxImpl();
					PostBoxNumber postBoxNumber = new PostBoxNumberImpl();
					postBoxNumber.setContent((String)properties.get("poBox"));

					postBox.setPostBoxNumber(postBoxNumber);
					locality.setPostBox(postBox);
				}

				country.setLocality(locality);
			}

			addressDetails.setCountry(country);

			XalAddressProperty xalAddressProperty = cityGMLFactory.createXalAddressProperty(core);
			xalAddressProperty.setAddressDetails(addressDetails);

			Address address = cityGMLFactory.createAddress(core);
			address.setXalAddress(xalAddressProperty);

			// multiPointGeometry			
			if (properties.get("multiPoint") != null) {
				JGeometry multiPoint = (JGeometry)properties.get("multiPoint");

				MultiPointProperty multiPointProperty = sdoGeometry.getMultiPointProperty(multiPoint, false);
				if (multiPointProperty != null) {
					address.setMultiPoint(multiPointProperty);
				}
			}					

			addressProperty = cityGMLFactory.createAddressProperty(core);
			addressProperty.setObject(address);
		}		

		return addressProperty;
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
