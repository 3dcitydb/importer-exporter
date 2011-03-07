package de.tub.citydb.filter.feature;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.filter.FilterGmlName;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;

public class GmlNameFilter implements Filter<String> {
	private final FilterConfig filter;
	private boolean isActive;
	private FilterGmlName gmlNameFilter;

	public GmlNameFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filter = config.getProject().getExporter().getFilter();
		else
			filter = config.getProject().getImporter().getFilter();
			
		init();
	}
	
	private void init() {
		isActive = filter.isSetComplex() &&
			filter.getComplexFilter().getGmlNameFilter().isSet();
		
		if (isActive)
			gmlNameFilter = filter.getComplexFilter().getGmlNameFilter();
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}
	
	public void reset() {
		init();
	}
	
	public boolean filter(String gmlName) {
		if (isActive) {
			if (gmlNameFilter.getValue() != null && gmlNameFilter.getValue().length() > 0) {
				String adaptedValue = gmlNameFilter.getValue().trim().toUpperCase();				
				if (!gmlName.trim().toUpperCase().equals(adaptedValue))
					return true;
			}
		}
		
		return false;
	}
	
	public String getFilterState() {
		return getInternalState(false);
	}
	
	public String getNotFilterState() {
		return getInternalState(true);
	}
	
	private String getInternalState(boolean inverse) {
		if (isActive) {
			if (!inverse && gmlNameFilter.getValue() != null && gmlNameFilter.getValue().length() > 0)
				return gmlNameFilter.getValue();
			else
				return null;			
 		} 
		
		return null;
	}
}
