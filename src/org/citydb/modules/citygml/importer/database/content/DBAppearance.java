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

import org.citydb.config.Config;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBAppearance implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAppearance;
	private DBSurfaceData surfaceDataImporter;

	private boolean replaceGmlId;
	private int batchCounter;

	public DBAppearance(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
		
		if (gmlIdCodespace != null && gmlIdCodespace.length() > 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "', ";
		else
			gmlIdCodespace = null;		
		
		StringBuilder stmt = new StringBuilder()
		.append("insert into APPEARANCE (ID, GMLID, ").append(gmlIdCodespace != null ? "GMLID_CODESPACE, " : "").append("NAME, NAME_CODESPACE, DESCRIPTION, THEME, CITYMODEL_ID, CITYOBJECT_ID) values ")
		.append("(?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?)");
		psAppearance = batchConn.prepareStatement(stmt.toString());

		surfaceDataImporter = (DBSurfaceData)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_DATA);
	}

	public long insert(Appearance appearance, CityGMLClass parent, long parentId) throws SQLException {
		long appearanceId = dbImporterManager.getDBId(DBSequencerEnum.APPEARANCE_ID_SEQ);
		boolean success = false;

		if (appearanceId != 0)
			success = insert(appearance, appearanceId, parent, parentId);

		if (success)
			return appearanceId;
		else
			return 0;
	}

	private boolean insert(Appearance appearance, long appearanceId, CityGMLClass parent, long parentId) throws SQLException {
		// ID
		psAppearance.setLong(1, appearanceId);

		// gml:id
		String origGmlId = appearance.getId();
		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (appearance.isSetId())
				dbImporterManager.putUID(appearance.getId(), appearanceId, -1, false, gmlId, appearance.getCityGMLClass());

			appearance.setId(gmlId);

		} else {
			if (appearance.isSetId())
				dbImporterManager.putUID(appearance.getId(), appearanceId, appearance.getCityGMLClass());
			else
				appearance.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		psAppearance.setString(2, appearance.getId());

		// gml:name
		if (appearance.isSetName()) {
			String[] dbGmlName = Util.codeList2string(appearance.getName());
			psAppearance.setString(3, dbGmlName[0]);
			psAppearance.setString(4, dbGmlName[1]);
		} else {
			psAppearance.setNull(3, Types.VARCHAR);
			psAppearance.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (appearance.isSetDescription()) {
			String description = appearance.getDescription().getValue();
			if (description != null)
				description = description.trim();

			psAppearance.setString(5, description);
		} else {
			psAppearance.setNull(5, Types.VARCHAR);
		}

		// app:theme
		psAppearance.setString(6, appearance.getTheme());

		// cityobject or citymodel id
		switch (parent) {
		case CITY_MODEL:
			psAppearance.setNull(7, Types.INTEGER);
			psAppearance.setNull(8, Types.INTEGER);
			break;
		case ABSTRACT_CITY_OBJECT:
			psAppearance.setNull(7, Types.INTEGER);
			psAppearance.setLong(8, parentId);
			break;
		default:
			return false;
		}

		psAppearance.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.APPEARANCE);

		// surfaceData members
		if (appearance.isSetSurfaceDataMember()) {
			for (SurfaceDataProperty surfaceDataProp : appearance.getSurfaceDataMember()) {
				if (surfaceDataProp.isSetSurfaceData()) {
					String gmlId = surfaceDataProp.getSurfaceData().getId();
					long id = surfaceDataImporter.insert(surfaceDataProp.getSurfaceData(), appearanceId, parent == CityGMLClass.ABSTRACT_CITY_OBJECT);
					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								CityGMLClass.APPEARANCE, 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								surfaceDataProp.getSurfaceData().getCityGMLClass(), 
								gmlId));								

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					surfaceDataProp.unsetSurfaceData();
				} else {
					// xlink
					String href = surfaceDataProp.getHref();					

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
								appearanceId,
								TableEnum.APPEARANCE,
								href,
								TableEnum.SURFACE_DATA
								));
					}
				}
			}
		}

		dbImporterManager.updateFeatureCounter(CityGMLClass.APPEARANCE, appearanceId, origGmlId, parent == CityGMLClass.CITY_MODEL);
		return true;
	}


	@Override
	public void executeBatch() throws SQLException {
		psAppearance.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psAppearance.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.APPEARANCE;
	}

}
