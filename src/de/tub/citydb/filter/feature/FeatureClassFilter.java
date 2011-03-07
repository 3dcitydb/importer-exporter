package de.tub.citydb.filter.feature;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.filter.FilterFeatureClass;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;

public class FeatureClassFilter implements Filter<CityGMLClass> {
	private final FilterConfig filter;
	boolean isActive;
	private FilterFeatureClass featureClassFilter;

	public FeatureClassFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filter = config.getProject().getExporter().getFilter();
		else
			filter = config.getProject().getImporter().getFilter();
		
		init();
	}
	
	private void init() {
		isActive = filter.isSetComplex() &&
			filter.getComplexFilter().getFeatureClassFilter().isSet();
		
		if (isActive)
			featureClassFilter = filter.getComplexFilter().getFeatureClassFilter();
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
			case CITYFURNITURE:
				return featureClassFilter.isSetCityFurniture();
			case LANDUSE:
				return featureClassFilter.isSetLandUse();
			case WATERBODY:
				return featureClassFilter.isSetWaterBody();
			case PLANTCOVER:
			case SOLITARYVEGETATIONOBJECT:
				return featureClassFilter.isSetVegetation();
			case TRANSPORTATIONCOMPLEX:
			case ROAD:
			case RAILWAY:
			case TRACK:
			case SQUARE:
				return featureClassFilter.isSetTransportation();
			case RELIEFFEATURE:
				return featureClassFilter.isSetReliefFeature();
			case GENERICCITYOBJECT:
				return featureClassFilter.isSetGenericCityObject();
			case CITYOBJECTGROUP:
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
				state.add(CityGMLClass.CITYFURNITURE);

			if (inverse ^ featureClassFilter.isSetLandUse())
				state.add(CityGMLClass.LANDUSE);

			if (inverse ^ featureClassFilter.isSetWaterBody())
				state.add(CityGMLClass.WATERBODY);

			if (inverse ^ featureClassFilter.isSetVegetation()) {
				state.add(CityGMLClass.PLANTCOVER);
				state.add(CityGMLClass.SOLITARYVEGETATIONOBJECT);
			}

			if (inverse ^ featureClassFilter.isSetTransportation()) {
				state.add(CityGMLClass.TRANSPORTATIONCOMPLEX);
				state.add(CityGMLClass.ROAD);
				state.add(CityGMLClass.RAILWAY);
				state.add(CityGMLClass.TRACK);
				state.add(CityGMLClass.SQUARE);
			}

			if (inverse ^ featureClassFilter.isSetReliefFeature())
				state.add(CityGMLClass.RELIEFFEATURE);
			
			if (inverse ^ featureClassFilter.isSetGenericCityObject())
				state.add(CityGMLClass.GENERICCITYOBJECT);

			if (inverse ^ featureClassFilter.isSetCityObjectGroup())
				state.add(CityGMLClass.CITYOBJECTGROUP);
		}

		else if (inverse) {
			state.add(CityGMLClass.BUILDING);
			state.add(CityGMLClass.CITYFURNITURE);
			state.add(CityGMLClass.LANDUSE);
			state.add(CityGMLClass.WATERBODY);
			state.add(CityGMLClass.PLANTCOVER);
			state.add(CityGMLClass.SOLITARYVEGETATIONOBJECT);
			state.add(CityGMLClass.TRANSPORTATIONCOMPLEX);
			state.add(CityGMLClass.ROAD);
			state.add(CityGMLClass.RAILWAY);
			state.add(CityGMLClass.TRACK);
			state.add(CityGMLClass.SQUARE);
			state.add(CityGMLClass.RELIEFFEATURE);
			state.add(CityGMLClass.GENERICCITYOBJECT);
			state.add(CityGMLClass.CITYOBJECTGROUP);
		}

		return state;
	}


}
