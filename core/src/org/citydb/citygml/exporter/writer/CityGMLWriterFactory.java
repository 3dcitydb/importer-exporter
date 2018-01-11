package org.citydb.citygml.exporter.writer;

import java.io.Writer;

import org.citydb.config.Config;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.query.Query;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.ade.ADEModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXWriter;

public class CityGMLWriterFactory implements FeatureWriterFactory {
	private final CityGMLVersion version;

	private SAXWriter saxWriter;

	public CityGMLWriterFactory(Query query, Config config) {
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
		CityGMLWriter featureWriter = new CityGMLWriter(saxWriter, version);
		featureWriter.writeStartDocument();

		return featureWriter;
	}

}
