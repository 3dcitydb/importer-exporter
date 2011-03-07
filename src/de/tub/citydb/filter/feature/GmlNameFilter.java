package de.tub.citydb.filter.feature;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;
import de.tub.citydb.config.project.filter.GmlName;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;

public class GmlNameFilter implements Filter<String> {
	private final AbstractFilterConfig filterConfig;

	private boolean isActive;
	private GmlName gmlNameFilter;

	public GmlNameFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else if (mode == FilterMode.KML_EXPORT)
			filterConfig = config.getProject().getKmlExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();

		init();
	}

	private void init() {
		isActive = filterConfig.isSetComplexFilter() &&
			filterConfig.getComplexFilter().getGmlName().isSet();

		if (isActive)
			gmlNameFilter = filterConfig.getComplexFilter().getGmlName();			
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
