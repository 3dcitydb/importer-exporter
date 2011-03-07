package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.relief.ReliefComponentProperty;
import de.tub.citygml4j.model.citygml.relief.ReliefFeature;

public class DBReliefFeature implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psReliefFeature;
	private DBCityObject cityObjectImporter;
	private DBReliefComponent reliefComponentImporter;
	
	private String gmlNameDelimiter;
	private int batchCounter;

	public DBReliefFeature(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}
	
	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();

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
		// CityObject
		long cityObjectId = cityObjectImporter.insert(reliefFeature, reliefFeatureId);
		if (cityObjectId == 0)
			return false;

		// ReliefFeature
		// ID
		psReliefFeature.setLong(1, cityObjectId);

		// gml:name
		if (reliefFeature.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(reliefFeature, gmlNameDelimiter);

			psReliefFeature.setString(2, dbGmlName[0]);
			psReliefFeature.setString(3, dbGmlName[1]);
		} else {
			psReliefFeature.setNull(2, Types.VARCHAR);
			psReliefFeature.setNull(3, Types.VARCHAR);
		}
		
		// gml:description
		if (reliefFeature.getDescription() != null) {
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
		List<ReliefComponentProperty> reliefComponentPropertyList = reliefFeature.getReliefComponent();
		if (reliefComponentPropertyList != null) {
			for (ReliefComponentProperty property : reliefComponentPropertyList) {
				if (property.getObject() != null) {
					long id = reliefComponentImporter.insert(property.getObject(), reliefFeatureId);
					if (id == 0)
        				System.out.println("Could not write ReliefComponent");
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
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.RELIEF_FEATURE;
	}

}
