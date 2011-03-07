package de.tub.citydb.config.project;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class ProjectSchemaWriter extends SchemaOutputResolver {
	private File file;
	
	public ProjectSchemaWriter(File file) {
		this.file = file;
	}
	
	@Override
	public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
		StreamResult res = new StreamResult(file);
		res.setSystemId(file.toString());
		return res;
	}

}
