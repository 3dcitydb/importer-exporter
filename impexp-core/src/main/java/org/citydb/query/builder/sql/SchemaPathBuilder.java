package org.citydb.query.builder.sql;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.database.schema.mapping.AbstractExtension;
import org.citydb.database.schema.mapping.AbstractJoin;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AbstractType;
import org.citydb.database.schema.mapping.AbstractTypeProperty;
import org.citydb.database.schema.mapping.ComplexAttribute;
import org.citydb.database.schema.mapping.Condition;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.GeometryProperty;
import org.citydb.database.schema.mapping.InjectedProperty;
import org.citydb.database.schema.mapping.Join;
import org.citydb.database.schema.mapping.JoinTable;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.ReverseJoin;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.mapping.SimpleType;
import org.citydb.database.schema.mapping.TableRole;
import org.citydb.database.schema.path.AbstractNode;
import org.citydb.database.schema.path.AbstractNodePredicate;
import org.citydb.database.schema.path.FeatureTypeNode;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.database.schema.path.predicate.comparison.ComparisonPredicateName;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.database.schema.path.predicate.logical.BinaryLogicalPredicate;
import org.citydb.database.schema.path.predicate.logical.LogicalPredicateName;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.expression.LiteralType;
import org.citydb.query.filter.selection.expression.TimestampLiteral;

import org.citydb.sqlbuilder.expression.AbstractSQLLiteral;
import org.citydb.sqlbuilder.expression.DoubleLiteral;
import org.citydb.sqlbuilder.expression.Expression;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.expression.StringLiteral;
import org.citydb.sqlbuilder.schema.AliasGenerator;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.DefaultAliasGenerator;
import org.citydb.sqlbuilder.schema.GlobalAliasGenerator;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.operator.logical.BinaryLogicalOperator;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationName;
import org.citydb.sqlbuilder.select.projection.Function;

public class SchemaPathBuilder {
	private final AbstractSQLAdapter sqlAdapter;
	private final String schemaName;
	private final BuildProperties buildProperties;
	private final DefaultAliasGenerator aliasGenerator;

	private Stack<HashMap<String, Table>> tableContext;
	private Table currentTable;
	private AbstractNode<?> currentNode;
	private boolean matchCase;

	protected SchemaPathBuilder(AbstractSQLAdapter sqlAdapter, String schemaName, BuildProperties buildProperties) {
		this.sqlAdapter = sqlAdapter;
		this.schemaName = schemaName;
		this.buildProperties = buildProperties;

		aliasGenerator = new DefaultAliasGenerator();
	}

	protected AliasGenerator geAliasGenerator() {
		return aliasGenerator;
	}

	protected SQLQueryContext buildSchemaPath(SchemaPath schemaPath, Set<Integer> objectClassIds) throws QueryBuildException {
		return buildSchemaPath(schemaPath, objectClassIds, true, true);
	}

	protected SQLQueryContext buildSchemaPath(SchemaPath schemaPath, Set<Integer> objectClassIds, boolean matchCase) throws QueryBuildException {
		return buildSchemaPath(schemaPath, objectClassIds, true, matchCase);
	}

	protected SQLQueryContext buildSchemaPath(SchemaPath schemaPath, Set<Integer> objectClassIds, boolean addProjection, boolean matchCase) throws QueryBuildException {
		FeatureTypeNode head = schemaPath.getFirstNode();
		AbstractNode<?> tail = schemaPath.getLastNode();

		// initialize context
		Select select = new Select();
		SQLQueryContext queryContext = new SQLQueryContext(select);
		aliasGenerator.reset();

		tableContext = new Stack<HashMap<String, Table>>();
		currentTable = new Table(head.getPathElement().getTable(), schemaName, aliasGenerator);
		currentNode = head;

		this.matchCase = matchCase;

		// iterate through schema path
		while (currentNode != null) {
			AbstractPathElement pathElement = currentNode.getPathElement();

			switch (pathElement.getElementType()) {
			case SIMPLE_ATTRIBUTE:
			case COMPLEX_ATTRIBUTE:
			case FEATURE_PROPERTY:
			case OBJECT_PROPERTY:
			case COMPLEX_PROPERTY:
			case IMPLICIT_GEOMETRY_PROPERTY:
			case GEOMETRY_PROPERTY:
				evaluatePropertyPath(select, currentNode.parent().getPathElement(), (AbstractProperty)pathElement);
				break;
			case FEATURE_TYPE:
			case OBJECT_TYPE:
			case COMPLEX_TYPE:
				AbstractType<?> type = (AbstractType<?>)pathElement;
				if (type.isSetTable()) {			
					tableContext.push(new HashMap<String, Table>());
					tableContext.peek().put(currentTable.getName(), currentTable);

					// correct table context in case of ADE subtypes
					if (currentNode != head && !type.getTable().equals(currentTable.getName()))
						traverseTypeHierarchy(type, select);
				}
				break;
			}

			// add projection and objectclass_id predicate
			if (currentNode == head) {
				queryContext.fromTable = currentTable;
				prepareStatement(select, head.getPathElement(), objectClassIds, addProjection);
			}

			// translate predicate to where-conditions
			if (currentNode.isSetPredicate())
				select.addSelection(evaluatePredicatePath(select, pathElement, currentNode.getPredicate()));

			currentNode = currentNode.child();
		}		

		// copy results to query context
		queryContext.toTable = currentTable;
		queryContext.schemaPath = schemaPath;

		if (tail.getPathElement().getElementType() == PathElementType.SIMPLE_ATTRIBUTE)
			queryContext.targetColumn = currentTable.getColumn(((SimpleAttribute)tail.getPathElement()).getColumn());
		else if (tail.getPathElement().getElementType() == PathElementType.GEOMETRY_PROPERTY) {
			GeometryProperty geometryProperty = (GeometryProperty)tail.getPathElement();
			queryContext.targetColumn = currentTable.getColumn(geometryProperty.isSetRefColumn() ? geometryProperty.getRefColumn() : geometryProperty.getInlineColumn());
		}

		// update alias generator
		GlobalAliasGenerator.getInstance().updateAlias(aliasGenerator);
		
		return queryContext;
	}

	private void prepareStatement(Select select, FeatureType featureType, Set<Integer> objectClassIds, boolean addProjection) throws QueryBuildException {
		if ((objectClassIds == null || objectClassIds.isEmpty()) && !addProjection)
			return;

		// retrieve table and column of id property
		Table fromTable = currentTable;
		AbstractProperty property = featureType.getProperty(MappingConstants.ID, MappingConstants.CITYDB_ADE_NAMESPACE_URI, true);	
		if (property == null)
			throw new QueryBuildException("Fatal database schema error: Failed to find '" + MappingConstants.ID + "' property.");

		evaluatePropertyPath(select, featureType, property);
		Column id = currentTable.getColumn(property.getPath());
		Column objectClassId = currentTable.getColumn(MappingConstants.OBJECTCLASS_ID);
		if (fromTable != currentTable)
			currentTable = fromTable;

		// add projection
		if (addProjection) {
			select.addProjection(id);
			select.addProjection(objectClassId);

			// check whether we shall add additional projection columns
			List<String> projectionColumns = buildProperties.getAdditionalProjectionColumns();
			if (!projectionColumns.isEmpty()) {
				Table table = id.getTable();
				for (String column : projectionColumns)
					select.addProjection(table.getColumn(column));
			}
		}

		// add objectclass_id predicate
		if (objectClassIds != null && !objectClassIds.isEmpty()) {
			if (objectClassIds.size() == 1)
				select.addSelection(ComparisonFactory.equalTo(objectClassId, new IntegerLiteral(objectClassIds.iterator().next())));
			else
				select.addSelection(ComparisonFactory.in(objectClassId, new LiteralList(objectClassIds.toArray(new Integer[objectClassIds.size()]))));	
		}
	}

	private void evaluatePropertyPath(Select select, AbstractPathElement parent, AbstractProperty property) throws QueryBuildException {
		boolean found = false;

		if (PathElementType.TYPES.contains(parent.getElementType())) {
			AbstractType<?> type = (AbstractType<?>)parent;

			while (!found && type != null) {
				for (AbstractProperty tmp : type.getProperties()) {
					if (tmp == property) {
						found = true;
						break;
					}

					// check whether a simple attribute is part of a complex attribute type definition
					if (property.getElementType() == PathElementType.SIMPLE_ATTRIBUTE &&
							tmp.getElementType() == PathElementType.COMPLEX_ATTRIBUTE &&
							((SimpleAttribute)property).hasParentAttributeType() &&
							((SimpleAttribute)property).getParentAttributeType() == ((ComplexAttribute)tmp).getType()) {
						property = tmp;
						found = true;
						break;
					}
				}

				if (!found) {
					if (type.isSetExtension()) {
						AbstractExtension<?> extension = type.getExtension();
						if (extension.isSetJoin())
							addJoin(select, extension.getJoin());

						type = extension.getBase();
					} else
						type = null;
				}
			}
		}

		if (property instanceof InjectedProperty) {
			InjectedProperty injectedProperty = (InjectedProperty)property;
			if (injectedProperty.isSetBaseJoin())
				addJoin(select, injectedProperty.getBaseJoin());
		}

		if (property.isSetJoin())
			addJoin(select, property.getJoin());
	}

	private PredicateToken evaluatePredicatePath(Select select, AbstractPathElement parent, AbstractNodePredicate predicate) throws QueryBuildException {
		if (predicate.getPredicateName() == ComparisonPredicateName.EQUAL_TO) {
			EqualToPredicate equalTo = (EqualToPredicate)predicate;
			Table fromTable = currentTable;

			if (PathElementType.TYPES.contains(parent.getElementType()))
				evaluatePropertyPath(select, parent, equalTo.getLeftOperand());

			Expression leftOperand = currentTable.getColumn(equalTo.getLeftOperand().getColumn());
			Expression rightOperand = equalTo.getRightOperand().convertToSQLPlaceHolder();

			if (!matchCase && ((PlaceHolder<?>)rightOperand).getValue() instanceof String) {
				rightOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), rightOperand);
				leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("string.upper"), leftOperand);
			}

			// implicit conversion from timestamp to date
			if (equalTo.getRightOperand().getLiteralType() == LiteralType.TIMESTAMP
					&& ((TimestampLiteral)equalTo.getRightOperand()).isDate()) {
				leftOperand = new Function(sqlAdapter.resolveDatabaseOperationName("timestamp.to_date"), leftOperand);
			}

			PredicateToken token = ComparisonFactory.equalTo(
					leftOperand, 
					rightOperand);

			if (currentTable != fromTable)
				currentTable = fromTable;

			return token;
		}

		else {
			// recursively build predicate that is composed of AND and OR operators
			BinaryLogicalPredicate logicalPredicate = (BinaryLogicalPredicate)predicate;
			PredicateToken leftOperand = evaluatePredicatePath(select, parent, logicalPredicate.getLeftOperand());
			PredicateToken rightOperand = evaluatePredicatePath(select, parent, logicalPredicate.getRightOperand());

			LogicalOperationName op = logicalPredicate.getPredicateName() == LogicalPredicateName.AND ? 
					LogicalOperationName.AND : LogicalOperationName.OR;

			return new BinaryLogicalOperator(leftOperand, op, rightOperand);
		}
	}

	private void traverseTypeHierarchy(AbstractType<?> type, Select select) throws QueryBuildException {
		// ADE types are often defined as subtype of a CityGML parent type and stored in their
		// own child table. The join of a property type might however point to the table of the
		// CityGML parent. In this case, the table context must be corrected to point to the 
		// child table of the ADE type rather than to the parent table. This is done by traversing
		// up the inheritance relations and adding reversed joins.
		String parentTable = currentTable.getName();

		while (type != null) {
			if (type.isSetExtension()) {
				AbstractExtension<?> extension = type.getExtension();				
				if (extension.isSetJoin()) {
					Join join = type.getExtension().getJoin();
					addJoin(select, new Join(type.getTable(), join.getToColumn(), join.getFromColumn(), TableRole.PARENT));
					if (join.getTable().equals(parentTable))
						break;
				}

				type = extension.getBase();
			} else 
				type = null;
		}
	}
	
	private void addJoin(Select select, AbstractJoin abstractJoin) throws QueryBuildException {		
		if (abstractJoin instanceof Join) {
			Join join = (Join)abstractJoin;
			String toTable = resolveTableToken(join, currentTable);
			addJoin(select, join, toTable, false);	
		}

		else if (abstractJoin instanceof JoinTable) {
			JoinTable joinTable = (JoinTable)abstractJoin;
			Table fromTable = currentTable;

			Table junctionTable = tableContext.peek().get(joinTable.getTable());
			if (junctionTable == null)
				junctionTable = new Table(joinTable.getTable(), schemaName, aliasGenerator);

			tableContext.push(new HashMap<String, Table>());
			tableContext.peek().put(currentTable.getName(), currentTable);

			for (Join join : joinTable.getJoins()) {
				currentTable = junctionTable;
				String toTable = resolveTableToken(join, fromTable);

				// create new context if required
				if (join.getTable().equals(MappingConstants.TARGET_TABLE_TOKEN) 
						&& fromTable.getName().equals(toTable))
					tableContext.push(new HashMap<String, Table>());

				addJoin(select, join, toTable, true);
			}
		}

		else if (abstractJoin instanceof ReverseJoin) {
			ListIterator<org.citydb.sqlbuilder.select.join.Join> iter = select.getJoins().listIterator(select.getJoins().size());
			while (iter.hasPrevious()) {
				org.citydb.sqlbuilder.select.join.Join candidate = iter.previous();
				if (candidate.getToColumn().getTable() == currentTable) {
					currentTable = candidate.getFromColumn().getTable();
					tableContext.peek().put(currentTable.getName(), currentTable);
					return;
				}
			}

			throw new QueryBuildException("Failed to find a reverse join for table '" + currentTable.getName() + "'.");
		}
	}

	private void addJoin(Select select, Join join, String joinTable, boolean force) throws QueryBuildException {
		Table toTable = null;

		// check whether we already have joined with target table
		if (!currentTable.getName().equals(joinTable)) {
			toTable = tableContext.peek().get(joinTable);
			if (!force && toTable != null) {
				currentTable = toTable;
				return;
			}
		}

		if (toTable == null)
			toTable = new Table(joinTable, schemaName, aliasGenerator);

		select.addJoin(JoinFactory.simple(toTable, join.getToColumn(), ComparisonName.EQUAL_TO, currentTable.getColumn(join.getFromColumn())));

		if (join.isSetConditions()) {
			for (Condition condition : join.getConditions()) {
				String value = condition.getValue();

				// resolve token in condition statement
				if (value.equals(MappingConstants.TARGET_OBJECTCLASS_ID_TOKEN)) {
					if (currentNode.child() == null)
						continue;

					AbstractPathElement target = (AbstractPathElement)currentNode.child().getPathElement();
					if (!PathElementType.OBJECT_TYPES.contains(target.getElementType()))
						throw new QueryBuildException("Failed to replace '" + MappingConstants.TARGET_OBJECTCLASS_ID_TOKEN + "' token due to missing target object type.");

					AbstractObjectType<?> objectType = (AbstractObjectType<?>)target;

					// we skip this condition if the table is not shared by multiple object types
					if (!objectType.hasSharedTable(true))
						continue;

					if (!objectType.isAbstract())
						value = String.valueOf(objectType.getObjectClassId());
					else {
						// we can skip the condition if the next element in the schema path is
						// the abstract object type that is the target of the property type
						if (PathElementType.TYPE_PROPERTIES.contains(currentNode.getPathElement().getElementType())) {
							AbstractTypeProperty<?> property = (AbstractTypeProperty<?>)currentNode.getPathElement();
							if (property.getType() == objectType)
								continue;
						}

						// otherwise create a literal list for an in operator
						List<? extends AbstractObjectType<?>> subTypes = objectType.listSubTypes(true);
						int[] ids = new int[subTypes.size()];
						for (int i = 0; i < subTypes.size(); i++)
							ids[i] = subTypes.get(i).getObjectClassId();

						select.addSelection(ComparisonFactory.in(toTable.getColumn(condition.getColumn()), new LiteralList(ids)));
						continue;
					}
				}

				else if (value.equals(MappingConstants.TARGET_ID_TOKEN)) {
					select.addSelection(ComparisonFactory.equalTo(toTable.getColumn(condition.getColumn()), toTable.getColumn(MappingConstants.ID)));
					continue;
				}

				AbstractSQLLiteral<?> literal = convertToSQLLiteral(value, condition.getType());
				select.addSelection(ComparisonFactory.equalTo(toTable.getColumn(condition.getColumn()), literal));
			}
		}

		// update tableContext and current fromTable pointer
		tableContext.peek().put(toTable.getName(), toTable);
		currentTable = toTable;
	}
	
	protected AbstractSQLLiteral<?> convertToSQLLiteral(String value, SimpleType type) throws QueryBuildException {
		AbstractSQLLiteral<?> literal = null;

		switch (type) {
			case INTEGER:
				try {
					literal = new IntegerLiteral(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					throw new QueryBuildException("Failed to convert '" + value + "' to an integer literal.", e);
				}
				break;
			case DOUBLE:
				try {
					literal = new DoubleLiteral(Double.parseDouble(value));
				} catch (NumberFormatException e) {
					throw new QueryBuildException("Failed to convert '" + value + "' to a double literal.", e);
				}
				break;
			case STRING:
				literal = new StringLiteral(value);
				break;
			case BOOLEAN:
			case DATE:
			case TIMESTAMP:
			case CLOB:
				throw new QueryBuildException(type + " values are not supported as join conditions.");
		}
		
		return literal;
	}

	private String resolveTableToken(Join join, Table fromTable) {
		String toTable = join.getTable();

		if (toTable.equals(MappingConstants.TARGET_TABLE_TOKEN)) {
			if (currentNode.child() != null) {	
				AbstractPathElement target = (AbstractPathElement)currentNode.child().getPathElement();
				switch (target.getElementType()) {
				case FEATURE_TYPE:
				case OBJECT_TYPE:
				case COMPLEX_TYPE:
					AbstractType<?> type = (AbstractType<?>)target;
					toTable = type.isSetTable() ? type.getTable() : fromTable.getName();
					break;
				case COMPLEX_ATTRIBUTE:
				case SIMPLE_ATTRIBUTE:
				case FEATURE_PROPERTY:
				case OBJECT_PROPERTY:
				case COMPLEX_PROPERTY:
				case IMPLICIT_GEOMETRY_PROPERTY:
				case GEOMETRY_PROPERTY:
					toTable = fromTable.getName();
					break;
				}
			} else
				toTable = fromTable.getName();
		}

		return toTable;
	}

}
