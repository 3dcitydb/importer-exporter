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
package org.citydb.core.ade.visExporter;

import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.project.visExporter.ADEPreference;
import org.citydb.config.project.visExporter.ADEPreferences;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.registry.ObjectRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class ADEVisExportExtensionManager {
	private static ADEVisExportExtensionManager instance;

	public static synchronized ADEVisExportExtensionManager getInstance() {
		if (instance == null)
			instance = new ADEVisExportExtensionManager();

		return instance;
	}

	public ADEVisExportExtension getADEVisExportExtension(ADEExtension extension) {
		if (extension != null && extension.isEnabled() && extension instanceof ADEVisExportExtension)
			return ((ADEVisExportExtension) extension);

		return null;
	}

	public List<ADEExtension> getUnsupportedADEExtensions() {
		return ADEExtensionManager.getInstance().getEnabledExtensions()
				.stream().filter(ade -> getADEVisExportExtension(ade) == null)
				.collect(Collectors.toList());
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
			return config.getVisExportConfig().getADEPreferences()
					.computeIfAbsent(adeExtension.getId(), v -> new ADEPreferences(adeExtension.getId()))
					.getPreferences()
					.computeIfAbsent(target, v -> new ADEPreference(target));
		} else
			return new ADEPreference(target);
	}
}
