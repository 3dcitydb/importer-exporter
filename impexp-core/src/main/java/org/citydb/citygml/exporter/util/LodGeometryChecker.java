/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.citygml.exporter.util;

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.LodRepresentation;
import org.citygml4j.util.walker.FeatureWalker;

public class LodGeometryChecker extends FeatureWalker {
	private final SchemaMapping schemaMapping;
	private final LodWalker lodWalker;

	public LodGeometryChecker(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
		lodWalker = new LodWalker();
	}

	public boolean satisfiesLodFilter(AbstractCityObject cityObject) {
		if (cityObject.getLodRepresentation().hasRepresentations())
			return true;

		lodWalker.reset(cityObject);
		cityObject.accept(lodWalker);

		if (!lodWalker.foundGeometry) {
			FeatureType featureType = schemaMapping.getFeatureType(Util.getObjectClassId(cityObject.getClass()));
			if (!featureType.hasLodProperties())
				return true;
		}

		return lodWalker.foundGeometry;
	}

	private final class LodWalker extends FeatureWalker {
		private AbstractCityObject root;
		private boolean foundGeometry = false;

		@Override
		public void visit(AbstractCityObject cityObject) {
			if (cityObject != root) {
				LodRepresentation lodRepresentation = cityObject.getLodRepresentation();
				if (lodRepresentation.hasRepresentations()) {
					foundGeometry = true;
					shouldWalk = false;
				}
			}
		}

		public void reset(AbstractCityObject cityObject) {
			super.reset();
			root = cityObject;
			foundGeometry = false;
		}
	}
}
