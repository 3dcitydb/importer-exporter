package de.tub.citydb.sax;

import java.util.Properties;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import de.tub.citydb.sax.events.EndElement;
import de.tub.citydb.sax.events.SAXEvent;
import de.tub.citydb.sax.events.StartElement;
import de.tub.citydb.util.JAXBUtil;

public class XMLHeaderWriter {
	private final SAXWriter saxWriter;
	private Vector<SAXEvent> rootElement;
	private boolean standAlone;
	private boolean rootElementCutStartTags;
	private boolean rootElementCutEndTag;

	public XMLHeaderWriter(SAXWriter saxWriter) {
		this.saxWriter = saxWriter;
		standAlone = true;
		rootElementCutEndTag = true;
		rootElementCutStartTags = true;
	}

	public void startRootElement() throws SAXException {
		if (standAlone)
			saxWriter.startDocument();

		if (rootElement != null) {
			long depth = 0;

			for (SAXEvent event : rootElement) {
				if (event instanceof StartElement)
					++depth;

				if (event instanceof EndElement) {
					--depth;
					if (depth <= 0 && rootElementCutEndTag)
						continue;
				}

				event.send(saxWriter);
			}
		}
	}


	public void endRootElement() throws SAXException {
		if (rootElement != null) {
			long depth = 0;

			for (SAXEvent event : rootElement) {
				if (event instanceof StartElement)
					++depth;

				if (event instanceof EndElement) {
					--depth;
					if (depth <= 0) {
						event.send(saxWriter);
						continue;
					}
				}

				if (!rootElementCutStartTags)
					event.send(saxWriter);
			}
		}

		saxWriter.endDocument();
	}

	public void setRootElement(JAXBElement<?> jaxbRootElement,
							   JAXBContext jaxbContext,
			 				   Properties marshallerProps) throws JAXBException {
		rootElement = JAXBUtil.jaxbElem2saxEvents(jaxbContext, jaxbRootElement, marshallerProps);
	}

	public void setRootElement(Vector<SAXEvent> rootElement) {
		this.rootElement = rootElement;
	}

	public boolean isStandAlone() {
		return standAlone;
	}

	public void setStandAlone(boolean standAlone) {
		this.standAlone = standAlone;
	}

	public boolean isRootElementCutStartTags() {
		return rootElementCutStartTags;
	}

	public void setRootElementCutStartTags(boolean rootElementCutStartTags) {
		this.rootElementCutStartTags = rootElementCutStartTags;
	}

	public boolean isRootElementCutEndTag() {
		return rootElementCutEndTag;
	}

	public void setRootElementCutEndTag(boolean rootElementCutEndTag) {
		this.rootElementCutEndTag = rootElementCutEndTag;
	}
}
