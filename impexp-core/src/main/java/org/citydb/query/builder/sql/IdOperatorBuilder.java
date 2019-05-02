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

import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.logical.BinaryLogicalOperator;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationName;
import org.citygml4j.model.module.gml.GMLCoreModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IdOperatorBuilder {
	private final Query query;
	private final SchemaPathBuilder schemaPathBuilder;
	private final SchemaMapping schemaMapping;
	private final AbstractSQLAdapter sqlAdapter;

	protected IdOperatorBuilder(Query query, SchemaPathBuilder schemaPathBuilder, SchemaMapping schemaMapping, AbstractSQLAdapter sqlAdapter) {
		this.query = query;
		this.schemaPathBuilder = schemaPathBuilder;
		this.schemaMapping = schemaMapping;
		this.sqlAdapter = sqlAdapter;
	}

	@SuppressWarnings("unchecked")
	protected SQLQueryContext buildResourceIdOperator(ResourceIdOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());		
		ValueReference valueReference;

		try {
			SchemaPath path = new SchemaPath(superType);	
			path.appendChild(superType.getProperty(MappingConstants.ID, GMLCoreModule.v3_1_1.getNamespaceURI(), true));
			valueReference = new ValueReference(path);
		} catch (InvalidSchemaPathException e) {
			throw new QueryBuildException(e.getMessage());
		}

		// build the value reference
		queryContext = schemaPathBuilder.buildSchemaPath(valueReference.getSchemaPath(), queryContext, useLeftJoins);
		List<PredicateToken> predicates = new ArrayList<>();

		if (operator.getResourceIds().size() == 1) {
			queryContext.addPredicate(ComparisonFactory.equalTo(queryContext.targetColumn, new PlaceHolder<>(operator.getResourceIds().iterator().next())));
		} else {
			List<PlaceHolder<String>> placeHolders = new ArrayList<>();
			int maxItems = sqlAdapter.getMaximumNumberOfItemsForInOperator();
			int i = 0;

			Iterator<String> iter = operator.getResourceIds().iterator();
			while (iter.hasNext()) {
				placeHolders.add(new PlaceHolder<>(iter.next()));

				if (++i == maxItems || !iter.hasNext()) {
					predicates.add(ComparisonFactory.in(queryContext.targetColumn, new LiteralList(placeHolders.toArray(new PlaceHolder[0])), negate));
					placeHolders.clear();
					i = 0;
				}
			}

			if (predicates.size() == 1) {
				queryContext.addPredicate(predicates.get(0));
			} else {
				LogicalOperationName name = negate ? LogicalOperationName.AND : LogicalOperationName.OR;
				queryContext.addPredicate(new BinaryLogicalOperator(name, predicates));
			}
		}

		return queryContext;
	}

}
