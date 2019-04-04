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

import org.citydb.ade.ADEExtensionManager;
import org.citydb.database.schema.mapping.AbstractExtension;
import org.citydb.database.schema.mapping.AbstractJoin;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.FeatureProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.GeometryProperty;
import org.citydb.database.schema.mapping.ImplicitGeometryProperty;
import org.citydb.database.schema.mapping.InjectedProperty;
import org.citydb.database.schema.mapping.JoinTable;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.mapping.SimpleType;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodFilterMode;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.SubQueryExpression;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.operator.logical.BinaryLogicalOperator;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationName;
import org.citydb.sqlbuilder.select.operator.set.SetOperationFactory;
import org.citydb.sqlbuilder.select.operator.set.SetOperationName;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LodFilterBuilder {
	private final String schemaName;
	private final Matcher lodMatcher;
	private final FeatureType cityObject;
	private final List<AppSchema> disabledADESchemas;

	protected LodFilterBuilder(SchemaMapping schemaMapping, String schemaName) {
		this.schemaName = schemaName;

		cityObject = schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI());
		lodMatcher = Pattern.compile("(?i)^lod([0-4]).*", Pattern.UNICODE_CHARACTER_CLASS).matcher("");		
		disabledADESchemas = ADEExtensionManager.getInstance().getDisabledSchemas(schemaMapping);
	}

	protected void buildLodFilter(LodFilter lodFilter, FeatureTypeFilter typeFilter, CityGMLVersion targetVersion, SQLQueryContext queryContext) throws QueryBuildException {
		List<Select> queries = new ArrayList<>();

		for (FeatureType type : typeFilter.getFeatureTypes(targetVersion)) {
			boolean isNested = hasNestedTypesWithLodProperties(type);

			if (!type.hasLodProperties() && !isNested)
				continue;

			if (!isNested && !satisfiesLodFilter(type, lodFilter))
				continue;

			List<Select> subQueries = new ArrayList<>();
			if (lodFilter.getFilterMode() != LodFilterMode.AND || !isNested)
				buildLodSelection(type, lodFilter, subQueries);	
			else {		
				for (LodIterator lods = lodFilter.iterator(0, 4); lods.hasNext(); ) {
					LodFilter singleLodFilter = new LodFilter(false, LodFilterMode.AND);
					singleLodFilter.setEnabled(lods.next(), true);
					singleLodFilter.setSearchDepth(lodFilter.getSearchDepth());
					
					buildLodSelection(type, singleLodFilter, subQueries);
				}
			}

			if (subQueries.size() == 1)
				queries.add(subQueries.get(0));
			else if (subQueries.size() > 1) {
				Select subQuery = new Select();
				SetOperationName operationName = lodFilter.getFilterMode() != LodFilterMode.AND ? SetOperationName.UNION_ALL : SetOperationName.INTERSECT;			
				Table tmp = new Table(SetOperationFactory.generic(operationName.toString(), subQueries));
				subQuery.addProjection(tmp.getColumn(((Column)subQueries.get(0).getProjection().get(0)).getName()));

				queries.add(subQuery);
			}
		}

		Select select = queryContext.select;
		Table table = queryContext.fromTable;

		if (!queries.isEmpty()) {
			SubQueryExpression subQueryExpression = queries.size() == 1 ? queries.get(0) : SetOperationFactory.unionAll(queries);
			select.addSelection(ComparisonFactory.in(table.getColumn(MappingConstants.ID), subQueryExpression));
		} else {
			// the selected feature types are not available for the requested LoD filter,
			// so we add a dummy predicate to make sure the query returns null
			select.addSelection(ComparisonFactory.isNull(table.getColumn(MappingConstants.ID)));
		}
	}

	private void buildLodSelection(FeatureType type, LodFilter lodFilter, List<Select> selects) {
		List<LodFilterQueryContext> queryContexts = buildLodQueryContexts(type, null);
		for (LodFilterQueryContext queryContext : queryContexts) {
			processGeometryProperties(queryContext, lodFilter);
			processLodProperties(queryContext, lodFilter);
			processNestedFeatures(queryContext, lodFilter);

			if (queryContext.hasLodPredicates() || queryContext.hasSubContexts())
				selects.add(queryContext.build(true));
		}
	}

	private List<LodFilterQueryContext> buildLodQueryContexts(FeatureType type, Table targetTable) {
		List<LodFilterQueryContext> contexts = new ArrayList<>();
		LodFilterQueryContext context = new LodFilterQueryContext(type, schemaName, targetTable);
		Table currentTable = context.getTable();

		while (type != null) {
			for (AbstractProperty property : type.getProperties()) {
				if (property.isSetJoin() && property.getJoin() instanceof org.citydb.database.schema.mapping.Join) {
					org.citydb.database.schema.mapping.Join join = (org.citydb.database.schema.mapping.Join)property.getJoin();

					if (join.isSetTreeHierarchy()) {

						if (context.isHierachical()) {
							// found multiple hierarchies within the same type
							// create new query context
							contexts.add(context);
							context = new LodFilterQueryContext(type, schemaName, type.getTable() == null ? currentTable : null);
						}

						context.setHierachical(true);
						context.setTargetColumn(context.getTable().getColumn(join.getTreeHierarchy().getRootColumn()));
					}
				}
			}

			if (type.isSetExtension() && type.getExtension().getBase() != cityObject) {
				AbstractExtension<FeatureType> extension = type.getExtension();
				if (extension.isSetJoin()) {
					org.citydb.database.schema.mapping.Join join = extension.getJoin();
					currentTable = new Table(join.getTable(), schemaName);
				}

				type = extension.getBase();

				// create new query context
				contexts.add(context);
				context = new LodFilterQueryContext(type, schemaName, currentTable);			
			} else
				break;			
		}

		contexts.add(context);
		return contexts;
	}
	
	private void processLodProperties(LodFilterQueryContext context, LodFilter lodFilter) {
		for (AbstractProperty property : context.getType().getProperties()) {
			if (property.getElementType() != PathElementType.SIMPLE_ATTRIBUTE
					|| ((SimpleAttribute)property).getType() != SimpleType.INTEGER
					|| !property.getPath().equalsIgnoreCase("lod"))
				continue;
						
			Table table = context.getTable();
			if (property instanceof InjectedProperty) {
				if (disabledADESchemas.contains(property.getSchema()))
					continue;
				
				table = context.getInjectionTable((InjectedProperty)property);
			}
			
			List<PredicateToken> lodPredicates = new ArrayList<>();
			LodIterator iter = lodFilter.iterator(0, 4);
			while (iter.hasNext())
				lodPredicates.add(ComparisonFactory.equalTo(table.getColumn(((SimpleAttribute)property).getColumn()), new IntegerLiteral(iter.next())));
			
			if (lodFilter.getFilterMode() == LodFilterMode.AND && lodPredicates.size() > 1)
				return;
			
			if (!lodPredicates.isEmpty())
				context.addLodPredicate(new BinaryLogicalOperator(lodFilter.getFilterMode() != LodFilterMode.AND ? LogicalOperationName.OR : LogicalOperationName.AND, lodPredicates));
		}
	}

	private void processGeometryProperties(LodFilterQueryContext context, LodFilter lodFilter) {
		HashMap<Integer, List<Column>> geometryColumns = new HashMap<>();

		LodIterator iter = lodFilter.iterator(0, 4);
		while (iter.hasNext())
			geometryColumns.put(iter.next(), new ArrayList<>());

		for (AbstractProperty property : context.getType().getProperties()) {			
			if (!PathElementType.GEOMETRY_PROPERTIES.contains(property.getElementType()))
				continue;			

			Table table = context.getTable();
			if (property instanceof InjectedProperty) {
				if (disabledADESchemas.contains(property.getSchema()))
					continue;
				
				table = context.getInjectionTable((InjectedProperty)property);
			}

			if (property.getElementType() == PathElementType.GEOMETRY_PROPERTY) {
				GeometryProperty geometryProperty = (GeometryProperty)property;

				int lod = geometryProperty.getLod();
				if (lod < 0) {
					lodMatcher.reset(geometryProperty.getPath());
					if (!lodMatcher.matches())
						continue;

					lod = Integer.valueOf(lodMatcher.group(1));
				}

				if (!lodFilter.isEnabled(lod))
					continue;

				if (geometryProperty.isSetRefColumn())
					geometryColumns.get(lod).add(table.getColumn(geometryProperty.getRefColumn()));
				if (geometryProperty.isSetInlineColumn())
					geometryColumns.get(lod).add(table.getColumn(geometryProperty.getInlineColumn()));
			}

			else if (property.getElementType() == PathElementType.IMPLICIT_GEOMETRY_PROPERTY) {
				ImplicitGeometryProperty implicitGeometryProperty = (ImplicitGeometryProperty)property;

				int lod = implicitGeometryProperty.getLod();
				if (!lodFilter.isEnabled(lod))
					continue;

				geometryColumns.get(lod).add(table.getColumn("lod" + lod + "_implicit_rep_id"));
			}

			if (property instanceof InjectedProperty)
				context.addInjectionJoin((InjectedProperty)property, context.getTable());
		}

		// put geometry columns into OR groups per LoD
		List<PredicateToken> orGroups = new ArrayList<>();		
		for (iter.reset(); iter.hasNext(); ) {
			List<Column> columns = geometryColumns.get(iter.next());

			List<PredicateToken> orGroup = new ArrayList<>();
			for (Column column : columns)
				orGroup.add(ComparisonFactory.isNotNull(column));

			if (!orGroup.isEmpty())
				orGroups.add(LogicalOperationFactory.OR(orGroup));
			else if (lodFilter.getFilterMode() == LodFilterMode.AND)
				return;
		}

		if (!orGroups.isEmpty())
			context.addLodPredicate(new BinaryLogicalOperator(lodFilter.getFilterMode() != LodFilterMode.AND ? LogicalOperationName.OR : LogicalOperationName.AND, orGroups));
	}

	private void processNestedFeatures(LodFilterQueryContext parentContext, LodFilter lodFilter) {
		processNestedFeatures(parentContext, lodFilter, new HashSet<>(), 0);
	}

	private void processNestedFeatures(LodFilterQueryContext parentContext, LodFilter lodFilter, Set<AbstractProperty> visitedProperties, int depth) {
		if (lodFilter.isSetSearchDepth() && depth >= lodFilter.getSearchDepth())
			return;

		// adapt LoD filter to make sure we only consider those LoDs of the nested types
		// that are also available for the parent type
		if (lodFilter.getFilterMode() != LodFilterMode.AND)
			lodFilter = adaptLodFilter(parentContext.getType(), lodFilter);

		for (AbstractProperty property : parentContext.getType().getProperties()) {
			if (property.getElementType() != PathElementType.FEATURE_PROPERTY)
				continue;

			FeatureType nestedType = ((FeatureProperty)property).getType();
			
			// we only consider subtypes of the CityGML root feature type
			if (!nestedType.isSubTypeOf(cityObject))
				continue;

			// check whether the nested type has at least one LoD property which satisfies the filter
			// if not, we skip it and all its nested types
			if (nestedType.hasLodProperties() && !satisfiesLodFilter(nestedType, lodFilter))
				continue;

			// build a list of the non-abstract subtypes of the referenced nested type
			List<FeatureType> candidates = new ArrayList<>(nestedType.listSubTypes(true));
			if (!nestedType.isAbstract())
				candidates.add(nestedType);

			// we do not consider the nested type, if one of its subtypes is a 
			// top-level feature. The rationale here is that a top-level feature does
			// not contribute to the LoD of another top-level feature
			if (candidates.stream().anyMatch(FeatureType::isTopLevel))
				continue;

			Set<FeatureType> visitedTypes = new HashSet<>();

			Table fromTable = parentContext.getTable();
			if (property instanceof InjectedProperty)
				fromTable = parentContext.getInjectionTable((InjectedProperty)property);

			for (FeatureType candidate : candidates) {
				// skip nested ADE types from disabled ADE extensions
				if (disabledADESchemas.contains(nestedType.getSchema()))
					continue;
				
				// create an LoD query context for each candidate subtype
				List<LodFilterQueryContext> subContexts = buildLodQueryContexts(candidate, candidate.getTable() == null ? fromTable : null);					

				for (LodFilterQueryContext subContext : subContexts) {
					if (!visitedTypes.add(subContext.getType()))
						continue;

					// if the candidate is both hierarchical and a subtype of the parent type,
					// then we assume that the hierarchical query is handled for the parent
					if (subContext.isHierachical()
							&& parentContext.getType().isEqualToOrSubTypeOf(subContext.getType()))
						continue;						

					processGeometryProperties(subContext, lodFilter);
					processLodProperties(subContext, lodFilter);

					if (subContext.hasLodPredicates()) {
						Table toTable = subContext.getTable();
						FeatureType toType = subContext.getType();

						// if the candidate is a subtype of the nested type, we might
						// have to add joins to reach the table of the nested type
						while (toType.isSubTypeOf(nestedType)) {
							if (toType.isSetExtension()) {
								AbstractExtension<FeatureType> extension = toType.getExtension();
								if (extension.isSetJoin()) {
									org.citydb.database.schema.mapping.Join join = extension.getJoin();
									Table tmp = new Table(join.getTable(), schemaName);									
									subContext.addParentJoin(JoinFactory.inner(tmp, join.getToColumn(), ComparisonName.EQUAL_TO, toTable.getColumn(join.getFromColumn())));
									toTable = tmp;
								}

								toType = toType.getExtension().getBase();
							} else
								break;
						}

						if (!nestedType.listSuperTypes(true).contains(toType))
							throw new IllegalStateException("Failed to generate LoD filter.");

						if (property instanceof InjectedProperty) {
							InjectedProperty injectedProperty = (InjectedProperty)property;

							if (property.isSetJoin()) {
								AbstractJoin abstractJoin = property.getJoin();

								if (abstractJoin instanceof org.citydb.database.schema.mapping.Join) {
									org.citydb.database.schema.mapping.Join join = (org.citydb.database.schema.mapping.Join)abstractJoin;
									subContext.addParentJoin(JoinFactory.inner(fromTable, join.getFromColumn(), ComparisonName.EQUAL_TO, toTable.getColumn(join.getToColumn())));
								} 

								else if (abstractJoin instanceof JoinTable) {
									JoinTable joinTable = (JoinTable)abstractJoin;
									Table intermediate = new Table(joinTable.getTable(), schemaName);
									subContext.addParentJoin(JoinFactory.inner(intermediate, joinTable.getInverseJoin().getFromColumn(), ComparisonName.EQUAL_TO, toTable.getColumn(joinTable.getInverseJoin().getToColumn())));
									subContext.addParentJoin(JoinFactory.inner(fromTable, joinTable.getJoin().getToColumn(), ComparisonName.EQUAL_TO, intermediate.getColumn(joinTable.getJoin().getFromColumn())));
								}
							}							

							org.citydb.database.schema.mapping.Join join = injectedProperty.getBaseJoin();
							subContext.setParentCondition(ComparisonFactory.equalTo(fromTable.getColumn(join.getFromColumn()), parentContext.getTable().getColumn(join.getToColumn())));
						}

						else if (property.isSetJoin()) {
							AbstractJoin abstractJoin = property.getJoin();

							if (abstractJoin instanceof org.citydb.database.schema.mapping.Join) {
								org.citydb.database.schema.mapping.Join join = (org.citydb.database.schema.mapping.Join)abstractJoin;
								subContext.setParentCondition(ComparisonFactory.equalTo(toTable.getColumn(join.getToColumn()), fromTable.getColumn(join.getFromColumn())));								
							} 

							else if (abstractJoin instanceof JoinTable) {
								JoinTable joinTable = (JoinTable)abstractJoin;
								Table intermediate = new Table(joinTable.getTable(), schemaName);
								subContext.setParentCondition(ComparisonFactory.equalTo(intermediate.getColumn(joinTable.getJoin().getFromColumn()), fromTable.getColumn(joinTable.getJoin().getToColumn())));
								subContext.addParentJoin(JoinFactory.inner(intermediate, joinTable.getInverseJoin().getFromColumn(), ComparisonName.EQUAL_TO, toTable.getColumn(joinTable.getInverseJoin().getToColumn())));
							}
						}

						if (!subContext.hasParentCondition())
							throw new IllegalStateException("Failed to build LoD filter.");	

						parentContext.addSubContext(subContext);
					}

					// avoid endless loops
					if (subContext.isHierachical() && !visitedProperties.add(property))
						continue;

					processNestedFeatures(subContext, lodFilter, visitedProperties, depth + 1);
				}
			}
		}
	}

	private LodFilter adaptLodFilter(FeatureType type, LodFilter lodFilter) {
		if (!type.hasLodProperties())
			return lodFilter;
		
		LodFilter adaptedLodFilter = new LodFilter(false, lodFilter.getFilterMode());
		adaptedLodFilter.setSearchDepth(lodFilter.getSearchDepth());
		
		for (int lod = 0; lod < 5; lod++) {
			if (type.isAvailableForLod(lod))
				adaptedLodFilter.setEnabled(lod, lodFilter.isEnabled(lod));
		}
		
		return adaptedLodFilter;
	}
	
	private boolean satisfiesLodFilter(FeatureType type, LodFilter lodFilter) {
		for (LodIterator iter = lodFilter.iterator(0, 4); iter.hasNext(); ) {
			boolean isAvailable = type.isAvailableForLod(iter.next());

			if (isAvailable && lodFilter.getFilterMode() != LodFilterMode.AND)
				return true;
			else if (!isAvailable && lodFilter.getFilterMode() == LodFilterMode.AND)
				return false;
		}

		return lodFilter.getFilterMode() == LodFilterMode.AND;			
	}

	private boolean hasNestedTypesWithLodProperties(FeatureType type) {
		if (type == cityObject)
			return false;

		for (AbstractProperty property : type.listProperties(false, true)) {
			if (property.getElementType() != PathElementType.FEATURE_PROPERTY)
				continue;

			FeatureType nestedType = ((FeatureProperty)property).getType();
			if (nestedType.isTopLevel())
				continue;			

			if (nestedType.hasLodProperties())
				return true;

			if (hasNestedTypesWithLodProperties(nestedType))
				return true;
		}

		return false;
	}

}
