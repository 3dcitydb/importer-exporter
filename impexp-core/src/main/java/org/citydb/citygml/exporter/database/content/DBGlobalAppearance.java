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

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.query.Query;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.ConstantColumn;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.util.walker.GMLWalker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DBGlobalAppearance extends AbstractAppearanceExporter {
	private final PreparedStatement ps;
	private final PreparedStatement psImport;

	private int batchSize;
	private int batchCounter;

	public DBGlobalAppearance(Connection connection, Query query, CacheTable cacheTable, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		super(true, connection, query, cacheTable, exporter, config);
		ps = cacheTable.getConnection().prepareStatement(select.toString());

		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		batchSize = config.getDatabaseConfig().getImportBatching().getTempBatchSize();
		if (batchSize > exporter.getDatabaseAdapter().getMaxBatchSize())
			batchSize = exporter.getDatabaseAdapter().getMaxBatchSize();

		Table table = new Table(TableEnum.TEXTUREPARAM.getName(), schema);
		Select select = new Select().addProjection(new ConstantColumn(new PlaceHolder<>()))
				.addSelection(ComparisonFactory.exists(new Select()
						.addProjection(new ConstantColumn(1).withFromTable(table))
						.addSelection(ComparisonFactory.equalTo(table.getColumn("surface_geometry_id"), new PlaceHolder<>()))));

		if (exporter.getDatabaseAdapter().getSQLAdapter().requiresPseudoTableInSelect())
			select.setPseudoTable(exporter.getDatabaseAdapter().getSQLAdapter().getPseudoTableName());

		psImport = cacheTable.getConnection().prepareStatement("insert into " + cacheTable.getTableName() + " " + select.toString());
	}

	protected Appearance doExport(long appearanceId) throws CityGMLExportException, SQLException {
		ps.setLong(1, appearanceId);

		try (ResultSet rs = ps.executeQuery()) {
			Map<Long, Appearance> appearances = doExport(rs);
			return !appearances.isEmpty() ? appearances.values().iterator().next() : null;
		}
	}

	protected void cacheGeometryIds(AbstractGML object) throws SQLException {
		Set<Long> ids = new HashSet<>();

		object.accept(new GMLWalker() {
			@Override
			public void visit(AbstractGeometry geometry) {
				Long id = (Long) geometry.getLocalProperty("global_app_cache_id");
				if (id != null)
					ids.add(id);
			}
		});

		for (Long id : ids) {
			psImport.setLong(1, id);
			psImport.setLong(2, id);
			psImport.addBatch();

			if (++batchCounter == batchSize) {
				psImport.executeBatch();
				batchCounter = 0;
			}
		}
	}

	@Override
	public void close() throws SQLException {
		if (batchCounter > 0)
			psImport.executeBatch();

		psImport.close();
		ps.close();
	}
}
