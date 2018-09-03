package org.citydb.log;

import org.citydb.config.project.global.LogLevel;

import java.io.PrintStream;

public interface ConsoleLogger {
    void log(LogLevel level, String msg);
    void log(String msg);
    PrintStream out();
    PrintStream err();
}
