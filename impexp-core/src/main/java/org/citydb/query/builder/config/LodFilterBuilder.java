package org.citydb.query.builder.config;

import org.citydb.config.project.query.filter.lod.LodSearchMode;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodFilterMode;

public class LodFilterBuilder {

	public LodFilterBuilder() {

	}

	public LodFilter buildLodFilter(org.citydb.config.project.query.filter.lod.LodFilter lodFilterConfig) {
		LodFilter lodFilter = new LodFilter(LodFilterMode.OR);

		switch (lodFilterConfig.getMode()) {
		case OR:
			lodFilter.setFilterMode(LodFilterMode.OR);
			break;
		case AND:
			lodFilter.setFilterMode(LodFilterMode.AND);
			break;
		}

		for (int lod = 0; lod < 5; lod++)
			lodFilter.setEnabled(lod, lodFilterConfig.isSetLod(lod));
		
		if (lodFilterConfig.getSearchMode() == LodSearchMode.DEPTH && lodFilterConfig.isSetSearchDepth())
			lodFilter.setSearchDepth(lodFilterConfig.getSearchDepth());
		else 
			lodFilter.setSearchDepth(Integer.MAX_VALUE);
		
		return lodFilter;
	}

}
