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
