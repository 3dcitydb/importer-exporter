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

import org.citydb.sqlbuilder.schema.AliasGenerator;
import org.citydb.sqlbuilder.schema.DefaultAliasGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildProperties {
	protected final DefaultAliasGenerator aliasGenerator;
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
