/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.config;

import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class ConfigNamespaceFilter extends XMLFilterImpl implements NamespaceContext {
	private final HashMap<String, String> prefixToUri;
	private final HashMap<String, Set<String>> uriToPrefix;

	private final String OLD_CITYDB_CONFIG_NAMESPACE_URI = "http://www.gis.tu-berlin.de/3dcitydb-impexp/config";

	public ConfigNamespaceFilter(XMLReader reader) {
		super(reader);
		prefixToUri = new HashMap<>();
		uriToPrefix = new HashMap<>();

		// bind default CityGML and ADE namespaces
		ModuleContext modules = new ModuleContext(CityGMLVersion.DEFAULT);
		for (Module module : modules.getModules())
			bindNamespace(module.getNamespacePrefix(), module.getNamespaceURI());

		// bind 3DCityDB ADE namespace
		bindNamespace("citydb", "http://www.3dcitydb.org/citygml-ade/3.0/citygml/2.0");
	}

	public ConfigNamespaceFilter() {
		this(null);
	}

	private void bindNamespace(String prefix, String uri) {
		prefixToUri.put(prefix, uri);
		uriToPrefix.computeIfAbsent(uri, k -> new HashSet<>());

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
			uri = ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI;

		// support config files from previous releases 
		else if (uri.startsWith(OLD_CITYDB_CONFIG_NAMESPACE_URI))
			uri = ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI;

		super.startPrefixMapping(prefix, uri);
		bindNamespace(prefix, uri);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (uri == null || uri.isEmpty())
			uri = ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI;

		// support config files from previous releases 
		else if (uri.startsWith(OLD_CITYDB_CONFIG_NAMESPACE_URI))
			uri = ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI;

		super.startElement(uri, localName, qName, atts);
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		for (Entry<String, String> entry : prefixToUri.entrySet())
			super.startPrefixMapping(entry.getKey(), entry.getValue());
	}

}
