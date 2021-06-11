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
package org.citydb.query.builder.config;

import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.exporter.SimpleTiling;
import org.citydb.config.project.exporter.SimpleTilingMode;
import org.citydb.config.project.visExporter.VisTiling;
import org.citydb.config.project.visExporter.VisTilingMode;
import org.citydb.config.project.visExporter.SimpleVisQuery;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.selection.spatial.WithinOperator;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.util.ValueReferenceBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.selection.operator.logical.BinaryLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.util.Util;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.List;

public class ConfigQueryBuilder {
	private final SchemaMapping schemaMapping;
	private final AbstractDatabaseAdapter databaseAdapter;

	public ConfigQueryBuilder(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
		this.schemaMapping = schemaMapping;
		this.databaseAdapter = databaseAdapter;
	}

	public Query buildQuery(QueryConfig queryConfig, NamespaceContext namespaceContext) throws QueryBuildException {
		Query query = new Query();

		ValueReferenceBuilder valueReferenceBuilder = new ValueReferenceBuilder(query, schemaMapping, namespaceContext);

		// target SRS
		if (queryConfig.isSetTargetSrs())
			query.setTargetSrs(queryConfig.getTargetSrs());
		else
			query.setTargetSrs(databaseAdapter.getConnectionMetaData().getReferenceSystem());

		// feature type filter
		if (queryConfig.isSetFeatureTypeFilter() && !queryConfig.getFeatureTypeFilter().isEmpty()) {
			FeatureTypeFilterBuilder typeBuilder = new FeatureTypeFilterBuilder(query, schemaMapping);
			query.setFeatureTypeFilter(typeBuilder.buildFeatureTypeFilter(queryConfig.getFeatureTypeFilter()));
		} else {
			try {
				query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
				query.setTargetVersion(CityGMLVersion.v2_0_0);
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the feature type filter.", e);
			}
		}

		// counter filter
		if (queryConfig.isSetCounterFilter()) {
			try {
				CounterFilterBuilder counterFilterBuilder = new CounterFilterBuilder();
				query.setCounterFilter(counterFilterBuilder.buildCounterFilter(queryConfig.getCounterFilter()));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the counter filter.", e);
			}
		}

		// lod filter
		if (queryConfig.isSetLodFilter()) {
			LodFilterBuilder lodFilterBuilder = new LodFilterBuilder();
			query.setLodFilter(lodFilterBuilder.buildLodFilter(queryConfig.getLodFilter()));
		}

		// projection filter
		if (queryConfig.isSetProjectionFilter()) {
			ProjectionFilterBuilder builder = new ProjectionFilterBuilder(schemaMapping);
			query.setProjection(builder.buildProjectionFilter(queryConfig.getProjectionFilter()));
		}

		// selection filter
		if (queryConfig.isSetSelectionFilter()) {
			AbstractPredicate predicate = queryConfig.getSelectionFilter().getPredicate();
			PredicateBuilder predicateBuilder = new PredicateBuilder(valueReferenceBuilder, databaseAdapter);
			query.setSelection(new SelectionFilter(predicateBuilder.buildPredicate(predicate)));
		}

		// appearance filter
		if (queryConfig.isSetAppearanceFilter()) {
			AppearanceFilterBuilder builder = new AppearanceFilterBuilder();
			query.setAppearanceFilter(builder.buildAppearanceFilter(queryConfig.getAppearanceFilter()));
		}

		// sorting clause
		if (queryConfig.isSetSorting() && queryConfig.getSorting().hasSortProperties()) {
			SortingBuilder sortingBuilder = new SortingBuilder(valueReferenceBuilder);
			query.setSorting(sortingBuilder.buildSorting(queryConfig.getSorting()));
		}

		// tiling
		if (queryConfig.isSetTiling()) {
			TilingFilterBuilder tilingFilterBuilder = new TilingFilterBuilder(databaseAdapter);
			query.setTiling(tilingFilterBuilder.buildTilingFilter(queryConfig.getTiling()));
		}

		return query;
	}

	public Query buildQuery(SimpleQuery queryConfig, NamespaceContext namespaceContext) throws QueryBuildException {
		// support for legacy CityGML export filter
		Query query = new Query();

		ValueReferenceBuilder valueReferenceBuilder = new ValueReferenceBuilder(query, schemaMapping, namespaceContext);
		PredicateBuilder predicateBuilder = new PredicateBuilder(valueReferenceBuilder, databaseAdapter);

		// CityGML version
		CityGMLVersion version = Util.toCityGMLVersion(queryConfig.getVersion());
		query.setTargetVersion(version);

		// target SRS
		if (queryConfig.isSetTargetSrs())
			query.setTargetSrs(queryConfig.getTargetSrs());
		else
			query.setTargetSrs(databaseAdapter.getConnectionMetaData().getReferenceSystem());

		// feature type filter
		if (queryConfig.isUseTypeNames()) {
			if (queryConfig.isSetFeatureTypeFilter() && !queryConfig.getFeatureTypeFilter().isEmpty()) {
				FeatureTypeFilterBuilder featureTypeFilterBuilder = new FeatureTypeFilterBuilder(query, schemaMapping);
				query.setFeatureTypeFilter(featureTypeFilterBuilder.buildFeatureTypeFilter(queryConfig.getFeatureTypeFilter(), version));
			} else
				throw new QueryBuildException("The feature type filter must not be empty.");
		} else {
			try {
				query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the feature type filter.", e);
			}
		}

		// lod filter
		if (queryConfig.isUseLodFilter() && queryConfig.isSetLodFilter()) {
			LodFilterBuilder lodFilterBuilder = new LodFilterBuilder();
			query.setLodFilter(lodFilterBuilder.buildLodFilter(queryConfig.getLodFilter()));
		}

		// simple filter settings
		List<Predicate> predicates = new ArrayList<>();

		// feature version filter
		if (queryConfig.isUseFeatureVersionFilter() && queryConfig.isSetFeatureVersionFilter()) {
			AbstractPredicate predicate = queryConfig.getFeatureVersionFilter().toPredicate();
			if (predicate != null) {
				predicates.add(predicateBuilder.buildPredicate(predicate));
			}
		}

		// attribute filter
		if (queryConfig.isUseAttributeFilter() && queryConfig.isSetAttributeFilter()) {
			for (AbstractPredicate predicate : queryConfig.getAttributeFilter().toPredicates()) {
				predicates.add(predicateBuilder.buildPredicate(predicate));
			}
		}

		// SQL filter
		if (queryConfig.isUseSQLFilter() && queryConfig.isSetSQLFilter()) {
			SelectOperatorBuilder selectOperatorBuilder = new SelectOperatorBuilder();
			predicates.add(selectOperatorBuilder.buildSelectOperator(queryConfig.getSQLFilter()));
		}

		// counter filter
		if (queryConfig.isUseCountFilter() && queryConfig.isSetCounterFilter()) {
			try {
				CounterFilterBuilder counterFilterBuilder = new CounterFilterBuilder();
				query.setCounterFilter(counterFilterBuilder.buildCounterFilter(queryConfig.getCounterFilter()));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the counter filter.", e);
			}
		}

		// bbox filter
		if (queryConfig.isUseBboxFilter() && queryConfig.isSetBboxFilter()) {
			SimpleTiling bboxFilter = queryConfig.getBboxFilter();
			if (!bboxFilter.isSetExtent())
				throw new QueryBuildException("The bounding box filter requires an " + GeometryType.ENVELOPE + " as spatial operand.");

			// tiling
			if (bboxFilter.getMode() == SimpleTilingMode.TILING) {
				TilingFilterBuilder tilingFilterBuilder = new TilingFilterBuilder(databaseAdapter);
				query.setTiling(tilingFilterBuilder.buildTilingFilter(bboxFilter));
			}

			// bbox
			else {
				if (bboxFilter.getMode() == SimpleTilingMode.BBOX) {
					BBOXOperator bbox = new BBOXOperator();
					bbox.setEnvelope(bboxFilter.getExtent());
					predicates.add(predicateBuilder.buildPredicate(bbox));
				} else if (bboxFilter.getMode() == SimpleTilingMode.WITHIN) {
					WithinOperator within = new WithinOperator();
					within.setSpatialOperand(bboxFilter.getExtent());
					predicates.add(predicateBuilder.buildPredicate(within));
				}
			}
		}

		if (!predicates.isEmpty()) {
			try {
				BinaryLogicalOperator predicate = new BinaryLogicalOperator(LogicalOperatorName.AND, predicates);
				query.setSelection(new SelectionFilter(predicate));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the export filter.", e);
			}
		}

		return query;
	}

	public Query buildQuery(SimpleVisQuery queryConfig, NamespaceContext namespaceContext) throws QueryBuildException {
		// support for legacy VIS export filter
		Query query = new Query();

		// always use CityGML 2.0 as target version
		query.setTargetVersion(CityGMLVersion.v2_0_0);

		ValueReferenceBuilder valueReferenceBuilder = new ValueReferenceBuilder(query, schemaMapping, namespaceContext);
		PredicateBuilder predicateBuilder = new PredicateBuilder(valueReferenceBuilder, databaseAdapter);

		// feature type filter
		if (queryConfig.isUseTypeNames()) {
			if (queryConfig.isSetFeatureTypeFilter() && !queryConfig.getFeatureTypeFilter().isEmpty()) {
				FeatureTypeFilterBuilder featureTypeFilterBuilder = new FeatureTypeFilterBuilder(query, schemaMapping);
				query.setFeatureTypeFilter(featureTypeFilterBuilder.buildFeatureTypeFilter(queryConfig.getFeatureTypeFilter(), CityGMLVersion.v2_0_0));
			} else {
				throw new QueryBuildException("The feature type filter must not be empty.");
			}
		} else {
			try {
				query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the export filter.", e);
			}
		}

		// simple filter settings
		List<Predicate> predicates = new ArrayList<>();

		// feature version filter
		if (queryConfig.isUseFeatureVersionFilter() && queryConfig.isSetFeatureVersionFilter()) {
			AbstractPredicate predicate = queryConfig.getFeatureVersionFilter().toPredicate();
			if (predicate != null) {
				predicates.add(predicateBuilder.buildPredicate(predicate));
			}
		}

		// attribute filter
		if (queryConfig.isUseAttributeFilter() && queryConfig.isSetAttributeFilter()) {
			for (AbstractPredicate predicate : queryConfig.getAttributeFilter().toPredicates()) {
				predicates.add(predicateBuilder.buildPredicate(predicate));
			}
		}

		// SQL filter
		if (queryConfig.isUseSQLFilter() && queryConfig.isSetSQLFilter()) {
			SelectOperatorBuilder selectOperatorBuilder = new SelectOperatorBuilder();
			predicates.add(selectOperatorBuilder.buildSelectOperator(queryConfig.getSQLFilter()));
		}

		if (!predicates.isEmpty()) {
			try {
				BinaryLogicalOperator predicate = new BinaryLogicalOperator(LogicalOperatorName.AND, predicates);
				query.setSelection(new SelectionFilter(predicate));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the export filter.", e);
			}
		}

		VisTiling spatialFilter = queryConfig.getBboxFilter();

		// check bbox filter
		if (queryConfig.isUseBboxFilter() && !spatialFilter.isSetExtent()) {
			throw new QueryBuildException("The bounding box filter requires an " + GeometryType.ENVELOPE + " as spatial operand.");
		}

		// apply tiling
		if (spatialFilter.getMode() != VisTilingMode.NO_TILING
				&& spatialFilter.isSetExtent()
				&& spatialFilter.getExtent().isValid()) {
			TilingFilterBuilder tilingFilterBuilder = new TilingFilterBuilder(databaseAdapter);
			query.setTiling(tilingFilterBuilder.buildTilingFilter(spatialFilter));
		}

		return query;
	}
}
