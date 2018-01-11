package org.citydb.query.builder.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildProperties {
	private boolean useDistinct;
	private List<String> projectionColumns;

	private BuildProperties() {
		// just to thwart instantiation
	}
	
	public static BuildProperties defaults() {
		return new BuildProperties().reset();
	}
	
	public BuildProperties reset() {
		projectionColumns = null;
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
	
}
