package de.tub.citydb.config.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class GuiConfigUtil {

	public static void marshal(Gui gui, String fileName, JAXBContext ctx) throws JAXBException {
		Marshaller m = ctx.createMarshaller();
		
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
		m.setProperty("com.sun.xml.bind.indentString", "  ");

		m.marshal(gui, new File(fileName));
	}
	
	public static Gui unmarshal(String fileName, JAXBContext ctx) throws JAXBException, FileNotFoundException {
		Unmarshaller um = ctx.createUnmarshaller();
		return (Gui)um.unmarshal(new FileInputStream(fileName));
	}
}
