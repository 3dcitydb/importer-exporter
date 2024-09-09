/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.database.version;

import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionWarning;
import org.citydb.core.database.connection.DatabaseConnectionWarning.ConnectionWarningType;
import org.citydb.core.util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultDatabaseVersionChecker implements DatabaseVersionChecker {
    private final DatabaseVersionSupport[] supportedVersions = new DatabaseVersionSupport[]{
            DatabaseVersionSupport.targetVersion(4, 4, 0).withBackwardsCompatibility(4, 0, 0).withRevisionForwardCompatibility(true),
            DatabaseVersionSupport.targetVersion(3, 3, 1).withBackwardsCompatibility(3, 0, 0).withRevisionForwardCompatibility(true)
    };

    @Override
    public List<DatabaseConnectionWarning> checkVersionSupport(AbstractDatabaseAdapter databaseAdapter) throws DatabaseVersionException {
        DatabaseVersion version = databaseAdapter.getConnectionMetaData().getCityDBVersion();
        List<DatabaseConnectionWarning> warnings = new ArrayList<DatabaseConnectionWarning>();

        // check for unsupported version
        if (!version.isSupportedBy(supportedVersions)) {
            String message = "The version " + version + " of the " + DatabaseConfig.CITYDB_PRODUCT_NAME + " is not supported.";

            String text = Language.I18N.getString("db.dialog.error.version.error");
            Object[] args = new Object[]{version, DatabaseConfig.CITYDB_PRODUCT_NAME, Util.collection2string(Arrays.asList(supportedVersions), ", ")};
            String formattedMessage = MessageFormat.format(text, args);

            throw new DatabaseVersionException(message, formattedMessage, DatabaseConfig.CITYDB_PRODUCT_NAME, Arrays.asList(supportedVersions));
        }

        // check for outdated version
        for (DatabaseVersionSupport supportedVersion : supportedVersions) {
            if (supportedVersion.getTargetVersion().compareTo(version) > 0) {
                String message = "The version " + version + " of the " + DatabaseConfig.CITYDB_PRODUCT_NAME + " is out of date. Consider upgrading.";

                String text = Language.I18N.getString("db.dialog.warn.version.outofdate");
                Object[] args = new Object[]{version, DatabaseConfig.CITYDB_PRODUCT_NAME};
                String formattedMessage = MessageFormat.format(text, args);

                warnings.add(new DatabaseConnectionWarning(message, formattedMessage, DatabaseConfig.CITYDB_PRODUCT_NAME, ConnectionWarningType.OUTDATED_DATABASE_VERSION));
                break;
            }
        }

        return warnings;
    }

    @Override
    public List<DatabaseVersionSupport> getSupportedVersions(String productName) {
        return DatabaseConfig.CITYDB_PRODUCT_NAME.equals(productName) ? Arrays.asList(supportedVersions) : Collections.<DatabaseVersionSupport>emptyList();
    }

}
