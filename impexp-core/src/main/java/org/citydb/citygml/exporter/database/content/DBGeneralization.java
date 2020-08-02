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
import org.citydb.database.schema.TableEnum;
import org.citydb.query.Query;
import org.citydb.query.filter.FilterException;
import org.citydb.sqlbuilder.expression.LiteralSelectExpression;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.GeneralizationRelation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class DBGeneralization implements DBExporter {
	private final Connection connection;
	private final CityGMLExportManager exporter;
	private final Query query;
	private final Select select;

	private PreparedStatement ps;

	public DBGeneralization(Connection connection, Query query, CityGMLExportManager exporter) {
		this.connection = connection;
		this.exporter = exporter;
		this.query = query;

		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		String subQuery = "select * from " + exporter.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("unnest") + "(?)";

		Table table = new Table(TableEnum.CITYOBJECT.getName(), schema);
		select = new Select().addProjection(table.getColumn("gmlid"))
				.addSelection(ComparisonFactory.in(table.getColumn("id"), new LiteralSelectExpression(subQuery)));

		if (query.isSetTiling()) select.addProjection(table.getColumn("envelope"));
	}

	protected void doExport(AbstractCityObject cityObject, Set<Long> generalizesTos) throws CityGMLExportException, SQLException {
		if (ps == null)
			ps = connection.prepareStatement(select.toString());

		ps.setArray(1, exporter.getDatabaseAdapter().getSQLAdapter().createIdArray(generalizesTos.toArray(new Long[0]), connection));

		try (ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String gmlId = rs.getString(1);
				if (rs.wasNull())
					continue;

				if (query.isSetTiling()) {
					Object object = rs.getObject(2);
					if (!rs.wasNull()) {
						GeometryObject geomObj = exporter.getDatabaseAdapter().getGeometryConverter().getEnvelope(object);
						double[] coordinates = geomObj.getCoordinates(0);

						try {
							if (!query.getTiling().getActiveTile().isOnTile(new Point(
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
		if (ps != null)
			ps.close();
	}
}
