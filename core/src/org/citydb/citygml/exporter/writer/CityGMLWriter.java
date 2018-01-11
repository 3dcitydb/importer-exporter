package org.citydb.citygml.exporter.writer;

import java.io.IOException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.citydb.concurrent.SingleWorkerPool;
import org.citydb.registry.ObjectRegistry;
import org.citydb.writer.XMLWriterWorkerFactory;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.FeatureMember;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.citygml4j.util.xml.SAXWriter;

public class CityGMLWriter implements FeatureWriter {
	private final SingleWorkerPool<SAXEventBuffer> writerPool;
	private final SAXWriter saxWriter;
	private final CityGMLBuilder cityGMLBuilder;
	private final JAXBMarshaller jaxbMarshaller;
	private final CityGMLVersion version;

	public CityGMLWriter(SAXWriter saxWriter, CityGMLVersion version) {
		this.saxWriter = saxWriter;
		this.version = version;
		
		cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();
		jaxbMarshaller = cityGMLBuilder.createJAXBMarshaller(version);

		writerPool = new SingleWorkerPool<SAXEventBuffer>(
				"citygml_writer_pool",
				new XMLWriterWorkerFactory(saxWriter, ObjectRegistry.getInstance().getEventDispatcher()),
				100,
				false);

		writerPool.prestartCoreWorkers();
	}

	protected void writeStartDocument() throws FeatureWriteException {
		writeCityModel(WriteMode.HEAD);
	}
	
	protected void writeEndDocument() throws FeatureWriteException {
		writeCityModel(WriteMode.TAIL);
	}

	@Override
	public void write(AbstractFeature feature) throws FeatureWriteException {
		FeatureProperty<? extends AbstractFeature> member = null;

		// wrap feature with a feature property element
		if (feature instanceof AbstractCityObject) {
			member = new CityObjectMember();
			((CityObjectMember)member).setCityObject((AbstractCityObject)feature);
		} 

		else if (feature instanceof Appearance) {
			member = new AppearanceMember();
			((AppearanceMember)member).setAppearance((Appearance)feature);
		} 

		else {
			member = new FeatureMember();
			((FeatureMember)member).setFeature(feature);
		}

		if (member != null) {
			try {
				SAXEventBuffer buffer = new SAXEventBuffer();
				Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

				JAXBElement<?> jaxbElement = jaxbMarshaller.marshalJAXBElement(member);
				if (jaxbElement != null)
					marshaller.marshal(jaxbElement, buffer);

				if (!buffer.isEmpty())
					writerPool.addWork(buffer);
			} catch (JAXBException e) {
				throw new FeatureWriteException("Failed to write CityGML feature.", e);
			}
		}
	}

	@Override
	public void close() throws FeatureWriteException {			
		try {
			writerPool.shutdownAndWait();
			writeEndDocument();
			saxWriter.getOutputWriter().close();
		} catch (IOException | InterruptedException e) {			
			throw new FeatureWriteException("Failed to close CityGML writer.", e);
		} finally {
			if (!writerPool.isTerminated())
				writerPool.shutdownNow();
		}
	}
	
	private void writeCityModel(WriteMode mode) throws FeatureWriteException {
		try {
			SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(
					new QName(version.getCityGMLModule(CityGMLModuleType.CORE).getNamespaceURI(), "CityModel"), 
					saxWriter, 
					mode);

			JAXBElement<?> jaxbElement = jaxbMarshaller.marshalJAXBElement(new CityModel());
			if (jaxbElement != null) {
				Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
				marshaller.marshal(jaxbElement, fragmentWriter);
			}
		} catch (JAXBException e) {
			throw new FeatureWriteException("Failed to write CityGML document header.", e);
		}
	}

}
