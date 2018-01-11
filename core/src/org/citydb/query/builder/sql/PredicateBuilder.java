package org.citydb.query.builder.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.comparison.AbstractComparisonOperator;
import org.citydb.query.filter.selection.operator.id.AbstractIdOperator;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;
import org.citydb.query.filter.selection.operator.logical.AbstractLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.BinaryLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;
import org.citydb.query.filter.selection.operator.logical.NotOperator;
import org.citydb.query.filter.selection.operator.spatial.AbstractSpatialOperator;

import vcs.sqlbuilder.schema.Column;
import vcs.sqlbuilder.schema.Table;
import vcs.sqlbuilder.select.PredicateToken;
import vcs.sqlbuilder.select.ProjectionToken;
import vcs.sqlbuilder.select.Select;
import vcs.sqlbuilder.select.operator.set.SetOperationName;
import vcs.sqlbuilder.select.operator.set.SetOperator;

public class PredicateBuilder {
	private final ComparisonOperatorBuilder comparisonBuilder;
	private final SpatialOperatorBuilder spatialBuilder;
	private final IdOperatorBuilder idBuilder;

	protected PredicateBuilder(Query query, Set<Integer> objectclassIds, SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter, String schemaName, BuildProperties buildProperties) {
		SchemaPathBuilder schemaPathBuilder = new SchemaPathBuilder(databaseAdapter.getSQLAdapter(), schemaName, buildProperties);
		comparisonBuilder = new ComparisonOperatorBuilder(schemaPathBuilder, objectclassIds, databaseAdapter.getSQLAdapter(), schemaName);
		spatialBuilder = new SpatialOperatorBuilder(query, schemaPathBuilder, objectclassIds, schemaMapping, databaseAdapter, schemaName);
		idBuilder = new IdOperatorBuilder(query, schemaPathBuilder, objectclassIds, schemaMapping, databaseAdapter.getSQLAdapter());
	}

	protected Select buildPredicate(Predicate predicate) throws QueryBuildException {
		return buildPredicate(predicate, false).select;
	}

	private SQLQueryContext buildPredicate(Predicate predicate, boolean negate) throws QueryBuildException {
		SQLQueryContext queryContext = null;

		switch (predicate.getPredicateName()) {
		case COMPARISON_OPERATOR:
			queryContext = comparisonBuilder.buildComparisonOperator((AbstractComparisonOperator)predicate, negate);
			break;
		case SPATIAL_OPERATOR:
			queryContext = spatialBuilder.buildSpatialOperator((AbstractSpatialOperator)predicate, negate);
			break;
		case LOGICAL_OPERATOR:
			queryContext = buildLogicalOperator((AbstractLogicalOperator)predicate, negate);
			break;
		case ID_OPERATOR:
			queryContext = buildIdOperator((AbstractIdOperator)predicate, negate);
			break;
		}

		return queryContext;
	}

	private SQLQueryContext buildIdOperator(AbstractIdOperator operator, boolean negate) throws QueryBuildException {
		SQLQueryContext queryContext = null;

		switch (operator.getOperatorName()) {
		case RESOURCE_ID:
			queryContext = idBuilder.buildResourceIdOperator((ResourceIdOperator)operator, negate);
			break;
		}

		return queryContext;
	}

	private SQLQueryContext buildLogicalOperator(AbstractLogicalOperator operator, boolean negate) throws QueryBuildException {
		if (operator.getOperatorName() == LogicalOperatorName.NOT) {
			NotOperator not = (NotOperator)operator;
			return buildPredicate(not.getOperand(), !negate);			
		}

		else {
			BinaryLogicalOperator binaryOperator = (BinaryLogicalOperator)operator;
			if (binaryOperator.numberOfOperands() == 0)
				throw new QueryBuildException("No operand provided for the binary logical " + binaryOperator.getOperatorName() + " operator.");

			if (binaryOperator.numberOfOperands() == 1)
				return buildPredicate(binaryOperator.getOperands().get(0), negate);

			List<SQLQueryContext> operands = new ArrayList<>(binaryOperator.numberOfOperands());
			for (Predicate operand : binaryOperator.getOperands())
				operands.add(buildPredicate(operand, negate));

			// try and combine AND conditions
			if (binaryOperator.getOperatorName() == LogicalOperatorName.AND) {
				combineAndConditions(operands);
				if (operands.size() == 1)
					return operands.get(0);
			}

			SetOperationName setOperation = null;
			switch (binaryOperator.getOperatorName()) {
			case AND:
				setOperation = !negate ? SetOperationName.INTERSECT : SetOperationName.UNION;
				break;
			case OR:
				setOperation = !negate ? SetOperationName.UNION : SetOperationName.INTERSECT;
				break;
			default:
				break;
			}

			List<Select> tmp = new ArrayList<>(operands.size());
			for (SQLQueryContext operand : operands)
				tmp.add(operand.select);

			Select select = new Select();
			Table table = new Table(new SetOperator(setOperation, operands.stream().map(item -> item.select).collect(Collectors.toList())));

			for (ProjectionToken token : tmp.get(0).getProjection()) {
				if (token instanceof Column) {
					Column column = (Column)token;
					select.addProjection(table.getColumn(column.getName()));
				}
			}

			return new SQLQueryContext(select);
		}
	}

	private void combineAndConditions(List<SQLQueryContext> operands) {
		// filter candidate contexts
		List<SQLQueryContext> candidates = new ArrayList<>();
		Iterator<SQLQueryContext> iter = operands.iterator();		
		while (iter.hasNext()) {
			SQLQueryContext context = iter.next();
			if (context.schemaPath != null) {
				context.backup = context.schemaPath.copy();
				candidates.add(context);
				iter.remove();
			}
		}

		if (candidates.isEmpty())
			return;

		// sort candidate contexts by schema path length and remove last path element
		candidates.sort((a, b) -> Integer.compare(b.schemaPath.size(), a.schemaPath.size()));
		for (SQLQueryContext context : candidates) {
			while (context.backup.getLastNode().getPathElement() instanceof AbstractProperty)
				context.backup.removeLastPathElement();
		}

		// group candidates by schema path containment
		Map<SQLQueryContext, List<SQLQueryContext>> groups = new HashMap<>();
		do {
			SQLQueryContext parent = candidates.remove(0);
			List<SQLQueryContext> children = new ArrayList<>();
			for (SQLQueryContext context : candidates) {
				if (parent.backup.contains(context.backup, false))
					children.add(context);
			}

			groups.put(parent, children);
			candidates.removeAll(children);
		} while (!candidates.isEmpty());

		// combine predicates of grouped candidates
		for (Entry<SQLQueryContext, List<SQLQueryContext>> entry : groups.entrySet()) {
			SQLQueryContext parent = entry.getKey();

			SQLQueryContext context = new SQLQueryContext(parent.select);
			context.schemaPath = parent.schemaPath;
			operands.add(context);

			// add predicates from children
			String selectString = parent.select.toString();
			for (SQLQueryContext child : entry.getValue()) {
				for (PredicateToken predicate : child.select.getSelection()) {
					String predicateString = predicate.toString();

					if (!selectString.contains(predicateString)
							|| predicateString.contains("?")
							|| predicateString.contains(child.targetColumn.toString()))
						parent.select.addSelection(predicate);
				}

				selectString = parent.select.toString();
				
				// copy optimizer hint if required
				String optimizerHint = child.select.getOptimizerString();
				if (optimizerHint != null)
					parent.select.setOptimizerString(optimizerHint);
			}
		}
	}

}
