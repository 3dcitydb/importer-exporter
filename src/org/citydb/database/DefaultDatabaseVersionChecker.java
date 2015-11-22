package org.citydb.database;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citydb.api.database.DatabaseAdapter;
import org.citydb.api.database.DatabaseConnectionWarning;
import org.citydb.api.database.DatabaseConnectionWarning.ConnectionWarningType;
import org.citydb.api.database.DatabaseVersion;
import org.citydb.api.database.DatabaseVersionChecker;
import org.citydb.api.database.DatabaseVersionException;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Database;
import org.citydb.util.Util;

public class DefaultDatabaseVersionChecker implements DatabaseVersionChecker {
	private final List<DatabaseVersion> supportedVersions = Arrays.asList(new DatabaseVersion(3,1,0), new DatabaseVersion(3,0,0));

	@Override
	public List<DatabaseConnectionWarning> checkVersionSupport(DatabaseAdapter databaseAdapter) throws DatabaseVersionException {
		DatabaseVersion version = databaseAdapter.getConnectionMetaData().getCityDBVersion();
		List<DatabaseConnectionWarning> warnings = new ArrayList<DatabaseConnectionWarning>();
		
		// check for unsupported version
		if (!supportedVersions.contains(version)) {
			String message = "The version " + version + " of the " + Database.CITYDB_PRODUCT_NAME + " is not supported.";
			
			String text = Language.I18N.getString("db.dialog.error.version.error");
			Object[] args = new Object[]{ version, Util.collection2string(supportedVersions, ", ") };
			String formattedMessage = MessageFormat.format(text, args);	
		
			throw new DatabaseVersionException(message, formattedMessage, Database.CITYDB_PRODUCT_NAME, new ArrayList<DatabaseVersion>(supportedVersions));
		}
		
		// check for outdated version
		for (DatabaseVersion tmp : supportedVersions) {
			if (tmp.compareTo(version) > 0) {
				String message = "The version " + version + " of the " + Database.CITYDB_PRODUCT_NAME + " is outdated. Consider upgrading.";
				
				String text = Language.I18N.getString("db.dialog.warn.version.outdated");
				Object[] args = new Object[]{ version };
				String formattedMessage = MessageFormat.format(text, args);
				
				warnings.add(new DatabaseConnectionWarning(message, formattedMessage, Database.CITYDB_PRODUCT_NAME, ConnectionWarningType.OUTDATED_DATABASE_VERSION));				
				break;
			}
		}
		
		return warnings;
	}

	@Override
	public List<DatabaseVersion> getSupportedVersions(String productName) {
		return Database.CITYDB_PRODUCT_NAME.equals(productName) ? new ArrayList<DatabaseVersion>(supportedVersions) : Collections.<DatabaseVersion>emptyList();
	}
	
}
