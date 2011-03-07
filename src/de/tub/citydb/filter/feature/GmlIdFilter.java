package de.tub.citydb.filter.feature;

import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.filter.FilterGmlId;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;

public class GmlIdFilter implements Filter<String> {
	private final FilterConfig filter;
	private boolean isActive;
	private FilterGmlId gmlIdFilter;

	public GmlIdFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filter = config.getProject().getExporter().getFilter();
		else
			filter = config.getProject().getImporter().getFilter();
			
		init();
	}
	
	private void init() {
		isActive = filter.isSetSimple();
		
		if (isActive)
			gmlIdFilter = filter.getSimpleFilter().getGmlIdFilter();			
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
