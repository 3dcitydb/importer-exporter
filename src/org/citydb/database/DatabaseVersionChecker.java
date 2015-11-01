package org.citydb.database;

import java.util.List;

import org.citydb.api.database.DatabaseVersion;

public class DatabaseVersionChecker {

	public boolean isSupportedVersion(DatabaseVersion version, List<DatabaseVersion> supportedVersions, String productName) {
		return supportedVersions.contains(version);
	}
	
	public boolean isOutdatedVersion(DatabaseVersion version, List<DatabaseVersion> supportedVersions, String productName) {
		for (DatabaseVersion tmp : supportedVersions) {
			if (tmp.compareTo(version) > 0)
				return true;
		}
		
		return false;
	}
	
}
