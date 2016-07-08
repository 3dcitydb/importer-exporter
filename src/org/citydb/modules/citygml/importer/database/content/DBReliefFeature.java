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

import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;

public class DBReliefFeature implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psReliefFeature;
	private DBCityObject cityObjectImporter;
	private DBReliefComponent reliefComponentImporter;

	private int batchCounter;

	public DBReliefFeature(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psReliefFeature = batchConn.prepareStatement("insert into RELIEF_FEATURE (ID, LOD) values (?, ?)");

		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		reliefComponentImporter = (DBReliefComponent)dbImporterManager.getDBImporter(DBImporterEnum.RELIEF_COMPONENT);
	}

	public long insert(ReliefFeature reliefFeature) throws SQLException {
		long reliefFeatureId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (reliefFeatureId != 0)
			success = insert(reliefFeature, reliefFeatureId);

		if (success)
			return reliefFeatureId;
		else
			return 0;
	}

	private boolean insert(ReliefFeature reliefFeature, long reliefFeatureId) throws SQLException {
		String origGmlId = reliefFeature.getId();

		// CityObject
		long cityObjectId = cityObjectImporter.insert(reliefFeature, reliefFeatureId, true);
		if (cityObjectId == 0)
			return false;

		// ReliefFeature
		// ID
		psReliefFeature.setLong(1, cityObjectId);

		// dem:lod
		psReliefFeature.setInt(2, reliefFeature.getLod());

		psReliefFeature.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.RELIEF_FEATURE);

		// relief component
		if (reliefFeature.isSetReliefComponent()) {
			for (ReliefComponentProperty property : reliefFeature.getReliefComponent()) {
				AbstractReliefComponent component = property.getReliefComponent();

				if (component != null) {
					String gmlId = component.getId();

					if (component.getCityGMLClass() != CityGMLClass.RASTER_RELIEF) {
						long id = reliefComponentImporter.insert(component, reliefFeatureId);

						if (id == 0) {
							StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
									reliefFeature.getCityGMLClass(), 
									origGmlId));
							msg.append(": Failed to write ");
							msg.append(Util.getFeatureSignature(
									component.getCityGMLClass(), 
									gmlId));

							LOG.error(msg.toString());
						}
					} else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								component.getCityGMLClass(), 
								gmlId));
						msg.append(": Raster relief components are not supported.");

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					property.unsetReliefComponent();
				} else {
					// xlink
					String href = property.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
								reliefFeatureId,
								TableEnum.RELIEF_FEATURE,
								href,
								TableEnum.RELIEF_COMPONENT
								));
					}
				}
			}
		}
		
		// insert local appearance
		cityObjectImporter.insertAppearance(reliefFeature, reliefFeatureId);

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psReliefFeature.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psReliefFeature.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.RELIEF_FEATURE;
	}

}
