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
package org.citydb.query.builder.sql;

import org.citydb.database.schema.path.AbstractNode;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SQLQueryContext {
	final Select select;
	Column targetColumn;
	Table fromTable;
	Table toTable;
	BuildContext buildContext;

	SchemaPath schemaPath;
	SchemaPath backup;
	
	SQLQueryContext(Select select) {
		this.select = Objects.requireNonNull(select, "Select object may not be null.");
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

	static class BuildContext {
		final AbstractNode<?> node;
		Map<String, Table> tableContext;
		Table currentTable;
		List<BuildContext> children;

		BuildContext(AbstractNode<?> node) {
			this.node = Objects.requireNonNull(node, "Node object may not be null.");
		}

		BuildContext addSubContext(AbstractNode<?> node) {
			BuildContext nodeContext = null;

			if (node != null) {
				if (children == null)
					children = new ArrayList<>();

				nodeContext = new BuildContext(node);
				children.add(nodeContext);
			}

			return nodeContext;
		}

		boolean hasSubContexts() {
			return children != null && !children.isEmpty();
		}

		BuildContext findSubContext(AbstractNode<?> node) {
			if (children != null) {
				for (BuildContext child : children) {
					if (child.node.isEqualTo(node, false))
						return child;
				}
			}

			return null;
		}
	}
	
}
