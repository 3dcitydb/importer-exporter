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
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractTypeExporter implements DBExporter {
	protected final CityGMLExportManager exporter;	
	protected Table table;
	protected Select select;
	
	public AbstractTypeExporter(CityGMLExportManager exporter) {
		this.exporter = exporter;
	}
	
	protected List<Table> addJoinsToADEHookTables(TableEnum type, Table fromTable) {
		List<Table> tables = null;
		if (exporter.hasADESupport()) {
			Set<String> tableNames = exporter.getADEHookTables(type);
			if (!tableNames.isEmpty()) {
				tables = new ArrayList<>();
				for (String tableName : tableNames) {
					Table table = new Table(tableName, exporter.getDatabaseAdapter().getConnectionDetails().getSchema());
					tables.add(table);
					select.addProjection(table.getColumn("id", table.getAlias() + table.getName()))
							.addJoin(JoinFactory.left(table, "id", ComparisonName.EQUAL_TO, fromTable.getColumn("id")));
				}
			}
		}

		return tables;
	}
	
	protected List<String> retrieveADEHookTables(List<Table> tables, ResultSet rs) throws SQLException {
		List<String> tableNames = null;
		for (Table table : tables) {
			rs.getLong(table.getAlias() + table.getName());
			if (!rs.wasNull()) {
				if (tableNames == null)
					tableNames = new ArrayList<>();
				
				tableNames.add(table.getName());
			}
		}
		
		return tableNames;
	}
	
}
