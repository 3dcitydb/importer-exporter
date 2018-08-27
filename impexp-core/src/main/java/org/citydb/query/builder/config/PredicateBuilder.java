package org.citydb.query.builder.config;

import org.citydb.config.ConfigNamespaceFilter;
import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.comparison.AbstractComparisonOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.logical.AbstractBinaryLogicalOperator;
import org.citydb.config.project.query.filter.selection.logical.AbstractLogicalOperator;
import org.citydb.config.project.query.filter.selection.logical.NotOperator;
import org.citydb.config.project.query.filter.selection.spatial.AbstractSpatialOperator;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.util.SimpleXPathParser;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.logical.BinaryLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;

import javax.xml.namespace.NamespaceContext;

public class PredicateBuilder {
	private final ComparisonOperatorBuilder comparisonBuilder;
	private final SpatialOperatorBuilder spatialBuilder;
	private final IdOperatorBuilder idBuilder;
	private final SelectOperatorBuilder selectBuilder;
	private final SimpleXPathParser xPathParser;
	
	protected PredicateBuilder(Query query, SchemaMapping schemaMapping, NamespaceContext namespaceContext, AbstractDatabaseAdapter databaseAdapter) {
		if (namespaceContext == null)
			namespaceContext = new ConfigNamespaceFilter();
		
		xPathParser = new SimpleXPathParser(schemaMapping);
		comparisonBuilder = new ComparisonOperatorBuilder(query, xPathParser, schemaMapping, namespaceContext);
		spatialBuilder = new SpatialOperatorBuilder(query, xPathParser, schemaMapping, namespaceContext, databaseAdapter);
		idBuilder = new IdOperatorBuilder();
		selectBuilder = new SelectOperatorBuilder();
	}

	protected Predicate buildPredicate(AbstractPredicate predicateConfig) throws QueryBuildException {
		Predicate predicate = null;
		
		switch (predicateConfig.getPredicateName()) {
		case COMPARISON_OPERATOR:
			predicate = comparisonBuilder.buildComparisonOperator((AbstractComparisonOperator)predicateConfig);
			break;
		case SPATIAL_OPERATOR:
			predicate = spatialBuilder.buildSpatialOperator((AbstractSpatialOperator)predicateConfig);
			break;
		case LOGICAL_OPERATOR:
			predicate = buildLogicalOperator((AbstractLogicalOperator)predicateConfig);
			break;
		case ID_OPERATOR:
			predicate = idBuilder.buildResourceIdOperator((ResourceIdOperator)predicateConfig);
			break;
		case SQL_OPERATOR:
			predicate = selectBuilder.buildSelectOperator((SelectOperator)predicateConfig);
			break;
		}
		
		return predicate;
	}
	
	private Predicate buildLogicalOperator(AbstractLogicalOperator logicalOperatorConfig) throws QueryBuildException {
		if (logicalOperatorConfig.getOperatorName() == org.citydb.config.project.query.filter.selection.logical.LogicalOperatorName.NOT) {
			NotOperator not = (NotOperator)logicalOperatorConfig;
			return LogicalOperationFactory.NOT(buildPredicate(not.getOperand()));
		}

		else {
			AbstractBinaryLogicalOperator binaryOperatorConfig = (AbstractBinaryLogicalOperator)logicalOperatorConfig;
			if (binaryOperatorConfig.numberOfOperands() == 0)
				throw new QueryBuildException("No operand provided for the binary logical " + binaryOperatorConfig.getOperatorName() + " operator.");

			if (binaryOperatorConfig.numberOfOperands() == 1)
				return buildPredicate(binaryOperatorConfig.getOperands().get(0));
			
			BinaryLogicalOperator logicalOperator;
			switch (logicalOperatorConfig.getOperatorName()) {
			case AND:
				logicalOperator = new BinaryLogicalOperator(LogicalOperatorName.AND);
				break;
			case OR:
				logicalOperator = new BinaryLogicalOperator(LogicalOperatorName.OR);
				break;
			default:
				return null;
			}
			
			for (AbstractPredicate predicateConfig : binaryOperatorConfig.getOperands())
				logicalOperator.addOperand(buildPredicate(predicateConfig));
			
			return logicalOperator;
		}
	}
	
}
