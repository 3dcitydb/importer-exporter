package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NamespaceAdapter extends XmlAdapter<NamespaceAdapter.NamespaceList, LinkedHashMap<String, Namespace>> {

    public static class NamespaceList {
        @XmlElement(name = "namespace")
        private List<Namespace> namespaces;
    }

    @Override
    public LinkedHashMap<String, Namespace> unmarshal(NamespaceList v) {
        LinkedHashMap<String, Namespace> namespaces = null;

        if (v != null && v.namespaces != null && !v.namespaces.isEmpty()) {
            namespaces = new LinkedHashMap<>();
            for (Namespace namespace : v.namespaces) {
                if (namespace.isSetURI())
                    namespaces.put(namespace.getURI(), namespace);
            }
        }

        return namespaces;
    }

    @Override
    public NamespaceList marshal(LinkedHashMap<String, Namespace> v) {
        NamespaceList list = null;

        if (v != null && !v.isEmpty()) {
            list = new NamespaceList();
            list.namespaces = new ArrayList<>(v.values());
        }

        return list;
    }
}