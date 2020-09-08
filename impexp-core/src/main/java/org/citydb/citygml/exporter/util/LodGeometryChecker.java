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
package org.citydb.citygml.exporter.util;

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodFilterMode;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.LodRepresentation;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.base.ModelObjects;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.util.child.ChildInfo;
import org.citygml4j.util.walker.FeatureWalker;
import org.citygml4j.util.walker.GMLWalker;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class LodGeometryChecker extends FeatureWalker {
	private final SchemaMapping schemaMapping;
	private final ChildInfo childInfo;
	private final LodFilterMode mode;
	private final boolean removeGeometries;

	private LodIterator lodIterator;
	private int selectedLod;

	public LodGeometryChecker(LodFilter lodFilter, SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;

		childInfo = new ChildInfo();
		mode = lodFilter.getFilterMode();
		removeGeometries = mode == LodFilterMode.MINIMUM || mode == LodFilterMode.MAXIMUM;

		if (removeGeometries) {
			lodIterator = lodFilter.iterator(0, 4, mode == LodFilterMode.MAXIMUM);
			selectedLod = mode == LodFilterMode.MINIMUM ?
					lodFilter.getMinimumLod() :
					lodFilter.getMaximumLod();
		}
	}

	public void cleanupCityObjects(AbstractGML object) {
		Map<AbstractCityObject, LodRepresentation> cityObjects = new IdentityHashMap<>();
		Set<AbstractCityObject> keep = Collections.newSetFromMap(new IdentityHashMap<>());
		int targetLod = -1;

		// collect all city objects with their LoD representations
		object.accept(new GMLWalker() {
			@Override
			public void visit(AbstractCityObject cityObject) {
				cityObjects.put(cityObject, cityObject.getLodRepresentation());
			}
		});

		if (removeGeometries) {
			// determine target LoD
			targetLod = mode == LodFilterMode.MINIMUM ? Integer.MAX_VALUE : -Integer.MAX_VALUE;
			for (LodRepresentation representation : cityObjects.values()) {
				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();
					if (!representation.getGeometry(lod).isEmpty()) {
						if ((mode == LodFilterMode.MINIMUM && lod < targetLod)
								|| (mode == LodFilterMode.MAXIMUM && lod > targetLod))
							targetLod = lod;

						break;
					}
				}

				if (targetLod == selectedLod)
					break;
			}

			// remove all LoDs besides the target LoD
			for (Map.Entry<AbstractCityObject, LodRepresentation> entry : cityObjects.entrySet()) {
				LodRepresentation representation = entry.getValue();
				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();
					if (lod != targetLod) {
						for (GeometryProperty<?> property : representation.getGeometry(lod))
							ModelObjects.unsetProperty(entry.getKey(), property);
					}
				}
			}
		}

		// build list of city objects that can be kept
		for (Map.Entry<AbstractCityObject, LodRepresentation> entry : cityObjects.entrySet()) {
			AbstractCityObject cityObject = entry.getKey();
			LodRepresentation representation = entry.getValue();
			FeatureType featureType = schemaMapping.getFeatureType(Util.getObjectClassId(cityObject.getClass()));

			if (!featureType.hasLodProperties()
					|| (!removeGeometries && representation.hasRepresentations())
					|| !representation.getGeometry(targetLod).isEmpty()) {
				do {
					// keep the city object and its parents
					if (!keep.add(cityObject))
						break;
				} while ((cityObject = childInfo.getParentCityObject(cityObject)) != object);
			}
		}

		// clean up city objects
		for (AbstractCityObject cityObject : cityObjects.keySet()) {
			if (cityObject != object) {
				if (!keep.contains(cityObject)) {
					ModelObject property = cityObject.getParent();
					if (property instanceof Child)
						ModelObjects.unsetProperty(((Child) property).getParent(), property);
				}
			}
		}
	}
}
