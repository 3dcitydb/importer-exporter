/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.citygml.exporter.writer;

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
import org.citygml4j.util.internal.xml.TransformerChain;
import org.citygml4j.util.internal.xml.TransformerChainFactory;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;

public class CityGMLWriter implements FeatureWriter {
	private final SingleWorkerPool<SAXEventBuffer> writerPool;
	private final SAXWriter saxWriter;
	private final CityGMLBuilder cityGMLBuilder;
	private final JAXBMarshaller jaxbMarshaller;
	private final CityGMLVersion version;
	private final TransformerChainFactory transformerChainFactory;

	protected CityGMLWriter(SAXWriter saxWriter, CityGMLVersion version, TransformerChainFactory transformerChainFactory) {
		this.saxWriter = saxWriter;
		this.version = version;
		this.transformerChainFactory = transformerChainFactory;

		cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();
		jaxbMarshaller = cityGMLBuilder.createJAXBMarshaller(version);

		writerPool = new SingleWorkerPool<>(
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

		try {
			SAXEventBuffer buffer = new SAXEventBuffer();

			JAXBElement<?> jaxbElement = jaxbMarshaller.marshalJAXBElement(member);
			if (jaxbElement != null) {
				Marshaller marshaller = cityGMLBuilder.getJAXBContext().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

				if (transformerChainFactory == null)
					marshaller.marshal(jaxbElement, buffer);
				else {
					TransformerChain chain = transformerChainFactory.buildChain();
					chain.tail().setResult(new SAXResult(buffer));
					chain.head().startDocument();
					marshaller.marshal(jaxbElement, chain.head());
					chain.head().endDocument();
				}
			}

			if (!buffer.isEmpty())
				writerPool.addWork(buffer);
		} catch (JAXBException | SAXException | TransformerConfigurationException e) {
			throw new FeatureWriteException("Failed to write CityGML feature.", e);
		}
	}

	@Override
	public void close() throws FeatureWriteException {			
		try {
			writerPool.shutdownAndWait();
			writeEndDocument();
			saxWriter.getOutputWriter().close();
		} catch (Throwable e) {
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

				if (transformerChainFactory == null)
					marshaller.marshal(jaxbElement, fragmentWriter);
				else {
					TransformerChain chain = transformerChainFactory.buildChain();
					chain.tail().setResult(new SAXResult(fragmentWriter));
					marshaller.marshal(jaxbElement, chain.head());
				}
			}
		} catch (JAXBException | TransformerConfigurationException e) {
			throw new FeatureWriteException("Failed to write CityGML document header.", e);
		}
	}

}
