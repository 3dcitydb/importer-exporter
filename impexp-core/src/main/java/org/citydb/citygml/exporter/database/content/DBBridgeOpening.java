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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.bridge.AbstractOpening;
import org.citygml4j.model.citygml.bridge.Door;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;

public class DBBridgeOpening extends AbstractFeatureExporter<AbstractOpening> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBAddress addressExporter;

	private String bridgeModule;
	private LodFilter lodFilter;
	private Set<String> openingADEHookTables;
	private Set<String> addressADEHookTables;

	public DBBridgeOpening(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractOpening.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_OPENING.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.BRIDGE_OPENING.getName(), schema);
		Table address = new Table(TableEnum.ADDRESS.getName(), schema);

		select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("lod3MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("lod3ImplicitRepresentation", bridgeModule))
			select.addProjection(table.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point")), table.getColumn("lod3_implicit_transformation"));
		if (projectionFilter.containsProperty("lod4ImplicitRepresentation", bridgeModule))
			select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));
		if (projectionFilter.containsProperty("address", bridgeModule)) {
			select.addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, table.getColumn("address_id")))
			.addProjection(table.getColumn("address_id"), address.getColumn("street"), address.getColumn("house_number"), address.getColumn("po_box"), address.getColumn("zip_code"), address.getColumn("city"),
					address.getColumn("state"), address.getColumn("country"), address.getColumn("xal_source"), exporter.getGeometryColumn(address.getColumn("multi_point")));
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			openingADEHookTables = exporter.getADEHookTables(TableEnum.BRIDGE_OPENING);
			addressADEHookTables = exporter.getADEHookTables(TableEnum.ADDRESS);			
			if (openingADEHookTables != null) addJoinsToADEHookTables(openingADEHookTables, table);
			if (addressADEHookTables != null) addJoinsToADEHookTables(addressADEHookTables, address);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		addressExporter = exporter.getExporter(DBAddress.class);
	}

	@Override
	protected Collection<AbstractOpening> doExport(long id, AbstractOpening root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			long currentOpeningId = 0;
			AbstractOpening opening = null;
			ProjectionFilter projectionFilter = null;
			HashMap<Long, AbstractOpening> openings = new HashMap<>();

			while (rs.next()) {
				long openingId = rs.getLong("id");

				if (openingId != currentOpeningId || opening == null) {
					currentOpeningId = openingId;

					opening = openings.get(openingId);
					if (opening == null) {
						FeatureType featureType = null;						
						if (openingId == id && root != null) {
							opening = root;
							featureType = rootType;
						} else {
							int objectClassId = rs.getInt("objectclass_id");
							featureType = exporter.getFeatureType(objectClassId);
							if (featureType == null)
								continue;

							// create bridge opening object
							opening = exporter.createObject(objectClassId, AbstractOpening.class);
							if (opening == null) {
								exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, openingId) + " as bridge opening object.");
								continue;
							}
						}

						// get projection filter
						projectionFilter = exporter.getProjectionFilter(featureType);

						// export city object information
						cityObjectExporter.doExport(opening, openingId, featureType, projectionFilter);

						LodIterator lodIterator = lodFilter.iterator(3, 4);
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiSurface").toString(), bridgeModule))
								continue;

							long lodMultiSurfaceId = rs.getLong(new StringBuilder("lod").append(lod).append("_multi_surface_id").toString());
							if (rs.wasNull()) 
								continue;

							SurfaceGeometry geometry = geometryExporter.doExport(lodMultiSurfaceId);
							if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
								if (geometry.isSetGeometry())
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getReference());

								switch (lod) {
								case 3:
									opening.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									opening.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}

						lodIterator.reset();
						while (lodIterator.hasNext()) {
							int lod = lodIterator.next();

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("ImplicitRepresentation").toString(), bridgeModule))
								continue;

							// get implicit geometry details
							long implicitGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_implicit_rep_id").toString());
							if (rs.wasNull())
								continue;

							GeometryObject referencePoint = null;
							Object referencePointObj = rs.getObject(new StringBuilder("lod").append(lod).append("_implicit_ref_point").toString());
							if (!rs.wasNull())
								referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

							String transformationMatrix = rs.getString(new StringBuilder("lod").append(lod).append("_implicit_transformation").toString());

							ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
							if (implicit != null) {
								ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
								implicitProperty.setObject(implicit);

								switch (lod) {
								case 3:
									opening.setLod3ImplicitRepresentation(implicitProperty);
									break;
								case 4:
									opening.setLod4ImplicitRepresentation(implicitProperty);
									break;
								}
							}
						}

						// delegate export of generic ADE properties
						if (openingADEHookTables != null) {
							List<String> adeHookTables = retrieveADEHookTables(openingADEHookTables, rs);
							if (adeHookTables != null)
								exporter.delegateToADEExporter(adeHookTables, opening, openingId, featureType, projectionFilter);
						}

						opening.setLocalProperty("projection", projectionFilter);
						openings.put(openingId, opening);
					} else
						projectionFilter = (ProjectionFilter)opening.getLocalProperty("projection");
				}

				if (opening instanceof Door && projectionFilter.containsProperty("address", bridgeModule)) {
					long addressId = rs.getLong("address_id");
					if (!rs.wasNull()) {
						AddressProperty addressProperty = addressExporter.doExport(addressId, rs);
						if (addressProperty != null) {
							((Door)opening).addAddress(addressProperty);

							// delegate export of generic ADE properties
							if (addressADEHookTables != null) {
								List<String> adeHookTables = retrieveADEHookTables(addressADEHookTables, rs);
								if (adeHookTables != null) {
									Address address = addressProperty.getAddress();
									FeatureType featureType = exporter.getFeatureType(address);
									exporter.delegateToADEExporter(adeHookTables, address, addressId, featureType, exporter.getProjectionFilter(featureType));
								}
							}
						}
					}
				}
			}

			// check whether lod filter is satisfied
			if (!lodFilter.preservesGeometry()) {
				for (Iterator<Entry<Long, AbstractOpening>> iter = openings.entrySet().iterator(); iter.hasNext(); ) {
					opening = iter.next().getValue();
					if (!exporter.satisfiesLodFilter(opening))
						iter.remove();
				}
			}

			return openings.values();
		}
	}

}
