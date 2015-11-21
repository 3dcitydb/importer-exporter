package org.citydb.database;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citydb.api.database.DatabaseConnectionWarning;
import org.citydb.api.database.DatabaseConnectionWarning.ConnectionWarningType;
import org.citydb.api.database.DatabaseVersion;
import org.citydb.api.database.DatabaseVersionChecker;
import org.citydb.api.database.DatabaseVersionException;
import org.citydb.config.language.Language;
import org.citydb.util.Util;

public class DefaultDatabaseVersionChecker implements DatabaseVersionChecker {
	private final List<DatabaseVersion> supportedVersions = Arrays.asList(new DatabaseVersion(3,1,0), new DatabaseVersion(3,0,0));

	@Override
	public void checkIfSupported(DatabaseVersion version, String productName) throws DatabaseVersionException {
		if (!supportedVersions.contains(version)) {
			String message = "The version " + version + " of the " + productName + " is not supported.";
			
			String text = Language.I18N.getString("db.dialog.error.version.error");
			Object[] args = new Object[]{ version, Util.collection2string(supportedVersions, ", ") };
			String formattedMessage = MessageFormat.format(text, args);	
		
			throw new DatabaseVersionException(message, formattedMessage, new ArrayList<DatabaseVersion>(supportedVersions));
		}
	}
	
	@Override
	public void checkIfOutdated(DatabaseVersion version, String productName) throws DatabaseConnectionWarning {
		for (DatabaseVersion tmp : supportedVersions) {
			if (tmp.compareTo(version) > 0) {
				String message = "The version " + version + " of the " + productName + " is outdated. Consider upgrading.";
				
				String text = Language.I18N.getString("db.dialog.warn.version.outdated");
				Object[] args = new Object[]{ version };
				String formattedMessage = MessageFormat.format(text, args);
				
				throw new DatabaseConnectionWarning(message, formattedMessage, ConnectionWarningType.OUTDATED_DATABASE_VERSION);				
			}
		}
	}
	
}
