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
package org.citydb.citygml.exporter.database.content;

import org.citydb.ade.model.LastModificationDateProperty;
import org.citydb.ade.model.LineageProperty;
import org.citydb.ade.model.ReasonForUpdateProperty;
import org.citydb.ade.model.UpdatingPersonProperty;
import org.citydb.ade.model.module.CityDBADE100Module;
import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.config.project.exporter.FeatureEnvelopeMode;
import org.citydb.config.project.exporter.SimpleTilingOptions;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.Query;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.query.filter.tiling.Tile;
import org.citydb.query.filter.tiling.Tiling;
import org.citydb.sqlbuilder.expression.LiteralSelectExpression;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.geometry.BoundingBox;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.RelativeToTerrain;
import org.citygml4j.model.citygml.core.RelativeToWater;
import org.citygml4j.model.citygml.generics.GenericAttributeSet;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.gml.GMLCoreModule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DBCityObject implements DBExporter {
	private final Query query;
	private final CityGMLExportManager exporter;
	private final Connection connection;
	private final PreparedStatement psSelect;
	private final PreparedStatement psBulk;
	private final Map<Long, ObjectContext> batches;
	private final DBGeneralization generalizesToExporter;
	private final DBCityObjectGenericAttrib genericAttributeExporter;

	private final String gmlSrsName;
	private final boolean exportAppearance;
	private final boolean useTiling;
	private final boolean exportCityDBMetadata;

	private final AttributeValueSplitter valueSplitter;
	private final String coreModule;
	private final String appearanceModule;
	private final String gmlModule;

	private DBLocalAppearance appearanceExporter;
	private boolean setTileInfoAsGenericAttribute;
	private Tile activeTile;
	private SimpleTilingOptions tilingOptions;
	private String cityDBADEModule;

	public DBCityObject(Connection connection, Query query, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		this.query = query;
		this.connection = connection;
		this.exporter = exporter;

		batches = new LinkedHashMap<>();
		gmlSrsName = query.getTargetSrs().getGMLSrsName();
		exportAppearance = exporter.getExportConfig().getAppearances().isSetExportAppearance();

		useTiling = query.isSetTiling();
		if (useTiling) {
			Tiling tiling = query.getTiling();
			tilingOptions = tiling.getTilingOptions() instanceof SimpleTilingOptions ? (SimpleTilingOptions) tiling.getTilingOptions() : new SimpleTilingOptions();
			setTileInfoAsGenericAttribute = tilingOptions.isIncludeTileAsGenericAttribute();
			activeTile = tiling.getActiveTile();
		}

		exportCityDBMetadata = exporter.getExportConfig().getContinuation().isExportCityDBMetadata();
		if (exportCityDBMetadata) {
			cityDBADEModule = exporter.getTargetCityGMLVersion() == CityGMLVersion.v2_0_0 ?
					CityDBADE200Module.v3_0.getNamespaceURI() : CityDBADE100Module.v3_0.getNamespaceURI();
		}

		generalizesToExporter = exporter.getExporter(DBGeneralization.class);
		genericAttributeExporter = exporter.getExporter(DBCityObjectGenericAttrib.class);
		valueSplitter = exporter.getAttributeValueSplitter();
		if (exportAppearance)
			appearanceExporter = exporter.getExporter(DBLocalAppearance.class);

		coreModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.CORE).getNamespaceURI();
		appearanceModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.APPEARANCE).getNamespaceURI();
		gmlModule = GMLCoreModule.v3_1_1.getNamespaceURI();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		Table table = new Table(TableEnum.CITYOBJECT.getName(), schema);
		Table externalReference = new Table(TableEnum.EXTERNAL_REFERENCE.getName(), schema);
		Table generalization = new Table(TableEnum.GENERALIZATION.getName(), schema);
		Table genericAttributes = new Table(TableEnum.CITYOBJECT_GENERICATTRIB.getName(), schema);

		Select select = new Select().addProjection(table.getColumn("gmlid"), exporter.getGeometryColumn(table.getColumn("envelope")),
				table.getColumn("name"), table.getColumn("name_codespace"), table.getColumn("description"), table.getColumn("creation_date"),
				table.getColumn("termination_date"), table.getColumn("relative_to_terrain"), table.getColumn("relative_to_water"),
				externalReference.getColumn("id", "exid"), externalReference.getColumn("infosys"), externalReference.getColumn("name", "exname"), externalReference.getColumn("uri"),
				generalization.getColumn("generalizes_to_id"))
				.addJoin(JoinFactory.left(externalReference, "cityobject_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
				.addJoin(JoinFactory.left(generalization, "cityobject_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
		genericAttributeExporter.addProjection(select, genericAttributes, "ga")
				.addJoin(JoinFactory.left(genericAttributes, "cityobject_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
		if (exportCityDBMetadata) select.addProjection(table.getColumns("last_modification_date", "updating_person", "reason_for_update", "lineage"));
		if (exportAppearance) {
			Table appearance = new Table(TableEnum.APPEARANCE.getName(), schema);
			select.addProjection(appearance.getColumn("id", "apid"))
					.addJoin(JoinFactory.left(appearance, "cityobject_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
		}

		String subQuery = exporter.getDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS ?
				"select * from unnest(?)" :
				"select * from table(?)";

		psBulk = connection.prepareStatement(new Select(select)
				.addProjection(table.getColumn("id"))
				.addSelection(ComparisonFactory.in(table.getColumn("id"), new LiteralSelectExpression(subQuery))).toString());

		psSelect = connection.prepareStatement(new Select(select)
				.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>())).toString());
	}

	protected void addBatch(AbstractGML object, long objectId, AbstractObjectType<?> objectType, ProjectionFilter projectionFilter) {
		batches.put(objectId, new ObjectContext(object, objectType, projectionFilter));
	}

	public boolean executeBatch() throws CityGMLExportException, SQLException {
		if (batches.isEmpty())
			return true;

		try {
			if (batches.size() == 1) {
				Map.Entry<Long, ObjectContext> entry = batches.entrySet().iterator().next();
				doExport(entry.getKey(), entry.getValue());
			} else {
				psBulk.setArray(1, exporter.getDatabaseAdapter().getSQLAdapter().createIdArray(batches.keySet().toArray(new Long[0]), connection));

				try (ResultSet rs = psBulk.executeQuery()) {
					long currentObjectId = 0;
					ObjectContext context = null;

					while (rs.next()) {
						long objectId = rs.getLong("id");

						if (objectId != currentObjectId || context == null) {
							currentObjectId = objectId;
							context = batches.get(objectId);
							if (context != null) {
								if (context.initialize() && !initializeObject(context, rs))
									return false;
							} else {
								exporter.logOrThrowErrorMessage("Failed to read city object for id " + objectId + ".");
								continue;
							}
						}

						if (context.isCityObject)
							addProperties(context, rs);
					}

					for (Map.Entry<Long, ObjectContext> entry : batches.entrySet())
						postprocess(entry.getValue(), entry.getKey());
				}
			}

			if (exportAppearance)
				appearanceExporter.executeBatch();

			return true;
		} finally {
			batches.clear();
		}
	}

	protected boolean doExport(AbstractGML object, long objectId, AbstractObjectType<?> objectType) throws CityGMLExportException, SQLException {
		return doExport(objectId, new ObjectContext(object, objectType, query.getProjectionFilter(objectType)));
	}

	protected boolean doExport(AbstractGML object, long objectId, AbstractObjectType<?> objectType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
		return doExport(objectId, new ObjectContext(object, objectType, projectionFilter));
	}

	private boolean doExport(long objectId, ObjectContext context) throws CityGMLExportException, SQLException {
		psSelect.setLong(1, objectId);

		try (ResultSet rs = psSelect.executeQuery()) {
			if (rs.next()) {
				if (!initializeObject(context, rs))
					return false;

				if (context.isCityObject) {
					do {
						addProperties(context, rs);
					} while (rs.next());
				}

				postprocess(context, objectId);
			}

			if (exportAppearance)
				appearanceExporter.executeBatch();

			return true;
		}
	}

	protected boolean initializeObject(ObjectContext context, ResultSet rs) throws CityGMLExportException, SQLException {
		boolean setEnvelope = !context.isCityObject || (context.projectionFilter.containsProperty("boundedBy", gmlModule)
				&& (exporter.getExportConfig().getCityGMLOptions().getGMLEnvelope().getFeatureMode() == FeatureEnvelopeMode.ALL
				|| (exporter.getExportConfig().getCityGMLOptions().getGMLEnvelope().getFeatureMode() == FeatureEnvelopeMode.TOP_LEVEL && context.isTopLevel)));
		boolean getEnvelope = context.isFeature && ((useTiling && context.isTopLevel) || setEnvelope);

		// gml:id
		context.object.setId(rs.getString("gmlid"));

		// gml:name
		if (!context.isCityObject || context.projectionFilter.containsProperty("name", gmlModule)) {
			for (SplitValue splitValue : valueSplitter.split(rs.getString("name"), rs.getString("name_codespace"))) {
				Code name = new Code(splitValue.result(0));
				name.setCodeSpace(splitValue.result(1));
				context.object.addName(name);
			}
		}

		// gml:description
		if (!context.isCityObject || context.projectionFilter.containsProperty("description", gmlModule)) {
			String description = rs.getString("description");
			if (!rs.wasNull())
				context.object.setDescription(new StringOrRef(description));
		}

		if (getEnvelope) {
			BoundingShape boundedBy = null;
			Object geom = rs.getObject("envelope");
			if (!rs.wasNull() && geom != null) {
				GeometryObject geomObj = exporter.getDatabaseAdapter().getGeometryConverter().getEnvelope(geom);
				double[] coordinates = geomObj.getCoordinates(0);

				Envelope envelope = new Envelope();
				envelope.setLowerCorner(new Point(coordinates[0], coordinates[1], coordinates[2]));
				envelope.setUpperCorner(new Point(coordinates[3], coordinates[4], coordinates[5]));
				envelope.setSrsDimension(3);
				envelope.setSrsName(gmlSrsName);

				boundedBy = new BoundingShape();
				boundedBy.setEnvelope(envelope);
			}

			// check bounding volume filter
			if (useTiling && context.isTopLevel) {
				if (boundedBy == null || !boundedBy.isSetEnvelope())
					return false;

				try {
					BoundingBox bbox = boundedBy.getEnvelope().toBoundingBox();
					if (!activeTile.isOnTile(new org.citydb.config.geometry.Point(
									(bbox.getLowerCorner().getX() + bbox.getUpperCorner().getX()) / 2.0,
									(bbox.getLowerCorner().getY() + bbox.getUpperCorner().getY()) / 2.0,
									query.getTargetSrs()),
							exporter.getDatabaseAdapter()))
						return false;
				} catch (FilterException e) {
					throw new CityGMLExportException("Failed to apply the tiling filter.", e);
				}
			}

			// gml:boundedBy
			if (setEnvelope)
				((AbstractFeature) context.object).setBoundedBy(boundedBy);
		}

		if (context.isCityObject) {
			AbstractCityObject cityObject = (AbstractCityObject) context.object;

			// core:creationDate
			if (context.projectionFilter.containsProperty("creationDate", coreModule)) {
				OffsetDateTime creationDate = rs.getObject("creation_date", OffsetDateTime.class);
				if (!rs.wasNull())
					cityObject.setCreationDate(creationDate.atZoneSameInstant(ZoneId.systemDefault()));
			}

			// core:terminationDate
			if (context.projectionFilter.containsProperty("terminationDate", coreModule)) {
				OffsetDateTime terminationDate = rs.getObject("termination_date", OffsetDateTime.class);
				if (terminationDate != null)
					cityObject.setTerminationDate(terminationDate.atZoneSameInstant(ZoneId.systemDefault()));
			}

			// core:relativeToTerrain
			if (context.projectionFilter.containsProperty("relativeToTerrain", coreModule)) {
				String relativeToTerrain = rs.getString("relative_to_terrain");
				if (!rs.wasNull())
					cityObject.setRelativeToTerrain(RelativeToTerrain.fromValue(relativeToTerrain));
			}

			// core:relativeToWater
			if (context.projectionFilter.containsProperty("relativeToWater", coreModule)) {
				String relativeToWater = rs.getString("relative_to_water");
				if (!rs.wasNull())
					cityObject.setRelativeToWater(RelativeToWater.fromValue(relativeToWater));
			}

			// 3DCityDB ADE metadata
			if (exportCityDBMetadata && context.isTopLevel) {
				if (context.projectionFilter.containsProperty("lastModificationDate", cityDBADEModule)) {
					OffsetDateTime lastModificationDate = rs.getObject("last_modification_date", OffsetDateTime.class);
					if (!rs.wasNull()) {
						LastModificationDateProperty property = new LastModificationDateProperty(
								lastModificationDate.atZoneSameInstant(ZoneId.systemDefault()));
						cityObject.addGenericApplicationPropertyOfCityObject(property);
					}
				}

				if (context.projectionFilter.containsProperty("updatingPerson", cityDBADEModule)) {
					String updatingPerson = rs.getString("updating_person");
					if (!rs.wasNull()) {
						UpdatingPersonProperty property = new UpdatingPersonProperty(updatingPerson);
						cityObject.addGenericApplicationPropertyOfCityObject(property);
					}
				}

				if (context.projectionFilter.containsProperty("reasonForUpdate", cityDBADEModule)) {
					String reasonForUpdate = rs.getString("reason_for_update");
					if (!rs.wasNull()) {
						ReasonForUpdateProperty property = new ReasonForUpdateProperty(reasonForUpdate);
						cityObject.addGenericApplicationPropertyOfCityObject(property);
					}
				}

				if (context.projectionFilter.containsProperty("lineage", cityDBADEModule)) {
					String lineage = rs.getString("lineage");
					if (!rs.wasNull()) {
						LineageProperty property = new LineageProperty(lineage);
						cityObject.addGenericApplicationPropertyOfCityObject(property);
					}
				}
			}

			// add tile as generic attribute
			if (context.isTopLevel && setTileInfoAsGenericAttribute) {
				String value;

				double minX = activeTile.getExtent().getLowerCorner().getX();
				double minY = activeTile.getExtent().getLowerCorner().getY();
				double maxX = activeTile.getExtent().getUpperCorner().getX();
				double maxY = activeTile.getExtent().getUpperCorner().getY();

				switch (tilingOptions.getGenericAttributeValue()) {
					case XMIN_YMIN:
						value = String.valueOf(minX) + ' ' + minY;
						break;
					case XMAX_YMIN:
						value = String.valueOf(maxX) + ' ' + minY;
						break;
					case XMIN_YMAX:
						value = String.valueOf(minX) + ' ' + maxY;
						break;
					case XMAX_YMAX:
						value = String.valueOf(maxX) + ' ' + maxY;
						break;
					case XMIN_YMIN_XMAX_YMAX:
						value = String.valueOf(minX) + ' ' + minY + ' ' + maxX + ' ' + maxY;
						break;
					default:
						value = String.valueOf(activeTile.getX()) + ' ' + activeTile.getY();
				}

				StringAttribute genericStringAttrib = new StringAttribute();
				genericStringAttrib.setName("tile");
				genericStringAttrib.setValue(value);
				cityObject.addGenericAttribute(genericStringAttrib);
			}
		}

		return true;
	}

	private void addProperties(ObjectContext context, ResultSet rs) throws SQLException {
		AbstractCityObject cityObject = (AbstractCityObject) context.object;

		// app::appearance
		if (exportAppearance && context.projectionFilter.containsProperty("appearance", appearanceModule)) {
			long appearanceId = rs.getLong("apid");
			if (!rs.wasNull() && context.appearances.add(appearanceId))
				appearanceExporter.addBatch(appearanceId, cityObject);
		}

		// core:generalizesTo
		if (context.projectionFilter.containsProperty("generalizesTo", coreModule)) {
			long generalizesTo = rs.getLong("generalizes_to_id");
			if (!rs.wasNull())
				context.generalizesTos.add(generalizesTo);
		}

		// core:externalReference
		if (context.projectionFilter.containsProperty("externalReference", coreModule)) {
			long externalReferenceId = rs.getLong("exid");
			if (!rs.wasNull() && context.externalReferences.add(externalReferenceId)) {
				ExternalReference externalReference = new ExternalReference();
				ExternalObject externalObject = new ExternalObject();

				externalReference.setInformationSystem(rs.getString("infosys"));

				String name = rs.getString("exname");
				String uri = rs.getString("uri");

				if (name != null || uri != null) {
					if (name != null)
						externalObject.setName(name);
					if (uri != null)
						externalObject.setUri(uri);
				} else
					externalObject.setUri("");

				externalReference.setExternalObject(externalObject);
				cityObject.addExternalReference(externalReference);
			}
		}

		// gen:_genericAttribute
		long genericAttributeId = rs.getLong("gaid");
		if (!rs.wasNull() && context.genericAttributes.add(genericAttributeId))
			genericAttributeExporter.doExport(genericAttributeId, cityObject, context.projectionFilter, "ga", context.genericAttributeSets, rs);
	}

	private void postprocess(ObjectContext context, long cityObjectId) throws CityGMLExportException, SQLException {
		if (context.isCityObject) {
			AbstractCityObject cityObject = (AbstractCityObject) context.object;

			// export generalization features
			if (!context.generalizesTos.isEmpty())
				generalizesToExporter.doExport(cityObject, cityObjectId, context.generalizesTos);
		}

		// ADE-specific extensions
		if (exporter.hasADESupport())
			exporter.delegateToADEExporter(context.object, cityObjectId, context.objectType, context.projectionFilter);
	}

	@Override
	public void close() throws SQLException {
		psBulk.close();
		psSelect.close();
	}

	private static class ObjectContext {
		final AbstractGML object;
		final AbstractObjectType<?> objectType;
		final ProjectionFilter projectionFilter;
		final boolean isFeature;
		final boolean isCityObject;
		final boolean isTopLevel;

		boolean isInitialized;
		Set<Long> appearances;
		Set<Long> generalizesTos;
		Set<Long> externalReferences;
		Set<Long> genericAttributes;
		Map<Long, GenericAttributeSet> genericAttributeSets;

		ObjectContext(AbstractGML object, AbstractObjectType<?> objectType, ProjectionFilter projectionFilter) {
			this.object = object;
			this.objectType = objectType;
			this.projectionFilter = projectionFilter;

			isFeature = object instanceof AbstractFeature;
			isCityObject = object instanceof AbstractCityObject;
			isTopLevel = objectType instanceof FeatureType && ((FeatureType) objectType).isTopLevel();

			if (isCityObject) {
				appearances = new HashSet<>();
				generalizesTos = new HashSet<>();
				externalReferences = new HashSet<>();
				genericAttributes = new HashSet<>();
				genericAttributeSets = new HashMap<>();
			}
		}

		boolean initialize() {
			if (!isInitialized) {
				isInitialized = true;
				return true;
			} else
				return false;
		}
	}
}
