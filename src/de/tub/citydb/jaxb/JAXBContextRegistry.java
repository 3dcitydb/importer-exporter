package de.tub.citydb.jaxb;

import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBContextRegistry {
	private static HashMap<String, JAXBContext> contextMap = new HashMap<String, JAXBContext>();

	private JAXBContextRegistry() {
		// just to thwart instantiation
	}

	public static synchronized JAXBContext getInstance(String contextPath) throws JAXBException {
		JAXBContext context = contextMap.get(contextPath);

		if (context == null) {
			context = JAXBContext.newInstance(contextPath, Thread.currentThread().getContextClassLoader());
			contextMap.put(contextPath, context);
		}

		return context;
	}
	
	public static synchronized JAXBContext registerInstance(String contextPath, JAXBContext context) {
		JAXBContext value = contextMap.get(contextPath);
		
		if (value == null) {
			contextMap.put(contextPath, context);
			value = context;
		}
		
		return value;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
