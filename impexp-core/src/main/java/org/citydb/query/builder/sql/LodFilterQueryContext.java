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

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.InjectedProperty;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.Join;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;
import org.citydb.sqlbuilder.select.projection.ConstantColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LodFilterQueryContext {
	private final FeatureType type;
	private final String schema;
	private final Table table;

	private HashMap<String, Table> injectionTables;
	private HashMap<String, Join> injectionJoins;
	private List<PredicateToken> lodPredicates;		
	private List<LodFilterQueryContext> subContexts;
	private List<Join> parentJoins;
	private PredicateToken parentCondition;

	private boolean isHierachical = false;
	private Column targetColumn;

	protected LodFilterQueryContext(FeatureType type, String schema, Table table) {
		this.type = type;
		this.schema = schema;
		this.table = table == null ? new Table(type.getTable(), schema) : table;
		targetColumn = this.table.getColumn(MappingConstants.ID);
	}

	protected FeatureType getType() {
		return type;
	}

	protected Table getTable() {
		return table;
	}

	protected boolean isHierachical() {
		return isHierachical;
	}

	protected void setHierachical(boolean isHierachical) {
		this.isHierachical = isHierachical;
	}	

	protected Column getTargetColumn() {
		return targetColumn;
	}

	protected void setTargetColumn(Column targetColumn) {
		this.targetColumn = targetColumn;
	}

	protected boolean hasLodPredicates() {
		return lodPredicates != null && !lodPredicates.isEmpty();
	}

	protected List<PredicateToken> getLodPredicates() {
		return hasLodPredicates() ? lodPredicates : Collections.emptyList();
	}

	protected void addLodPredicate(PredicateToken lodPredicate) {
		if (lodPredicates == null)
			lodPredicates = new ArrayList<>();

		lodPredicates.add(lodPredicate);
	}

	protected boolean hasSubContexts() {
		return subContexts != null && !subContexts.isEmpty();
	}

	protected List<LodFilterQueryContext> getSubContexts() {
		return hasSubContexts() ? subContexts : Collections.emptyList();
	}

	protected void addSubContext(LodFilterQueryContext subContext) {
		if (subContexts == null)
			subContexts = new ArrayList<>();

		subContexts.add(subContext);
	}

	protected boolean hasParentJoins() {
		return parentJoins != null && !parentJoins.isEmpty();
	}

	protected List<Join> getParentJoins() {
		return hasParentJoins() ? parentJoins : Collections.emptyList();
	}

	protected void addParentJoin(Join parentJoin) {
		if (parentJoins == null)
			parentJoins = new ArrayList<>();

		parentJoins.add(parentJoin);
	}

	protected boolean hasParentCondition() {
		return parentCondition != null;
	}

	protected PredicateToken getParentCondition() {
		return parentCondition;
	}

	protected void setParentCondition(PredicateToken parentCondition) {
		this.parentCondition = parentCondition;
	}

	protected Table getInjectionTable(InjectedProperty injectedProperty) {
		String tableName = injectedProperty.getBaseJoin().getTable();

		Table table = null;
		if (injectionTables != null)
			table = injectionTables.get(tableName);
		else
			injectionTables = new HashMap<>();

		if (table == null) {
			table = new Table(tableName, schema);
			injectionTables.put(tableName, table);
		}

		return table;
	}

	protected boolean hasInjectionJoins() {
		return injectionJoins != null && !injectionJoins.isEmpty();
	}

	protected Collection<Join> getInjectionJoins() {
		return hasInjectionJoins() ? injectionJoins.values() : Collections.emptyList();
	}

	protected void addInjectionJoin(InjectedProperty injectedProperty, Table fromTable) {
		org.citydb.database.schema.mapping.Join join = injectedProperty.getBaseJoin();

		if (injectionJoins == null)
			injectionJoins = new HashMap<>();

		if (!injectionJoins.containsKey(join.getTable())) {
			Table toTable = getInjectionTable(injectedProperty);
			injectionJoins.put(join.getTable(), JoinFactory.left(toTable, join.getToColumn(), ComparisonName.EQUAL_TO, fromTable.getColumn(join.getToColumn())));
		}
	}

	protected Select build(boolean buildSubQueries) {	
		Select select = new Select();
		select.addProjection(targetColumn);
		
		if (hasInjectionJoins()) {
			for (Join join : injectionJoins.values())
				select.addJoin(join);
		}

		if (buildSubQueries && hasSubContexts()) {
			for (LodFilterQueryContext subContext : subContexts)
				buildSubQuery(subContext, this);
		}

		if (hasLodPredicates())
			select.addSelection(LogicalOperationFactory.OR(lodPredicates));

		return select;
	}

	private void buildSubQuery(LodFilterQueryContext subContext, LodFilterQueryContext parentContext) {		
		Select select = new Select();

		select.addProjection(new ConstantColumn(1).withFromTable(subContext.table));

		if (subContext.hasInjectionJoins()) {		
			for (Join join : subContext.injectionJoins.values())
				select.addJoin(join);
		}

		if (subContext.hasParentJoins()) {
			for (Join join : subContext.parentJoins)
				select.addJoin(join);
		}

		select.addSelection(subContext.getParentCondition());

		if (subContext.hasLodPredicates())
			select.addSelection(LogicalOperationFactory.OR(subContext.lodPredicates));

		parentContext.addLodPredicate(ComparisonFactory.exists(select));

		if (subContext.subContexts != null) {
			for (LodFilterQueryContext tmp : subContext.subContexts)
				buildSubQuery(tmp, subContext);
		}
	}

}
