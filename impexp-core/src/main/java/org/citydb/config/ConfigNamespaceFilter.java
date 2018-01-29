package org.citydb.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.registry.ObjectRegistry;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class ConfigNamespaceFilter extends XMLFilterImpl implements NamespaceContext {
	private final HashMap<String, String> prefixToUri;
	private final HashMap<String, Set<String>> uriToPrefix;

	private final String CITYDB_CONFIG_NAMESPACE_URI = "http://www.3dcitydb.org/importer-exporter/config";
	private final String OLD_CITYDB_CONFIG_NAMESPACE_URI = "http://www.gis.tu-berlin.de/3dcitydb-impexp/config";

	public ConfigNamespaceFilter(XMLReader reader) {
		super(reader);
		prefixToUri = new HashMap<String, String>();
		uriToPrefix = new HashMap<String, Set<String>>();

		// bind default namespaces
		SchemaMapping schemaMapping = (SchemaMapping)ObjectRegistry.getInstance().lookup(SchemaMapping.class.getName());
		if (schemaMapping != null) {
			for (Entry<String, String> entry : schemaMapping.getNamespaceContext().entrySet())
				bindNamespace(entry.getKey(), entry.getValue());
		}
	}

	public ConfigNamespaceFilter() {
		this(null);
	}

	private void bindNamespace(String prefix, String uri) {
		prefixToUri.put(prefix, uri);
		if (uriToPrefix.get(uri) == null)
			uriToPrefix.put(uri, new HashSet<String>());

		uriToPrefix.get(uri).add(prefix);
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null)
			throw new IllegalArgumentException("namespace prefix may not be null.");

		return prefixToUri.get(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI == null)
			throw new IllegalArgumentException("namespace URI may not be null.");

		if (uriToPrefix.containsKey(namespaceURI))
			return uriToPrefix.get(namespaceURI).iterator().next();

		return null;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		if (namespaceURI == null)
			throw new IllegalArgumentException("namespace URI may not be null.");

		if (uriToPrefix.containsKey(namespaceURI))
			return uriToPrefix.get(namespaceURI).iterator();

		return Collections.<String>emptySet().iterator();
	}

	public Iterator<String> getPrefixes() {
		return prefixToUri.keySet().iterator();
	}

	public Iterator<String> getNamespaceURIs() {
		return uriToPrefix.keySet().iterator();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (uri == null || uri.isEmpty())
			uri = CITYDB_CONFIG_NAMESPACE_URI;

		// support config files from previous releases 
		else if (uri.startsWith(OLD_CITYDB_CONFIG_NAMESPACE_URI))
			uri = CITYDB_CONFIG_NAMESPACE_URI;

		super.startPrefixMapping(prefix, uri);
		bindNamespace(prefix, uri);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (uri == null || uri.isEmpty())
			uri = CITYDB_CONFIG_NAMESPACE_URI;

		// support config files from previous releases 
		else if (uri.startsWith(OLD_CITYDB_CONFIG_NAMESPACE_URI))
			uri = CITYDB_CONFIG_NAMESPACE_URI;

		super.startElement(uri, localName, qName, atts);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		for (Entry<String, String> entry : prefixToUri.entrySet())
			super.startPrefixMapping(entry.getKey(), entry.getValue());
	}

}
