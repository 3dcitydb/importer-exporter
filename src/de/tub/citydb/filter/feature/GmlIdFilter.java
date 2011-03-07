package de.tub.citydb.filter.feature;

import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;
import de.tub.citydb.config.project.filter.GmlId;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;

public class GmlIdFilter implements Filter<String> {
	private final AbstractFilterConfig filterConfig;

	private boolean isActive;
	private GmlId gmlIdFilter;

	public GmlIdFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else if (mode == FilterMode.KML_EXPORT)
			filterConfig = config.getProject().getKmlExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();

		init();
	}

	private void init() {
		isActive = filterConfig.isSetSimpleFilter();
		if (isActive)
			gmlIdFilter = filterConfig.getSimpleFilter().getGmlIdFilter();			
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}

	public boolean filter(String gmlId) {
		if (isActive) {
			List<String> gmlIdList = gmlIdFilter.getGmlIds();
			if (gmlIdList != null) {
				for (String item : gmlIdList)
					if (gmlId.equals(item))
						return false;
			}

			return true;
		}

		return false;
	}

	public List<String> getFilterState() {
		return getInternalState(false);
	}

	public List<String> getNotFilterState() {
		return getInternalState(true);
	}

	private List<String> getInternalState(boolean inverse) {
		if (isActive) {
			if (!inverse)
				return gmlIdFilter.getGmlIds();
			else
				return null;			
		} 

		return null;
	}
}
