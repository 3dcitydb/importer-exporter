package de.tub.citydb.sax;

import java.util.Enumeration;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.filter.FilterMode;
import de.tub.citydb.filter.statistic.FeatureCounterFilter;
import de.tub.citydb.sax.SAXBuffer.SAXEvent;
import de.tub.citydb.util.UUIDManager;
import de.tub.citygml4j.model.citygml.CityGMLClass;

public class SAXSplitter extends XMLFilterImpl {
	private final WorkerPool<Vector<SAXEvent>> workerPool;
	private SAXBuffer saxBuffer;
	private NamespaceSupport namespaces;
	private DefaultHandler defaultHandler;

	// context related instance members
	private Stack<ParserContext> contextStack;
	private long depth;
	private CityGMLClass type;
	private boolean bufferEvents;
	private CityObjectChildElement cityObjectChildElement;

	// instance member used for filtering
	private FeatureCounterFilter counterFilter;
	private long elementCounter;
	private Long firstElement;
	private Long lastElement;
	
	public SAXSplitter(WorkerPool<Vector<SAXEvent>> threadPool, Config config) {
		this.workerPool = threadPool;
		saxBuffer = new SAXBuffer();
		namespaces = new NamespaceSupport();
		defaultHandler = new DefaultHandler();
		contextStack = new Stack<ParserContext>();
		counterFilter = new FeatureCounterFilter(config, FilterMode.IMPORT);

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

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {	
		// according to occurring city objects we have possibly to switch the parser context

		if ((uri.equals("http://www.citygml.org/citygml/1/0/0") || uri.startsWith("http://www.opengis.net/citygml/"))
				&& localName.equals("CityModel")) {
			switchContext(CityGMLClass.CITYMODEL, true);
			fireStartDocument();
			super.startElement(uri, localName, qName, atts);		
		}

		else if ((uri.equals("http://www.citygml.org/citygml/1/0/0") || uri.startsWith("http://www.opengis.net/citygml/cityobjectgroup/")) && 
				(localName.equals("groupMember") || 
						localName.equals("parent") || 
						localName.equals("generalizesTo"))) {		
			if (!bufferEvents) {
				switchContext(CityGMLClass.CITYOBJECT, false);
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
				cityObjectChildElement = new CityObjectChildElement(uri, localName, qName, atts);
			} else {
				super.startElement(uri, localName, qName, atts);
			}

			switchContext(CityGMLClass.CITYOBJECT, true);
			fireStartDocument();
		}

		else if ((uri.equals("http://www.citygml.org/citygml/1/0/0") || uri.startsWith("http://www.opengis.net/citygml/"))
				&& localName.equals("cityObjectMember")) {
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

		else if ((uri.equals("http://www.citygml.org/citygml/1/0/0") || uri.startsWith("http://www.opengis.net/citygml/appearance/")) 
				&& localName.equals("appearanceMember")) {
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

		else if (depth > 0)	{
			depth++;

			if (cityObjectChildElement != null) {
				if (type == CityGMLClass.CITYOBJECT) {					
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
					AttributesImpl groupMemberPropertyAtts = new AttributesImpl(cityObjectChildElement.getAtts());
					groupMemberPropertyAtts.addAttribute("http://www.w3.org/1999/xlink", "href", "href", "CDATA", "#" + gmlId);

					// and add modified groupMember tag to previous context 
					ParserContext prevContext = contextStack.peek();

					SAXBuffer.StartElement groupMemberElement = new SAXBuffer.StartElement(
							cityObjectChildElement.getUri(), 
							cityObjectChildElement.getLocalName(), 
							cityObjectChildElement.getQName(), 
							groupMemberPropertyAtts);
					prevContext.getSaxEvents().add(groupMemberElement);				

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

	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (depth > 0) {			
			depth--;

			if (depth == 0) {	

				switch (type) {
				case CITYMODEL:
				case APPEARANCEMEMBER:
					super.endElement(uri, localName, qName);
				}

				fireEndDocument();
				setContentHandler(defaultHandler);

				if (bufferEvents)
					workerPool.addWork(saxBuffer.getEvents());

				CityGMLClass oldType = restoreContext();

				switch (oldType) {
				case CITYOBJECT:
				case CITYOBJECTMEMBER:
				case APPEARANCEMEMBER:
					super.endElement(uri, localName, qName);
					break;
				}					

			} else
				super.endElement(uri, localName, qName);
		}
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		namespaces.pushContext();
		namespaces.declarePrefix(prefix,uri);

		super.startPrefixMapping(prefix, uri);
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		namespaces.popContext();

		super.endPrefixMapping(prefix);
	}

	private void switchContext(CityGMLClass type, boolean bufferEvents) {
		// push old context on stack
		contextStack.push(new ParserContext(
				depth, 
				saxBuffer.getEvents(), 
				this.type,
				this.bufferEvents));

		// and initialize new context
		saxBuffer.renew();
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
		saxBuffer.saxEvents = context.getSaxEvents();
		depth = context.getDepth();		
		type = context.getType();
		bufferEvents = context.isBufferEvents();
		setContentHandler(bufferEvents ? saxBuffer : defaultHandler);

		return oldType;
	}

	private void fireStartDocument() throws SAXException {
		// fire SAX events to emulate the start of a new document.
		saxBuffer.startDocument();

		// keep in track with namespace declarations.
		Enumeration<String> prefixes = namespaces.getPrefixes();
		while (prefixes.hasMoreElements()) {
			String prefix    = prefixes.nextElement();
			String prefixURI = namespaces.getURI(prefix);

			saxBuffer.startPrefixMapping(prefix, prefixURI);
		}

		String defaultURI = namespaces.getURI("");
		if (defaultURI != null)
			saxBuffer.startPrefixMapping("", defaultURI);
	}

	private void fireEndDocument() throws SAXException {
		// keep in track with namespace declarations
		Enumeration<String> prefixes = namespaces.getPrefixes();
		while (prefixes.hasMoreElements()) {
			String prefix = prefixes.nextElement();

			saxBuffer.endPrefixMapping(prefix);
		}

		String defaultURI = namespaces.getURI("");
		if (defaultURI != null)
			saxBuffer.endPrefixMapping("");

		// fire SAX events to emulate the end of the document
		saxBuffer.endDocument();
	}

	private static final class ParserContext {
		private long depth;
		private Vector<SAXEvent> saxEvents;
		private CityGMLClass type;
		private boolean bufferEvents;

		public ParserContext(long depth, Vector<SAXEvent> saxEvents, CityGMLClass type, boolean bufferEvents) {
			this.depth = depth;
			this.saxEvents = saxEvents;
			this.type = type;
			this.bufferEvents = bufferEvents;
		}

		public long getDepth() {
			return depth;
		}

		public Vector<SAXEvent> getSaxEvents() {
			return saxEvents;
		}

		public CityGMLClass getType() {
			return type;
		}

		public boolean isBufferEvents() {
			return bufferEvents;
		}
	}

	private static final class CityObjectChildElement {
		private String uri;
		private String localName;
		private String qName;
		private AttributesImpl atts;

		public CityObjectChildElement(String uri, String localName, String qName, Attributes atts) {
			this.uri = uri;
			this.localName = localName;
			this.qName = qName;
			this.atts = new AttributesImpl(atts);
		}

		public String getUri() {
			return uri;
		}

		public String getLocalName() {
			return localName;
		}

		public String getQName() {
			return qName;
		}

		public Attributes getAtts() {
			return atts;
		}

	}
}