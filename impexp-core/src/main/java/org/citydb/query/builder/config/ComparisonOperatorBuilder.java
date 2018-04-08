package org.citydb.query.builder.config;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.NamespaceContext;

import org.citydb.config.project.query.filter.selection.comparison.AbstractBinaryComparisonOperator;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.database.schema.util.SimpleXPathParser;
import org.citydb.database.schema.util.XPathException;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.expression.AbstractLiteral;
import org.citydb.query.filter.selection.expression.BooleanLiteral;
import org.citydb.query.filter.selection.expression.DateLiteral;
import org.citydb.query.filter.selection.expression.DoubleLiteral;
import org.citydb.query.filter.selection.expression.IntegerLiteral;
import org.citydb.query.filter.selection.expression.StringLiteral;
import org.citydb.query.filter.selection.expression.TimestampLiteral;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.comparison.AbstractComparisonOperator;
import org.citydb.query.filter.selection.operator.comparison.BetweenOperator;
import org.citydb.query.filter.selection.operator.comparison.BinaryComparisonOperator;
import org.citydb.query.filter.selection.operator.comparison.ComparisonFactory;
import org.citydb.query.filter.selection.operator.comparison.LikeOperator;
import org.citydb.query.filter.selection.operator.comparison.NullOperator;

public class ComparisonOperatorBuilder {
	private final Query query;
	private final SimpleXPathParser xPathParser;
	private final SchemaMapping schemaMapping;
	private final NamespaceContext namespaceContext;
	
	protected ComparisonOperatorBuilder(Query query, SimpleXPathParser xPathParser, SchemaMapping schemaMapping, NamespaceContext namespaceContext) {
		this.query = query;
		this.xPathParser = xPathParser;
		this.schemaMapping = schemaMapping;
		this.namespaceContext = namespaceContext;	
	}

	protected Predicate buildComparisonOperator(org.citydb.config.project.query.filter.selection.comparison.AbstractComparisonOperator operatorConfig) throws QueryBuildException {
		if (!operatorConfig.isSetValueReference())
			throw new QueryBuildException("The comparison operator " + operatorConfig.getOperatorName() + " requires a value reference.");

		AbstractComparisonOperator operator = null;

		try {
			switch (operatorConfig.getOperatorName()) {
			case EQUAL_TO:
			case NOT_EQUAL_TO:
			case GREATER_THAN:
			case GREATER_THAN_OR_EQUAL_TO:
			case LESS_THAN:
			case LESS_THAN_OR_EQUAL_TO:
				operator = buildBinaryOperator((AbstractBinaryComparisonOperator)operatorConfig);
				break;
			case BETWEEN:
				operator = buildBetweenOperator((org.citydb.config.project.query.filter.selection.comparison.BetweenOperator)operatorConfig);
				break;
			case LIKE:
				operator = buildLikeOperator((org.citydb.config.project.query.filter.selection.comparison.LikeOperator)operatorConfig);
				break;
			case NULL:
				operator = buildNullComparison((org.citydb.config.project.query.filter.selection.comparison.NullOperator)operatorConfig);
				break;
			}
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build the comparison operator " + operatorConfig.getOperatorName() + ".", e);
		}

		return operator;
	}

	private BinaryComparisonOperator buildBinaryOperator(AbstractBinaryComparisonOperator operatorConfig) throws FilterException, QueryBuildException {
		if (!operatorConfig.isSetLiteral())
			throw new QueryBuildException("The binary comparison operator " + operatorConfig.getOperatorName() + " requires a literal value.");

		// build the value reference
		ValueReference valueReference = buildValueReference(operatorConfig);
		if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
			throw new QueryBuildException("The value reference of the comparison operator " + operatorConfig.getOperatorName() + " must point to a simple thematic attribute.");

		// convert literal
		AbstractLiteral<?> literal = convertToLiteral(operatorConfig.getLiteral(), (SimpleAttribute)valueReference.getTarget());

		// create equivalent filter operation
		BinaryComparisonOperator operator = null;
		switch (operatorConfig.getOperatorName()) {
		case EQUAL_TO:
			operator = ComparisonFactory.equalTo(valueReference, literal);
			break;
		case NOT_EQUAL_TO:
			operator = ComparisonFactory.notEqualTo(valueReference, literal);
			break;
		case GREATER_THAN:
			operator = ComparisonFactory.greaterThan(valueReference, literal);
			break;
		case GREATER_THAN_OR_EQUAL_TO:
			operator = ComparisonFactory.greaterThanOrEqualTo(valueReference, literal);
			break;
		case LESS_THAN:
			operator = ComparisonFactory.lessThan(valueReference, literal);
			break;
		case LESS_THAN_OR_EQUAL_TO:
			operator = ComparisonFactory.lessThanOrEqualTo(valueReference, literal);
			break;
		default:
			throw new QueryBuildException("Failed to build the binary comparison operator " + operatorConfig.getOperatorName() + ".");
		}

		// finally, set match case
		operator.setMatchCase(operatorConfig.isMatchCase());

		return operator;
	}

	private BetweenOperator buildBetweenOperator(org.citydb.config.project.query.filter.selection.comparison.BetweenOperator operatorConfig) throws FilterException, QueryBuildException {
		if (!operatorConfig.isSetLowerBoundary() || !operatorConfig.isSetUpperBoundary())
			throw new QueryBuildException("The between operator requires both a lower and upper boundary value.");

		// build the value reference
		ValueReference valueReference = buildValueReference(operatorConfig);
		if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
			throw new QueryBuildException("The value reference of the comparison operator " + operatorConfig.getOperatorName() + " must point to a simple thematic attribute.");

		// convert lower and upper boundary
		AbstractLiteral<?> lowerBoundary = convertToLiteral(operatorConfig.getLowerBoundary(), (SimpleAttribute)valueReference.getTarget());
		AbstractLiteral<?> upperBoundary = convertToLiteral(operatorConfig.getUpperBoundary(), (SimpleAttribute)valueReference.getTarget());

		// finally, create equivalent filter operation
		return ComparisonFactory.between(valueReference, lowerBoundary, upperBoundary);
	}

	private LikeOperator buildLikeOperator(org.citydb.config.project.query.filter.selection.comparison.LikeOperator operatorConfig) throws FilterException, QueryBuildException {
		if (!operatorConfig.isSetLiteral())
			throw new QueryBuildException("The like operator requires a literal value.");

		// build the value reference
		ValueReference valueReference = buildValueReference(operatorConfig);
		if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
			throw new QueryBuildException("The value reference of the comparison operator " + operatorConfig.getOperatorName() + " must point to a simple thematic attribute.");

		// convert literal
		AbstractLiteral<?> literal = convertToLiteral(operatorConfig.getLiteral(), (SimpleAttribute)valueReference.getTarget());

		// check wildcard and escape characters
		String wildCard = operatorConfig.getWildCard();
		String singleCharacter = operatorConfig.getSingleCharacter();
		String escapeCharacter = operatorConfig.getEscapeCharacter();

		if (wildCard == null || wildCard.length() > 1)
			throw new QueryBuildException("Wildcards must be defined by a single character.");

		if (singleCharacter == null || singleCharacter.length() > 1)
			throw new QueryBuildException("Wildcards must be defined by a single character.");

		if (escapeCharacter == null || escapeCharacter.length() > 1)
			throw new QueryBuildException("An escape character must be defined by a single character.");

		// finally, create equivalent filter operation
		LikeOperator operator = ComparisonFactory.like(valueReference, literal);
		operator.setWildCard(wildCard);
		operator.setSingleCharacter(singleCharacter);
		operator.setEscapeCharacter(escapeCharacter);
		operator.setMatchCase(operatorConfig.isMatchCase());

		return operator;
	}

	private NullOperator buildNullComparison(org.citydb.config.project.query.filter.selection.comparison.NullOperator operatorConfig) throws FilterException, QueryBuildException {
		// build the value reference
		ValueReference valueReference = buildValueReference(operatorConfig);
		if (!(valueReference.getTarget() instanceof AbstractProperty))
			throw new QueryBuildException("The value reference of the comparison operator " + operatorConfig.getOperatorName() + " must point to a property.");

		// finally, create equivalent filter operation
		return ComparisonFactory.isNull(valueReference);
	}

	private ValueReference buildValueReference(org.citydb.config.project.query.filter.selection.comparison.AbstractComparisonOperator operatorConfig) throws QueryBuildException {
		ValueReference valueReference = null;

		try {
			FeatureType featureType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
			SchemaPath path = xPathParser.parse(operatorConfig.getValueReference(), featureType, namespaceContext);
			valueReference = new ValueReference(path);

			// reset XPath expression using default namespace prefixes
			operatorConfig.setValueReference(path.toXPath());
		} catch (XPathException | InvalidSchemaPathException e) {
			throw new QueryBuildException("Failed to parse the value reference " + operatorConfig.getValueReference() + ".", e);
		}

		return valueReference;
	}

	private AbstractLiteral<?> convertToLiteral(String literalValue, SimpleAttribute attribute) throws QueryBuildException {
		AbstractLiteral<?> literal = null;

		switch (attribute.getType()) {
			case STRING:
				literal = new StringLiteral(literalValue);
				break;
			case DOUBLE:
				try {
					literal = new DoubleLiteral(Double.parseDouble(literalValue));
				} catch (NumberFormatException e) {
					//
				}
				break;
			case INTEGER:
				try {
					literal = new IntegerLiteral(Integer.parseInt(literalValue));
				} catch (NumberFormatException e) {
					//
				}
				break;
			case BOOLEAN:
				try {
					if ("true".equals(literalValue.toLowerCase()))
						literal = new BooleanLiteral(true);
					else if ("false".equals(literalValue.toLowerCase()))
						literal = new BooleanLiteral(false);
					else {
						long value = Integer.parseInt(literalValue);
						literal = new BooleanLiteral(value == 1);
					}
				} catch (Exception e) {
					//
				}
				break;
			case DATE:
				try {
					literal = new DateLiteral(DatatypeConverter.parseDateTime(literalValue));
					((DateLiteral)literal).setXMLLiteral(literalValue);
				} catch (IllegalArgumentException e) {
					//
				}
				break;
			case TIMESTAMP:
				try {
					XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(literalValue);
					literal = new TimestampLiteral(cal.toGregorianCalendar());
					((TimestampLiteral)literal).setXMLLiteral(literalValue);
					((TimestampLiteral)literal).setDate(cal.getXMLSchemaType() == DatatypeConstants.DATE);
				} catch (DatatypeConfigurationException | IllegalArgumentException e) {
					//
				}
				break;
			case CLOB:
				throw new QueryBuildException("CLOB columns are not supported in comparison operations.");
		}

		if (literal == null)
			throw new QueryBuildException("Failed to map '" + literalValue + "' to a " + attribute.getType().value() + " value.");

		return literal;
	}

}
