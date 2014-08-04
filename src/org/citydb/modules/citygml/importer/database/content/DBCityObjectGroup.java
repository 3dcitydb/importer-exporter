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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBCityObjectGroup implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psCityObjectGroup;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;

	public DBCityObjectGroup(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into CITYOBJECTGROUP (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("BREP_ID, OTHER_GEOM, PARENT_CITYOBJECT_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psCityObjectGroup = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(CityObjectGroup cityObjectGroup) throws SQLException {
		long cityObjectGroupId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (cityObjectGroupId != 0)
			success = insert(cityObjectGroup, cityObjectGroupId);

		if (success)
			return cityObjectGroupId;
		else
			return 0;
	}

	private boolean insert(CityObjectGroup cityObjectGroup, long cityObjectGroupId) throws SQLException {
		String origGmlId = cityObjectGroup.getId();

		// CityObject
		long cityObjectId = cityObjectImporter.insert(cityObjectGroup, cityObjectGroupId, true);
		if (cityObjectId == 0)
			return false;

		// CityObjectGroup
		// ID
		psCityObjectGroup.setLong(1, cityObjectId);

		// class
		if (cityObjectGroup.isSetClazz() && cityObjectGroup.getClazz().isSetValue()) {
			psCityObjectGroup.setString(2, cityObjectGroup.getClazz().getValue());
			psCityObjectGroup.setString(3, cityObjectGroup.getClazz().getCodeSpace());
		} else {
			psCityObjectGroup.setNull(2, Types.VARCHAR);
			psCityObjectGroup.setNull(3, Types.VARCHAR);
		}

		// function
		if (cityObjectGroup.isSetFunction()) {
			String[] function = Util.codeList2string(cityObjectGroup.getFunction());
			psCityObjectGroup.setString(4, function[0]);
			psCityObjectGroup.setString(5, function[1]);
		} else {
			psCityObjectGroup.setNull(4, Types.VARCHAR);
			psCityObjectGroup.setNull(5, Types.VARCHAR);
		}

		// usage
		if (cityObjectGroup.isSetUsage()) {
			String[] usage = Util.codeList2string(cityObjectGroup.getUsage());
			psCityObjectGroup.setString(6, usage[0]);
			psCityObjectGroup.setString(7, usage[1]);
		} else {
			psCityObjectGroup.setNull(6, Types.VARCHAR);
			psCityObjectGroup.setNull(7, Types.VARCHAR);
		}

		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (cityObjectGroup.isSetGeometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = cityObjectGroup.getGeometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.insert(abstractGeometry, cityObjectGroupId);
				else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
					geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
				else {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							cityObjectGroup.getCityGMLClass(), 
							cityObjectGroup.getId()));
					msg.append(": Unsupported geometry type ");
					msg.append(abstractGeometry.getGMLClass()).append('.');

					LOG.error(msg.toString());
				}

				geometryProperty.unsetGeometry();
			} else {
				// xlink
				String href = geometryProperty.getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
							href, 
							cityObjectGroupId, 
							TableEnum.CITYOBJECTGROUP, 
							"BREP_ID"));
				}
			}
		}

		if (geometryId != 0)
			psCityObjectGroup.setLong(8, geometryId);
		else
			psCityObjectGroup.setNull(8, Types.NULL);

		if (geometryObject != null)
			psCityObjectGroup.setObject(9, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psCityObjectGroup.setNull(9, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// parent
		psCityObjectGroup.setNull(10, Types.NULL);

		psCityObjectGroup.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECTGROUP);		

		// group parent
		if (cityObjectGroup.isSetGroupParent()) {
			if (cityObjectGroup.getGroupParent().isSetCityObject()) {
				StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
						CityGMLClass.CITY_OBJECT_GROUP, 
						origGmlId));

				msg.append(": XML read error while parsing parent element.");
				LOG.error(msg.toString());
			} else {			
				// xlink
				String href = cityObjectGroup.getGroupParent().getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkGroupToCityObject(
							cityObjectGroupId,
							href,
							true));
				}
			}
		} 

		// group member
		if (cityObjectGroup.isSetGroupMember()) {
			for (CityObjectGroupMember groupMember : cityObjectGroup.getGroupMember()) {
				if (groupMember.isSetObject()) {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							CityGMLClass.CITY_OBJECT_GROUP, 
							origGmlId));

					msg.append(": XML read error while parsing groupMember element.");
					LOG.error(msg.toString());
				} else {
					// xlink
					String href = groupMember.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkGroupToCityObject xlink = new DBXlinkGroupToCityObject(
								cityObjectGroupId,
								href,
								false);

						xlink.setRole(groupMember.getGroupRole());						
						dbImporterManager.propagateXlink(xlink);
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(cityObjectGroup, cityObjectGroupId);

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psCityObjectGroup.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psCityObjectGroup.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.CITYOBJECTGROUP;
	}

}
