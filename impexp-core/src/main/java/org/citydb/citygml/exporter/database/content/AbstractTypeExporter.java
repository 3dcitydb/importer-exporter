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
	
	protected void addJoinsToADEHookTables(Set<String> adeHookTables, Table fromTable) {
		for (String adeHookTable : adeHookTables) {
			Table table = new Table(adeHookTable, exporter.getDatabaseAdapter().getConnectionDetails().getSchema());
			select.addProjection(table.getColumn("id", adeHookTable))
			.addJoin(JoinFactory.left(table, "id", ComparisonName.EQUAL_TO, fromTable.getColumn("id")));
		}
	}
	
	protected List<String> retrieveADEHookTables(Set<String> candidates, ResultSet rs) throws SQLException {
		List<String> adeHookTables = null;		
		for (String adeHookTable : candidates) {
			rs.getLong(adeHookTable);
			if (!rs.wasNull()) {
				if (adeHookTables == null)
					adeHookTables = new ArrayList<>();
				
				adeHookTables.add(adeHookTable);
			}
		}
		
		return adeHookTables;
	}
	
}
