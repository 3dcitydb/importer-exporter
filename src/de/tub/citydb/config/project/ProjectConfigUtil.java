package de.tub.citydb.config.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class ProjectConfigUtil {

	public static void marshal(Project project, String fileName, JAXBContext ctx) throws JAXBException {
		Marshaller m = ctx.createMarshaller();
		
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
		m.setProperty("com.sun.xml.bind.indentString", "  ");

		m.marshal(project, new File(fileName));
	}
	
	public static Project unmarshal(String fileName, JAXBContext ctx) throws JAXBException, FileNotFoundException {
		Unmarshaller um = ctx.createUnmarshaller();
		return (Project)um.unmarshal(new FileInputStream(fileName));
	}
	
	public static void generateSchema(JAXBContext ctx, File file) throws IOException {
		ctx.generateSchema(new ProjectSchemaWriter(file));
	}
}
