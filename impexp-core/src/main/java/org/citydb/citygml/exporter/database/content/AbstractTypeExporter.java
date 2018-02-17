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
			Table table = new Table(adeHookTable);
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
