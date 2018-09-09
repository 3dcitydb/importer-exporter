package org.citydb.config.gui.style;

import org.citydb.config.project.global.LogLevel;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ConsoleStyleType", propOrder={
        "debug",
        "info",
        "warn",
        "error"
})
public class ConsoleStyle {
    private LogLevelStyle debug = new LogLevelStyle("#ff0000ee");
    private LogLevelStyle info = new LogLevelStyle();
    private LogLevelStyle warn = new LogLevelStyle("#ffa66f00");
    private LogLevelStyle error = new LogLevelStyle("#ffcd0000");

    public LogLevelStyle getLogLevelStyle(LogLevel level) {
        switch (level) {
            case DEBUG:
                return debug;
            case INFO:
                return info;
            case WARN:
                return warn;
            case ERROR:
                return error;
            default:
                throw new IllegalArgumentException("No style definition for log level " + level.value() + ".");
        }
    }

    public void setLogLevelStyle(LogLevel level, LogLevelStyle style) {
        if (style != null) {
            switch (level) {
                case DEBUG:
                    debug = style;
                    break;
                case INFO:
                    info = style;
                    break;
                case WARN:
                    warn = style;
                    break;
                case ERROR:
                    error = style;
                    break;
            }
        }
    }
}
