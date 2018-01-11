package org.citydb.query.builder.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;
import org.citygml4j.model.module.gml.GMLCoreModule;

import vcs.sqlbuilder.expression.LiteralList;
import vcs.sqlbuilder.expression.PlaceHolder;
import vcs.sqlbuilder.select.PredicateToken;
import vcs.sqlbuilder.select.operator.comparison.ComparisonFactory;
import vcs.sqlbuilder.select.operator.logical.BinaryLogicalOperator;
import vcs.sqlbuilder.select.operator.logical.LogicalOperationName;

public class IdOperatorBuilder {
	private final Query query;
	private final SchemaPathBuilder schemaPathBuilder;
	private final Set<Integer> objectclassIds;
	private final SchemaMapping schemaMapping;
	private final AbstractSQLAdapter sqlAdapter;

	public IdOperatorBuilder(Query query, SchemaPathBuilder schemaPathBuilder, Set<Integer> objectclassIds, SchemaMapping schemaMapping, AbstractSQLAdapter sqlAdapter) {
		this.query = query;
		this.schemaPathBuilder = schemaPathBuilder;
		this.objectclassIds = objectclassIds;
		this.schemaMapping = schemaMapping;
		this.sqlAdapter = sqlAdapter;
	}

	@SuppressWarnings("unchecked")
	protected SQLQueryContext buildResourceIdOperator(ResourceIdOperator operator, boolean negate) throws QueryBuildException {
		FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());		
		ValueReference valueReference = null;

		try {
			SchemaPath path = new SchemaPath(superType);	
			path.appendChild(superType.getProperty("id", GMLCoreModule.v3_1_1.getNamespaceURI(), true));
			valueReference = new ValueReference(path);
		} catch (InvalidSchemaPathException e) {
			throw new QueryBuildException(e.getMessage());
		}

		// build the value reference
		SQLQueryContext queryContext = schemaPathBuilder.buildSchemaPath(valueReference.getSchemaPath(), objectclassIds);
		List<PredicateToken> predicates = new ArrayList<PredicateToken>();

		if (operator.getResourceIds().size() == 1) {
			queryContext.select.addSelection(ComparisonFactory.equalTo(queryContext.targetColumn, new PlaceHolder<String>(operator.getResourceIds().iterator().next())));
		} else {
			List<PlaceHolder<String>> placeHolders = new ArrayList<PlaceHolder<String>>();
			int maxItems = sqlAdapter.getMaximumNumberOfItemsForInOperator();
			int i = 0;

			Iterator<String> iter = operator.getResourceIds().iterator();
			while (iter.hasNext()) {
				placeHolders.add(new PlaceHolder<String>(iter.next()));

				if (++i == maxItems || !iter.hasNext()) {
					predicates.add(ComparisonFactory.in(queryContext.targetColumn, new LiteralList(placeHolders.toArray(new PlaceHolder[placeHolders.size()])), negate));
					placeHolders.clear();
					i = 0;
				}
			}

			if (predicates.size() == 1) {
				queryContext.select.addSelection(predicates.get(0));
			} else {
				LogicalOperationName name = negate ? LogicalOperationName.AND : LogicalOperationName.OR;
				queryContext.select.addSelection(new BinaryLogicalOperator(name, predicates));
			}
		}

		return queryContext;
	}

}
