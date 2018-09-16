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
package org.citydb.query.builder.sql;

import org.citydb.database.schema.path.SchemaPath;

import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;

public class SQLQueryContext {
	protected final Select select;	
	protected Column targetColumn;
	protected Table fromTable;
	protected Table toTable;
	protected SchemaPath schemaPath;
	SchemaPath backup;
	
	protected SQLQueryContext(Select select) {
		if (select == null)
			throw new IllegalArgumentException("Select object may not be null.");
			
		this.select = select;
	}
	
	public Select getSelect() {
		return select;
	}

	public Column getTargetColumn() {
		return targetColumn;
	}

	public Table getFromTable() {
		return fromTable;
	}

	public Table getToTable() {
		return toTable;
	}
	
}
