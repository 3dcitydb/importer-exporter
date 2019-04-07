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
import org.citydb.database.schema.mapping.AbstractJoin;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AbstractType;
import org.citydb.database.schema.mapping.AbstractTypeProperty;
import org.citydb.database.schema.mapping.ComplexAttribute;
import org.citydb.database.schema.mapping.Condition;
import org.citydb.database.schema.mapping.InjectedProperty;
import org.citydb.database.schema.mapping.Join;
import org.citydb.database.schema.mapping.JoinTable;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.path.AbstractNode;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.expression.AbstractLiteral;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.LiteralType;
import org.citydb.query.filter.selection.expression.StringLiteral;
import org.citydb.query.filter.selection.expression.TimestampLiteral;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.comparison.AbstractComparisonOperator;
import org.citydb.query.filter.selection.operator.comparison.BetweenOperator;
import org.citydb.query.filter.selection.operator.comparison.BinaryComparisonOperator;
import org.citydb.query.filter.selection.operator.comparison.ComparisonOperatorName;
import org.citydb.query.filter.selection.operator.comparison.LikeOperator;
import org.citydb.query.filter.selection.operator.comparison.NullOperator;
import org.citydb.sqlbuilder.expression.AbstractSQLLiteral;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinName;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;
import org.citydb.sqlbuilder.select.projection.ConstantColumn;
import org.citydb.sqlbuilder.select.projection.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ComparisonOperatorBuilder {
	private final SchemaPathBuilder schemaPathBuilder;
	private final AbstractSQLAdapter sqlAdapter;
	private final Set<Integer> objectclassIds;
	private final String schemaName;

	protected ComparisonOperatorBuilder(SchemaPathBuilder schemaPathBuilder, Set<Integer> objectclassIds, AbstractSQLAdapter sqlAdapter, String schemaName) {
		this.schemaPathBuilder = schemaPathBuilder;
		this.objectclassIds = objectclassIds;
		this.sqlAdapter = sqlAdapter;
		this.schemaName = schemaName;
	}

	protected SQLQueryContext buildComparisonOperator(AbstractComparisonOperator operator, boolean negate) throws QueryBuildException {
		SQLQueryContext queryContext = null;

		switch (operator.getOperatorName()) {
		case EQUAL_TO:
		case NOT_EQUAL_TO:
		case LESS_THAN:
		case GREATER_THAN:
		case LESS_THAN_OR_EQUAL_TO:
		case GREATER_THAN_OR_EQUAL_TO:
			queryContext = buildBinaryOperator((BinaryComparisonOperator)operator, negate);
			break;
		case BETWEEN:
			queryContext = buildBetweenOperator((BetweenOperator)operator, negate);
			break;
		case LIKE:
			queryContext = buildLikeOperator((LikeOperator)operator, negate);
			break;
		case NULL:
			queryContext = buildNullComparison((NullOperator)operator, negate);
			break;
		}

		return queryContext;
	}

	private SQLQueryContext buildBinaryOperator(BinaryComparisonOperator operator, boolean negate) throws QueryBuildException {
		if (!ComparisonOperatorName.BINARY_COMPARISONS.contains(operator.getOperatorName()))
			throw new QueryBuildException(operator + " is not a binary comparison operator.");

		if (!operator.isSetLeftOperand() || !operator.isSetRightOperand())
			throw new QueryBuildException("Only one operand found for binary comparison operator.");

		// we currently only support a combination of ValueReference and Literal as operands	
		ValueReference valueReference = null;
		AbstractLiteral<?> literal = null;

		for (Expression expression : operator.getOperands()) {
			switch (expression.getExpressionName()) {
			case VALUE_REFERENCE:
				valueReference = (ValueReference)expression;
				break;
			case LITERAL:
				literal = (AbstractLiteral<?>)expression;
				break;
			case FUNCTION:
				break;
			}
		}

		if (valueReference == null || literal == null)
			throw new QueryBuildException("Only combinations of ValueReference and Literal are supported as operands of a binary comparison operator.");

		// build the value reference
		SQLQueryContext queryContext = schemaPathBuilder.buildSchemaPath(valueReference.getSchemaPath(), objectclassIds, operator.isMatchCase());

		// check for type mismatch of literal
		SimpleAttribute attribute = (SimpleAttribute)valueReference.getTarget();
		if (!literal.evalutesToSchemaType(attribute.getType()))
			throw new QueryBuildException("Type mismatch between provided literal and database representation.");

		// map operands
		org.citydb.sqlbuilder.expression.Expression rightOperand = literal.convertToSQLPlaceHolder();
		org.citydb.sqlbuilder.expression.Expression leftOperand = queryContext.targetColumn;

		// consider match case
		if (!operator.isMatchCase() && ((PlaceHolder<?>)rightOperand).getValue() instanceof String) {
			rightOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), rightOperand);
			leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), leftOperand);
		}

		// implicit conversion from timestamp to date
		if (literal.getLiteralType() == LiteralType.TIMESTAMP && ((TimestampLiteral)literal).isDate())
			leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("timestamp.to_date"), leftOperand);

		// finally, create equivalent sql operation
		switch (operator.getOperatorName()) {
		case EQUAL_TO:
			queryContext.select.addSelection(ComparisonFactory.equalTo(leftOperand, rightOperand, negate));
			break;
		case NOT_EQUAL_TO:
			queryContext.select.addSelection(ComparisonFactory.notEqualTo(leftOperand, rightOperand, negate));
			break;
		case LESS_THAN:
			queryContext.select.addSelection(ComparisonFactory.lessThan(leftOperand, rightOperand, negate));
			break;
		case GREATER_THAN:
			queryContext.select.addSelection(ComparisonFactory.greaterThan(leftOperand, rightOperand, negate));
			break;
		case LESS_THAN_OR_EQUAL_TO:
			queryContext.select.addSelection(ComparisonFactory.lessThanOrEqualTo(leftOperand, rightOperand, negate));
			break;
		case GREATER_THAN_OR_EQUAL_TO:
			queryContext.select.addSelection(ComparisonFactory.greaterThanOrEqual(leftOperand, rightOperand, negate));
			break;
		default:
			break;			
		}

		return queryContext;
	}

	private SQLQueryContext buildBetweenOperator(BetweenOperator operator, boolean negate) throws QueryBuildException {
		if (operator.getOperand().getExpressionName() != ExpressionName.VALUE_REFERENCE)
			throw new QueryBuildException("Only ValueRefernce is supported as operand of a between operator.");

		if (operator.getLowerBoundary().getExpressionName() != ExpressionName.LITERAL || operator.getUpperBoundary().getExpressionName() != ExpressionName.LITERAL)
			throw new QueryBuildException("Only Literal values are supported as lower and upper boundary of a between operator.");

		ValueReference valueReference = (ValueReference)operator.getOperand();
		AbstractLiteral<?> lowerBoundary = (AbstractLiteral<?>)operator.getLowerBoundary();
		AbstractLiteral<?> upperBoundary = (AbstractLiteral<?>)operator.getUpperBoundary();

		// build the value reference
		SQLQueryContext queryContext = schemaPathBuilder.buildSchemaPath(valueReference.getSchemaPath(), objectclassIds);

		// check for type mismatch of literal
		SimpleAttribute attribute = (SimpleAttribute)valueReference.getTarget();
		if (!lowerBoundary.evalutesToSchemaType(attribute.getType()))
			throw new QueryBuildException("Type mismatch between provided lower boundary literal and database representation.");

		// check for type mismatch of literal
		if (!upperBoundary.evalutesToSchemaType(attribute.getType()))
			throw new QueryBuildException("Type mismatch between provided upper boundary literal and database representation.");

		// map operands
		PlaceHolder<?> lowerBoundaryLiteral = lowerBoundary.convertToSQLPlaceHolder();
		PlaceHolder<?> upperBoundaryLiteral = upperBoundary.convertToSQLPlaceHolder();

		// finally, create equivalent sql operation
		queryContext.select.addSelection(ComparisonFactory.between(queryContext.targetColumn, lowerBoundaryLiteral, upperBoundaryLiteral, negate));
		return queryContext;
	}

	private SQLQueryContext buildLikeOperator(LikeOperator operator, boolean negate) throws QueryBuildException {
		if (!operator.isSetLeftOperand() || !operator.isSetRightOperand())
			throw new QueryBuildException("Only one operand found for like comparison operator.");

		// we currently only support a combination of ValueReference and Literal as operands	
		ValueReference valueReference = null;
		StringLiteral literal = null;

		for (Expression expression : operator.getOperands()) {
			switch (expression.getExpressionName()) {
			case VALUE_REFERENCE:
				valueReference = (ValueReference)expression;
				break;
			case LITERAL:
				// we only support string literals
				if (((AbstractLiteral<?>)expression).getLiteralType() != LiteralType.STRING)
					throw new QueryBuildException("Only string literals are supported for a like operator.");

				literal = (StringLiteral)expression;
				break;
			case FUNCTION:
				break;
			}
		}

		if (valueReference == null || literal == null)
			throw new QueryBuildException("Only combinations of ValueReference and Literal are supported as operands of a like operator.");

		// build the value reference
		SQLQueryContext queryContext = schemaPathBuilder.buildSchemaPath(valueReference.getSchemaPath(), objectclassIds, operator.isMatchCase());

		// check for type mismatch of literal
		SimpleAttribute attribute = (SimpleAttribute)valueReference.getTarget();
		if (!literal.evalutesToSchemaType(attribute.getType()))
			throw new QueryBuildException("Type mismatch between provided literal and database representation.");

		// check wildcard and escape characters
		String wildCard = operator.getWildCard();
		String singleCharacter = operator.getSingleCharacter();
		String escapeCharacter = operator.getEscapeCharacter();

		if (wildCard == null || wildCard.length() > 1)
			throw new QueryBuildException("Wildcards must be defined by a single character.");

		if (singleCharacter == null || singleCharacter.length() > 1)
			throw new QueryBuildException("Wildcards must be defined by a single character.");

		if (escapeCharacter == null || escapeCharacter.length() > 1)
			throw new QueryBuildException("An escape character must be defined by a single character.");

		// replace wild cards
		String value = replaceWildCards(literal.getValue(), wildCard.charAt(0), singleCharacter.charAt(0), escapeCharacter.charAt(0));

		// map operands
		org.citydb.sqlbuilder.expression.Expression rightOperand = new PlaceHolder<>(value);
		org.citydb.sqlbuilder.expression.Expression leftOperand = queryContext.targetColumn;

		// consider match case
		if (!operator.isMatchCase()) {
			rightOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), rightOperand);
			leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), leftOperand);
		}

		// finally, create equivalent sql operation
		queryContext.select.addSelection(ComparisonFactory.like(leftOperand,
				rightOperand,
				value.contains(escapeCharacter) ? new org.citydb.sqlbuilder.expression.StringLiteral(escapeCharacter) : null,
				negate));

		return queryContext;
	}

	private SQLQueryContext buildNullComparison(NullOperator operator, boolean negate) throws QueryBuildException {
		if (operator.getOperand().getExpressionName() != ExpressionName.VALUE_REFERENCE)
			throw new QueryBuildException("Only ValueRefernce is supported as operand of a null operator.");

		ValueReference valueReference = (ValueReference)operator.getOperand();
		
		// create a copy of the schema path and create an is null check
		// the schema path might be changed by this operation
		SchemaPath schemaPath = valueReference.getSchemaPath();
		PredicateToken token = buildIsNullPredicate((AbstractProperty)valueReference.getTarget(), schemaPath.copy(), negate);

		// create equivalent sql operation
		SQLQueryContext queryContext = schemaPathBuilder.buildSchemaPath(schemaPath, objectclassIds);
		queryContext.select.addSelection(token);

		// if the target property is an injected ADE property, we need to change the join for
		// the injection table from an inner join to a left join
		List<org.citydb.sqlbuilder.select.join.Join> joins = queryContext.select.getJoins();
		if (!joins.isEmpty()) {
			AbstractNode<?> node = schemaPath.getLastNode();
			while (node.getPathElement() instanceof AbstractProperty) {
				if (node.getPathElement() instanceof InjectedProperty) {
					InjectedProperty property = (InjectedProperty) node.getPathElement();
					org.citydb.sqlbuilder.select.join.Join join = joins.get(joins.size() - 1);
					if (join.getToColumn().getTable().getName().equalsIgnoreCase(property.getBaseJoin().getTable())) {
						join.setJoinName(JoinName.LEFT_JOIN);
						break;
					}
				}

				node = node.parent();
			}
		}

		return queryContext;
	}
	
	private PredicateToken buildIsNullPredicate(AbstractProperty property, SchemaPath schemaPath, boolean negate) throws QueryBuildException {
		if (property.getElementType() == PathElementType.SIMPLE_ATTRIBUTE || property.getElementType() == PathElementType.GEOMETRY_PROPERTY) {
			// for simple properties, we just check whether the column is null
			SQLQueryContext queryContext = schemaPathBuilder.buildSchemaPath(schemaPath, objectclassIds);
			return ComparisonFactory.isNull(queryContext.targetColumn, negate);
		}

		else if (property.getElementType() == PathElementType.COMPLEX_ATTRIBUTE || PathElementType.TYPE_PROPERTIES.contains(property.getElementType())) {
			AbstractJoin abstractJoin = property.getJoin();

			if (abstractJoin != null) {
				// for complex properties that involve a join, we check whether
				// there is no foreign key reference from the join table to the target table.
				// we therefore remove the complex property from the schema
				// path and create an exists clause
				schemaPath.removeLastPathElement();
				SQLQueryContext queryContext = schemaPathBuilder.buildSchemaPath(schemaPath, objectclassIds);

				// derive join information
				String toTable;
				String toColumn;
				String fromColumn;
				List<Condition> conditions;
				
				if (abstractJoin instanceof Join) {
					Join join = (Join)abstractJoin;
					toTable = join.getTable();
					toColumn = join.getToColumn();
					fromColumn = join.getFromColumn();
					conditions = join.getConditions();
				} else if (abstractJoin instanceof JoinTable) {
					JoinTable joinTable = (JoinTable)abstractJoin;
					toTable = joinTable.getTable();

					Join join = joinTable.getJoin();
					toColumn = join.getFromColumn();
					fromColumn = join.getToColumn();
					conditions = join.getConditions();
				} else
					throw new QueryBuildException("Failed to build null operator for property '" + property + "'.");

				// create select based on join information 
				Table table = new Table(toTable, schemaName, schemaPathBuilder.getAliasGenerator());
				Select select = new Select()
						.addProjection(new ConstantColumn(1).withFromTable(table))
						.addSelection(ComparisonFactory.equalTo(table.getColumn(toColumn), queryContext.toTable.getColumn(fromColumn)));

				// add join conditions if required
				if (conditions != null) {
					for (Condition condition : conditions) {
						String value = condition.getValue();
						
						// we can skip this token since we do not care about the referenced object type
						if (value.equals(MappingConstants.TARGET_OBJECTCLASS_ID_TOKEN))
							continue;
						
						else if (value.equals(MappingConstants.TARGET_ID_TOKEN)) {
							select.addSelection(ComparisonFactory.equalTo(table.getColumn(condition.getColumn()), table.getColumn(MappingConstants.ID)));
							continue;
						}
						
						AbstractSQLLiteral<?> literal = schemaPathBuilder.convertToSQLLiteral(value, condition.getType());
						select.addSelection(ComparisonFactory.equalTo(table.getColumn(condition.getColumn()), literal));
					}
				}
				
				// finally, create exists clause from select
				return ComparisonFactory.exists(select, !negate);
			}

			else {
				try {
					// if we deal with a complex property without a join, then we have
					// found a type whose properties are stored inline the same table.
					// checking whether the complex property is null therefore requires checking
					// whether all its properties are null.					
					List<? extends AbstractProperty> innerProperties;

					if (property.getElementType() == PathElementType.COMPLEX_ATTRIBUTE) {
						innerProperties = ((ComplexAttribute)property).getType().getAttributes();
					} else {
						AbstractType<?> type = ((AbstractTypeProperty<?>)property).getType();
						innerProperties = type.getProperties();
						
						// add the inline object or data type to the schema path
						schemaPath.appendChild(type);
					}

					// we iterate over all type properties and recursively create
					// an is null check predicate using copies of the schema path
					List<PredicateToken> tokens = new ArrayList<>();
					for (AbstractProperty innerProperty : innerProperties) {
						SchemaPath innerPath = schemaPath.copy();
						innerPath.appendChild(innerProperty);
						
						tokens.add(buildIsNullPredicate(innerProperty, innerPath, negate));
					}

					return !negate ?
							LogicalOperationFactory.AND(tokens) :
							LogicalOperationFactory.OR(tokens);
				} catch (InvalidSchemaPathException e) {
					//
				}
			}
		}

		throw new QueryBuildException("Failed to build null operator for property '" + property + "'.");
	}

	private String replaceWildCards(String value, char wildCard, char singleChar, char escapeChar) {
		boolean escapeSQLWildCard = wildCard != '%' && singleChar != '%';
		boolean espaceSQLSingleChar = wildCard != '_' && singleChar != '_';

		StringBuilder tmp = new StringBuilder();

		for (int offset = 0; offset < value.length(); offset++) {
			char ch = value.charAt(offset);

			if (ch == escapeChar) {
				// keep escaped chars as is
				tmp.append(ch);
				if (++offset < value.length())
					tmp.append(value.charAt(offset));
			} else if ((ch == '%' && escapeSQLWildCard) || (ch == '_' && espaceSQLSingleChar)) {
				// escape SQL wild cards
				tmp.append(escapeChar);
				tmp.append(ch);
			} else if (ch == wildCard) {
				// replace user-defined wild card
				tmp.append('%');
			} else if (ch == singleChar) {
				// replace user-defined single char
				tmp.append('_');
			} else
				tmp.append(ch);
		}

		return tmp.toString();
	}

}
