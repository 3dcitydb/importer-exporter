package org.citydb.database.schema.util;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.citydb.database.schema.mapping.SchemaMapping;

public class SchemaMappingWriter {

	public static void main(String[] args) throws Exception {
		final File file = new File("resources/jar/resources/3dcitydb/3dcitydb-schema.xsd");		
		System.out.print("Generting XML schema in " + file.getAbsolutePath() + "...");
		
		JAXBContext ctx = JAXBContext.newInstance(SchemaMapping.class);
		ctx.generateSchema(new SchemaOutputResolver() {
			
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {				
				StreamResult res = new StreamResult(file);
				res.setSystemId(file.toURI().toString());
				return res;
			}
			
		});
		
		System.out.println("finished.");
	}

}
