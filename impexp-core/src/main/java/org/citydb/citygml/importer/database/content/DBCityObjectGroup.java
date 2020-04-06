/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.database.content;

import org.citydb.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBCityObjectGroup implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psCityObjectGroup;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;

	public DBCityObjectGroup(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".cityobjectgroup (id, class, class_codespace, function, function_codespace, usage, usage_codespace, " +
				"brep_id, other_geom" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psCityObjectGroup = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(CityObjectGroup cityObjectGroup) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(cityObjectGroup);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long cityObjectGroupId = cityObjectImporter.doImport(cityObjectGroup, featureType);

		// CityObjectGroup
		// ID
		psCityObjectGroup.setLong(1, cityObjectGroupId);

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
			valueJoiner.join(cityObjectGroup.getFunction(), Code::getValue, Code::getCodeSpace);
			psCityObjectGroup.setString(4, valueJoiner.result(0));
			psCityObjectGroup.setString(5, valueJoiner.result(1));
		} else {
			psCityObjectGroup.setNull(4, Types.VARCHAR);
			psCityObjectGroup.setNull(5, Types.VARCHAR);
		}

		// grp:usage
		if (cityObjectGroup.isSetUsage()) {
			valueJoiner.join(cityObjectGroup.getUsage(), Code::getValue, Code::getCodeSpace);
			psCityObjectGroup.setString(6, valueJoiner.result(0));
			psCityObjectGroup.setString(7, valueJoiner.result(1));
		} else {
			psCityObjectGroup.setNull(6, Types.VARCHAR);
			psCityObjectGroup.setNull(7, Types.VARCHAR);
		}

		// grp:geometry
		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (cityObjectGroup.isSetGeometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = cityObjectGroup.getGeometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (importer.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.doImport(abstractGeometry, cityObjectGroupId);
				else if (importer.isPointOrLineGeometry(abstractGeometry))
					geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
				else 
					importer.logOrThrowUnsupportedGeometryMessage(cityObjectGroup, abstractGeometry);

				geometryProperty.unsetGeometry();
			} else {
				String href = geometryProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.CITYOBJECTGROUP.getName(),
							cityObjectGroupId, 
							href, 
							"brep_id"));
				}
			}
		}

		if (geometryId != 0)
			psCityObjectGroup.setLong(8, geometryId);
		else
			psCityObjectGroup.setNull(8, Types.NULL);

		if (geometryObject != null)
			psCityObjectGroup.setObject(9, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psCityObjectGroup.setNull(9, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// objectclass id
		if (hasObjectClassIdColumn)
			psCityObjectGroup.setLong(10, featureType.getObjectClassId());

		psCityObjectGroup.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.CITYOBJECTGROUP);		

		// grp:groupParent
		if (cityObjectGroup.isSetGroupParent()) {
			if (cityObjectGroup.getGroupParent().isSetCityObject()) {
				importer.logOrThrowErrorMessage(importer.getObjectSignature(cityObjectGroup) +
						": XML read error while parsing group parent.");
			} else {			
				String href = cityObjectGroup.getGroupParent().getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkGroupToCityObject(
							cityObjectGroupId,
							href,
							true));
				}
			}
		} 

		// grp:groupMember
		if (cityObjectGroup.isSetGroupMember()) {
			for (CityObjectGroupMember groupMember : cityObjectGroup.getGroupMember()) {
				if (groupMember.isSetObject()) {
					importer.logOrThrowErrorMessage(importer.getObjectSignature(cityObjectGroup) +
							": XML read error while parsing group members.");
				} else {
					String href = groupMember.getHref();
					if (href != null && href.length() != 0) {
						DBXlinkGroupToCityObject xlink = new DBXlinkGroupToCityObject(
								cityObjectGroupId,
								href,
								false);

						xlink.setRole(groupMember.getGroupRole());						
						importer.propagateXlink(xlink);
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(cityObjectGroup, cityObjectGroupId, featureType);

		return cityObjectGroupId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psCityObjectGroup.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psCityObjectGroup.close();
	}

}
