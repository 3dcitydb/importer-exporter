/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.common.filter.feature;

import java.util.ArrayList;
import java.util.List;

import org.citydb.config.Config;
import org.citydb.config.project.filter.AbstractFilterConfig;
import org.citydb.config.project.filter.FeatureClass;
import org.citydb.modules.common.filter.Filter;
import org.citydb.modules.common.filter.FilterMode;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.module.citygml.CityGMLVersion;

public class FeatureClassFilter implements Filter<CityGMLClass> {
	private final AbstractFilterConfig filterConfig;
	private final CityGMLVersion version;

	boolean isActive;
	private FeatureClass featureClassFilter;

	public FeatureClassFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT) {
			filterConfig = config.getProject().getExporter().getFilter();
			version = Util.toCityGMLVersion(config.getProject().getExporter().getCityGMLVersion());
		} else if (mode == FilterMode.KML_EXPORT) {
			filterConfig = config.getProject().getKmlExporter().getFilter();
			version = CityGMLVersion.v2_0_0;			
		} else {
			filterConfig = config.getProject().getImporter().getFilter();
			version = CityGMLVersion.v2_0_0;
		}

		init();
	}

	private void init() {
		isActive = filterConfig.isSetComplexFilter() &&
				filterConfig.getComplexFilter().getFeatureClass().isSet();

		if (isActive)
			featureClassFilter = filterConfig.getComplexFilter().getFeatureClass();
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}

	public boolean filter(CityGMLClass type) {
		if (isActive) {
			// do filter job
			switch (type) {
			case BUILDING:
				return featureClassFilter.isSetBuilding();
			case BRIDGE:
				return featureClassFilter.isSetBridge() && version == CityGMLVersion.v2_0_0;
			case TUNNEL:
				return featureClassFilter.isSetTunnel() && version == CityGMLVersion.v2_0_0;
			case CITY_FURNITURE:
				return featureClassFilter.isSetCityFurniture();
			case LAND_USE:
				return featureClassFilter.isSetLandUse();
			case WATER_BODY:
				return featureClassFilter.isSetWaterBody();
			case PLANT_COVER:
			case SOLITARY_VEGETATION_OBJECT:
				return featureClassFilter.isSetVegetation();
			case TRANSPORTATION_COMPLEX:
			case ROAD:
			case RAILWAY:
			case TRACK:
			case SQUARE:
				return featureClassFilter.isSetTransportation();
			case RELIEF_FEATURE:
				return featureClassFilter.isSetReliefFeature();
			case GENERIC_CITY_OBJECT:
				return featureClassFilter.isSetGenericCityObject();
			case CITY_OBJECT_GROUP:
				return featureClassFilter.isSetCityObjectGroup();
			default:
				return false;
			}
		}

		return false;
	}	

	public List<CityGMLClass> getFilterState() {
		return getInternalState(false);
	}

	public List<CityGMLClass> getNotFilterState() {
		return getInternalState(true);
	}

	private List<CityGMLClass> getInternalState(boolean inverse) {
		List<CityGMLClass> state = new ArrayList<CityGMLClass>();

		if (isActive) {
			// get state
			if (inverse ^ featureClassFilter.isSetBuilding())
				state.add(CityGMLClass.BUILDING);

			if (version == CityGMLVersion.v2_0_0 && (inverse ^ featureClassFilter.isSetBridge()))
				state.add(CityGMLClass.BRIDGE);

			if (version == CityGMLVersion.v2_0_0 && (inverse ^ featureClassFilter.isSetTunnel()))
				state.add(CityGMLClass.TUNNEL);

			if (inverse ^ featureClassFilter.isSetCityFurniture())
				state.add(CityGMLClass.CITY_FURNITURE);

			if (inverse ^ featureClassFilter.isSetLandUse())
				state.add(CityGMLClass.LAND_USE);

			if (inverse ^ featureClassFilter.isSetWaterBody())
				state.add(CityGMLClass.WATER_BODY);

			if (inverse ^ featureClassFilter.isSetVegetation()) {
				state.add(CityGMLClass.PLANT_COVER);
				state.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);
			}

			if (inverse ^ featureClassFilter.isSetPlantCover())
				state.add(CityGMLClass.PLANT_COVER);

			if (inverse ^ featureClassFilter.isSetSolitaryVegetationObject())
				state.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);

			if (inverse ^ featureClassFilter.isSetTransportation()) {
				state.add(CityGMLClass.TRANSPORTATION_COMPLEX);
				state.add(CityGMLClass.ROAD);
				state.add(CityGMLClass.RAILWAY);
				state.add(CityGMLClass.TRACK);
				state.add(CityGMLClass.SQUARE);
			}

			if (inverse ^ featureClassFilter.isSetTransportationComplex())
				state.add(CityGMLClass.TRANSPORTATION_COMPLEX);

			if (inverse ^ featureClassFilter.isSetRoad())
				state.add(CityGMLClass.ROAD);

			if (inverse ^ featureClassFilter.isSetRailway())
				state.add(CityGMLClass.RAILWAY);

			if (inverse ^ featureClassFilter.isSetSquare())
				state.add(CityGMLClass.SQUARE);

			if (inverse ^ featureClassFilter.isSetTrack())
				state.add(CityGMLClass.TRACK);

			if (inverse ^ featureClassFilter.isSetReliefFeature())
				state.add(CityGMLClass.RELIEF_FEATURE);

			if (inverse ^ featureClassFilter.isSetGenericCityObject())
				state.add(CityGMLClass.GENERIC_CITY_OBJECT);

			if (inverse ^ featureClassFilter.isSetCityObjectGroup())
				state.add(CityGMLClass.CITY_OBJECT_GROUP);
		}

		else if (inverse) {
			state.add(CityGMLClass.BUILDING);
			state.add(CityGMLClass.CITY_FURNITURE);
			state.add(CityGMLClass.LAND_USE);
			state.add(CityGMLClass.WATER_BODY);
			state.add(CityGMLClass.PLANT_COVER);
			state.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);
			state.add(CityGMLClass.TRANSPORTATION_COMPLEX);
			state.add(CityGMLClass.ROAD);
			state.add(CityGMLClass.RAILWAY);
			state.add(CityGMLClass.TRACK);
			state.add(CityGMLClass.SQUARE);
			state.add(CityGMLClass.RELIEF_FEATURE);
			state.add(CityGMLClass.GENERIC_CITY_OBJECT);
			state.add(CityGMLClass.CITY_OBJECT_GROUP);

			if (version == CityGMLVersion.v2_0_0) {
				state.add(CityGMLClass.BRIDGE);
				state.add(CityGMLClass.TUNNEL);
			}
		}

		return state;
	}


}
