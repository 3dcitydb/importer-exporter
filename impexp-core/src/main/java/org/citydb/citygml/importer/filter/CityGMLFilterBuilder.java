/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.citygml.importer.filter;

import org.citydb.citygml.importer.filter.selection.comparison.LikeFilter;
import org.citydb.citygml.importer.filter.selection.id.ResourceIdFilter;
import org.citydb.citygml.importer.filter.selection.spatial.SimpleBBOXFilter;
import org.citydb.citygml.importer.filter.type.FeatureTypeFilter;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.importer.ImportFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.simple.SimpleAttributeFilter;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.filter.FilterException;

public class CityGMLFilterBuilder {
	private final SchemaMapping schemaMapping;
	private final AbstractDatabaseAdapter databaseAdapter;
	
	public CityGMLFilterBuilder(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
		this.schemaMapping = schemaMapping;
		this.databaseAdapter = databaseAdapter;
	}

	public CityGMLFilter buildCityGMLFilter(ImportFilter filterConfig) throws FilterException {
		CityGMLFilter filter = new CityGMLFilter(schemaMapping);

		// feature type filter
		if (filterConfig.isUseTypeNames()) {
			if (filterConfig.isSetFeatureTypeFilter() && filterConfig.getFeatureTypeFilter().getTypeNames().isEmpty())
				filter.setFeatureTypeFilter(new FeatureTypeFilter(filterConfig.getFeatureTypeFilter(), schemaMapping));
			else
				throw new FilterException("The feature type filter must not be empty.");
		}

		// simple attribute filter
		if (filterConfig.isUseAttributeFilter() && filterConfig.isSetAttributeFilter()) {
			SimpleAttributeFilter attributeFilterConfig = filterConfig.getAttributeFilter();

			// gml:id filter
			if (attributeFilterConfig.isSetGmlIdFilter() && attributeFilterConfig.getGmlIdFilter().isSetResourceIds())
				filter.getSelectionFilter().setResourceIdFilter(new ResourceIdFilter(attributeFilterConfig.getGmlIdFilter()));

			// gml:name filter
			if (attributeFilterConfig.isSetGmlNameFilter() && attributeFilterConfig.getGmlNameFilter().isSetLiteral()) {
				LikeOperator likeOperator = attributeFilterConfig.getGmlNameFilter();
				if (!likeOperator.isSetWildCard() || likeOperator.getWildCard().length() > 1)
					throw new FilterException("Wildcards must be defined by a single character.");

				if (!likeOperator.isSetSingleCharacter() || likeOperator.getSingleCharacter().length() > 1)
					throw new FilterException("Wildcards must be defined by a single character.");

				if (!likeOperator.isSetEscapeCharacter() || likeOperator.getEscapeCharacter().length() > 1)
					throw new FilterException("An escape character must be defined by a single character.");

				filter.getSelectionFilter().setGmlNameFilter(new LikeFilter(attributeFilterConfig.getGmlNameFilter()));
			}
		}

		// counter filter
		if (filterConfig.isUseCountFilter() && filterConfig.isSetCounterFilter()) {
			CounterFilter counterFilterConfig = filterConfig.getCounterFilter();
			if (!counterFilterConfig.isSetLowerLimit())
				counterFilterConfig.setLowerLimit(1L);

			if (!counterFilterConfig.isSetUpperLimit()
					|| counterFilterConfig.getLowerLimit() <= 0
					|| filterConfig.getCounterFilter().getUpperLimit() < filterConfig.getCounterFilter().getLowerLimit())
				throw new FilterException("Invalid limit values for counter filter.");

			filter.setCounterFilter(filterConfig.getCounterFilter());
		}

		// bbox filter
		if (filterConfig.isUseBboxFilter()) {
			if (!filterConfig.getBboxFilter().isSetEnvelope())
				throw new FilterException("The bounding box filter requires an " + GeometryType.ENVELOPE + " as spatial operand.");

			SimpleBBOXFilter bboxFilter = new SimpleBBOXFilter(filterConfig.getBboxFilter());
			bboxFilter.transform(databaseAdapter.getConnectionMetaData().getReferenceSystem(), databaseAdapter);
			filter.getSelectionFilter().setBboxFilter(bboxFilter);
		}

		return filter;
	}
	
}
