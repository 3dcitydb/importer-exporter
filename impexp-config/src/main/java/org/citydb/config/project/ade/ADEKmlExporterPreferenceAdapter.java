package org.citydb.config.project.ade;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ADEKmlExporterPreferenceAdapter extends XmlAdapter<ADEKmlExporterPreferenceAdapter.ADEKmlExporterPreferenceList, Map<String, ADEKmlExporterPreference>> {

    public static class ADEKmlExporterPreferenceList {
        @XmlElement(name = "preference")
        private List<ADEKmlExporterPreference> preferences;
    }

    @Override
    public Map<String, ADEKmlExporterPreference> unmarshal(ADEKmlExporterPreferenceList v) {
        Map<String, ADEKmlExporterPreference> preferences = null;

        if (v != null && v.preferences != null && !v.preferences.isEmpty()) {
            preferences = new HashMap<>();
            for (ADEKmlExporterPreference preference : v.preferences) {
                if (preference.isSetTarget())
                    preferences.put(preference.getTarget(), preference);
            }
        }

        return preferences;
    }

    @Override
    public ADEKmlExporterPreferenceList marshal(Map<String, ADEKmlExporterPreference> v) {
        ADEKmlExporterPreferenceList list = null;

        if (v != null && !v.isEmpty()) {
            list = new ADEKmlExporterPreferenceList();
            list.preferences = new ArrayList<>(v.values());
        }

        return list;
    }
}