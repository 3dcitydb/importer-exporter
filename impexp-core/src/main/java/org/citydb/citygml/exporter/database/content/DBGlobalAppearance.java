/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.Config;
import org.citydb.query.Query;
import org.citygml4j.model.citygml.appearance.Appearance;

public class DBGlobalAppearance extends AbstractAppearanceExporter {

	public DBGlobalAppearance(Connection connection, Query query, CacheTable cacheTable, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		super(true, connection, query, cacheTable, exporter, config);
	}

	protected Appearance doExport(long appearanceId) throws CityGMLExportException, SQLException {
		ps.setLong(1, appearanceId);

		try (ResultSet rs = ps.executeQuery()) {
			Appearance appearance = new Appearance();
			boolean isInited = false;

			while (rs.next()) {
				if (!isInited) {
					getAppearanceProperties(appearance, appearanceId, rs);
					clearTextureImageCache();
					isInited = true;
				}

				// add surface data to appearance
				addSurfaceData(appearance, rs, false);
			}

			return appearance.isSetSurfaceDataMember() ? appearance : null;
		}
	}
}
