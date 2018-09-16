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
package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.citydb.config.project.query.filter.selection.SimpleSelectionFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.BridgeModule;
import org.citygml4j.model.module.citygml.BuildingModule;
import org.citygml4j.model.module.citygml.CityFurnitureModule;
import org.citygml4j.model.module.citygml.CityObjectGroupModule;
import org.citygml4j.model.module.citygml.GenericsModule;
import org.citygml4j.model.module.citygml.LandUseModule;
import org.citygml4j.model.module.citygml.ReliefModule;
import org.citygml4j.model.module.citygml.TransportationModule;
import org.citygml4j.model.module.citygml.TunnelModule;
import org.citygml4j.model.module.citygml.VegetationModule;
import org.citygml4j.model.module.citygml.WaterBodyModule;

@XmlType(name="SimpleKmlExportQueryType", propOrder={
		"featureTypeFilter",
		"filter",
		"tilingOptions"
})
public class SimpleKmlQuery {
	@XmlAttribute
	private SimpleSelectionFilterMode mode = SimpleSelectionFilterMode.COMPLEX;

	@XmlElement(name = "typeNames")
	protected FeatureTypeFilter featureTypeFilter;
	private SimpleSelectionFilter filter;
	private KmlTilingOptions tilingOptions;

	public SimpleKmlQuery() {
		featureTypeFilter = new FeatureTypeFilter();
		filter = new SimpleSelectionFilter();
		tilingOptions = new KmlTilingOptions();

		// add CityGML types per default
		featureTypeFilter.addTypeName(new QName(BridgeModule.v2_0_0.getNamespaceURI(), "Bridge"));
		featureTypeFilter.addTypeName(new QName(BuildingModule.v2_0_0.getNamespaceURI(), "Building"));
		featureTypeFilter.addTypeName(new QName(CityFurnitureModule.v2_0_0.getNamespaceURI(), "CityFurniture"));
		featureTypeFilter.addTypeName(new QName(CityObjectGroupModule.v2_0_0.getNamespaceURI(), "CityObjectGroup"));
		featureTypeFilter.addTypeName(new QName(GenericsModule.v2_0_0.getNamespaceURI(), "GenericCityObject"));
		featureTypeFilter.addTypeName(new QName(LandUseModule.v2_0_0.getNamespaceURI(), "LandUse"));
		featureTypeFilter.addTypeName(new QName(ReliefModule.v2_0_0.getNamespaceURI(), "ReliefFeature"));
		featureTypeFilter.addTypeName(new QName(TransportationModule.v2_0_0.getNamespaceURI(), "TransportationComplex"));
		featureTypeFilter.addTypeName(new QName(TransportationModule.v2_0_0.getNamespaceURI(), "Track"));
		featureTypeFilter.addTypeName(new QName(TransportationModule.v2_0_0.getNamespaceURI(), "Railway"));
		featureTypeFilter.addTypeName(new QName(TransportationModule.v2_0_0.getNamespaceURI(), "Road"));
		featureTypeFilter.addTypeName(new QName(TransportationModule.v2_0_0.getNamespaceURI(), "Square"));
		featureTypeFilter.addTypeName(new QName(TunnelModule.v2_0_0.getNamespaceURI(), "Tunnel"));
		featureTypeFilter.addTypeName(new QName(VegetationModule.v2_0_0.getNamespaceURI(), "SolitaryVegetationObject"));
		featureTypeFilter.addTypeName(new QName(VegetationModule.v2_0_0.getNamespaceURI(), "PlantCover"));
		featureTypeFilter.addTypeName(new QName(WaterBodyModule.v2_0_0.getNamespaceURI(), "WaterBody"));
	}

	public SimpleSelectionFilterMode getMode() {
		return mode;
	}

	public void setMode(SimpleSelectionFilterMode mode) {
		this.mode = mode;
	}

	public FeatureTypeFilter getFeatureTypeFilter() {
		return featureTypeFilter;
	}

	public boolean isSetFeatureTypeFilter() {
		return featureTypeFilter != null;
	}

	public void setFeatureTypeFilter(FeatureTypeFilter featureTypeFilter) {
		this.featureTypeFilter = featureTypeFilter;
	}

	public SimpleSelectionFilter getFilter() {
		return filter;
	}

	public void setFilter(SimpleSelectionFilter filter) {
		this.filter = filter;
	}

	public KmlTilingOptions getTilingOptions() {
		return tilingOptions;
	}

	public boolean isSetTilingOptions() {
		return tilingOptions != null;
	}

	public void setTilingOptions(KmlTilingOptions tilingOptions) {
		this.tilingOptions = tilingOptions;
	}

}
