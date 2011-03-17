package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.relief.ReliefComponent;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.log.Logger;
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
		long cityObjectId = cityObjectImporter.insert(reliefFeature, reliefFeatureId);
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
				ReliefComponent component = property.getObject();
				
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
				} else {
					// xlink
        			String href = property.getHref();

        			if (href != null && href.length() != 0) {
        				dbImporterManager.propagateXlink(new DBXlinkBasic(
        						reliefFeatureId,
        						DBTableEnum.RELIEF_FEATURE,
        						href,
        						DBTableEnum.RELIEF_COMPONENT
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
