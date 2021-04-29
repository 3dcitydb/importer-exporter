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
package org.citydb.ade.kmlExporter;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.registry.ObjectRegistry;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ADEBalloonExtensionManager {
	private static ADEBalloonExtensionManager instance;
	private final IdentityHashMap<ADEBalloonExtension, ADEBalloonManager> balloonManagers = new IdentityHashMap<>();

	public static synchronized ADEBalloonExtensionManager getInstance() {
		if (instance == null)
			instance = new ADEBalloonExtensionManager();

		return instance;
	}

	public ADEBalloonExtension getBalloonExtension(ADEExtension extension) {
		if (extension != null && extension.isEnabled() && extension instanceof ADEBalloonExtension)
			return ((ADEBalloonExtension) extension);

		return null;
	}

	public List<ADEExtension> getUnsupportedADEExtensions() {
		return ADEExtensionManager.getInstance().getEnabledExtensions()
				.stream().filter(ade -> getBalloonExtension(ade) == null)
				.collect(Collectors.toList());
	}

	public synchronized ADEBalloonManager getBalloonManager(ADEExtension adeExtension) {
		ADEBalloonManager adeBalloonManager = null;
		ADEBalloonExtension adeBalloonExtension = getBalloonExtension(adeExtension);

		if (adeBalloonExtension != null) {
			adeBalloonManager = balloonManagers.get(adeBalloonExtension);
			if (adeBalloonManager == null) {
				adeBalloonManager = adeBalloonExtension.createBalloonManager();
				if (adeBalloonManager != null)
					balloonManagers.put(adeBalloonExtension, adeBalloonManager);
			}
		}

		return adeBalloonManager;
	}

	public ADEBalloonManager getBalloonManager(int objectClassId) throws ADEBalloonException {
		ADEExtension adeExtension = ADEExtensionManager.getInstance().getExtensionByObjectClassId(objectClassId);
		ADEBalloonManager adeBalloonManager = this.getBalloonManager(adeExtension);
		if (adeBalloonManager == null) {
			throw new ADEBalloonException("The Balloon-Export extension is not enabled " +
					"for the ADE class '" + ObjectRegistry.getInstance().getSchemaMapping().getFeatureType(objectClassId).getPath() + "'.");
		}
		return adeBalloonManager;
	}

	public ADEBalloonManager getBalloonManager(String table) throws ADEBalloonException {
		ADEExtension adeExtension = ADEExtensionManager.getInstance().getExtensionByTableName(table);
		ADEBalloonManager adeBalloonManager = this.getBalloonManager(adeExtension);
		if (adeBalloonManager == null) {
			throw new ADEBalloonException("The ADE Balloon extension is not enabled for the ADE table '" + table + "'.");
		}
		return adeBalloonManager;
	}

}
