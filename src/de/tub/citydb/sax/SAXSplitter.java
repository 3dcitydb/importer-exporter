/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.sax;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModule;
import org.citygml4j.model.citygml.CityGMLModuleVersion;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.util.CityGMLModules;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.concurrent.InterruptEnum;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.validation.SchemaLocationEvent;
import de.tub.citydb.filter.FilterMode;
import de.tub.citydb.filter.statistic.FeatureCounterFilter;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.events.StartElement;
import de.tub.citydb.util.UUIDManager;
import de.tub.citydb.util.Util;

public class SAXSplitter extends XMLFilterImpl {	
	private final Logger LOG = Logger.getInstance();

	private final WorkerPool<SAXBuffer> workerPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	
	private SAXBuffer saxBuffer;
	private DefaultHandler defaultHandler;
	private Locator locator;

	// context related instance members
	private Stack<ParserContext> contextStack;
	private long depth;
	private CityGMLClass type;
	private boolean bufferEvents;
	private StartElement cityObjectChildElement;

	// instance member used for filtering
	private FeatureCounterFilter counterFilter;
	private long elementCounter;
	private Long firstElement;
	private Long lastElement;

	// XML validation
	private XMLValidation xmlValidation;
	private AtomicBoolean isRootElement = new AtomicBoolean(true);

	public SAXSplitter(WorkerPool<SAXBuffer> threadPool, Config config, EventDispatcher eventDispatcher) {
		this.workerPool = threadPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		defaultHandler = new DefaultHandler();
		contextStack = new Stack<ParserContext>();
		counterFilter = new FeatureCounterFilter(config, FilterMode.IMPORT);
		xmlValidation = config.getProject().getImporter().getXMLValidation();
		saxBuffer = new SAXBuffer(config.getInternal().isUseXMLValidation());

		init();
	}

	private void init() {
		// set initial context
		depth = 0;
		type = CityGMLClass.UNDEFINED;
		bufferEvents = false;
		setContentHandler(defaultHandler);

		// get filter settings
		List<Long> counterFilterState = counterFilter.getFilterState();
		firstElement = counterFilterState.get(0);
		lastElement = counterFilterState.get(1);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {	

		if (isRootElement.compareAndSet(true, false) &&
				config.getInternal().isUseXMLValidation() && 
				!xmlValidation.getUseLocalSchemas().isSet()) 
			handleSchemaLocation(atts);

		boolean isCityGMLElement = uri.equals("http://www.citygml.org/citygml/1/0/0") || uri.startsWith("http://www.opengis.net/citygml/");

		if (isCityGMLElement && localName.equals("cityObjectMember")) {
			super.startElement(uri, localName, qName, atts);
			elementCounter++;

			if ((firstElement != null && elementCounter < firstElement) || 
					(lastElement != null && elementCounter > lastElement)) {
				switchContext(CityGMLClass.CITYOBJECTMEMBER, false);
			} else {
				switchContext(CityGMLClass.CITYOBJECTMEMBER, true);
				fireStartDocument();
			}
		} 

		else if (isCityGMLElement && (localName.equals("groupMember") || 
				localName.equals("parent") || 
				localName.equals("generalizesTo"))) {		
			if (!bufferEvents) {
				switchContext(CityGMLClass.CITYOBJECTGROUP, false);
				return;
			}

			boolean xlink = false;
			for (int i = 0; i < atts.getLength(); i++) {
				if (atts.getURI(i).equals("http://www.w3.org/1999/xlink") 
						&& atts.getLocalName(i).equals("href")) {
					xlink = true;
					break;
				}
			}

			if (!xlink) {
				cityObjectChildElement = new StartElement(uri, localName, atts, null);
			} else {
				super.startElement(uri, localName, qName, atts);
			}

			switchContext(CityGMLClass.CITYOBJECTGROUP, true);
			fireStartDocument();
		}

		else if (isCityGMLElement && localName.equals("appearanceMember")) {
			if (type == CityGMLClass.CITYMODEL) {
				super.startElement(uri, localName, qName, atts);
				elementCounter++;

				if ((firstElement != null && elementCounter < firstElement) || 
						(lastElement != null && elementCounter > lastElement)) {
					switchContext(CityGMLClass.APPEARANCEMEMBER, false);
				} else {				
					switchContext(CityGMLClass.APPEARANCEMEMBER, true);
					fireStartDocument();
				}
			} else
				depth++;

			super.startElement(uri, localName, qName, atts);
		} 

		else if (isCityGMLElement && localName.equals("CityModel")) {
			switchContext(CityGMLClass.CITYMODEL, true);
			fireStartDocument();
			super.startElement(uri, localName, qName, atts);		
		}

		else if (depth > 0)	{
			depth++;

			if (cityObjectChildElement != null) {
				if (type == CityGMLClass.CITYOBJECTGROUP) {					
					String gmlId = null;
					boolean setGmlId = false;

					for (int i = 0; i < atts.getLength(); i++) {
						if (atts.getURI(i).equals("http://www.opengis.net/gml") 
								&& atts.getLocalName(i).equals("id")) {
							gmlId = atts.getValue(i);
							break;
						}
					}

					if (gmlId == null) {
						gmlId = UUIDManager.randomUUID();
						setGmlId = true;
					}

					// set xlink as new attribute for groupMember element
					AttributesImpl groupMemberPropertyAtts = new AttributesImpl(cityObjectChildElement.getAttributes());
					groupMemberPropertyAtts.addAttribute("http://www.w3.org/1999/xlink", "href", "href", "CDATA", "#" + gmlId);

					// and add modified groupMember tag to previous context 
					ParserContext prevContext = contextStack.peek();
					prevContext.getSAXBuffer().startElement(
							cityObjectChildElement.getURI(), 
							cityObjectChildElement.getLocalName(),
							null,
							groupMemberPropertyAtts);

					if (setGmlId) {
						AttributesImpl groupMemberAtts = new AttributesImpl(atts);
						groupMemberAtts.addAttribute("http://www.opengis.net/gml", "id", "id", "CDATA", gmlId);
						atts = groupMemberAtts;
					}
				}

				cityObjectChildElement = null;
			}

			super.startElement(uri, localName, qName, atts);
		}

		else if (isCityGMLElement) {
			elementCounter++;

			if ((firstElement != null && elementCounter < firstElement) || 
					(lastElement != null && elementCounter > lastElement)) {
				switchContext(CityGMLClass.CITYOBJECT, false);
			} else {				
				switchContext(CityGMLClass.CITYOBJECT, true);
				fireStartDocument();
				super.startElement(uri, localName, qName, atts);		
			}
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (depth > 0) {			
			depth--;

			if (depth == 0) {	

				switch (type) {
				case CITYMODEL:
				case CITYOBJECT:
				case APPEARANCEMEMBER:
					super.endElement(uri, localName, qName);
				}

				fireEndDocument();
				setContentHandler(defaultHandler);

				if (bufferEvents)
					workerPool.addWork(saxBuffer);

				CityGMLClass oldType = restoreContext();

				switch (oldType) {
				case CITYOBJECTGROUP:
				case CITYOBJECTMEMBER:
				case APPEARANCEMEMBER:
					super.endElement(uri, localName, qName);
					break;
				}					

				// interrupt import if we exceed user specified range				
				if (lastElement != null && elementCounter > lastElement) {
					try {
						eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptEnum.OUT_OF_RANGE));
					} catch (Exception e) {
						// 
					}
				}
			} else
				super.endElement(uri, localName, qName);
		}
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		super.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		super.endPrefixMapping(prefix);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	private void switchContext(CityGMLClass type, boolean bufferEvents) {
		// push old context on stack
		contextStack.push(new ParserContext(
				depth, 
				saxBuffer, 
				this.type,
				this.bufferEvents));

		// and initialize new context
		saxBuffer = new SAXBuffer(saxBuffer.isTrackLocation());
		depth = 1;
		this.type = type;
		this.bufferEvents = bufferEvents;		
		setContentHandler(bufferEvents ? saxBuffer : defaultHandler);
	}

	private CityGMLClass restoreContext() {
		// pop old context from stack
		CityGMLClass oldType = type;
		ParserContext context = contextStack.pop();

		// and set current context accordingly
		saxBuffer = context.getSAXBuffer();
		depth = context.getDepth();		
		type = context.getType();
		bufferEvents = context.isBufferEvents();
		setContentHandler(bufferEvents ? saxBuffer : defaultHandler);

		return oldType;
	}

	private void fireStartDocument() throws SAXException {
		saxBuffer.startDocument();
		saxBuffer.setDocumentLocator(locator);
	}

	private void fireEndDocument() throws SAXException {
		saxBuffer.endDocument();
	}

	private void handleSchemaLocation(Attributes atts) {
		List<String> schemaLocations = new ArrayList<String>();			
		for (int i = 0; i < atts.getLength(); i++) {
			if (atts.getURI(i).equals("http://www.w3.org/2001/XMLSchema-instance")) { 
				if (atts.getLocalName(i).equals("schemaLocation")) {
					List<String> tokens = Util.string2string(atts.getValue(i), "\\s+");
					int j = 0;

					for (String token : tokens) {
						if (token == null || token.length() == 0)
							continue;

						j ^= 1;
						if (j == 0)
							schemaLocations.add(token);
					}
				} else if (atts.getLocalName(i).equals("noNamespaceSchemaLocation"))
					schemaLocations.add(atts.getValue(i));
			}
		}

		Set<URL> schemaLocationURLs = new HashSet<URL>();
		boolean containsOfficialSchemaLocation = false;

		for (String schemaLocation : schemaLocations) {
			URL schemaLocationURL = null;
			boolean isOfficialSchemaLocation = false;

			try {
				schemaLocationURL = new URL(schemaLocation);

				// checking whether we deal with an official CityGML schema
				if (schemaLocation.equals(CoreModule.v0_4_0.getSchemaLocation()))
					isOfficialSchemaLocation = true;
				else {
					for (CityGMLModule module : CityGMLModules.getModules()) {
						if (module.getModuleVersion() == CityGMLModuleVersion.v0_4_0)
							continue;

						if (schemaLocation.equals(module.getSchemaLocation())) {
							isOfficialSchemaLocation = true;
							break;
						}
					}
				}

			} catch (MalformedURLException e) {
				//
			}

			if (schemaLocationURL == null) {
				File schemaLocationLocal = new File(schemaLocation);
				if (!schemaLocationLocal.isAbsolute())
					schemaLocationLocal = new File(config.getInternal().getImportPath() + File.separator + schemaLocationLocal.toString());

				try {
					schemaLocationURL = schemaLocationLocal.getAbsoluteFile().toURI().toURL();
				} catch (MalformedURLException e) {
					//
				}
			}

			if (schemaLocationURL != null) {
				schemaLocationURLs.add(schemaLocationURL);
				LOG.info("Reading schema: " + schemaLocationURL.toString());

				if (!isOfficialSchemaLocation)
					LOG.warn("'" + schemaLocationURL.toString() + "' is not an official CityGML schema location.");
				else
					containsOfficialSchemaLocation = true;
			}
		}		

		if (!schemaLocationURLs.isEmpty()) {	
			if (!containsOfficialSchemaLocation)
				LOG.warn("The document does not refer to an official CityGML schema location. Please make sure this is really a CityGML instance document.");

			try {
				// trigger a sync event to announce schema locations. by this means we make
				// sure that this thread is blocked until all listeners have consumed the event
				eventDispatcher.triggerSyncEvent(new SchemaLocationEvent(schemaLocationURLs));
			} catch (Exception e) {
				//
			}
		} else {
			try {
				eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptEnum.READ_SCHEMA_ERROR, 
						"Failed to read schema documents from root element.", LogLevelType.ERROR));
			} catch (Exception e) {
				//
			}
		}
	}

	private final class ParserContext {
		private long depth;
		private SAXBuffer saxBuffer;
		private CityGMLClass type;
		private boolean bufferEvents;

		public ParserContext(long depth, SAXBuffer saxBuffer, CityGMLClass type, boolean bufferEvents) {
			this.depth = depth;
			this.saxBuffer = saxBuffer;
			this.type = type;
			this.bufferEvents = bufferEvents;
		}

		public long getDepth() {
			return depth;
		}

		public SAXBuffer getSAXBuffer() {
			return saxBuffer;
		}

		public CityGMLClass getType() {
			return type;
		}

		public boolean isBufferEvents() {
			return bufferEvents;
		}
	}

}
