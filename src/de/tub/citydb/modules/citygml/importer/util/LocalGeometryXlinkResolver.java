/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.appearance.AppearanceModuleComponent;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.util.child.ChildInfo;
import org.citygml4j.util.walker.GMLWalker;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class LocalGeometryXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private AbstractCityObject abstractCityObject;
	private HashSet<String> targets;
	private HashMap<String, AbstractGeometry> geometries;
	private ChildInfo childInfo;
	private Stack<String> circularTargets;

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

		// we follow a three-phase resolving approach which is a 
		// compromise between performance and memory consumption.

		// phase 1: iterate through all elements and detect 
		// xlink references to geometry object
		ResolverWalker resolver = new ResolverWalker();
		resolver.state = ResolverState.GET_XLINKS;
		abstractCityObject.accept(resolver);

		if (targets.isEmpty())
			return true;

		// phase 2: iterate through all elements again and collect
		// the geometry objects for the detected xlinks		
		resolver.reset();
		resolver.state = ResolverState.GET_GEOMETRY;
		abstractCityObject.accept(resolver);

		// phase 3: finally iterate through all elements once more
		// and actually replace xlinks by geometries
		resolver.reset();
		resolver.state = ResolverState.RESOLVE_XLINKS;
		abstractCityObject.accept(resolver);

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
			if (!geometryProperty.isSetObject() && geometryProperty.isSetHref()) {
				if (state == ResolverState.RESOLVE_XLINKS) {
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
								// ok, we can replace the link by the object
								geometryProperty.setGeometry((T)geometry);
								geometryProperty.unsetHref();
								geometry.setLocalProperty(Internal.GEOMETRY_XLINK, true);

								for (int i = 0; i < parents; ++i)
									circularTargets.pop();
							} else
								// ups, circular reference detected
								circularTargets.push(geometryProperty.getHref());
						} else {
							StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
									abstractCityObject.getCityGMLClass(), 
									abstractCityObject.getId()));
							msg.append(": Incompatible type of geometry referenced by '")
							.append(target)
							.append("'.");
							
							LOG.error(msg.toString());						
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
			if (state == ResolverState.GET_GEOMETRY && abstractGeometry.isSetId()&& targets.contains(abstractGeometry.getId()))
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
			if (featureProperty.isSetObject() && featureProperty.getObject() instanceof AppearanceModuleComponent)
				return;

			super.visit(featureProperty);
		}
	}

	private String clipGMLId(String target) {
		return target.replaceAll("^.*?#+?", "");
	}

}
