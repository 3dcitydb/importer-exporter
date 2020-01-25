/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.query.builder.sql;

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureTypeFilterBuilder {
	private final SchemaPathBuilder builder;

	protected FeatureTypeFilterBuilder(SchemaPathBuilder builder) {
		this.builder = builder;
	}

	protected void buildFeatureTypeFilter(FeatureTypeFilter typeFilter, CityGMLVersion targetVersion, SQLQueryContext queryContext) throws QueryBuildException {
		if (targetVersion == null)
			targetVersion = CityGMLVersion.DEFAULT;

		List<FeatureType> featureTypes = typeFilter.getFeatureTypes(targetVersion);
		if (featureTypes.isEmpty())
			return;

		Set<Integer> ids = new HashSet<>(featureTypes.size());
		for (FeatureType featureType : featureTypes)
			ids.add(featureType.getObjectClassId());

		Select select = queryContext.getSelect();
		Table cityObject = builder.joinCityObjectTable(queryContext);
		Column objectClassId = cityObject.getColumn(MappingConstants.OBJECTCLASS_ID);

		if (ids.size() == 1)
			select.addSelection(ComparisonFactory.equalTo(objectClassId, new IntegerLiteral(ids.iterator().next())));
		else
			select.addSelection(ComparisonFactory.in(objectClassId, new LiteralList(ids.toArray(new Integer[0]))));
	}

	protected void buildFeatureTypeFilter(FeatureTypeFilter typeFilter, SQLQueryContext queryContext) throws QueryBuildException {
		buildFeatureTypeFilter(typeFilter, CityGMLVersion.DEFAULT, queryContext);
	}
}
