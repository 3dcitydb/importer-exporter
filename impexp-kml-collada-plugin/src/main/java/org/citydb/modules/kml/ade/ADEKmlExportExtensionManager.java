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
package org.citydb.modules.kml.ade;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.project.ade.ADEKmlExporterPreference;
import org.citydb.registry.ObjectRegistry;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

public class ADEKmlExportExtensionManager {
	private static ADEKmlExportExtensionManager instance;
	private final IdentityHashMap<ADEKmlExportExtension, ADEKmlExportManager> adeKmlExportManagers = new IdentityHashMap<>();

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

	public ADEKmlExporterPreference getPreference(Config config, int objectClassId) {
		ADEKmlExporterPreference preference = null;
		String target = ObjectRegistry.getInstance().getSchemaMapping().getFeatureType(objectClassId).toString();

		ADEExtension adeExtension = ADEExtensionManager.getInstance().getExtensionByObjectClassId(objectClassId);
		if (adeExtension != null) {
			String extensionId = ADEExtensionManager.getInstance().getExtensionByObjectClassId(objectClassId).getId();
			preference = config.getProject().getAdeExtensions().get(extensionId).getKmlExporter().getPreferences().get(target);
		}

		return preference;
	}

}
