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
package org.citydb.operation.importer.util;

import org.citydb.log.Logger;
import org.citydb.operation.importer.database.content.CityGMLImportManager;
import org.citydb.util.CoreConstants;
import org.citygml4j.builder.copy.CopyBuilder;
import org.citygml4j.builder.copy.ShallowCopyBuilder;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.appearance.AppearanceModuleComponent;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.util.child.ChildInfo;
import org.citygml4j.util.walker.GMLWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class LocalGeometryXlinkResolver {
	private final Logger log = Logger.getInstance();
	private final ResolverWalker resolver;
	
	private AbstractGML rootObject;
	private HashSet<String> targets;
	private HashMap<String, AbstractGeometry> geometries;
	private ChildInfo childInfo;
	private Stack<String> circularTargets;
	private CopyBuilder copyBuilder = new ShallowCopyBuilder();
	private CityGMLImportManager importer;

	private enum ResolverState {
		GET_XLINKS,
		GET_GEOMETRY,
		RESOLVE_XLINKS
	};

	public LocalGeometryXlinkResolver() {
		resolver = new ResolverWalker();
		
		childInfo = new ChildInfo();
		targets = new HashSet<>();
		geometries = new HashMap<>();
		circularTargets = new Stack<>();
	}
	
	public LocalGeometryXlinkResolver(CityGMLImportManager importer) {
		this();
		this.importer = importer;
	}

	public boolean resolveGeometryXlinks(AbstractGML abstractGML) {
		// we follow a three-phase resolving approach which is a 
		// compromise between performance and memory consumption.

		resolver.hasCircularReference = false;
		circularTargets.clear();
		rootObject = abstractGML;
		
		// phase 1: iterate through all elements and detect 
		// xlink references to geometry object
		resolver.state = ResolverState.GET_XLINKS;
		abstractGML.accept(resolver);

		if (targets.isEmpty())
			return true;

		// phase 2: iterate through all elements again and collect
		// the geometry objects for the detected xlinks		
		resolver.reset();
		resolver.state = ResolverState.GET_GEOMETRY;
		abstractGML.accept(resolver);

		// phase 3: finally iterate through all elements once more
		// and actually replace xlinks by geometries
		resolver.reset();
		resolver.state = ResolverState.RESOLVE_XLINKS;
		abstractGML.accept(resolver);

		// clean up
		targets.clear();
		geometries.clear();
		if (!resolver.hasCircularReference)
			circularTargets.clear();

		return !resolver.hasCircularReference;
	}

	public List<String> getCircularReferences() {
		return new ArrayList<>(circularTargets);
	}

	private class ResolverWalker extends GMLWalker {
		private boolean hasCircularReference = false;
		private ResolverState state;

		@Override
		public <T extends AbstractGeometry> void visit(GeometryProperty<T> property) {
			if (!property.isSetGeometry() && property.isSetHref()) {
				if (state == ResolverState.RESOLVE_XLINKS && shouldWalk()) {
					final String target = clipGMLId(property.getHref());
					final AbstractGeometry geometry = geometries.get(target);

					if (geometry != null) {
						// check whether the type of the referenced geometry is allowed
						if (property.getAssociableClass().isInstance(geometry)) {

							// check whether we have already seen this target while 
							// iterating through the geometry tree.
							if (circularTargets.contains(target)) {
								setShouldWalk(false);
								hasCircularReference = true;
								circularTargets.clear();
								circularTargets.push(property.getHref());

								return;
							}

							// iterate through all parent geometries and get their
							// gml:ids to be able to detect circular references
							Child child = property;
							int parents = 0;

							while ((child = childInfo.getParentGeometry(child)) != null) {
								circularTargets.push(((AbstractGeometry)child).getId());
								parents++;
							}

							// recursively walk through the referenced geometry
							geometry.accept(this);

							if (!hasCircularReference) {
								// ok, we can replace the link by a shallow copy of the object
								T copy = property.getAssociableClass().cast(geometry.copy(copyBuilder));

								property.setGeometry(copy);
								property.unsetHref();
								copy.setLocalProperty(CoreConstants.GEOMETRY_XLINK, true);
								copy.setLocalProperty(CoreConstants.GEOMETRY_ORIGINAL, geometry);

								geometry.setLocalProperty(CoreConstants.GEOMETRY_XLINK, true);

								targets.remove(target);
								for (int i = 0; i < parents; ++i)
									circularTargets.pop();
							} else {
								// ups, circular reference detected
								if (!circularTargets.peek().equals(property.getHref()))
									circularTargets.push(property.getHref());
							}
						} else {
							if (importer != null) {
								log.error(importer.getObjectSignature(rootObject) +
										": Incompatible type of geometry referenced by '" +
										target + "'.");
							}
							
							property.unsetHref();
						}
					}
				}

				else if (state == ResolverState.GET_XLINKS)				
					targets.add(clipGMLId(property.getHref()));
			}

			super.visit(property);
		}

		@Override
		public void visit(AbstractGeometry abstractGeometry) {
			if (state == ResolverState.GET_GEOMETRY && abstractGeometry.isSetId() && targets.contains(abstractGeometry.getId()))
				geometries.put(abstractGeometry.getId(), abstractGeometry);
		}

		@Override
		public void visit(ADEGenericElement adeGenericElement) {
			// we do not support generic ADEs...
		}

		@Override
		public <T extends AbstractFeature> void visit(FeatureProperty<T> featureProperty) {
			// we skip appearance elements for performance reasons;
			// there could be an xlink reference to the reference point of a 
			// GeoreferencedTexture. However, this is unlikely and thus is
			// ruled out here.
			if (featureProperty.isSetFeature() && featureProperty.getFeature() instanceof AppearanceModuleComponent)
				return;

			super.visit(featureProperty);
		}
	}

	private String clipGMLId(String target) {
		return target.replaceAll("^.*?#+?", "");
	}

}
