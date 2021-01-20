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
package org.citydb.citygml.exporter.writer.citygml;

import org.citydb.ade.model.module.CityDBADE100Module;
import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.citygml.exporter.writer.FeatureWriter;
import org.citydb.citygml.exporter.writer.FeatureWriterFactory;
import org.citydb.config.Config;
import org.citydb.config.project.exporter.CityGMLOptions;
import org.citydb.config.project.exporter.Namespace;
import org.citydb.config.project.exporter.NamespaceMode;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.file.FileType;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.ade.ADEModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CityObjectGroupModule;
import org.citygml4j.util.internal.xml.TransformerChainFactory;
import org.citygml4j.util.xml.SAXWriter;

import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CityGMLWriterFactory implements FeatureWriterFactory {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final CityGMLVersion version;
	private final FeatureTypeFilter featureTypeFilter;

	private TransformerChainFactory transformerChainFactory;
	private CityGMLOptions cityGMLOptions;
	private boolean setAllCityGMLPrefixes;
	private boolean useSequentialWriting;

	public CityGMLWriterFactory(Query query, SchemaMapping schemaMapping, Config config) throws FeatureWriteException {
		this.config = config;

		version = query.getTargetVersion();
		featureTypeFilter = query.getFeatureTypeFilter();
		cityGMLOptions = config.getExportConfig().getCityGMLOptions();

		// require sequential writing if a sorting clause is defined for the query
		useSequentialWriting = query.isSetSorting();

		// build XSLT transformer chain
		if (config.getExportConfig().getXSLTransformation().isEnabled()
				&& config.getExportConfig().getXSLTransformation().isSetStylesheets()) {
			try {
				log.info("Applying XSL transformations on export data.");

				List<String> stylesheets = config.getExportConfig().getXSLTransformation().getStylesheets();
				SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
				Templates[] templates = new Templates[stylesheets.size()];

				for (int i = 0; i < stylesheets.size(); i++) {
					Templates template = factory.newTemplates(new StreamSource(new File(stylesheets.get(i))));
					templates[i] = template;
				}

				transformerChainFactory = new TransformerChainFactory(templates);
			} catch (TransformerConfigurationException e) {
				throw new FeatureWriteException("Failed to configure the XSL transformation.", e);
			}
		}

		// if only city object groups shall be exported, then we must set all CityGML prefixes
		// and schema locations because also the group members will be exported
		if (featureTypeFilter.size() == 1) {
			FeatureType cityObjectGroupType = schemaMapping.getFeatureType("CityObjectGroup", CityObjectGroupModule.v2_0_0.getNamespaceURI());
			setAllCityGMLPrefixes = featureTypeFilter.getFeatureTypes().get(0).isEqualToOrSubTypeOf(cityObjectGroupType);
		}
	}

	@Override
	public FeatureWriter createFeatureWriter(OutputStream outputStream, FileType fileType) throws FeatureWriteException {
		// prepare SAX writer
		SAXWriter saxWriter;
		try {
			saxWriter = new SAXWriter(outputStream, config.getExportConfig().getCityGMLOptions().getFileEncoding());
			saxWriter.setWriteEncoding(true);
			saxWriter.setIndentString(fileType == FileType.REGULAR ? "  " : "");
		} catch (IOException e) {
			throw new FeatureWriteException("Failed to create CityGML writer.", e);
		}

		if (cityGMLOptions.isWriteProductHeader()) {
			saxWriter.setHeaderComment("Written by " + this.getClass().getPackage().getImplementationTitle() + ", version \"" +
							this.getClass().getPackage().getImplementationVersion() + '"',
					this.getClass().getPackage().getImplementationVendor());
		}

		ModuleContext moduleContext = new ModuleContext(version);
		Map<String, Namespace> namespaces = getNamespaces(moduleContext);

		// add default prefixes and schema locations
		Namespace core = namespaces.get(moduleContext.getModule(CityGMLModuleType.CORE).getNamespaceURI());
		if (core.getMode() != NamespaceMode.SKIP) {
			saxWriter.setPrefix(core.getPrefix(), core.getURI());
			if (core.isSetSchemaLocation())
				saxWriter.setSchemaLocation(core.getURI(), core.getSchemaLocation());
		}

		Namespace generics = namespaces.get(moduleContext.getModule(CityGMLModuleType.GENERICS).getNamespaceURI());
		if (generics.getMode() != NamespaceMode.SKIP) {
			saxWriter.setPrefix(generics.getPrefix(), generics.getURI());
			saxWriter.setSchemaLocation(generics.getURI(), generics.getSchemaLocation());
		}

		Namespace appearance = namespaces.get(moduleContext.getModule(CityGMLModuleType.APPEARANCE).getNamespaceURI());
		if (appearance.getMode() != NamespaceMode.SKIP && config.getExportConfig().getAppearances().isSetExportAppearance()) {
			saxWriter.setPrefix(appearance.getPrefix(), appearance.getURI());
			saxWriter.setSchemaLocation(appearance.getURI(), appearance.getSchemaLocation());
		}

		// add XML prefixes and schema locations for non-CityGML modules
		for (Module module : moduleContext.getModules()) {
			if (!(module instanceof CityGMLModule)) {
				// skip 3DCityDB ADE prefix and namespace if metadata shall not be exported
				if ((module == CityDBADE200Module.v3_0 || module == CityDBADE100Module.v3_0)
						&& !config.getExportConfig().getContinuation().isExportCityDBMetadata())
					continue;

				Namespace namespace = namespaces.get(module.getNamespaceURI());
				if (namespace.getMode() != NamespaceMode.SKIP) {
					saxWriter.setPrefix(namespace.getPrefix(), namespace.getURI());
					if (module instanceof ADEModule)
						saxWriter.setSchemaLocation(namespace.getURI(), namespace.getSchemaLocation());
				}
			}
		}

		// set XML prefixes and schema locations for selected feature types
		List<CityGMLModule> modules;
		if (setAllCityGMLPrefixes)
			modules = version.getCityGMLModules();
		else {
			modules = new ArrayList<>();
			for (FeatureType featureType : featureTypeFilter.getFeatureTypes()) {
				if (featureType.isAvailableForCityGML(version)) {
					CityGMLModule module = Modules.getCityGMLModule(featureType.getSchema().getNamespace(version).getURI());
					if (module != null)
						modules.add(module);
				}
			}
		}

		for (CityGMLModule module : modules) {
			Namespace namespace = namespaces.get(module.getNamespaceURI());
			if (namespace.getMode() != NamespaceMode.SKIP) {
				saxWriter.setPrefix(namespace.getPrefix(), namespace.getURI());
				saxWriter.setSchemaLocation(namespace.getURI(), namespace.getSchemaLocation());
			}
		}

		// force namespace prefixes
		for (Namespace namespace : namespaces.values()) {
			if (namespace.getMode() == NamespaceMode.FORCE) {
				saxWriter.setPrefix(namespace.getPrefix(), namespace.getURI());
				saxWriter.setSchemaLocation(namespace.getURI(), namespace.getSchemaLocation());
			}
		}

		// create CityGML writer
		return new CityGMLWriter(saxWriter, version, transformerChainFactory, useSequentialWriting);
	}

	private Map<String, Namespace> getNamespaces(ModuleContext moduleContext) {
		Map<String, Namespace> namespaces = cityGMLOptions.isSetNamespaces() ? new LinkedHashMap<>(cityGMLOptions.getNamespaces()) : new LinkedHashMap<>();
		for (Module module : moduleContext.getModules()) {
			Namespace namespace = namespaces.get(module.getNamespaceURI());
			if (namespace == null) {
				namespace = new Namespace();
				namespace.setURI(module.getNamespaceURI());
				namespace.setSchemaLocation(module.getSchemaLocation());
				namespace.setPrefix(module.getType() != CityGMLModuleType.CORE ? module.getNamespacePrefix() : XMLConstants.DEFAULT_NS_PREFIX);
			}

			namespaces.put(namespace.getURI(), namespace);
		}

		return namespaces;
	}
}
