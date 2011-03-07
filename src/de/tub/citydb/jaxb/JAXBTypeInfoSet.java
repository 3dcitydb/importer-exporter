package de.tub.citydb.jaxb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;

public class JAXBTypeInfoSet {
	private static HashMap<String, JAXBTypeInfoSet> typeInfoSetMap = new HashMap<String, JAXBTypeInfoSet>();
	private TypeInfoSet<Type, Class, Field, Method> typeInfoSet;

	private JAXBTypeInfoSet() {
		// just to thwart instantiation
	}

	public static synchronized JAXBTypeInfoSet getInstance(String contextPath) throws JAXBException {
		JAXBTypeInfoSet jaxbTypeInfoSet = typeInfoSetMap.get(contextPath);

		if (jaxbTypeInfoSet == null) {
			jaxbTypeInfoSet = new JAXBTypeInfoSet();
			StringTokenizer packages = new StringTokenizer(contextPath, ":");
			StringBuilder objectFactoryClass;
			Vector<Class> objectFactories = new Vector<Class>();

			if (!packages.hasMoreTokens())
				throw new JAXBException("no packages in context path");

			while (packages.hasMoreTokens()) {
				String packageName = packages.nextToken();
				objectFactoryClass = new StringBuilder().append(packageName).append(".ObjectFactory");

				try {
					objectFactories.add(Class.forName(objectFactoryClass.toString()));
				} catch (ClassNotFoundException e) {
					throw new JAXBException(e);
				}
			}

			if (objectFactories.isEmpty())
				throw new JAXBException("no ObjectFactory classes found in context path");

			ModelBuilder modelBuilder = new ModelBuilder<Type, Class, Field, Method>(
				new RuntimeInlineAnnotationReader(), Navigator.REFLECTION,
				Collections.<Class, Class>emptyMap(), null);

			IllegalAnnotationsException.Builder errorHandler = new IllegalAnnotationsException.Builder();
			modelBuilder.setErrorHandler(errorHandler);

			for (Class objectFactory : objectFactories) {
				modelBuilder.getTypeInfo(new Ref<Type, Class>(Navigator.REFLECTION.use(objectFactory)));
			}

			errorHandler.check();
			jaxbTypeInfoSet.typeInfoSet = modelBuilder.link();
			typeInfoSetMap.put(contextPath, jaxbTypeInfoSet);
		}

		return jaxbTypeInfoSet;
	}

	public String getElementClass(QName elementName) {
		ElementInfo<Type, Class> elementInfo = typeInfoSet.getElementInfo((Class) null, elementName);
		Type classType = elementInfo.getContentInMemoryType();

		if (classType == null)
			return null;

		return (classType.toString().split(" "))[1];
	}
}
