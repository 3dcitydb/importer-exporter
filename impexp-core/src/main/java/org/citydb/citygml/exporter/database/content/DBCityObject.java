/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.exporter.SimpleTilingOptions;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.Query;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.query.filter.tiling.Tile;
import org.citydb.query.filter.tiling.Tiling;
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
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.HashSet;

public class DBCityObject implements DBExporter {
	private final Query query;
	private final CityGMLExportManager exporter;

	private PreparedStatement ps;
	private DBLocalAppearance appearanceExporter;
	private DBGeneralization generalizesToExporter;
	private DBCityObjectGenericAttrib genericAttributeExporter;

	private String gmlSrsName;
	private boolean exportAppearance;
	private boolean useTiling;
	private boolean setTileInfoAsGenericAttribute;
	private Tile activeTile;
	private SimpleTilingOptions tilingOptions;

	private boolean exportCityDBMetadata;
	private AttributeValueSplitter valueSplitter;

	private HashSet<Long> generalizesTos;
	private HashSet<Long> externalReferences;
	private String coreModule;
	private String appearanceModule;
	private String gmlModule;
	private String cityDBADEModule;

	public DBCityObject(Connection connection, Query query, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		this.exporter = exporter;
		this.query = query;

		generalizesTos = new HashSet<>();
		externalReferences = new HashSet<>();

		coreModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.CORE).getNamespaceURI();
		appearanceModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.APPEARANCE).getNamespaceURI();
		gmlModule = GMLCoreModule.v3_1_1.getNamespaceURI();

		useTiling = query.isSetTiling();
		if (useTiling) {
			Tiling tiling = query.getTiling();
			tilingOptions = tiling.getTilingOptions() instanceof SimpleTilingOptions ? (SimpleTilingOptions) tiling.getTilingOptions() : new SimpleTilingOptions();
			setTileInfoAsGenericAttribute = tilingOptions.isIncludeTileAsGenericAttribute();
			activeTile = tiling.getActiveTile();
		}

		exportCityDBMetadata = config.getProject().getExporter().getContinuation().isExportCityDBMetadata();
		if (exportCityDBMetadata) {
			cityDBADEModule = exporter.getTargetCityGMLVersion() == CityGMLVersion.v2_0_0 ?
					CityDBADE200Module.v3_0.getNamespaceURI() : CityDBADE100Module.v3_0.getNamespaceURI();
		}

		exportAppearance = config.getProject().getExporter().getAppearances().isSetExportAppearance();
		gmlSrsName = query.getTargetSrs().getGMLSrsName();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
		Table externalReference = new Table(TableEnum.EXTERNAL_REFERENCE.getName(), schema);
		Table generalization = new Table(TableEnum.GENERALIZATION.getName(), schema);
		Select select = new Select();

		select.addProjection(cityObject.getColumn("gmlid"), exporter.getGeometryColumn(cityObject.getColumn("envelope")),
				cityObject.getColumn("name"), cityObject.getColumn("name_codespace"), cityObject.getColumn("description"), cityObject.getColumn("creation_date"),
				cityObject.getColumn("termination_date"), cityObject.getColumn("relative_to_terrain"), cityObject.getColumn("relative_to_water"),
				externalReference.getColumn("id", "exid"), externalReference.getColumn("infosys"), externalReference.getColumn("name", "exname"), externalReference.getColumn("uri"),
				generalization.getColumn("generalizes_to_id"))
		.addJoin(JoinFactory.left(externalReference, "cityobject_id", ComparisonName.EQUAL_TO, cityObject.getColumn("id")))
		.addJoin(JoinFactory.left(generalization, "cityobject_id", ComparisonName.EQUAL_TO, cityObject.getColumn("id")))
		.addSelection(ComparisonFactory.equalTo(cityObject.getColumn("id"), new PlaceHolder<>()));
		if (exportCityDBMetadata) select.addProjection(cityObject.getColumn("last_modification_date"), cityObject.getColumn("updating_person"), cityObject.getColumn("reason_for_update"), cityObject.getColumn("lineage"));
		ps = connection.prepareStatement(select.toString());

		generalizesToExporter = exporter.getExporter(DBGeneralization.class);
		genericAttributeExporter = exporter.getExporter(DBCityObjectGenericAttrib.class);
		valueSplitter = exporter.getAttributeValueSplitter();
		if (exportAppearance)
			appearanceExporter = exporter.getExporter(DBLocalAppearance.class);
	}

	protected boolean doExport(AbstractGML object, long objectId, AbstractObjectType<?> objectType) throws CityGMLExportException, SQLException {
		return doExport(object, objectId, objectType, query.getProjectionFilter(objectType));
	}

	protected boolean doExport(AbstractGML object, long objectId, AbstractObjectType<?> objectType, ProjectionFilter projectionFilter) throws CityGMLExportException, SQLException {
		boolean isFeature = object instanceof AbstractFeature;
		boolean isCityObject = object instanceof AbstractCityObject;
		boolean isTopLevel = objectType instanceof FeatureType && ((FeatureType)objectType).isTopLevel();

		ps.setLong(1, objectId);

		try (ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				// gml:id
				object.setId(rs.getString("gmlid"));

				// gml:name
				if (!isCityObject || projectionFilter.containsProperty("name", gmlModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("name"), rs.getString("name_codespace"))) {
						Code name = new Code(splitValue.result(0));
						name.setCodeSpace(splitValue.result(1));
						object.addName(name);
					}
				}

				// gml:description
				if (!isCityObject || projectionFilter.containsProperty("description", gmlModule)) {
					String description = rs.getString("description");
					if (!rs.wasNull())
						object.setDescription(new StringOrRef(description));
				}

				if (isFeature && isTopLevel) {
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
					if (useTiling) {
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
					if (!isCityObject || projectionFilter.containsProperty("boundedBy", gmlModule))
						((AbstractFeature)object).setBoundedBy(boundedBy);
				}

				if (isCityObject) {
					// core:creationDate
					if (projectionFilter.containsProperty("creationDate", coreModule)) {
						Timestamp creationDate = rs.getTimestamp("creation_date");
						if (!rs.wasNull())
							((AbstractCityObject)object).setCreationDate(creationDate.toLocalDateTime().atZone(ZoneId.systemDefault()));
					}

					// core:terminationDate
					if (projectionFilter.containsProperty("terminationDate", coreModule)) {
						Timestamp terminationDate = rs.getTimestamp("termination_date");
						if (terminationDate != null)
							((AbstractCityObject)object).setTerminationDate(terminationDate.toLocalDateTime().atZone(ZoneId.systemDefault()));
					}

					// core:relativeToTerrain
					if (projectionFilter.containsProperty("relativeToTerrain", coreModule)) {
						String relativeToTerrain = rs.getString("relative_to_terrain");
						if (!rs.wasNull())
							((AbstractCityObject)object).setRelativeToTerrain(RelativeToTerrain.fromValue(relativeToTerrain));
					}

					// core:relativeToWater
					if (projectionFilter.containsProperty("relativeToWater", coreModule)) {
						String relativeToWater = rs.getString("relative_to_water");
						if (!rs.wasNull())
							((AbstractCityObject)object).setRelativeToWater(RelativeToWater.fromValue(relativeToWater));
					}

					// 3DCityDB ADE metadata
					if (exportCityDBMetadata && isTopLevel) {
						if (projectionFilter.containsProperty("lastModificationDate", cityDBADEModule)) {
							Timestamp lastModificationDate = rs.getTimestamp("last_modification_date");
							if (!rs.wasNull()) {
								LastModificationDateProperty property = new LastModificationDateProperty(
										lastModificationDate.toLocalDateTime().atZone(ZoneId.systemDefault()));
								((AbstractCityObject) object).addGenericApplicationPropertyOfCityObject(property);
							}
						}

						if (projectionFilter.containsProperty("updatingPerson", cityDBADEModule)) {
							String updatingPerson = rs.getString("updating_person");
							if (!rs.wasNull()) {
								UpdatingPersonProperty property = new UpdatingPersonProperty(updatingPerson);
								((AbstractCityObject) object).addGenericApplicationPropertyOfCityObject(property);
							}
						}

						if (projectionFilter.containsProperty("reasonForUpdate", cityDBADEModule)) {
							String reasonForUpdate = rs.getString("reason_for_update");
							if (!rs.wasNull()) {
								ReasonForUpdateProperty property = new ReasonForUpdateProperty(reasonForUpdate);
								((AbstractCityObject) object).addGenericApplicationPropertyOfCityObject(property);
							}
						}

						if (projectionFilter.containsProperty("lineage", cityDBADEModule)) {
							String lineage = rs.getString("lineage");
							if (!rs.wasNull()) {
								LineageProperty property = new LineageProperty(lineage);
								((AbstractCityObject) object).addGenericApplicationPropertyOfCityObject(property);
							}
						}
					}

					do {
						// core:generalizesTo
						if (projectionFilter.containsProperty("generalizesTo", coreModule)) {
							long generalizesTo = rs.getLong("generalizes_to_id");
							if (!rs.wasNull())
								generalizesTos.add(generalizesTo);
						}

						// core:externalReference
						if (projectionFilter.containsProperty("externalReference", coreModule)) {
							long externalReferenceId = rs.getLong("exid");
							if (!rs.wasNull() && externalReferences.add(externalReferenceId)) {
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
								((AbstractCityObject)object).addExternalReference(externalReference);
							}
						}

					} while (rs.next());

					// core:generalizesTo
					if (!generalizesTos.isEmpty())
						generalizesToExporter.doExport(((AbstractCityObject)object), objectId, generalizesTos);

					// gen:_genericAttribute
					genericAttributeExporter.doExport(((AbstractCityObject)object), objectId, projectionFilter);

					// add tile as generic attribute
					if (isTopLevel && setTileInfoAsGenericAttribute) {
						String value;

						double minX = activeTile.getExtent().getLowerCorner().getX();
						double minY = activeTile.getExtent().getLowerCorner().getY();
						double maxX = activeTile.getExtent().getUpperCorner().getX();
						double maxY = activeTile.getExtent().getUpperCorner().getY();

						switch (tilingOptions.getGenericAttributeValue()) {
						case XMIN_YMIN:
							value = String.valueOf(minX) + ' ' + String.valueOf(minY);
							break;
						case XMAX_YMIN:
							value = String.valueOf(maxX) + ' ' + String.valueOf(minY);
							break;
						case XMIN_YMAX:
							value = String.valueOf(minX) + ' ' + String.valueOf(maxY);
							break;
						case XMAX_YMAX:
							value = String.valueOf(maxX) + ' ' + String.valueOf(maxY);
							break;
						case XMIN_YMIN_XMAX_YMAX:
							value = String.valueOf(minX) + ' ' + String.valueOf(minY) + ' ' +
									String.valueOf(maxX) + ' ' + String.valueOf(maxY);
							break;
						default:
							value = String.valueOf(activeTile.getX()) + ' ' + String.valueOf(activeTile.getY());
						} 

						StringAttribute genericStringAttrib = new StringAttribute();
						genericStringAttrib.setName("tile");
						genericStringAttrib.setValue(value);
						((AbstractCityObject)object).addGenericAttribute(genericStringAttrib);
					}

					// export appearance information associated with the city object
					if (exportAppearance && projectionFilter.containsProperty("appearance", appearanceModule)) {
						boolean lazyExport = !exporter.getLodFilter().preservesGeometry();
						appearanceExporter.read(((AbstractCityObject)object), objectId, isTopLevel, lazyExport);
					}
				}
			}
			
			// ADE-specific extensions
			if (exporter.hasADESupport())
				exporter.delegateToADEExporter(object, objectId, objectType, projectionFilter);
			
			return true;
		} finally {
			generalizesTos.clear();
			externalReferences.clear();
		}
	}

	@Override
	public void close() throws SQLException {
		ps.close();
	}
	
}
