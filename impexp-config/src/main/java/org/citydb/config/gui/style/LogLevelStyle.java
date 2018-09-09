package org.citydb.config.gui.style;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LogLevelStyleType", propOrder={
        "foreground",
        "background"
})
public class LogLevelStyle {
    @XmlAttribute(name = "fgColor")
    private String foreground;
    @XmlAttribute(name = "bgColor")
    private String background;

    public LogLevelStyle() {

    }

    LogLevelStyle(String foreground) {
        this.foreground = foreground;
    }

    public boolean isSetForeground() {
        return foreground != null;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }

    public boolean isSetBackground() {
        return background != null;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }
}
