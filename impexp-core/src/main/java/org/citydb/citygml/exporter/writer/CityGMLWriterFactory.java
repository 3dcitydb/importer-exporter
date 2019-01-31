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
import org.citydb.database.schema.mapping.FeatureType;
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
import org.citygml4j.util.internal.xml.TransformerChainFactory;
import org.citygml4j.util.xml.SAXWriter;

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

	public CityGMLWriterFactory(Query query, Config config) throws FeatureWriteException {
		this.config = config;

		version = query.getTargetVersion();
		featureTypeFilter = query.getFeatureTypeFilter();

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
		saxWriter.setDefaultNamespace(moduleContext.getModule(CityGMLModuleType.CORE).getNamespaceURI());

		// add default prefixes and schema locations
		Module generics = moduleContext.getModule(CityGMLModuleType.GENERICS);
		saxWriter.setPrefix(generics.getNamespacePrefix(), generics.getNamespaceURI());
		saxWriter.setSchemaLocation(generics.getNamespaceURI(), generics.getSchemaLocation());
		if (config.getProject().getExporter().getAppearances().isSetExportAppearance()) {
			Module appearance = moduleContext.getModule(CityGMLModuleType.APPEARANCE);
			saxWriter.setPrefix(appearance.getNamespacePrefix(), appearance.getNamespaceURI());
			saxWriter.setSchemaLocation(appearance.getNamespaceURI(), appearance.getSchemaLocation());
		}

		// add XML prefixes and schema locations for non-CityGML modules
		for (Module module : moduleContext.getModules()) {
			if (!(module instanceof CityGMLModule)) {
				// skip 3DCityDB ADE prefix and namespace if metadata shall not be exported
				if ((module == CityDBADE200Module.v3_0 || module == CityDBADE100Module.v3_0)
						&& !config.getProject().getExporter().getContinuation().isExportCityDBMetadata())
					continue;

				saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
				if (module instanceof ADEModule)
					saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
			}
		}

		// set XML prefixes and schema locations for selected feature types
		for (FeatureType featureType : featureTypeFilter.getFeatureTypes()) {
			if (featureType.isAvailableForCityGML(version)) {
				CityGMLModule module = Modules.getCityGMLModule(featureType.getSchema().getNamespace(version).getURI());
				if (module != null) {
					saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
					saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
				}
			}
		}

		// set writer as output for SAXWriter
		saxWriter.setOutput(writer);

		// create CityGML writer
		return new CityGMLWriter(saxWriter, version, transformerChainFactory);
	}

}
