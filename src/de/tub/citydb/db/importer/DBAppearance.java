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
import de.tub.citydb.util.UUIDManager;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.appearance.Appearance;
import de.tub.citygml4j.model.citygml.appearance.SurfaceDataProperty;

public class DBAppearance implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAppearance;
	private DBSurfaceData surfaceDataImporter;

	private boolean replaceGmlId;
	private String gmlNameDelimiter;
	private int batchCounter;

	public DBAppearance(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();

		if (gmlIdCodespace != null && gmlIdCodespace.length() != 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "'";
		else
			gmlIdCodespace = "null";

		psAppearance = batchConn.prepareStatement("insert into APPEARANCE (ID, GMLID, GMLID_CODESPACE, NAME, NAME_CODESPACE, DESCRIPTION, THEME, CITYMODEL_ID, CITYOBJECT_ID) values " +
			"(?, ?, " + gmlIdCodespace + ", ?, ?, ?, ?, ?, ?)");

		surfaceDataImporter = (DBSurfaceData)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_DATA);
	}

	public long insert(Appearance appearance, CityGMLClass parent, long parentId) throws SQLException {
		long appearanceId = dbImporterManager.getDBId(DBSequencerEnum.APPEARANCE_SEQ);
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
		if (replaceGmlId) {
			String gmlId = UUIDManager.randomUUID();

			// mapping entry
			if (appearance.getId() != null)
				dbImporterManager.putGmlId(appearance.getId(), appearanceId, -1, false, gmlId, appearance.getCityGMLClass());

			appearance.setId(gmlId);

		} else {
			if (appearance.getId() != null)
				dbImporterManager.putGmlId(appearance.getId(), appearanceId, appearance.getCityGMLClass());
			else
				appearance.setId(UUIDManager.randomUUID());
		}

		psAppearance.setString(2, appearance.getId());

		// gml:name
		if (appearance.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(appearance, gmlNameDelimiter);

			psAppearance.setString(3, dbGmlName[0]);
			psAppearance.setString(4, dbGmlName[1]);
		} else {
			psAppearance.setNull(3, Types.VARCHAR);
			psAppearance.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (appearance.getDescription() != null) {
			String description = appearance.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psAppearance.setString(5, description);
		} else {
			psAppearance.setNull(5, Types.VARCHAR);
		}

		// theme
		psAppearance.setString(6, appearance.getTheme());

		// cityobject or citymodel id
		switch (parent) {
		case CITYMODEL:
			psAppearance.setNull(7, Types.INTEGER);
			psAppearance.setNull(8, Types.INTEGER);
			break;
		case CITYOBJECT:
			psAppearance.setNull(7, Types.INTEGER);
			psAppearance.setLong(8, parentId);
			break;
		}

		psAppearance.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.APPEARANCE);

		// surfaceData members
		List<SurfaceDataProperty> surfaceDataMemberList = appearance.getSurfaceDataMember();
		if (surfaceDataMemberList != null) {
			for (SurfaceDataProperty surfaceDataProp : surfaceDataMemberList) {
				if (surfaceDataProp.getSurfaceData() != null) {
					long id = surfaceDataImporter.insert(surfaceDataProp.getSurfaceData(), appearanceId);
					if (id == 0)
						System.out.println("Could not write AbstractSurfaceData.");

				} else {
					// xlink
					String href = surfaceDataProp.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
								appearanceId,
								DBTableEnum.APPEARANCE,
								href,
								DBTableEnum.SURFACE_DATA
						));
					}
				}
			}
		}

		dbImporterManager.updateFeatureCounter(CityGMLClass.APPEARANCE);
		return true;
	}


	@Override
	public void executeBatch() throws SQLException {
		psAppearance.executeBatch();
		batchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.APPEARANCE;
	}

}
