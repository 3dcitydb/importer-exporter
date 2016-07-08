/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
