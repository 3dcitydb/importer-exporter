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
package org.citydb.core.operation.exporter.database.content;

import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.exporter.CityGMLExportException;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DBGeneralization implements DBExporter {
	private final Connection connection;
	private final CityGMLExportManager exporter;
	private final Select select;
	private final Map<Long, AbstractCityObject> batches;
	private final int batchSize;

	private PreparedStatement ps;

	public DBGeneralization(Connection connection, CityGMLExportManager exporter) {
		this.connection = connection;
		this.exporter = exporter;

		batches = new LinkedHashMap<>();
		batchSize = exporter.getFeatureBatchSize();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		String placeHolders = String.join(",", Collections.nCopies(batchSize, "?"));

		Table table = new Table(TableEnum.CITYOBJECT.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("gmlid"))
				.addSelection(ComparisonFactory.in(table.getColumn("id"), new LiteralSelectExpression(placeHolders)));
	}

	protected void addBatch(long generalizesToId, AbstractCityObject cityObject) throws CityGMLExportException, SQLException {
		batches.put(generalizesToId, cityObject);
		if (batches.size() == batchSize)
			executeBatch();
	}

	protected void executeBatch() throws CityGMLExportException, SQLException {
		if (!batches.isEmpty()) {
			try {
				if (ps == null)
					ps = connection.prepareStatement(select.toString());

				Long[] ids = batches.keySet().toArray(new Long[0]);
				for (int i = 0; i < batchSize; i++)
					ps.setLong(i + 1, i < ids.length ? ids[i] : 0);

				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						long id = rs.getLong(1);
						String gmlId = rs.getString(2);
						if (rs.wasNull())
							continue;

						AbstractCityObject cityObject = batches.get(id);
						if (cityObject == null) {
							exporter.logOrThrowErrorMessage("Failed to assign generalization with id " + id + " to a city object.");
							continue;
						}

						GeneralizationRelation generalizesTo = new GeneralizationRelation();
						generalizesTo.setHref("#" + gmlId);
						cityObject.addGeneralizesTo(generalizesTo);
					}
				}
			} finally {
				batches.clear();
			}
		}
	}

	@Override
	public void close() throws SQLException {
		if (ps != null)
			ps.close();
	}
}
