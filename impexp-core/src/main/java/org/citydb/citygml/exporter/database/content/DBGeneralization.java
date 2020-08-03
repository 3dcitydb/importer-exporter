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

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.Point;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.BuildProperties;
import org.citydb.query.builder.sql.SQLQueryBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.LongLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.GeneralizationRelation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class DBGeneralization implements DBExporter {
	private final Connection connection;
	private final CityGMLExportManager exporter;
	private final Query generalizationQuery;
	private final SQLQueryBuilder builder;

	public DBGeneralization(Connection connection, Query query, CityGMLExportManager exporter) throws CityGMLExportException {
		this.connection = connection;
		this.exporter = exporter;

		BuildProperties buildProperties = BuildProperties.defaults().addProjectionColumn(MappingConstants.GMLID);
		if (query.isSetTiling())
			buildProperties.addProjectionColumn(MappingConstants.ENVELOPE);

		builder = new SQLQueryBuilder(
				exporter.getSchemaMapping(), 
				exporter.getDatabaseAdapter(),
				buildProperties);

		generalizationQuery = new Query(query);

		// set generic spatial filter
		if (generalizationQuery.isSetSelection()) {
			try {
				Predicate predicate = generalizationQuery.getSelection().getGenericSpatialFilter(exporter.getSchemaMapping().getCommonSuperType(generalizationQuery.getFeatureTypeFilter().getFeatureTypes()));
				generalizationQuery.setSelection(new SelectionFilter(predicate));
			} catch (FilterException e) {
				throw new CityGMLExportException("Failed to build generic spatial filter for generalization objects.", e);
			}
		}
	}

	protected void doExport(AbstractCityObject cityObject, long cityObjectId, HashSet<Long> generalizesTos) throws CityGMLExportException, SQLException {
		// create select statement
		Select select;
		try {
			select = builder.buildQuery(generalizationQuery);
		} catch (QueryBuildException e) {
			throw new CityGMLExportException("Failed to build sub-query for generalization objects.", e);
		}

		// add generalization predicate
		if (generalizesTos.size() == 1)
			select.addSelection(ComparisonFactory.equalTo((Column)select.getProjection().get(0), new LongLiteral(generalizesTos.iterator().next())));
		else
			select.addSelection(ComparisonFactory.in((Column)select.getProjection().get(0), new LiteralList(generalizesTos.toArray(new Long[0]))));

		try (PreparedStatement stmt = exporter.getDatabaseAdapter().getSQLAdapter().prepareStatement(select, connection);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				String gmlId = rs.getString("gmlid");			
				if (rs.wasNull())
					continue;

				if (generalizationQuery.isSetTiling()) {
					Object object = rs.getObject("envelope");
					if (!rs.wasNull()) {
						GeometryObject geomObj = exporter.getDatabaseAdapter().getGeometryConverter().getEnvelope(object);
						double[] coordinates = geomObj.getCoordinates(0);

						try {
							if (!generalizationQuery.getTiling().getActiveTile().isOnTile(new Point(
									(coordinates[0] + coordinates[3]) / 2.0,
									(coordinates[1] + coordinates[4]) / 2.0), 
									exporter.getDatabaseAdapter()))
								continue;
						} catch (FilterException e) {
							throw new CityGMLExportException("Failed to apply the tiling filter to generalization objects.", e);
						}
					}	
				}

				GeneralizationRelation generalizesTo = new GeneralizationRelation();
				generalizesTo.setHref("#" + gmlId);
				cityObject.addGeneralizesTo(generalizesTo);
			}
		}
	}

	@Override
	public void close() throws SQLException {
		// nothing to do...
	}

}
