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
package org.citydb.ade.kmlExporter;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.ADEPreference;
import org.citydb.config.project.kmlExporter.ADEPreferences;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.registry.ObjectRegistry;

public class ADEKmlExportExtensionManager {
	private static ADEKmlExportExtensionManager instance;

	public static synchronized ADEKmlExportExtensionManager getInstance() {
		if (instance == null)
			instance = new ADEKmlExportExtensionManager();

		return instance;
	}

	public ADEKmlExportExtension getADEKmlExportExtension(ADEExtension extension) {
		if (extension != null && extension.isEnabled() && extension instanceof ADEKmlExportExtension)
			return ((ADEKmlExportExtension) extension);

		return null;
	}

	public ADEPreference getPreference(Config config, FeatureType featureType) {
		return getPreference(config, featureType.getObjectClassId(), featureType.toString());
	}

	public ADEPreference getPreference(Config config, int objectClassId) {
		return getPreference(config, objectClassId, ObjectRegistry.getInstance().getSchemaMapping().getFeatureType(objectClassId).toString());
	}

	private ADEPreference getPreference(Config config, int objectClassId, String target) {
		ADEExtension adeExtension = ADEExtensionManager.getInstance().getExtensionByObjectClassId(objectClassId);
		if (adeExtension != null) {
			return config.getProject().getKmlExportConfig().getADEPreferences()
					.computeIfAbsent(adeExtension.getId(), v -> new ADEPreferences(adeExtension.getId()))
					.getPreferences()
					.computeIfAbsent(target, v -> new ADEPreference(target));
		} else
			return new ADEPreference(target);
	}
}
