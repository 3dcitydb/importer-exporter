package org.citydb.database.adapter.postgis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractSchemaManagerAdapter;

public class SchemaManagerAdapter extends AbstractSchemaManagerAdapter {
	private final String defaultSchema = "citydb";

	protected SchemaManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
	}

	@Override
	public String getDefaultSchema() {
		return defaultSchema;
	}

	@Override
	public boolean equalsDefaultSchema(String schema) {
		return (schema == null || schema.trim().length() == 0 || defaultSchema.equals(schema.trim()));
	}

	@Override
	public boolean existsSchema(Connection connection, String schema) {
		if (schema == null)
			throw new IllegalArgumentException("Schema name may not be null.");

		schema = schema.trim();
		if (!schema.equals(defaultSchema) && (schema.length() == 0 || defaultSchema.equals(schema)))
			schema = defaultSchema;

		try (PreparedStatement stmt = connection.prepareStatement("select exists(select schema_name from information_schema.schemata where schema_name = ?)")) {
			stmt.setString(1, schema);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? rs.getBoolean(1) : false;
			}
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public List<String> fetchSchemasFromDatabase(Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(new StringBuilder("select s.schema_name from information_schema.schemata s ")
						.append("where exists (select 1 from information_schema.tables t ")
						.append("where t.table_name='database_srs' and t.table_schema=s.schema_name) ")
						.append("order by s.schema_name").toString())) {
			List<String> schemas = new ArrayList<>();
			while (rs.next())
				schemas.add(rs.getString(1));

			return schemas;
		}
	}

}