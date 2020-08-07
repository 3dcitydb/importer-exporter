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
package org.citydb.ade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.citydb.ade.exporter.ADEExportManager;
import org.citydb.ade.importer.ADEImportManager;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.Metadata;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citygml4j.model.citygml.ade.binding.ADEContext;

public abstract class ADEExtension {
	public static final String LIB_PATH = "lib";
	public static final String SCHEMA_MAPPING_PATH = "schema-mapping";

	private String id;
	private Path basePath;
	private Path schemaMappingFile;
	private Metadata metadata;
	private boolean enabled;
	private List<AppSchema> schemas;
	
	public ADEExtension() {
	}
	
	public ADEExtension(Path basePath) {
		this.basePath = basePath;
	}
	
	public abstract void init(SchemaMapping schemaMapping) throws ADEExtensionException;
	public abstract List<ADEContext> getADEContexts();
	public abstract ADEObjectMapper getADEObjectMapper();
	public abstract ADEImportManager createADEImportManager();
	public abstract ADEExportManager createADEExportManager();

	public final String getId() {
		return id;
	}

	final void setId(String id) {
		this.id = id;
	}

	public final Path getBasePath() {
		return basePath;
	}

	public final void setBasePath(Path basePath) {
		this.basePath = basePath;
	}

	public final Metadata getMetadata() {
		return metadata;
	}

	final void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public final Path getSchemaMappingFile() {
		return schemaMappingFile;
	}
	
	final void setSchemas(List<AppSchema> schemas) {
		this.schemas = schemas;
	}
	
	public final List<AppSchema> getSchemas() {
		return schemas != null ? schemas : Collections.emptyList();
	}

	final void validate() throws ADEExtensionException {		
		if (getADEContexts() == null || getADEContexts().isEmpty())
			throw new ADEExtensionException("The ADE extension lacks a citygml4j module.");

		if (getADEObjectMapper() == null)
			throw new ADEExtensionException("The ADE extension lacks an object type mapper.");

		if (basePath == null)
			throw new ADEExtensionException("No base path provided for the ADE extension.");

		// validate ADE extension package content
		try {
			List<Path> candidates = new ArrayList<>();
			Path path = basePath.resolve(SCHEMA_MAPPING_PATH);
			if (Files.exists(path)) {
				try (Stream<Path> stream = Files.walk(path)
						.filter(candidate -> candidate.getFileName().toString().toLowerCase().endsWith(".xml"))) {
					stream.forEach(candidates::add);
				}
			}
			
			if (candidates.isEmpty())
				throw new ADEExtensionException("Failed to find a schema mapping file in '" + SCHEMA_MAPPING_PATH + "'.");

			if (candidates.size() > 1)
				throw new ADEExtensionException("Found multiple schema mapping candidates in '" + SCHEMA_MAPPING_PATH + "'.");
			
			schemaMappingFile = candidates.get(0);			
		} catch (IOException e) {
			throw new ADEExtensionException("Failed to find the schema mapping file of the ADE extension.", e);
		}
	}

}
