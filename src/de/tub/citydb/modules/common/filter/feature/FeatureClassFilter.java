/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.common.filter.feature;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;
import de.tub.citydb.config.project.filter.FeatureClass;
import de.tub.citydb.modules.common.filter.Filter;
import de.tub.citydb.modules.common.filter.FilterMode;

public class FeatureClassFilter implements Filter<CityGMLClass> {
	private final AbstractFilterConfig filterConfig;

	boolean isActive;
	private FeatureClass featureClassFilter;

	public FeatureClassFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();

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

			if (inverse ^ featureClassFilter.isSetTransportation()) {
				state.add(CityGMLClass.TRANSPORTATION_COMPLEX);
				state.add(CityGMLClass.ROAD);
				state.add(CityGMLClass.RAILWAY);
				state.add(CityGMLClass.TRACK);
				state.add(CityGMLClass.SQUARE);
			}

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
		}

		return state;
	}


}
