package org.citydb.citygml.exporter.writer;

import org.citydb.config.Config;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.log.Logger;
import org.citydb.query.Query;
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
	private final CityGMLVersion version;

	private SAXWriter saxWriter;
	private TransformerChainFactory transformerChainFactory;

	public CityGMLWriterFactory(Query query, Config config) throws FeatureWriteException {
		version = query.getTargetVersion();

		// prepare SAX writer
		saxWriter = new SAXWriter();
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
				saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
				if (module instanceof ADEModule)
					saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
			}
		}

		// set XML prefixes and schema locations for selected feature types
		for (FeatureType featureType : query.getFeatureTypeFilter().getFeatureTypes()) {
			if (featureType.isAvailableForCityGML(version)) {
				CityGMLModule module = Modules.getCityGMLModule(featureType.getSchema().getNamespace(version).getURI());
				if (module != null) {
					saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
					saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
				}
			}
		}

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

		// add CityDB ADE namespace and schema location if required
		if (config.getProject().getExporter().getCityDBADE().isExportMetadata()) {
			saxWriter.setPrefix(MappingConstants.CITYDB_ADE_NAMESPACE_PREFIX, MappingConstants.CITYDB_ADE_NAMESPACE_URI);
			saxWriter.setSchemaLocation(MappingConstants.CITYDB_ADE_NAMESPACE_URI, MappingConstants.CITYDB_ADE_SCHEMA_LOCATIONS.get(version));
		}
	}

	@Override
	public FeatureWriter createFeatureWriter(Writer writer) throws FeatureWriteException {
		// set writer as output for SAXWriter
		saxWriter.setOutput(writer);

		// create CityGML writer and write XML header
		CityGMLWriter featureWriter = new CityGMLWriter(saxWriter, version, transformerChainFactory);
		featureWriter.writeStartDocument();

		return featureWriter;
	}

}
