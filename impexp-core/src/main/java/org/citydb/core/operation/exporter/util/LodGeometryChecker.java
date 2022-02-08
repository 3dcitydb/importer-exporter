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
package org.citydb.core.operation.exporter.util;

import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.exporter.database.content.CityGMLExportManager;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.lod.LodFilterMode;
import org.citydb.core.query.filter.lod.LodIterator;
import org.citydb.core.util.Util;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.LodRepresentation;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.base.ModelObjects;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.child.ChildInfo;
import org.citygml4j.util.walker.FeatureWalker;
import org.citygml4j.util.walker.GMLWalker;
import org.citygml4j.util.walker.GeometryWalker;

import java.util.*;

public class LodGeometryChecker extends FeatureWalker {
	private final SchemaMapping schemaMapping;
	private final LodFilter lodFilter;
	private final ChildInfo childInfo;
	private final CityGMLVersion targetVersion;
	private final LodFilterMode mode;
	private final boolean deleteGeometries;

	private LodIterator lodIterator;

	public LodGeometryChecker(CityGMLExportManager manager, SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;

		lodFilter = manager.getLodFilter();
		childInfo = new ChildInfo();
		targetVersion = manager.getTargetCityGMLVersion();
		mode = lodFilter.getFilterMode();
		deleteGeometries = mode == LodFilterMode.MINIMUM || mode == LodFilterMode.MAXIMUM;

		if (deleteGeometries)
			lodIterator = lodFilter.iterator(0, 4, mode == LodFilterMode.MAXIMUM);
	}

	public void cleanupCityObjects(AbstractGML parent) {
		Map<AbstractCityObject, LodRepresentation> cityObjects = new IdentityHashMap<>();
		Map<String, AbstractGeometry> deletedGeometries = null;
		int targetLod = -1;

		// collect all city objects and their LoD representations
		parent.accept(new GMLWalker() {
			@Override
			public void visit(AbstractCityObject cityObject) {
				cityObjects.put(cityObject, cityObject.getLodRepresentation());
			}
		});

		// delete all geometries not belonging to the target LoD
		if (deleteGeometries) {
			targetLod = getTargetLod(cityObjects);
			deletedGeometries = deleteGeometries(cityObjects, targetLod);
		}

		// delete city objects not having a matching LoD
		deleteCityObjects(cityObjects, targetLod, parent);

		// resolve cross-LoD-XLinks
		if (deleteGeometries)
			resolveCrossLodReferences(deletedGeometries, parent);
	}

	private int getTargetLod(Map<AbstractCityObject, LodRepresentation> cityObjects) {
		int targetLod = mode == LodFilterMode.MINIMUM ? Integer.MAX_VALUE : -Integer.MAX_VALUE;
		int selectedLod = mode == LodFilterMode.MINIMUM ?
				lodFilter.getMinimumLod() :
				lodFilter.getMaximumLod();

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

		return targetLod;
	}

	private Map<String, AbstractGeometry> deleteGeometries(Map<AbstractCityObject, LodRepresentation> cityObjects, int targetLod) {
		Map<String, AbstractGeometry> geometries = new HashMap<>();
		for (Map.Entry<AbstractCityObject, LodRepresentation> entry : cityObjects.entrySet()) {
			LodRepresentation representation = entry.getValue();
			lodIterator.reset();

			while (lodIterator.hasNext()) {
				int lod = lodIterator.next();
				if (lod != targetLod) {
					for (GeometryProperty<?> property : representation.getGeometry(lod)) {
						if (property.isSetGeometry()) {
							// the target LoD might referece geometries that we are about to delete.
							// Thus, we must keep the deleted geometries to be able to resolve them later.
							property.getGeometry().accept(new GeometryWalker() {
								@Override
								public void visit(AbstractGeometry geometry) {
									if (geometry.isSetId())
										geometries.put("#" + geometry.getId(), geometry);
								}
							});
						}

						ModelObjects.unsetProperty(entry.getKey(), property);
					}
				}
			}
		}

		return geometries;
	}

	private void deleteCityObjects(Map<AbstractCityObject, LodRepresentation> cityObjects, int targetLod, AbstractGML parent) {
		// filter city objects that have a matching LoD and thus can be kept
		Set<AbstractCityObject> keep = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Map.Entry<AbstractCityObject, LodRepresentation> entry : cityObjects.entrySet()) {
			AbstractCityObject cityObject = entry.getKey();
			LodRepresentation representation = entry.getValue();
			FeatureType featureType = schemaMapping.getFeatureType(Util.getObjectClassId(cityObject.getClass()));

			if (!featureType.hasLodProperties()
					|| (!deleteGeometries && representation.hasRepresentations())
					|| !representation.getGeometry(targetLod).isEmpty()) {
				do {
					// keep the city object and its parents
					if (!keep.add(cityObject))
						break;
				} while ((cityObject = childInfo.getParentCityObject(cityObject)) != parent);
			}
		}

		// delete all other city objects
		for (AbstractCityObject cityObject : cityObjects.keySet()) {
			if (cityObject != parent) {
				if (!keep.contains(cityObject)) {
					ModelObject property = cityObject.getParent();
					if (property instanceof Child)
						ModelObjects.unsetProperty(((Child) property).getParent(), property);
				}
			}
		}
	}

	private void resolveCrossLodReferences(Map<String, AbstractGeometry> deletedGeometries, AbstractGML parent) {
		// collect all properties with XLink references to deleted geometries
		Map<AbstractGeometry, List<GeometryProperty<?>>> properties = new IdentityHashMap<>();
		parent.accept(new GMLWalker() {
			@Override
			public <T extends AbstractGeometry> void visit(GeometryProperty<T> property) {
				if (!property.isSetGeometry()) {
					AbstractGeometry geometry = deletedGeometries.get(property.getHref());
					if (geometry != null && property.getAssociableClass().isInstance(geometry))
						properties.computeIfAbsent(geometry, v -> new ArrayList<>()).add(property);
				}

				super.visit(property);
			}
		});

		// resolve the references
		for (Map.Entry<AbstractGeometry, List<GeometryProperty<?>>> entry : properties.entrySet()) {
			GeometryProperty<?> property = null;

			// CityGML enforces the direction of XLinks if a feature has boundary surfaces.
			// So, if we find more than one property referencing the same geometry, we must check this.
			if (entry.getValue().size() > 1) {
				Class<? extends AbstractCityObject> type = null;
				boolean assignToBoundarySurface = true;

				if (parent instanceof AbstractBuilding)
					type = org.citygml4j.model.citygml.building.AbstractBoundarySurface.class;
				else if (parent instanceof AbstractBridge)
					type = org.citygml4j.model.citygml.bridge.AbstractBoundarySurface.class;
				else if (parent instanceof AbstractTunnel)
					type = org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface.class;
				else if (parent instanceof WaterBody) {
					type = AbstractWaterBoundarySurface.class;
					assignToBoundarySurface = targetVersion != CityGMLVersion.v1_0_0;
				}

				if (type != null) {
					for (GeometryProperty<?> candidate : entry.getValue()) {
						AbstractCityObject boundarySurface = childInfo.getParentCityObject(candidate, type);
						if ((boundarySurface != null && assignToBoundarySurface)
								|| (boundarySurface == null && !assignToBoundarySurface)) {
							property = candidate;
							break;
						}
					}
				}
			}

			if (property == null)
				property = entry.getValue().get(0);

			property.setObjectIfValid(entry.getKey());
			property.unsetHref();
		}
	}
}
