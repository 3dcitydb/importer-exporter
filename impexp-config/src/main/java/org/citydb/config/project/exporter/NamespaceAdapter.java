package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamespaceAdapter extends XmlAdapter<NamespaceAdapter.NamespaceList, Map<String, Namespace>> {

    public static class NamespaceList {
        @XmlElement(name = "namespace")
        private List<Namespace> namespaces;
    }

    @Override
    public Map<String, Namespace> unmarshal(NamespaceList v) throws Exception {
        Map<String, Namespace> namespaces = null;

        if (v != null && v.namespaces != null && !v.namespaces.isEmpty()) {
            namespaces = new HashMap<>();
            for (Namespace namespace : v.namespaces) {
                if (namespace.isSetURI())
                    namespaces.put(namespace.getURI(), namespace);
            }
        }

        return namespaces;
    }

    @Override
    public NamespaceList marshal(Map<String, Namespace> v) throws Exception {
        NamespaceList list = null;

        if (v != null && !v.isEmpty()) {
            list = new NamespaceList();
            list.namespaces = new ArrayList<>(v.values());
        }

        return list;
    }
}