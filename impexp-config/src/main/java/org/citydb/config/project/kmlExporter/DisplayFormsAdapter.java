package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

public class DisplayFormsAdapter extends XmlAdapter<DisplayFormsAdapter.DisplayFormList, DisplayForms> {

    public static class DisplayFormList {
        @XmlElement(name = "displayForm")
        private List<DisplayForm> displayForms;
    }

    @Override
    public DisplayForms unmarshal(DisplayFormList v) {
        DisplayForms displayForms = new DisplayForms();

        if (v != null
                && v.displayForms != null
                && !v.displayForms.isEmpty()) {
            v.displayForms.forEach(displayForms::add);
        }

        return displayForms;
    }

    @Override
    public DisplayFormList marshal(DisplayForms v) {
        DisplayFormList list = null;

        if (v != null && !v.isEmpty()) {
            list = new DisplayFormList();
            list.displayForms = new ArrayList<>(v.values());
        }

        return list;
    }
}