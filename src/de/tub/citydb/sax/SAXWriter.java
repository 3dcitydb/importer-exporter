package de.tub.citydb.sax;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

public class SAXWriter extends XMLFilterImpl {
	private NamespaceSupport namespaces;
	private HashMap<String, String> forcedNSMap;
	private HashSet<String> suppressNSSet;
	private HashSet<String> declURISet;

	private long depth;
	private boolean writeForcedNSDecl;
	private boolean prefixMapping;
	private String indentString;
	private String lineSeparator;
	private Writer writer;

	private int lastXMLContent;
	private static final int NOTHING = 0;
	private static final int ELEMENT = 1;
	private static final int DATA = 2;
	private static final int PI = 3;
	private Stack<Integer> lastXMLContentStack;

	public SAXWriter() {
		init(null);
	}

	public SAXWriter(XMLReader xmlReader) {
		super(xmlReader);
		init(null);
	}

	public SAXWriter(Writer writer) {
		init(writer);
	}

	public SAXWriter(XMLReader xmlReader, Writer writer) {
		super(xmlReader);
		init(writer);
	}

	private void init(Writer writer) {
		namespaces = new NamespaceSupport();
		forcedNSMap = new HashMap<String, String>();
		suppressNSSet = new HashSet<String>();
		declURISet = new HashSet<String>();
		lastXMLContentStack = new Stack<Integer>();
		
		lastXMLContent = NOTHING;
		indentString = "";
		lineSeparator = System.getProperty("line.separator");
		writeForcedNSDecl = true;

		setWriter(writer);
		suppressNSSet.add("http://www.w3.org/XML/1998/namespace");
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (length > 0) {
			if (lastXMLContent == NOTHING)
				write('>');

			lastXMLContent = DATA;
			writeEscaped(ch, start, length, false);
		}

		super.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		writeIndent();

		try {
			flush();
		} catch (IOException io) {
			throw new SAXException(io);
		}

		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);

		if (depth > 0) {
			depth--;

			if (lastXMLContent == NOTHING) {
				// empty element tag
				write("/>");
			} else {
				if (lastXMLContent == ELEMENT)
					writeIndent();

				write("</");
				writeName(uri, localName);
				write('>');
			}

			lastXMLContent = lastXMLContentStack.pop();
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if (length > 0) {
			if (lastXMLContent == NOTHING)
				write('>');

			lastXMLContent = DATA;
			writeEscaped(ch, start, length, false);
		}

		super.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		lastXMLContent = PI;

		write("<?");
		write(target);
		write(' ');
		write(data);
		write("?>");
		writeIndent();

		super.processingInstruction(target, data);
	}

	@Override
	public void startDocument() throws SAXException {
		reset();
		lastXMLContent = PI;

		write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		writeIndent();
		write("<!-- Written by " + this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + "\" -->");
		writeIndent();
		write("<!-- " + this.getClass().getPackage().getImplementationVendor() + " -->");
		writeIndent();
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (lastXMLContent == NOTHING)
			write('>');

		lastXMLContentStack.push(ELEMENT);
		lastXMLContent = NOTHING;

		if (depth > 0)
			writeIndent();

		write('<');
		writeName(uri, localName);

		if (depth == 0)
			writeForcedNSDecl();

		if (prefixMapping) {
			writeNSDecl();
			prefixMapping = false;
		}
		
		writeAttributes(atts);

		depth++;
		super.startElement(uri, localName, qName, atts);
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (!forcedNSMap.containsKey(uri)) {
			namespaces.pushContext();
			namespaces.declarePrefix(prefix,uri);
			
			prefixMapping = true;
		}

		super.startPrefixMapping(prefix, uri);
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		if (namespaces.getURI(prefix) != null) {
			declURISet.remove(namespaces.getURI(prefix));
			namespaces.popContext();
		}

		super.endPrefixMapping(prefix);
	}

	// public methods
	public void reset() {
		namespaces.reset();
		lastXMLContent = NOTHING;
		lastXMLContentStack.clear();
		depth = 0;
	}

	public void flush() throws IOException {
		writer.flush();
	}

	public void setWriter(Writer writer) {
		if (writer != null)
			this.writer = writer;
		else
			this.writer = new OutputStreamWriter(System.out);
	}

	public boolean isWriteForcedNSDecl() {
		return writeForcedNSDecl;
	}

	public void setWriteForcedNSDecl(boolean writeForcedNSDecl) {
		this.writeForcedNSDecl = writeForcedNSDecl;
	}

	public void forceNSDecl(String uri, String prefix) {
		forcedNSMap.put(uri, prefix);
	}

	public void suppressNSDecl(String uri) {
		suppressNSSet.add(uri);
	}
	
	public String getIndentString() {
		return indentString;
	}

	public void setIndentString(String indentString) {
		if (indentString == null)
			indentString = "";

		this.indentString = indentString;
	}

	private String getPrefix(String uri) {
		String prefix = forcedNSMap.get(uri);
		if (prefix == null)
			prefix = namespaces.getPrefix(uri);

		return prefix;
	}

	private void writeForcedNSDecl() throws SAXException {
		Iterator<String> iter = forcedNSMap.keySet().iterator();
		while (iter.hasNext()) {
			String uri = iter.next();
			String prefix = forcedNSMap.get(uri);

			if (writeForcedNSDecl) {
				write(' ');

				if (prefix.length() == 0) {
					write("xmlns=\"");
				} else {
					write("xmlns:");
					write(prefix);
					write("=\"");
				}

				char[] ch = uri.toCharArray();
				writeEscaped(ch, 0, ch.length, true);
				write('"');
			}

			declURISet.add(uri);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void writeNSDecl() throws SAXException {
		String defaultURI = namespaces.getURI("");
		if (defaultURI != null && !declURISet.contains(defaultURI)) {
			write(' ');
			write("xmlns=\"");

			char[] ch = defaultURI.toCharArray();
			writeEscaped(ch, 0, ch.length, true);
			write('"');

			declURISet.add(defaultURI);
		}

		Enumeration<String> prefixes = namespaces.getPrefixes();
		while (prefixes.hasMoreElements()) {
			String prefix = prefixes.nextElement();
			String prefixURI = namespaces.getURI(prefix);

			if (prefixURI == null)
				prefixURI = "";

			if (prefix.length() == 0 ||
					declURISet.contains(prefixURI) ||
					suppressNSSet.contains(prefixURI))
				continue;

			char[] ch = prefixURI.toCharArray();
			write(' ');

			write("xmlns:");
			write(prefix);
			write("=\"");

			writeEscaped(ch, 0, ch.length, true);
			write('"');

			declURISet.add(prefixURI);
		}
	}

	private void writeName(String uri, String localName) throws SAXException {
		String prefix = getPrefix(uri);
		if (prefix != null && prefix.length() != 0) {
			write(prefix);
			write(':');
		}

		write(localName);
	}

	private void writeAttributes(Attributes atts) throws SAXException {
		for (int i = 0; i < atts.getLength(); i++) {
			char[] ch = atts.getValue(i).toCharArray();
			write(' ');
			writeName(atts.getURI(i), atts.getLocalName(i));
			write("=\"");
			writeEscaped(ch, 0, ch.length, true);
			write('"');
		}
	}

	private void writeIndent() throws SAXException {
		if (indentString.length() == 0)
			return;

		write(lineSeparator);
		for (long i = 0; i < depth; i++)
			write(indentString);
	}

	private void write(char ch) throws SAXException {
		try {
			writer.write(ch);
		} catch (IOException io) {
			throw new SAXException(io);
		}
	}

	private void write(String s) throws SAXException {
		try {
			writer.write(s);
		} catch (IOException io) {
			throw new SAXException(io);
		}
	}

	private void writeEscaped(char[] ch, int start, int length, boolean isAttr) throws SAXException {
		for (int i = start; i < start + length; i++) {
			switch (ch[i]) {
			case '&':
				write("&amp;");
				break;
			case '<':
				write("&lt;");
				break;
			case '>':
				write("&gt;");
				break;
			case '"':
				if (isAttr)
					write("&quot;");
				else
					write('"');
				break;
			default:
				write(ch[i]);
			}
		}
	}
}
