package org.citydb.query.builder.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.exporter.TilingOptions;
import org.citydb.config.project.kmlExporter.KmlTilingOptions;
import org.citydb.config.project.kmlExporter.SimpleKmlQuery;
import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.selection.spatial.SimpleBBOXMode;
import org.citydb.config.project.query.filter.selection.spatial.WithinOperator;
import org.citydb.config.project.query.filter.tiling.Tiling;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.selection.operator.logical.BinaryLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.util.Util;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;

public class ConfigQueryBuilder {
	private final SchemaMapping schemaMapping;
	private final AbstractDatabaseAdapter databaseAdapter;

	public ConfigQueryBuilder(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
		this.schemaMapping = schemaMapping;
		this.databaseAdapter = databaseAdapter;
	}

	public Query buildQuery(org.citydb.config.project.query.Query queryConfig, NamespaceContext namespaceContext) throws QueryBuildException {
		Query query = new Query();

		// target SRS
		if (queryConfig.isSetTargetSRS())
			query.setTargetSRS(queryConfig.getTargetSRS());
		else
			query.setTargetSRS(databaseAdapter.getConnectionMetaData().getReferenceSystem());

		// feature type filter
		if (queryConfig.isSetFeatureTypeFilter() && !queryConfig.getFeatureTypeFilter().isEmpty()) {
			FeatureTypeFilterBuilder typeBuilder = new FeatureTypeFilterBuilder(query, schemaMapping);
			query.setFeatureTypeFilter(typeBuilder.buildFeatureTypeFilter(queryConfig.getFeatureTypeFilter()));
		} else {
			try {
				query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
				query.setTargetVersion(CityGMLVersion.v2_0_0);
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the export filter.", e);
			}
		}

		// counter filter
		if (queryConfig.isSetCounterFilter()) {
			try {
				CounterFilterBuilder counterFilterBuilder = new CounterFilterBuilder();
				query.setCounterFilter(counterFilterBuilder.buildCounterFilter(queryConfig.getCounterFilter()));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the export filter.", e);
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
			for (ProjectionFilter projectionFilter : builder.buildProjectionFilter(queryConfig.getProjectionFilter()))
				query.addProjectionFilter(projectionFilter);
		}

		// selection filter
		if (queryConfig.isSetSelectionFilter()) {
			AbstractPredicate predicate = queryConfig.getSelectionFilter().getPredicate();
			PredicateBuilder predicateBuilder = new PredicateBuilder(query, schemaMapping, namespaceContext, databaseAdapter);			
			query.setSelection(new SelectionFilter(predicateBuilder.buildPredicate(predicate)));
		}

		// appearance filter
		if (queryConfig.isSetAppearanceFilter()) {
			AppearanceFilterBuilder builder = new AppearanceFilterBuilder();
			query.setAppearanceFilter(builder.buildAppearanceFilter(queryConfig.getAppearanceFilter()));
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

		// CityGML version
		CityGMLVersion version = Util.toCityGMLVersion(queryConfig.getVersion()); 
		query.setTargetVersion(version);

		// target SRS
		if (queryConfig.isSetTargetSRS())
			query.setTargetSRS(queryConfig.getTargetSRS());
		else
			query.setTargetSRS(databaseAdapter.getConnectionMetaData().getReferenceSystem());

		SimpleSelectionFilter exportFilter = queryConfig.getFilter();
		PredicateBuilder predicateBuilder = new PredicateBuilder(query, schemaMapping, namespaceContext, databaseAdapter);

		// lod filter
		if (queryConfig.isUseLodFilter() && queryConfig.isSetLodFilter()) {
			LodFilterBuilder lodFilterBuilder = new LodFilterBuilder();
			query.setLodFilter(lodFilterBuilder.buildLodFilter(queryConfig.getLodFilter()));
		}

		// simple filter settings
		if (queryConfig.getMode() == SimpleSelectionFilterMode.SIMPLE) {			
			// set feature type filter
			try {
				query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the export filter.", e);
			}

			// gml:id filter
			if (exportFilter.isSetGmlIdFilter())
				query.setSelection(new SelectionFilter(predicateBuilder.buildPredicate(exportFilter.getGmlIdFilter())));
		}

		// complex filter settings
		else {
			List<Predicate> predicates = new ArrayList<>();

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
					throw new QueryBuildException("Failed to build the export filter.", e);
				}
			}

			// gml:name filter
			if (queryConfig.isUseGmlNameFilter() && exportFilter.isSetGmlNameFilter()) {
				LikeOperator gmlNameFilter = exportFilter.getGmlNameFilter();
				gmlNameFilter.setLiteral(gmlNameFilter.getLiteral());
				gmlNameFilter.setValueReference("gml:name");
				predicates.add(predicateBuilder.buildPredicate(gmlNameFilter));
			}

			// counter filter
			if (queryConfig.isUseCountFilter() && queryConfig.isSetCounterFilter()) {
				try {
					CounterFilterBuilder counterFilterBuilder = new CounterFilterBuilder();
					query.setCounterFilter(counterFilterBuilder.buildCounterFilter(queryConfig.getCounterFilter()));
				} catch (FilterException e) {
					throw new QueryBuildException("Failed to build the export filter.", e);
				}
			}

			// bbox filter
			if (queryConfig.isUseBboxFilter() && exportFilter.isSetBboxFilter()) {
				if (!exportFilter.getBboxFilter().isSetEnvelope())
					throw new QueryBuildException("The bounding box filter requires an " + GeometryType.ENVELOPE + " as spatial operand.");

				// tiling
				if (queryConfig.isUseTiling()) {
					TilingOptions tilingOptions = queryConfig.getTilingOptions();

					Tiling tiling = new Tiling();
					tiling.setExtent((BoundingBox)exportFilter.getBboxFilter().getEnvelope());
					tiling.setRows(tilingOptions.getRows());
					tiling.setColumns(tilingOptions.getColumns());
					tiling.setTilingOptions(tilingOptions);

					TilingFilterBuilder tilingFilterBuilder = new TilingFilterBuilder(databaseAdapter);
					query.setTiling(tilingFilterBuilder.buildTilingFilter(tiling));
				}

				// bbox
				else {				
					if (exportFilter.getBboxMode() == SimpleBBOXMode.BBOX) {
						BBOXOperator bbox = exportFilter.getBboxFilter();
						bbox.setValueReference("gml:boundedBy");					
						predicates.add(predicateBuilder.buildPredicate(bbox));
					}

					else if (exportFilter.getBboxMode() == SimpleBBOXMode.WITHIN) {
						WithinOperator within = new WithinOperator();
						within.setValueReference(exportFilter.getBboxFilter().getValueReference());
						within.setSpatialOperand(exportFilter.getBboxFilter().getEnvelope());
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
		}

		return query;
	}

	public Query buildQuery(SimpleKmlQuery queryConfig, NamespaceContext namespaceContext) throws QueryBuildException {
		// support for legacy KML export filter
		Query query = new Query();

		SimpleSelectionFilter exportFilter = queryConfig.getFilter();
		PredicateBuilder predicateBuilder = new PredicateBuilder(query, schemaMapping, namespaceContext, databaseAdapter);

		// simple filter settings
		if (queryConfig.getMode() == SimpleSelectionFilterMode.SIMPLE) {			
			// set feature type filter
			try {
				query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the export filter.", e);
			}

			// gml:id filter
			if (exportFilter.isSetGmlIdFilter())
				query.setSelection(new SelectionFilter(predicateBuilder.buildPredicate(exportFilter.getGmlIdFilter())));
		}

		// complex filter settings
		else {
			List<Predicate> predicates = new ArrayList<>();

			// feature type filter
			if (queryConfig.isSetFeatureTypeFilter()) {
				if (queryConfig.getFeatureTypeFilter().isEmpty())
					throw new QueryBuildException("The feature type filter must not be empty.");
				
				FeatureTypeFilterBuilder featureTypeFilterBuilder = new FeatureTypeFilterBuilder(query, schemaMapping);
				query.setFeatureTypeFilter(featureTypeFilterBuilder.buildFeatureTypeFilter(queryConfig.getFeatureTypeFilter(), CityGMLVersion.v2_0_0));
			} else {
				try {
					query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
				} catch (FilterException e) {
					throw new QueryBuildException("Failed to build the export filter.", e);
				}
			}

			// bbox filter
			if (exportFilter.isSetBboxFilter()) {
				if (!exportFilter.getBboxFilter().isSetEnvelope())
					throw new QueryBuildException("The bounding box filter requires an " + GeometryType.ENVELOPE + " as spatial operand.");

				KmlTilingOptions tilingOptions = queryConfig.getTilingOptions();

				Tiling tiling = new Tiling();
				tiling.setExtent((BoundingBox)exportFilter.getBboxFilter().getEnvelope());
				tiling.setRows(tilingOptions.getRows());
				tiling.setColumns(tilingOptions.getColumns());
				tiling.setTilingOptions(tilingOptions);

				TilingFilterBuilder tilingFilterBuilder = new TilingFilterBuilder(databaseAdapter);
				query.setTiling(tilingFilterBuilder.buildTilingFilter(tiling));				
			}

			if (!predicates.isEmpty()) {
				try {
					BinaryLogicalOperator predicate = new BinaryLogicalOperator(LogicalOperatorName.AND, predicates);
					query.setSelection(new SelectionFilter(predicate));
				} catch (FilterException e) {
					throw new QueryBuildException("Failed to build the export filter.", e);
				}				
			}
		}

		return query;
	}

}
