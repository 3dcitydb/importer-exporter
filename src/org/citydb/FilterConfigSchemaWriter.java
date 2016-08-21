package org.citydb;


import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.citydb.config.project.exporter.ExportFilter;

public class FilterConfigSchemaWriter {

	public static void main(String[] args) throws Exception {
		System.out.print("Generting XML schema in config ... ");
		
		JAXBContext ctx = JAXBContext.newInstance(ExportFilter.class);
		ctx.generateSchema(new SchemaOutputResolver() {
			
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				File file;

				if (namespaceUri.equals("http://www.3dcitydb.org/importer-exporter/config"))
					file = new File("config/config.xsd");
				else
					file = new File("config" + "/plugins/" + suggestedFileName);
				
				file.getAbsoluteFile().getParentFile().mkdirs();
				
				StreamResult res = new StreamResult();
				res.setSystemId(file.toURI().toString());
				return res;
			}
			
		});
		
		System.out.println("finished.");
	}

}
