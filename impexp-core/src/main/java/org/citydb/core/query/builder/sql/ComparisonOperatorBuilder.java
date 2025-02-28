/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

import org.citydb.core.database.adapter.AbstractSQLAdapter;
import org.citydb.core.database.schema.mapping.*;
import org.citydb.core.database.schema.path.AbstractNode;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.filter.selection.expression.*;
import org.citydb.core.query.filter.selection.operator.comparison.*;
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

import java.util.List;

public class ComparisonOperatorBuilder {
    private final SchemaPathBuilder schemaPathBuilder;
    private final AbstractSQLAdapter sqlAdapter;
    private final String schemaName;

    protected ComparisonOperatorBuilder(SchemaPathBuilder schemaPathBuilder, AbstractSQLAdapter sqlAdapter, String schemaName) {
        this.schemaPathBuilder = schemaPathBuilder;
        this.sqlAdapter = sqlAdapter;
        this.schemaName = schemaName;
    }

    protected void buildComparisonOperator(AbstractComparisonOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
        switch (operator.getOperatorName()) {
            case EQUAL_TO:
            case NOT_EQUAL_TO:
            case LESS_THAN:
            case GREATER_THAN:
            case LESS_THAN_OR_EQUAL_TO:
            case GREATER_THAN_OR_EQUAL_TO:
                buildBinaryOperator((BinaryComparisonOperator) operator, queryContext, negate, useLeftJoins);
                break;
            case BETWEEN:
                buildBetweenOperator((BetweenOperator) operator, queryContext, negate, useLeftJoins);
                break;
            case LIKE:
                buildLikeOperator((LikeOperator) operator, queryContext, negate, useLeftJoins);
                break;
            case NULL:
                buildNullComparison((NullOperator) operator, queryContext, negate, useLeftJoins);
                break;
        }
    }

    private void buildBinaryOperator(BinaryComparisonOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
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
                    valueReference = (ValueReference) expression;
                    break;
                case LITERAL:
                    literal = (AbstractLiteral<?>) expression;
                    break;
                case FUNCTION:
                    break;
            }
        }

        if (valueReference == null || literal == null)
            throw new QueryBuildException("Only combinations of ValueReference and Literal are supported as operands of a binary comparison operator.");

        // build the value reference
        schemaPathBuilder.addSchemaPath(valueReference.getSchemaPath(), queryContext, operator.isMatchCase(), useLeftJoins);

        // check for type mismatch of literal
        SimpleAttribute attribute = (SimpleAttribute) valueReference.getTarget();
        if (!literal.evaluatesToSchemaType(attribute.getType()))
            throw new QueryBuildException("Type mismatch between provided literal and database representation.");

        // map operands
        org.citydb.sqlbuilder.expression.Expression rightOperand = literal.convertToSQLPlaceHolder();
        org.citydb.sqlbuilder.expression.Expression leftOperand = queryContext.getTargetColumn();

        // consider match case
        if (!operator.isMatchCase() && ((PlaceHolder<?>) rightOperand).getValue() instanceof String) {
            rightOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), rightOperand);
            leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), leftOperand);
        }

        // implicit conversion from timestamp to date
        if (literal.getLiteralType() == LiteralType.TIMESTAMP && ((TimestampLiteral) literal).isDate())
            leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("timestamp.to_date"), leftOperand);

        // finally, create equivalent sql operation
        switch (operator.getOperatorName()) {
            case EQUAL_TO:
                queryContext.addPredicate(ComparisonFactory.equalTo(leftOperand, rightOperand, negate));
                break;
            case NOT_EQUAL_TO:
                queryContext.addPredicate(ComparisonFactory.notEqualTo(leftOperand, rightOperand, negate));
                break;
            case LESS_THAN:
                queryContext.addPredicate(ComparisonFactory.lessThan(leftOperand, rightOperand, negate));
                break;
            case GREATER_THAN:
                queryContext.addPredicate(ComparisonFactory.greaterThan(leftOperand, rightOperand, negate));
                break;
            case LESS_THAN_OR_EQUAL_TO:
                queryContext.addPredicate(ComparisonFactory.lessThanOrEqualTo(leftOperand, rightOperand, negate));
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                queryContext.addPredicate(ComparisonFactory.greaterThanOrEqualTo(leftOperand, rightOperand, negate));
                break;
            default:
                break;
        }
    }

    private void buildBetweenOperator(BetweenOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
        if (operator.getOperand().getExpressionName() != ExpressionName.VALUE_REFERENCE)
            throw new QueryBuildException("Only ValueRefernce is supported as operand of a between operator.");

        if (operator.getLowerBoundary().getExpressionName() != ExpressionName.LITERAL || operator.getUpperBoundary().getExpressionName() != ExpressionName.LITERAL)
            throw new QueryBuildException("Only Literal values are supported as lower and upper boundary of a between operator.");

        ValueReference valueReference = (ValueReference) operator.getOperand();
        AbstractLiteral<?> lowerBoundary = (AbstractLiteral<?>) operator.getLowerBoundary();
        AbstractLiteral<?> upperBoundary = (AbstractLiteral<?>) operator.getUpperBoundary();

        // build the value reference
        schemaPathBuilder.addSchemaPath(valueReference.getSchemaPath(), queryContext, useLeftJoins);

        // check for type mismatch of literal
        SimpleAttribute attribute = (SimpleAttribute) valueReference.getTarget();
        if (!lowerBoundary.evaluatesToSchemaType(attribute.getType()))
            throw new QueryBuildException("Type mismatch between provided lower boundary literal and database representation.");

        // check for type mismatch of literal
        if (!upperBoundary.evaluatesToSchemaType(attribute.getType()))
            throw new QueryBuildException("Type mismatch between provided upper boundary literal and database representation.");

        // map operands
        PlaceHolder<?> lowerBoundaryLiteral = lowerBoundary.convertToSQLPlaceHolder();
        PlaceHolder<?> upperBoundaryLiteral = upperBoundary.convertToSQLPlaceHolder();

        // finally, create equivalent sql operation
        queryContext.addPredicate(ComparisonFactory.between(queryContext.getTargetColumn(), lowerBoundaryLiteral, upperBoundaryLiteral, negate));
    }

    private void buildLikeOperator(LikeOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
        if (!operator.isSetLeftOperand() || !operator.isSetRightOperand())
            throw new QueryBuildException("Only one operand found for like comparison operator.");

        // we currently only support a combination of ValueReference and Literal as operands
        ValueReference valueReference = null;
        StringLiteral literal = null;

        for (Expression expression : operator.getOperands()) {
            switch (expression.getExpressionName()) {
                case VALUE_REFERENCE:
                    valueReference = (ValueReference) expression;
                    break;
                case LITERAL:
                    // we only support string literals
                    if (((AbstractLiteral<?>) expression).getLiteralType() != LiteralType.STRING)
                        throw new QueryBuildException("Only string literals are supported for a like operator.");

                    literal = (StringLiteral) expression;
                    break;
                case FUNCTION:
                    break;
            }
        }

        if (valueReference == null || literal == null)
            throw new QueryBuildException("Only combinations of ValueReference and Literal are supported as operands of a like operator.");

        // build the value reference
        schemaPathBuilder.addSchemaPath(valueReference.getSchemaPath(), queryContext, operator.isMatchCase(), useLeftJoins);

        // check for type mismatch of literal
        SimpleAttribute attribute = (SimpleAttribute) valueReference.getTarget();
        if (!literal.evaluatesToSchemaType(attribute.getType()))
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
        org.citydb.sqlbuilder.expression.Expression leftOperand = queryContext.getTargetColumn();

        // consider match case
        if (!operator.isMatchCase()) {
            rightOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), rightOperand);
            leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), leftOperand);
        }

        // finally, create equivalent sql operation
        queryContext.addPredicate(ComparisonFactory.like(leftOperand,
                rightOperand,
                value.contains(escapeCharacter) ? new org.citydb.sqlbuilder.expression.StringLiteral(escapeCharacter) : null,
                negate));
    }

    private void buildNullComparison(NullOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
        if (operator.getOperand().getExpressionName() != ExpressionName.VALUE_REFERENCE)
            throw new QueryBuildException("Only ValueRefernce is supported as operand of a null operator.");

        ValueReference valueReference = (ValueReference) operator.getOperand();
        SchemaPath schemaPath = valueReference.getSchemaPath();

        // build the is null checks. we use a copy of the schema path
        // as it might be changed by this operation
        buildIsNullPredicate((AbstractProperty) valueReference.getTarget(), schemaPath.copy(), queryContext, negate, useLeftJoins);

        // if the target property is an injected ADE property, we need to change the join for
        // the injection table from an inner join to a left join
        List<org.citydb.sqlbuilder.select.join.Join> joins = queryContext.getSelect().getJoins();
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
    }

    private void buildIsNullPredicate(AbstractProperty property, SchemaPath schemaPath, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
        if (property.getElementType() == PathElementType.SIMPLE_ATTRIBUTE || property.getElementType() == PathElementType.GEOMETRY_PROPERTY) {
            // for simple properties, we just check whether the column is null
            schemaPathBuilder.addSchemaPath(schemaPath, queryContext, useLeftJoins);
            queryContext.addPredicate(ComparisonFactory.isNull(queryContext.getTargetColumn(), negate));
        } else if (property.getElementType() == PathElementType.COMPLEX_ATTRIBUTE || PathElementType.TYPE_PROPERTIES.contains(property.getElementType())) {
            AbstractJoin abstractJoin = property.getJoin();

            if (abstractJoin != null) {
                // for complex properties that involve a join, we check whether
                // there is no foreign key reference from the join table to the target table.
                // we therefore remove the complex property from the schema
                // path and create an exists clause
                schemaPath.removeLastPathElement();
                schemaPathBuilder.addSchemaPath(schemaPath, queryContext, useLeftJoins);

                // derive join information
                String toTable;
                String toColumn;
                String fromColumn;
                List<Condition> conditions;

                if (abstractJoin instanceof Join) {
                    Join join = (Join) abstractJoin;
                    toTable = join.getTable();
                    toColumn = join.getToColumn();
                    fromColumn = join.getFromColumn();
                    conditions = join.getConditions();
                } else if (abstractJoin instanceof JoinTable) {
                    JoinTable joinTable = (JoinTable) abstractJoin;
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
                        .addSelection(ComparisonFactory.equalTo(table.getColumn(toColumn), queryContext.getToTable().getColumn(fromColumn)));

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
                queryContext.addPredicate(ComparisonFactory.exists(select, !negate));
            } else {
                try {
                    // if we deal with a complex property without a join, then we have
                    // found a type whose properties are stored inline the same table.
                    // checking whether the complex property is null therefore requires checking
                    // whether all its properties are null.
                    List<? extends AbstractProperty> innerProperties;

                    if (property.getElementType() == PathElementType.COMPLEX_ATTRIBUTE) {
                        innerProperties = ((ComplexAttribute) property).getType().getAttributes();
                    } else {
                        AbstractType<?> type = ((AbstractTypeProperty<?>) property).getType();
                        innerProperties = type.getProperties();

                        // add the inline object or data type to the schema path
                        schemaPath.appendChild(type);
                    }

                    // we iterate over all type properties and recursively create
                    // an is null check predicate using copies of the schema path
                    for (AbstractProperty innerProperty : innerProperties) {
                        SchemaPath innerPath = schemaPath.copy();
                        innerPath.appendChild(innerProperty);
                        buildIsNullPredicate(innerProperty, innerPath, queryContext, negate, useLeftJoins);
                    }

                    // if we shall check for not is null, then we combine the predicates using or
                    if (negate) {
                        PredicateToken predicate = LogicalOperationFactory.OR(queryContext.getPredicates());
                        queryContext.unsetPredicates();
                        queryContext.addPredicate(predicate);
                    }
                } catch (InvalidSchemaPathException e) {
                    //
                }
            }
        } else
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
