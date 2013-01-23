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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
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
	
	private int batchCounter;
	
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

	public long insert(AbstractOpening opening, long parentId) throws SQLException {
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
		psOpening.setString(5, TypeAttributeValueEnum.fromCityGMLClass(opening.getCityGMLClass()).toString());

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
								TableEnum.OPENING,
								href,
								TableEnum.ADDRESS
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
								TableEnum.OPENING,
								href,
								TableEnum.SURFACE_GEOMETRY
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
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.OPENING);
		
		openingToThemSurfaceImporter.insert(openingId, parentId);

		return openingId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psOpening.executeBatch();
		batchCounter = 0;
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
