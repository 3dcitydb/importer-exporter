/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.citydb.config.internal.Internal;
import org.citydb.log.Logger;
import org.citydb.util.Util;
import org.citygml4j.builder.copy.CopyBuilder;
import org.citygml4j.builder.copy.ShallowCopyBuilder;
import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.appearance.AppearanceModuleComponent;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.util.child.ChildInfo;
import org.citygml4j.util.walker.GMLWalker;

public class LocalGeometryXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private AbstractCityObject abstractCityObject;
	private HashSet<String> targets;
	private HashMap<String, AbstractGeometry> geometries;
	private ChildInfo childInfo;
	private Stack<String> circularTargets;
	private CopyBuilder copyBuilder = new ShallowCopyBuilder();

	private enum ResolverState {
		GET_XLINKS,
		GET_GEOMETRY,
		RESOLVE_XLINKS
	};

	public LocalGeometryXlinkResolver() {
		childInfo = new ChildInfo();

		targets = new HashSet<String>();
		geometries = new HashMap<String, AbstractGeometry>();
		circularTargets = new Stack<String>();	
	}

	public boolean resolveGeometryXlinks(AbstractCityObject abstractCityObject) {
		this.abstractCityObject = abstractCityObject;
		return resolveGeometryXlinks((AbstractGML)abstractCityObject);
	}
	
	public boolean resolveGeometryXlinks(AbstractGeometry abstractGeometry) {
		return resolveGeometryXlinks((AbstractGML)abstractGeometry);
	}

	private boolean resolveGeometryXlinks(AbstractGML abstractGML) {
		// we follow a three-phase resolving approach which is a 
		// compromise between performance and memory consumption.

		circularTargets.clear();
		
		// phase 1: iterate through all elements and detect 
		// xlink references to geometry object
		ResolverWalker resolver = new ResolverWalker();
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
		return new ArrayList<String>(circularTargets);
	}

	private class ResolverWalker extends GMLWalker {
		private boolean hasCircularReference = false;
		private ResolverState state;

		@SuppressWarnings("unchecked")
		@Override
		public <T extends AbstractGeometry> void visit(GeometryProperty<T> geometryProperty) {
			if (!geometryProperty.isSetGeometry() && geometryProperty.isSetHref()) {
				if (state == ResolverState.RESOLVE_XLINKS && shouldWalk()) {
					final String target = clipGMLId(geometryProperty.getHref());
					final AbstractGeometry geometry = geometries.get(target);

					if (geometry != null) {
						// check whether the type of the referenced geometry is allowed
						if (geometryProperty.getAssociableClass().isInstance(geometry)) {

							// check whether we have already seen this target while 
							// iterating through the geometry tree.
							if (circularTargets.contains(target)) {
								setShouldWalk(false);
								hasCircularReference = true;
								circularTargets.clear();
								circularTargets.push(geometryProperty.getHref());

								return;
							}

							// iterate through all parent geometries and get their
							// gml:ids to be able to detect circular references
							Child child = (Child)geometryProperty;
							int parents = 0;

							while ((child = childInfo.getParentGeometry(child)) != null) {
								circularTargets.push(((AbstractGeometry)child).getId());
								parents++;
							}

							// recursively walk through the referenced geometry
							geometry.accept(this);
							addToVisited(geometry);

							if (!hasCircularReference) {
								// ok, we can replace the link by a shallow copy of the object
								T copy = (T)geometry.copy(copyBuilder);

								geometryProperty.setGeometry(copy);
								geometryProperty.unsetHref();								
								copy.setLocalProperty(Internal.GEOMETRY_XLINK, true);
								copy.setLocalProperty(Internal.GEOMETRY_ORIGINAL, geometry);

								geometry.setLocalProperty(Internal.GEOMETRY_XLINK, true);

								targets.remove(target);
								for (int i = 0; i < parents; ++i)
									circularTargets.pop();
							} else {
								// ups, circular reference detected
								if (!circularTargets.peek().equals(geometryProperty.getHref()))
									circularTargets.push(geometryProperty.getHref());
							}
						} else {
							if (abstractCityObject != null) {
								StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
										abstractCityObject.getCityGMLClass(), 
										abstractCityObject.getId()));
								msg.append(": Incompatible type of geometry referenced by '")
								.append(target)
								.append("'.");

								LOG.error(msg.toString());
							}
							
							geometryProperty.unsetHref();
						}
					}
				}

				else if (state == ResolverState.GET_XLINKS)				
					targets.add(clipGMLId(geometryProperty.getHref()));
			}

			super.visit(geometryProperty);			
		}

		@Override
		public void visit(AbstractGeometry abstractGeometry) {
			if (state == ResolverState.GET_GEOMETRY && abstractGeometry.isSetId() && targets.contains(abstractGeometry.getId()))
				geometries.put(abstractGeometry.getId(), abstractGeometry);
		}

		@Override
		public void visit(ADEComponent adeComponent) {
			// we do not support ADEs...
			return;
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
