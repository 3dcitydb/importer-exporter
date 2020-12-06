package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

public class StylesAdapter extends XmlAdapter<StylesAdapter.StylesList, Styles> {

    public static class StylesList {
        @XmlElement(name = "style")
        private List<Style> styles;
    }

    @Override
    public Styles unmarshal(StylesList v) {
        Styles styles = new Styles();

        if (v != null
                && v.styles != null
                && !v.styles.isEmpty()) {
            v.styles.forEach(styles::add);
        }

        return styles;
    }

    @Override
    public StylesList marshal(Styles v) {
        StylesList list = null;

        if (v != null && !v.isEmpty()) {
            list = new StylesList();
            list.styles = new ArrayList<>(v.values());
        }

        return list;
    }
}