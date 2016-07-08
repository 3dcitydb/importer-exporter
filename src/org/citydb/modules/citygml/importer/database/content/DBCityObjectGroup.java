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

		// grp:class
		if (cityObjectGroup.isSetClazz() && cityObjectGroup.getClazz().isSetValue()) {
			psCityObjectGroup.setString(2, cityObjectGroup.getClazz().getValue());
			psCityObjectGroup.setString(3, cityObjectGroup.getClazz().getCodeSpace());
		} else {
			psCityObjectGroup.setNull(2, Types.VARCHAR);
			psCityObjectGroup.setNull(3, Types.VARCHAR);
		}

		// grp:function
		if (cityObjectGroup.isSetFunction()) {
			String[] function = Util.codeList2string(cityObjectGroup.getFunction());
			psCityObjectGroup.setString(4, function[0]);
			psCityObjectGroup.setString(5, function[1]);
		} else {
			psCityObjectGroup.setNull(4, Types.VARCHAR);
			psCityObjectGroup.setNull(5, Types.VARCHAR);
		}

		// grp:usage
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
