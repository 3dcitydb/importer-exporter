package org.citydb.query.geometry;

import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.adapter.AbstractDatabaseAdapter;

public class DatabaseSrsParser extends SrsNameParser {
	private final AbstractDatabaseAdapter databaseAdapter;
	private final Config config;

	public DatabaseSrsParser(AbstractDatabaseAdapter databaseAdapter, Config config) {
		this.databaseAdapter = databaseAdapter;
		this.config = config;
	}

	public DatabaseSrs getDefaultSRS() {
		return databaseAdapter.getConnectionMetaData().getReferenceSystem();
	}

	public DatabaseSrs getDatabaseSRS(String srsName) throws SrsParseException {
		if (srsName.equals(databaseAdapter.getConnectionMetaData().getReferenceSystem().getGMLSrsName()))
			return databaseAdapter.getConnectionMetaData().getReferenceSystem();

		int epsgCode = getEPSGCode(srsName);
		if (epsgCode == databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid())
			return databaseAdapter.getConnectionMetaData().getReferenceSystem();

		// check whether SRS is supported by database
		DatabaseSrs targetSRS = null;
		for (DatabaseSrs srs: config.getProject().getDatabase().getReferenceSystems()) {
			if (srs.getSrid() == epsgCode) {
				if (!srs.isSupported())
					throw new SrsParseException("The CRS '" + srsName + "' is advertised but not supported by the database.");

				targetSRS = srs;
				break;
			}
		}

		if (targetSRS == null)
			throw new SrsParseException("The CRS '" + srsName + "' is not advertised.");

		return targetSRS;
	}

}
