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
package org.citydb.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.Config;
import org.citydb.query.Query;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;

import vcs.sqlbuilder.expression.PlaceHolder;

public class DBLocalAppearance extends AbstractAppearanceExporter {

	public DBLocalAppearance(Connection connection, Query query, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		super(false, connection, query, null, exporter, config);
	}

	public void read(AbstractCityObject cityObject, long cityObjectId, boolean isTopLevelObject, boolean lazyExport) throws CityGMLExportException, SQLException {
		// clear texture image cache
		if (isTopLevelObject)
			clearTextureImageCache();

		List<PlaceHolder<?>> themes = getThemeTokens();
		ps.setLong(1, cityObjectId);
		for (int i = 0; i < themes.size(); i++)
			ps.setString(i + 2, (String)themes.get(i).getValue());

		try (ResultSet rs = ps.executeQuery()) {
			long currentAppearanceId = 0;
			Appearance appearance = null;
			final HashMap<Long, Appearance> appearances = new HashMap<>();

			while (rs.next()) {
				long appearanceId = rs.getLong(1);

				if (appearanceId != currentAppearanceId) {
					currentAppearanceId = appearanceId;
					
					appearance = appearances.get(appearanceId);
					if (appearance == null) {
						appearance = new Appearance();
						getAppearanceProperties(appearance, appearanceId, rs);

						// add appearance to cityobject
						cityObject.addAppearance(new AppearanceProperty(appearance));

						appearances.put(appearanceId, appearance);
					}
				}

				// add surface data to appearance
				addSurfaceData(appearance, rs, lazyExport);
			}
		}
	}
}
