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
import java.util.Set;

public class LodGeometryChecker extends FeatureWalker {
	private final SchemaMapping schemaMapping;
	private final int targetLoD;

	public LodGeometryChecker(LodFilter lodFilter, SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
		targetLoD = lodFilter.getFilterMode() == LodFilterMode.MINIMUM ? lodFilter.getMinimumLod() :
				lodFilter.getFilterMode() == LodFilterMode.MAXIMUM ? lodFilter.getMaximumLod() : -1;
	}

	public void cleanupCityObjects(AbstractGML object) {
		Set<AbstractCityObject> keep = Collections.newSetFromMap(new IdentityHashMap<>());

		object.accept(new GMLWalker() {
			final ChildInfo childInfo = new ChildInfo();

			@Override
			public void visit(AbstractCityObject cityObject) {
				FeatureType featureType = schemaMapping.getFeatureType(Util.getObjectClassId(cityObject.getClass()));
				if (featureType.hasLodProperties()) {
					LodRepresentation representation = cityObject.getLodRepresentation();
					if (!representation.hasRepresentations())
						return;

					if (targetLoD != -1) {
						for (int lod = 0; lod < 5; lod++) {
							if (lod != targetLoD) {
								for (GeometryProperty<?> property : representation.getGeometry(lod))
									ModelObjects.unsetProperty(cityObject, property);
							}
						}

						if (representation.getGeometry(targetLoD).isEmpty())
							return;
					}
				}

				do {
					// keep the city object and its parents
					if (!keep.add(cityObject))
						break;
				} while ((cityObject = childInfo.getParentCityObject(cityObject)) != object);
			}
		});

		object.accept(new GMLWalker() {
			@Override
			public void visit(AbstractCityObject cityObject) {
				if (cityObject != object) {
					if (!keep.contains(cityObject)) {
						ModelObject property = cityObject.getParent();
						if (property instanceof Child)
							ModelObjects.unsetProperty(((Child) property).getParent(), property);
					}
				}
			}
		});
	}
}
