package org.citydb.modules.citygml.exporter.util;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.FeatureMember;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXEventBuffer;

public class FeatureWriter implements FeatureProcessor {
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final JAXBBuilder jaxbBuilder;
	private final JAXBMarshaller jaxbMarshaller;
	
	public FeatureWriter(WorkerPool<SAXEventBuffer> ioWriterPool, JAXBBuilder jaxbBuilder, Config config) {
		this.ioWriterPool = ioWriterPool;
		this.jaxbBuilder = jaxbBuilder;
				
		CityGMLVersion version = Util.toCityGMLVersion(config.getProject().getExporter().getCityGMLVersion());
		jaxbMarshaller = jaxbBuilder.createJAXBMarshaller(version);
	}
	
	@Override
	public void process(AbstractFeature abstractFeature) throws FeatureProcessException {
		FeatureProperty<? extends AbstractFeature> member = null;

		// wrap feature with a feature property element
		if (abstractFeature instanceof AbstractCityObject) {
			member = new CityObjectMember();
			((CityObjectMember)member).setCityObject((AbstractCityObject)abstractFeature);
		} 

		else if (abstractFeature instanceof Appearance) {
			member = new AppearanceMember();
			((AppearanceMember)member).setAppearance((Appearance)abstractFeature);
		} 

		else {
			member = new FeatureMember();
			((FeatureMember)member).setFeature(abstractFeature);
		}

		if (member != null) {
			try {
				SAXEventBuffer buffer = new SAXEventBuffer();
				Marshaller marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

				JAXBElement<?> jaxbElement = jaxbMarshaller.marshalJAXBElement(member);
				if (jaxbElement != null)
					marshaller.marshal(jaxbElement, buffer);

				if (!buffer.isEmpty())
					ioWriterPool.addWork(buffer);
			} catch (JAXBException e) {
				throw new FeatureProcessException("Caused by: ", e);
			}
		}
	}

}
