package org.citydb.query.builder.sql;

import org.citydb.sqlbuilder.schema.AliasGenerator;
import org.citydb.sqlbuilder.schema.DefaultAliasGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildProperties {
	protected final DefaultAliasGenerator aliasGenerator;
	private boolean useDistinct;
	private List<String> projectionColumns;

	private BuildProperties() {
		// just to thwart instantiation
		aliasGenerator = new DefaultAliasGenerator();
	}
	
	public static BuildProperties defaults() {
		return new BuildProperties().reset();
	}
	
	public BuildProperties reset() {
		projectionColumns = null;
		aliasGenerator.reset();
		return this;
	}

	public BuildProperties useDistinct(boolean useDistinct) {
		this.useDistinct = useDistinct;
		return this;
	}
	
	public boolean isUseDistinct() {
		return useDistinct;
	}
	
	public BuildProperties addProjectionColumn(String columnName) {
		if (projectionColumns == null)
			projectionColumns = new ArrayList<>();
		
		if (!projectionColumns.contains(columnName))		
			projectionColumns.add(columnName);
		
		return this;
	}
	
	public BuildProperties addProjectionColumns(String... columnNames) {
		for (String columnName : columnNames)
			addProjectionColumn(columnName);
		
		return this;
	}
	
	public List<String> getAdditionalProjectionColumns() {
		return projectionColumns != null ? new ArrayList<>(projectionColumns) : Collections.emptyList();
	}

	public AliasGenerator getAliasGenerator() {
		return aliasGenerator;
	}
	
}
