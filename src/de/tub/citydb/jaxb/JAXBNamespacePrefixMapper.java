package de.tub.citydb.jaxb;

import java.util.HashMap;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class JAXBNamespacePrefixMapper extends NamespacePrefixMapper {
	private HashMap<String, String> prefixMap = new HashMap<String, String>();

	public void setNamespacePrefixMapping(String uri, String prefix) {
		prefixMap.put(uri, prefix);
	}

	public String getNamespacePrefixMapping(String uri) {
		return prefixMap.get(uri);
	}

	public String getPreferredPrefix(String uri, String suggestion, boolean requirePrefix) {
		String prefix = prefixMap.get(uri);
		if (prefix != null)
			return prefix;

		return suggestion;
	}
}
