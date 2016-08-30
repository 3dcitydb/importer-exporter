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
import org.citydb.api.database.DatabaseVersionSupport;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Database;
import org.citydb.util.Util;

public class DefaultDatabaseVersionChecker implements DatabaseVersionChecker {
	private final DatabaseVersionSupport[] supportedVersions = new DatabaseVersionSupport[]{
			DatabaseVersionSupport.targetVersion(3, 3, 0).withBackwardsCompatibility(3, 0, 0).withRevisionForwardCompatibility(true)
	};

	@Override
	public List<DatabaseConnectionWarning> checkVersionSupport(DatabaseAdapter databaseAdapter) throws DatabaseVersionException {
		DatabaseVersion version = databaseAdapter.getConnectionMetaData().getCityDBVersion();
		List<DatabaseConnectionWarning> warnings = new ArrayList<DatabaseConnectionWarning>();

		// check for unsupported version
		if (!version.isSupportedBy(supportedVersions)) {
			String message = "The version " + version + " of the " + Database.CITYDB_PRODUCT_NAME + " is not supported.";

			String text = Language.I18N.getString("db.dialog.error.version.error");
			Object[] args = new Object[]{ version, Database.CITYDB_PRODUCT_NAME, Util.collection2string(Arrays.asList(supportedVersions), ", ") };
			String formattedMessage = MessageFormat.format(text, args);	

			throw new DatabaseVersionException(message, formattedMessage, Database.CITYDB_PRODUCT_NAME, Arrays.asList(supportedVersions));
		}

		// check for outdated version
		for (DatabaseVersionSupport supportedVersion : supportedVersions) {
			if (supportedVersion.getTargetVersion().compareTo(version) > 0) {
				String message = "The version " + version + " of the " + Database.CITYDB_PRODUCT_NAME + " is outdated. Consider upgrading.";

				String text = Language.I18N.getString("db.dialog.warn.version.outdated");
				Object[] args = new Object[]{ version, Database.CITYDB_PRODUCT_NAME };
				String formattedMessage = MessageFormat.format(text, args);

				warnings.add(new DatabaseConnectionWarning(message, formattedMessage, Database.CITYDB_PRODUCT_NAME, ConnectionWarningType.OUTDATED_DATABASE_VERSION));				
				break;
			}
		}

		return warnings;
	}

	@Override
	public List<DatabaseVersionSupport> getSupportedVersions(String productName) {
		return Database.CITYDB_PRODUCT_NAME.equals(productName) ? Arrays.asList(supportedVersions) : Collections.<DatabaseVersionSupport>emptyList();
	}

}
