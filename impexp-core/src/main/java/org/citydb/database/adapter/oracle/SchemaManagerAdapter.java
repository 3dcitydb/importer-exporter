package org.citydb.database.adapter.oracle;

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

	protected SchemaManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
	}
	
	@Override
	public String getDefaultSchema() {
		return databaseAdapter.getConnectionDetails().getUser();
	}
	
	@Override
	public boolean equalsDefaultSchema(String schema) {
		return (schema == null || schema.trim().length() == 0 || getDefaultSchema().equalsIgnoreCase(schema.trim()));
	}
	
	@Override
	public boolean existsSchema(Connection connection, String schema) {
		if (schema == null)
			throw new IllegalArgumentException("Schema name may not be null.");

		String defaultSchema = getDefaultSchema();
		schema = schema.trim();
		if (!schema.equals(defaultSchema) && (schema.length() == 0 || defaultSchema.equalsIgnoreCase(schema)))
			schema = defaultSchema;
		
		try (PreparedStatement stmt = connection.prepareStatement("select count(*) from all_users where username = upper(?)")) {
			stmt.setString(1, schema);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next() ? rs.getInt(1) > 0 : false;
			}
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public List<String> fetchSchemasFromDatabase(Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("select username from all_users order by username")) {
			List<String> schemas = new ArrayList<>();
			
			while (rs.next()) {
				String schema = rs.getString(1);
				try (Statement check = connection.createStatement();
						ResultSet checkRs = check.executeQuery(new StringBuilder("select 1 from ")
								.append(schema).append(".database_srs where rownum = 1").toString())) {
					if (checkRs.next())
						schemas.add(schema);
				} catch (SQLException e) {
					//
				}
			}

			return schemas;
		}
	}

}