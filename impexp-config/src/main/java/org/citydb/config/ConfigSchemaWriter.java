package org.citydb.config;

import org.citydb.config.project.Project;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigSchemaWriter {

	public static void main(String[] args) throws Exception {
		Path configFile = Paths.get("src/main/resources/org/citydb/config/schema/project.xsd");
		System.out.print("Generting XML schema in " + configFile.toAbsolutePath() + "... ");
		
		JAXBContext context = JAXBContext.newInstance(Project.class);
		context.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				if (ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI.equals(namespaceUri)) {
					Files.createDirectories(configFile.getParent());
					StreamResult res = new StreamResult();
					res.setSystemId(configFile.toUri().toString());

					return res;
				} else
					return null;
			}
			
		});
		
		System.out.println("finished.");
	}

}
