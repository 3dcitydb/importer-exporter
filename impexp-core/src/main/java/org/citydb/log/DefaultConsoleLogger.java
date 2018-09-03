package org.citydb.log;

import org.citydb.config.project.global.LogLevel;

import java.io.PrintStream;

public class DefaultConsoleLogger implements ConsoleLogger {

    @Override
    public void log(LogLevel level, String msg) {
        PrintStream stream = level == LogLevel.ERROR ? System.err : System.out;
        stream.println(msg);
    }

    @Override
    public void log(String msg) {
        System.out.println(msg);
    }

    @Override
    public PrintStream out() {
        return System.out;
    }

    @Override
    public PrintStream err() {
        return System.err;
    }

}
