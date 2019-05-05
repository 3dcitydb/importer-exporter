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

import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.Metadata;
import org.citydb.database.schema.mapping.Namespace;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SchemaMappingException;
import org.citydb.database.schema.mapping.SchemaMappingValidationException;
import org.citydb.database.schema.util.SchemaMappingUtil;
import org.citydb.log.Logger;
import org.citygml4j.model.citygml.ade.binding.ADEContext;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.module.ade.ADEModule;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

public class ADEExtensionManager {
	private static ADEExtensionManager instance;
	private HashMap<String, ADEExtension> extensions = new HashMap<>();
	private HashMap<Integer, ADEExtension> extensionsByObjectClassIds = new HashMap<>();
	private HashMap<String, ADEExtension> extensionsByTablePrefix = new HashMap<>();
	private HashMap<String, List<ADEExtensionException>> exceptions;
	private MessageDigest md5;

	private ADEExtensionManager() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			//
		}
	}

	public static synchronized ADEExtensionManager getInstance() {
		if (instance == null)
			instance = new ADEExtensionManager();

		return instance;
	}

	public void loadExtensions(ClassLoader loader) {
		ServiceLoader<ADEExtension> adeLoader = ServiceLoader.load(ADEExtension.class, loader);
		for (ADEExtension adeExtension : adeLoader) {
			boolean isLoaded = false;
			for (ADEExtension tmp : extensions.values()) {
				if (tmp.getClass() == adeExtension.getClass()) {
					isLoaded = true;
					break;
				}
			}

			if (!isLoaded)
				loadExtension(adeExtension);
		}
	}

	public void loadExtension(ADEExtension extension) {
		try {
			// determine base path
			if (extension.getBasePath() == null)
				extension.setBasePath(findBaseLocation(extension));

			// validate extension
			extension.validate();

			// create and set unqiue id
			String id = createMD5Fingerprint(extension.getSchemaMappingFile());
			extension.setId(id);

			ADEExtension previous = extensions.put(id, extension);
			if (previous != null) {
				addException(extension, new ADEExtensionException("The ADE extension " + previous.getClass().getName() + " shares the same unique ID."));
				addException(previous, new ADEExtensionException("The ADE extension " + previous.getClass().getName() + " shares the same unique ID."));					
				extensions.remove(id);
			}

		} catch (ADEExtensionException e) {
			addException(extension, e);
		}  catch (Throwable e) {
			addException(extension, new ADEExtensionException("Unexpected error while loading the ADE extension.", e));
		}
	}

	public void loadSchemaMappings(SchemaMapping schemaMapping) {
		for (Iterator<Entry<String, ADEExtension>> iter = extensions.entrySet().iterator(); iter.hasNext(); ) {
			ADEExtension extension = iter.next().getValue();
			Path schemaMappingFile = extension.getSchemaMappingFile();

			try {
				// load schema mapping
				SchemaMapping extensionMapping = SchemaMappingUtil.getInstance().unmarshal(schemaMapping, schemaMappingFile.toFile());

				// register objectclass ids
				for (AbstractObjectType<?> type : extensionMapping.getAbstractObjectTypes())
					extensionsByObjectClassIds.put(type.getObjectClassId(), extension);

				// set metadata
				if (extensionMapping.isSetMetadata()) {
					Metadata metadata = extensionMapping.getMetadata();
					extension.setMetadata(metadata);
					extensionsByTablePrefix.put(metadata.getDBPrefix().toLowerCase(), extension);
				} else
					throw new SchemaMappingException("The schema mapping does not provide metadata.");
				
				// set schemas
				extension.setSchemas(extensionMapping.getSchemas());

				// assign xml prefix
				for (AppSchema schema : extensionMapping.getSchemas()) {
					for (ADEContext context : extension.getADEContexts()) {
						for (ADEModule module : context.getADEModules()) {
							if (schema.matchesNamespaceURI(module.getNamespaceURI())) {
								if (schema.isGeneratedXMLPrefix())
									schema.setXMLPrefix(module.getNamespacePrefix());
								else if (schema.isSetXMLPrefix() && !module.getNamespacePrefix().equals(schema.getXMLPrefix()))
									module.setNamespacePrefix(schema.getXMLPrefix());
							}
						}
					}
				}
				
				// initialize ADE extension
				extension.init(extensionMapping);

				// merge with 3DCityDB schema mapping
				schemaMapping.merge(extensionMapping);

			} catch (ADEExtensionException e) {
				addException(extension, e);
				iter.remove();
			} catch (SchemaMappingException | SchemaMappingValidationException | JAXBException e) {
				addException(extension, new ADEExtensionException("Failed to load schema mapping from URL '" + schemaMappingFile + "'.", e));
				iter.remove();
			} catch (Throwable e) {
				addException(extension, new ADEExtensionException("Unexpected error while loading the ADE extension.", e));
				iter.remove();
			}
		}
	}

	public List<ADEContext> getADEContexts() {
		List<ADEContext> contexts = new ArrayList<>();
		for (ADEExtension extension : extensions.values())
			contexts.addAll(extension.getADEContexts());

		return contexts;
	}

	public List<ADEExtension> getExtensions() {
		return new ArrayList<>(extensions.values());
	}

	public List<ADEExtension> getEnabledExtensions() {
		return getExtensions(true);
	}

	public List<ADEExtension> getDisabledExtensions() {
		return getExtensions(false);
	}

	public List<AppSchema> getDisabledSchemas(SchemaMapping schemaMapping) {
		List<AppSchema> disabled = new ArrayList<>();
		for (AppSchema schema : schemaMapping.getSchemas()) {
			ADEExtension extension = getExtensionBySchema(schema);
			if (extension != null && !extension.isEnabled())
				disabled.add(schema);
		}

		return disabled;
	}

	private List<ADEExtension> getExtensions(boolean filter) {
		List<ADEExtension> result = new ArrayList<>();
		for (ADEExtension extension : extensions.values()) {
			if (extension.isEnabled() == filter)
				result.add(extension);
		}

		return result;
	}

	public ADEExtension getExtensionById(String id) {
		return extensions.get(id);
	}

	public ADEExtension getExtensionByObject(Class<? extends ADEModelObject> adeObjectClass) {
		for (ADEExtension extension : extensions.values()) {
			for (ADEContext adeContext : extension.getADEContexts()) {
				if (adeContext.getModelPackageNames().contains(adeObjectClass.getPackage().getName()))
					return extension;
			}
		}

		return null;
	}

	public ADEExtension getExtensionByObject(ADEModelObject adeObject) {
		return getExtensionByObject(adeObject.getClass());
	}

	public ADEExtension getExtensionByObjectClassId(int objectClassId) {
		return extensionsByObjectClassIds.get(objectClassId);
	}

	public ADEExtension getExtensionBySchema(AppSchema schema) {
		for (Namespace namespace : schema.getNamespaces()) {
			ADEExtension extension = getExtensionByURI(namespace.getURI());
			if (extension != null)
				return extension;
		}

		return null;
	}

	public ADEExtension getExtensionByURI(String namespaceURI) {
		for (ADEExtension extension : extensions.values()) {
			for (ADEContext adeContext : extension.getADEContexts()) {
				for (ADEModule adeModule : adeContext.getADEModules()) {
					if (adeModule.getNamespaceURI().equals(namespaceURI))
						return extension;
				}
			}
		}

		return null;
	}

	public ADEExtension getExtensionByTableName(String tableName) {
		int index = tableName.indexOf('_');
		if (index > 0) {
			String prefix = tableName.substring(0, index).toLowerCase();
			return extensionsByTablePrefix.get(prefix);
		}

		return null;
	}

	public boolean hasExceptions() {
		return exceptions != null;
	}

	public void logExceptions() {
		if (exceptions != null) {
			Logger log = Logger.getInstance();
			
			for (Entry<String, List<ADEExtensionException>> entry : exceptions.entrySet()) {
				log.error("Failed to initialize the ADE extension " + entry.getKey());
				for (ADEExtensionException e : entry.getValue()) {
					log.error("Cause: " + e.getMessage());
					Throwable cause = e;
					while ((cause = cause.getCause()) != null)
						log.error(cause.getClass().getTypeName() + ": " + cause.getMessage());
				}
			}
		}
	}
	
	public Map<String, List<ADEExtensionException>> getExceptions() {
		return exceptions;
	}

	private void addException(ADEExtension extension, ADEExtensionException exception) {
		if (this.exceptions == null)
			this.exceptions = new HashMap<>();

		List<ADEExtensionException> exceptions = this.exceptions.computeIfAbsent(extension.getClass().getName(), k -> new ArrayList<>());
		exceptions.add(exception);
	}

	private Path findBaseLocation(ADEExtension extension) throws ADEExtensionException {
		Class<?> c = extension.getClass();
		Path basePath = null;

		// find path to jar location
		try {
			basePath = Paths.get(c.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
		} catch (SecurityException | URISyntaxException | NullPointerException e) {
			//
		}

		if (basePath == null) {
			try {
				String url = c.getResource(c.getSimpleName() + ".class").toString();
				String suffix = c.getCanonicalName().replace('.', '/') + ".class";
				if (url.endsWith(suffix)) {
					String location = url.substring(0, url.length() - suffix.length());
					if (location.startsWith("jar:")) {
						int index = location.indexOf("!/");
						location = location.substring(4, (index > 0 ? index : location.length()));
					}

					basePath = Paths.get(new URL(location).toURI()).getParent();
				}
			} catch (MalformedURLException | URISyntaxException | NullPointerException e) {
				//
			}
		}

		// find base folder
		if (basePath != null) {
			do {
				if (basePath.toString().endsWith(ADEExtension.LIB_PATH)) {
					basePath = basePath.getParent();
					break;
				}
			} while ((basePath = basePath.getParent()) != null);
		}

		if (basePath == null)
			throw new ADEExtensionException("Failed to determine the base path of the ADE extension.");

		return basePath;
	}

	private String createMD5Fingerprint(Path schemaMappingFile) throws ADEExtensionException {
		try (InputStream stream = Files.newInputStream(schemaMappingFile); 
				ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			int nRead;
			byte[] data = new byte[4096];
			while ((nRead = stream.read(data, 0, data.length)) != -1)
				buffer.write(data, 0, nRead);

			buffer.flush();			
			byte[] hash = md5.digest(buffer.toByteArray());

			StringBuilder hex = new StringBuilder();
			for (byte aHash : hash)
				hex.append(Integer.toString((aHash & 0xff) + 0x100, 16).substring(1));

			return hex.toString();
		} catch (IOException e) {
			throw new ADEExtensionException("Failed to create a unique ID.", e);
		}
	}

}
