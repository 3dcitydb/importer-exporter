package org.citydb.citygml.importer.filter;

import org.citydb.citygml.importer.filter.selection.comparison.LikeFilter;
import org.citydb.citygml.importer.filter.selection.id.ResourceIdFilter;
import org.citydb.citygml.importer.filter.selection.spatial.SimpleBBOXFilter;
import org.citydb.citygml.importer.filter.type.FeatureTypeFilter;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.filter.FilterException;

public class CityGMLFilterBuilder {
	private final AbstractDatabaseAdapter databaseAdapter;
	
	public CityGMLFilterBuilder(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	public CityGMLFilter buildCityGMLFilter(ImportFilter filterConfig) throws FilterException {
		CityGMLFilter filter = new CityGMLFilter();
		SimpleSelectionFilter selectionConfig = filterConfig.getFilter();
		
		if (filterConfig.getMode() == SimpleSelectionFilterMode.SIMPLE) {
			if (!selectionConfig.isSetGmlIdFilter() 
					|| !selectionConfig.getGmlIdFilter().isSetResourceIds())
				throw new FilterException("The gml:id filter must not be empty.");
			
			filter.getSelectionFilter().setResourceIdFilter(new ResourceIdFilter(selectionConfig.getGmlIdFilter()));
		}
		
		else {
			// feature type filter
			if (filterConfig.isUseTypeNames()) {
				if (!filterConfig.isSetFeatureTypeFilter() 
						|| filterConfig.getFeatureTypeFilter().getTypeNames().isEmpty())
					throw new FilterException("The feature type filter must not be empty.");
				
				filter.setFeatureTypeFilter(new FeatureTypeFilter(filterConfig.getFeatureTypeFilter()));
			}
			
			// counter filter
			if (filterConfig.isUseCountFilter()) {
				if (!filterConfig.isSetCounterFilter()
						|| filterConfig.getCounterFilter().getLowerLimit() == null 
						|| filterConfig.getCounterFilter().getUpperLimit() == null
						|| filterConfig.getCounterFilter().getLowerLimit() <= 0
						|| filterConfig.getCounterFilter().getUpperLimit() < filterConfig.getCounterFilter().getLowerLimit())
					throw new FilterException("Invalid limit values for counter filter.");
				
				filter.setCounterFilter(filterConfig.getCounterFilter());
			}
			
			// gml:name filter
			if (filterConfig.isUseGmlNameFilter()) {
				if (!selectionConfig.isSetGmlNameFilter()
						|| !selectionConfig.getGmlNameFilter().isSetLiteral())
					throw new FilterException("The gml:name filter must not be empty.");
				
				LikeOperator likeOperator = selectionConfig.getGmlNameFilter();
				if (!likeOperator.isSetWildCard() || likeOperator.getWildCard().length() > 1)
					throw new FilterException("Wildcards must be defined by a single character.");

				if (!likeOperator.isSetSingleCharacter() || likeOperator.getSingleCharacter().length() > 1)
					throw new FilterException("Wildcards must be defined by a single character.");

				if (!likeOperator.isSetEscapeCharacter() || likeOperator.getEscapeCharacter().length() > 1)
					throw new FilterException("An escape character must be defined by a single character.");
					
				filter.getSelectionFilter().setGmlNameFilter(new LikeFilter(selectionConfig.getGmlNameFilter()));
			}
			
			// bbox filter
			if (filterConfig.isUseBboxFilter()) {
				if (!selectionConfig.getBboxFilter().isSetEnvelope())
					throw new FilterException("The bounding box filter requires an " + GeometryType.ENVELOPE + " as spatial operand.");
				
				SimpleBBOXFilter bboxFilter = new SimpleBBOXFilter(selectionConfig.getBboxFilter(), selectionConfig.getBboxMode());
				bboxFilter.transform(databaseAdapter.getConnectionMetaData().getReferenceSystem(), databaseAdapter);
				filter.getSelectionFilter().setBboxFilter(bboxFilter);
			}
		}
		
		return filter;
	}
	
}
