package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.Opening;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.MultiSurfaceProperty;

import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBOpening implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psOpening;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOpeningToThemSurface openingToThemSurfaceImporter;
	private DBAddress addressImporter;
	
	public DBOpening(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		psOpening = batchConn.prepareStatement("insert into OPENING (ID, NAME, NAME_CODESPACE, DESCRIPTION, TYPE, ADDRESS_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID) values " +
		"(?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		openingToThemSurfaceImporter = (DBOpeningToThemSurface)dbImporterManager.getDBImporter(DBImporterEnum.OPENING_TO_THEM_SURFACE);
		addressImporter = (DBAddress)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS);
	}

	public long insert(Opening opening, long parentId) throws SQLException {
		long openingId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (openingId == 0)
			return 0;

		String origGmlId = opening.getId();
		
		// CityObject
		cityObjectImporter.insert(opening, openingId);

		// Opening
		// ID
		psOpening.setLong(1, openingId);

		// gml:name
		if (opening.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(opening);

			psOpening.setString(2, dbGmlName[0]);
			psOpening.setString(3, dbGmlName[1]);
		} else {
			psOpening.setNull(2, Types.VARCHAR);
			psOpening.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (opening.isSetDescription()) {
			String description = opening.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psOpening.setString(4, description);
		} else {
			psOpening.setNull(4, Types.VARCHAR);
		}

		// TYPE
		psOpening.setString(5, opening.getCityGMLClass().toString());

		// citygml:address
		if (opening.getCityGMLClass() == CityGMLClass.DOOR) {
			Door door = (Door)opening;
			long addressId = 0;
			
			if (door.isSetAddress() && !door.getAddress().isEmpty()) {
				// unfortunately, we can just represent one address in database...
				AddressProperty addressProperty = door.getAddress().get(0);
				Address address = addressProperty.getObject();
				
				if (address != null) {
					String gmlId = address.getId();
					addressId = addressImporter.insert(address);
					
					if (addressId == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								opening.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.ADDRESS, 
								gmlId));
						
						LOG.error(msg.toString());
					}					
				} else {
					// xlink
					String href = addressProperty.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkBasic xlink = new DBXlinkBasic(
								openingId,
								DBTableEnum.OPENING,
								href,
								DBTableEnum.ADDRESS
						);

						xlink.setAttrName("ADDRESS_ID");
						dbImporterManager.propagateXlink(xlink);
					}
				}
			}

			if (addressId != 0)
				psOpening.setLong(6, addressId);
			else
				psOpening.setNull(6, 0);
			
		} else {
			psOpening.setNull(6, 0);
		}

		// Geometry
		for (int lod = 3; lod < 5; lod++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (lod) {
			case 3:
				multiSurfaceProperty = opening.getLod3MultiSurface();
				break;
			case 4:
				multiSurfaceProperty = opening.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), openingId);
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkBasic xlink = new DBXlinkBasic(
								openingId,
								DBTableEnum.OPENING,
								href,
								DBTableEnum.SURFACE_GEOMETRY
						);

						xlink.setAttrName("LOD" + lod + "_MULTI_SURFACE_ID");
						dbImporterManager.propagateXlink(xlink);
					}
				}
			}

			switch (lod) {
			case 3:
				if (multiSurfaceId != 0)
					psOpening.setLong(7, multiSurfaceId);
				else
					psOpening.setNull(7, 0);
				break;
			case 4:
				if (multiSurfaceId != 0)
					psOpening.setLong(8, multiSurfaceId);
				else
					psOpening.setNull(8, 0);
				break;
			}
		}

		psOpening.addBatch();
		openingToThemSurfaceImporter.insert(openingId, parentId);

		return openingId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psOpening.executeBatch();
	}

	@Override
	public void close() throws SQLException {
		psOpening.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.OPENING;
	}

}
