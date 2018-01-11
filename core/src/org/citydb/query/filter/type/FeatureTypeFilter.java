package org.citydb.query.filter.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.module.citygml.CityGMLVersion;

public class FeatureTypeFilter {
	private final Set<FeatureType> featureTypes;
	private final boolean useStrictMode;
	private final ADEExtensionManager adeManager;
	
	public FeatureTypeFilter(boolean useStrictMode) {
		featureTypes = new HashSet<FeatureType>();
		this.useStrictMode = useStrictMode;
		
		adeManager = ADEExtensionManager.getInstance();
	}
	
	public FeatureTypeFilter(FeatureType featureType, boolean useStrictMode) throws FilterException {
		this(useStrictMode);
		addFeatureType(featureType);
	}
	
	public FeatureTypeFilter(Set<FeatureType> featureTypes, boolean useStrictMode) throws FilterException {
		this(useStrictMode);
		for (FeatureType featureType : featureTypes)
			addFeatureType(featureType);
	}
	
	public FeatureTypeFilter() {
		this(true);
	}
	
	public FeatureTypeFilter(FeatureType featureType) throws FilterException {
		this(featureType, true);
	}
	
	public FeatureTypeFilter(Set<FeatureType> featureTypes) throws FilterException {
		this(featureTypes, true);
	}
	
	public int size() {
		return featureTypes.size();
	}
	
	public boolean isEmpty() {
		return featureTypes.isEmpty();
	}
	
	public void clear() {
		featureTypes.clear();
	}
	
	public boolean isAllowedFeatureType(FeatureType featureType) {
		if (useStrictMode && (!featureType.isTopLevel() || !featureType.isQueryable()))
			return false;
		
		ADEExtension extension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
		if (extension != null && !extension.isEnabled())
			return false;
		
		return true;
	}
	
	public List<FeatureType> getFeatureTypes() {
		return new ArrayList<FeatureType>(featureTypes);
	}
	
	public List<FeatureType> getFeatureTypes(CityGMLVersion version) {
		ArrayList<FeatureType> result = new ArrayList<>();
		for (FeatureType featureType : featureTypes) {
			if (featureType.isAvailableForCityGML(version))
				result.add(featureType);
		}
		
		return result;
	}
	
	public void addFeatureType(FeatureType featureType) throws FilterException {		
		if (featureType.isAbstract()) {
			for (FeatureType subType : featureType.listSubTypes(true)) {
				if (!isAllowedFeatureType(subType))
					continue;

				featureTypes.add(subType);
			}
		} else {
			if (!isAllowedFeatureType(featureType))
				throw new FilterException("The feature type must be both top-level and queryable.");

			featureTypes.add(featureType);
		}
	}
	
	public void removeFeatureType(FeatureType featureType) {
		featureTypes.remove(featureType);
	}
	
	public boolean containsFeatureType(FeatureType featureType) {
		return featureTypes.contains(featureType);
	}
}
