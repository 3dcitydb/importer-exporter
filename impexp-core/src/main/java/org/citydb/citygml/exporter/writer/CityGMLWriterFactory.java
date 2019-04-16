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
package org.citydb.citygml.exporter.writer;

import org.citydb.ade.model.module.CityDBADE100Module;
import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.config.Config;
import org.citydb.config.project.exporter.CityGMLOptions;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
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
import java.io.Writer;
import java.util.List;

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
		cityGMLOptions = config.getProject().getExporter().getCityGMLOptions();

		// require sequential writing if a sorting clause is defined for the query
		useSequentialWriting = query.isSetSorting();

		// build XSLT transformer chain
		if (config.getProject().getExporter().getXSLTransformation().isEnabled()
				&& config.getProject().getExporter().getXSLTransformation().isSetStylesheets()) {
			try {
				log.info("Applying XSL transformations on export data.");

				List<String> stylesheets = config.getProject().getExporter().getXSLTransformation().getStylesheets();
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
	public FeatureWriter createFeatureWriter(Writer writer) {
		SAXWriter saxWriter = new SAXWriter();

		// prepare SAX writer
		saxWriter.setWriteEncoding(true);
		saxWriter.setIndentString("  ");
		saxWriter.setHeaderComment("Written by " + this.getClass().getPackage().getImplementationTitle() + ", version \"" +
						this.getClass().getPackage().getImplementationVersion() + '"',
				this.getClass().getPackage().getImplementationVendor());

		ModuleContext moduleContext = new ModuleContext(version);

		// add default prefixes and schema locations
		Module core = moduleContext.getModule(CityGMLModuleType.CORE);
		String prefix = cityGMLOptions.getPrefix(core.getNamespaceURI());
		String schemaLocation = cityGMLOptions.getSchemaLocation(core.getNamespaceURI());
		saxWriter.setPrefix(prefix != null ? prefix : XMLConstants.DEFAULT_NS_PREFIX, core.getNamespaceURI());
		if (schemaLocation != null)
			saxWriter.setSchemaLocation(core.getNamespaceURI(), schemaLocation);

		Module generics = moduleContext.getModule(CityGMLModuleType.GENERICS);
		saxWriter.setPrefix(getPrefix(generics), generics.getNamespaceURI());
		saxWriter.setSchemaLocation(generics.getNamespaceURI(), getSchemaLocation(generics));
		if (config.getProject().getExporter().getAppearances().isSetExportAppearance()) {
			Module appearance = moduleContext.getModule(CityGMLModuleType.APPEARANCE);
			saxWriter.setPrefix(getPrefix(appearance), appearance.getNamespaceURI());
			saxWriter.setSchemaLocation(appearance.getNamespaceURI(), getSchemaLocation(appearance));
		}

		// add XML prefixes and schema locations for non-CityGML modules
		for (Module module : moduleContext.getModules()) {
			if (!(module instanceof CityGMLModule)) {
				// skip 3DCityDB ADE prefix and namespace if metadata shall not be exported
				if ((module == CityDBADE200Module.v3_0 || module == CityDBADE100Module.v3_0)
						&& !config.getProject().getExporter().getContinuation().isExportCityDBMetadata())
					continue;

				saxWriter.setPrefix(getPrefix(module), module.getNamespaceURI());
				if (module instanceof ADEModule)
					saxWriter.setSchemaLocation(module.getNamespaceURI(), getSchemaLocation(module));
			}
		}

		// set XML prefixes and schema locations for selected feature types
		if (setAllCityGMLPrefixes) {
			for (CityGMLModule module : version.getCityGMLModules()) {
				saxWriter.setPrefix(getPrefix(module), module.getNamespaceURI());
				saxWriter.setSchemaLocation(module.getNamespaceURI(), getSchemaLocation(module));
			}
		} else {
			for (FeatureType featureType : featureTypeFilter.getFeatureTypes()) {
				if (featureType.isAvailableForCityGML(version)) {
					CityGMLModule module = Modules.getCityGMLModule(featureType.getSchema().getNamespace(version).getURI());
					if (module != null) {
						saxWriter.setPrefix(getPrefix(module), module.getNamespaceURI());
						saxWriter.setSchemaLocation(module.getNamespaceURI(), getSchemaLocation(module));
					}
				}
			}
		}

		// set writer as output for SAXWriter
		saxWriter.setOutput(writer);

		// create CityGML writer
		return new CityGMLWriter(saxWriter, version, transformerChainFactory, useSequentialWriting);
	}

	private String getPrefix(Module module) {
		String prefix = cityGMLOptions.getPrefix(module.getNamespaceURI());
		return prefix != null ? prefix : module.getNamespacePrefix();
	}

	private String getSchemaLocation(Module module) {
		String schemaLocation = cityGMLOptions.getSchemaLocation(module.getNamespaceURI());
		return schemaLocation != null ? schemaLocation : module.getSchemaLocation();
	}
}
