/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.query.builder.sql;

import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.core.database.adapter.AbstractSQLAdapter;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.filter.selection.operator.id.AbstractIdOperator;
import org.citydb.core.query.filter.selection.operator.id.DatabaseIdOperator;
import org.citydb.core.query.filter.selection.operator.id.ResourceIdOperator;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.logical.BinaryLogicalOperator;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationName;
import org.citygml4j.model.module.gml.GMLCoreModule;

import java.util.ArrayList;
import java.util.Collection;
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

	protected void buildIdOperator(AbstractIdOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		switch (operator.getOperatorName()) {
			case RESOURCE_ID:
				buildResourceIdOperator((ResourceIdOperator) operator, queryContext, negate, useLeftJoins);
				break;
			case DATABASE_ID:
				buildDatabaseIdOperator((DatabaseIdOperator) operator, queryContext, negate, useLeftJoins);
				break;
		}
	}

	private void buildResourceIdOperator(ResourceIdOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		buildIdOperator(operator.getResourceIds(), GMLCoreModule.v3_1_1.getNamespaceURI(), queryContext, negate, useLeftJoins);
	}

	private void buildDatabaseIdOperator(DatabaseIdOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		buildIdOperator(operator.getDatabaseIds(), CityDBADE200Module.v3_0.getNamespaceURI(), queryContext, negate, useLeftJoins);
	}

	@SuppressWarnings("unchecked")
	private <T> void buildIdOperator(Collection<T> ids, String namespaceURI, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		SchemaPath schemaPath;
		try {
			FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
			schemaPath = new SchemaPath(superType).appendChild(superType.getProperty(MappingConstants.ID, namespaceURI, true));
		} catch (InvalidSchemaPathException e) {
			throw new QueryBuildException(e.getMessage());
		}

		// build the value reference
		schemaPathBuilder.addSchemaPath(schemaPath, queryContext, useLeftJoins);

		// build predicates
		if (ids.size() == 1) {
			queryContext.addPredicate(ComparisonFactory.equalTo(queryContext.getTargetColumn(), new PlaceHolder<>(ids.iterator().next())));
		} else {
			List<PredicateToken> predicates = new ArrayList<>();
			List<PlaceHolder<T>> placeHolders = new ArrayList<>();
			Iterator<T> iter = ids.iterator();
			int maxItems = sqlAdapter.getMaximumNumberOfItemsForInOperator();
			int i = 0;

			while (iter.hasNext()) {
				placeHolders.add(new PlaceHolder<>(iter.next()));

				if (++i == maxItems || !iter.hasNext()) {
					predicates.add(ComparisonFactory.in(queryContext.getTargetColumn(), new LiteralList(placeHolders.toArray(new PlaceHolder[0])), negate));
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
	}
}
