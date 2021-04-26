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
package org.citydb.query.builder.sql;

import org.citydb.database.schema.mapping.AbstractJoin;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.Join;
import org.citydb.database.schema.mapping.JoinTable;
import org.citydb.database.schema.mapping.Joinable;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.TableRole;
import org.citydb.database.schema.path.AbstractNode;
import org.citydb.database.schema.path.FeatureTypeNode;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLQueryContext {
	private final FeatureType featureType;
	private final BuildContext buildContext;
	private final Deque<LogicalOperatorName> logicalOperators;

	private Table fromTable;
	private Select select;
	private Table toTable;
	private Column targetColumn;
	private List<PredicateToken> predicates;
	private Table cityObjectTable;
	private int currentAndContext;

	private enum ReuseMode {
		NOT_REUSABLE,
		AND_CONTEXT
	}

	SQLQueryContext(FeatureType featureType, Table fromTable) {
		this.featureType = featureType;
		this.fromTable = toTable = fromTable;

		select = new Select();
		buildContext = new BuildContext(new FeatureTypeNode(featureType), fromTable, new HashMap<>());
		logicalOperators = new ArrayDeque<>();
	}

	FeatureType getFeatureType() {
		return featureType;
	}

	public Select getSelect() {
		return select;
	}

	void setSelect(Select select) {
		this.select = select;
	}

	public Column getTargetColumn() {
		return targetColumn;
	}

	void setTargetColumn(Column targetColumn) {
		this.targetColumn = targetColumn;
	}

	public Table getFromTable() {
		return fromTable;
	}

	void setFromTable(Table fromTable) {
		this.fromTable = fromTable;
		buildContext.currentTable = fromTable;
	}

	public Table getToTable() {
		return toTable;
	}

	void setToTable(Table toTable) {
		this.toTable = toTable;
	}

	boolean hasPredicates() {
		return predicates != null && !predicates.isEmpty();
	}

	void addPredicate(PredicateToken predicate) {
		if (predicates == null)
			predicates = new ArrayList<>();

		predicates.add(predicate);
	}

	void addPredicates(PredicateToken... predicates) {
		Arrays.stream(predicates).forEach(this::addPredicate);
	}

	void setPredicate(PredicateToken predicate) {
		predicates = new ArrayList<>();
		predicates.add(predicate);
	}

	List<PredicateToken> getPredicates() {
		return predicates;
	}

	void unsetPredicates() {
		predicates = null;
	}

	void applyPredicates() {
		if (predicates != null) {
			predicates.forEach(select::addSelection);
			predicates = null;
		}
	}

	Table getCityObjectTable() {
		return cityObjectTable;
	}

	void setCityObjectTable(Table cityObjectTable) {
		this.cityObjectTable = cityObjectTable;
	}

	boolean requiresDistinct() {
		return buildContext.requiresDistinct();
	}

	BuildContext getBuildContext() {
		return buildContext;
	}

	void pushLogicalContext(LogicalOperatorName logicalOperator) {
		logicalOperators.push(logicalOperator);
		if (logicalOperator == LogicalOperatorName.AND)
			currentAndContext++;
	}

	void popLogicalContext() {
		LogicalOperatorName previous = logicalOperators.pop();
		if (logicalOperators.peek() == LogicalOperatorName.AND)
			buildContext.invalidateSubContexts();

		if (previous == LogicalOperatorName.AND)
			currentAndContext--;
	}

	class BuildContext {
		private final AbstractNode<?> node;
		private final Map<String, Table> tableContext;
		private Table currentTable;
		private List<BuildContext> children;
		private ReuseMode reuseMode;
		private int reuseContext;

		BuildContext(AbstractNode<?> node, Table currentTable, Map<String, Table> tableContext) {
			this.node = node;
			this.currentTable = currentTable;
			this.tableContext = tableContext;
		}

		AbstractNode<?> getNode() {
			return node;
		}

		Map<String, Table> getTableContext() {
			return tableContext;
		}

		Table getCurrentTable() {
			return currentTable;
		}

		BuildContext addSubContext(AbstractNode<?> node, Table currentTable, Map<String, Table> tableContext) {
			BuildContext nodeContext = new BuildContext(node, currentTable, tableContext);

			// remember the logical context for 1:n or n:m left joins
			if (node.getPathElement() instanceof Joinable) {
				AbstractJoin join = ((Joinable) node.getPathElement()).getJoin();
				if ((join instanceof Join && ((Join) join).getToRole() == TableRole.CHILD) || join instanceof JoinTable) {
					nodeContext.reuseContext = currentAndContext;
					nodeContext.reuseMode = logicalOperators.peek() == LogicalOperatorName.AND ?
							ReuseMode.NOT_REUSABLE :
							ReuseMode.AND_CONTEXT;
				}
			}

			if (children == null)
				children = new ArrayList<>();

			children.add(nodeContext);
			return nodeContext;
		}

		BuildContext findSubContext(AbstractNode<?> node) {
			if (children != null && node != null) {
				for (BuildContext child : children) {
					if (child.reuseMode == ReuseMode.NOT_REUSABLE
							|| (child.reuseMode == ReuseMode.AND_CONTEXT
							&& child.reuseContext < currentAndContext))
						continue;

					if (child.node.isEqualTo(node, logicalOperators.peek() == LogicalOperatorName.AND)) {
						// only return the context of a property if the types are also identical
						// otherwise the schema paths substantially differ
						if (PathElementType.TYPE_PROPERTIES.contains(node.getPathElement().getElementType())
								&& child.findSubContext(node.child()) == null)
							continue;

						return child;
					}
				}
			}

			return null;
		}

		void invalidateSubContexts() {
			if (children != null) {
				for (BuildContext child : children) {
					if (child.reuseMode == ReuseMode.AND_CONTEXT && child.reuseContext >= currentAndContext)
						child.reuseMode = ReuseMode.NOT_REUSABLE;

					child.invalidateSubContexts();
				}
			}
		}

		private boolean requiresDistinct() {
			if (children != null) {
				for (BuildContext child : children) {
					if (child.node.getPathElement() instanceof Joinable) {
						AbstractJoin join = ((Joinable) child.node.getPathElement()).getJoin();
						if ((join instanceof Join && ((Join) join).getToRole() == TableRole.CHILD)
								|| join instanceof JoinTable) {
							return true;
						}
					}

					if (child.requiresDistinct())
						return true;
				}
			}

			return false;
		}
	}
}
