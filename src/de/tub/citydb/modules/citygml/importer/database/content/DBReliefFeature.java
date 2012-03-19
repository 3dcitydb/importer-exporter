/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

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
		psReliefFeature = batchConn.prepareStatement("insert into RELIEF_FEATURE (ID, NAME, NAME_CODESPACE, DESCRIPTION, LOD) values " +
				"(?, ?, ?, ?, ?)");

		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		reliefComponentImporter = (DBReliefComponent)dbImporterManager.getDBImporter(DBImporterEnum.RELIEF_COMPONENT);
	}
	
	public long insert(ReliefFeature reliefFeature) throws SQLException {
		long reliefFeatureId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
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

		// gml:name
		if (reliefFeature.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(reliefFeature);

			psReliefFeature.setString(2, dbGmlName[0]);
			psReliefFeature.setString(3, dbGmlName[1]);
		} else {
			psReliefFeature.setNull(2, Types.VARCHAR);
			psReliefFeature.setNull(3, Types.VARCHAR);
		}
		
		// gml:description
		if (reliefFeature.isSetDescription()) {
			String description = reliefFeature.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psReliefFeature.setString(4, description);
		} else {
			psReliefFeature.setNull(4, Types.VARCHAR);
		}
		
		// lod
		psReliefFeature.setInt(5, reliefFeature.getLod());
		
		psReliefFeature.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.RELIEF_FEATURE);
		
		// relief component
		if (reliefFeature.isSetReliefComponent()) {
			for (ReliefComponentProperty property : reliefFeature.getReliefComponent()) {
				AbstractReliefComponent component = property.getReliefComponent();
				
				if (component != null) {
					String gmlId = component.getId();
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
