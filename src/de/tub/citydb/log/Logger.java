package de.tub.citydb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

public class Logger {
	private static Logger INSTANCE = new Logger();

	private LogLevelType consoleLogLevel = LogLevelType.INFO;
	private LogLevelType fileLogLevel = LogLevelType.INFO;
	
	private Calendar cal;
	private DecimalFormat df = new DecimalFormat("00");

	private boolean isLogToFile = false;
	private BufferedWriter logFile;

	private Logger() {
		// just to thwart instantiation
	}

	public static Logger getInstance() {
		return INSTANCE;
	}

	public void setConsoleLogLevel(LogLevelType consoleLogLevel) {
		this.consoleLogLevel = consoleLogLevel;
	}
	
	public void setFileLogLevel(LogLevelType fileLogLevel) {
		this.fileLogLevel = fileLogLevel;
	}

	public String getPrefix(LogLevelType type) {
		cal = Calendar.getInstance();

		int h = cal.get(Calendar.HOUR_OF_DAY);
		int m = cal.get(Calendar.MINUTE);
		int s = cal.get(Calendar.SECOND);

		StringBuffer prefix = new StringBuffer();
		prefix.append("[");
		prefix.append(df.format(h));
		prefix.append(":");
		prefix.append(df.format(m));
		prefix.append(":");
		prefix.append(df.format(s));
		prefix.append(" ");
		prefix.append(type.value());
		prefix.append("] ");

		return prefix.toString();
	}

	public void log(LogLevelType type, String msg) {
		StringBuffer buffer = new StringBuffer(getPrefix(type));
		buffer.append(msg);

		if (consoleLogLevel.ordinal() >= type.ordinal())
			System.out.println(buffer.toString());

		if (isLogToFile && fileLogLevel.ordinal() >= type.ordinal()) {
			try {
				logFile.write(buffer.toString());
				logFile.newLine();
				logFile.flush();
			} catch (IOException e) {
				//
			}
		}
	}

	public void debug(String msg) {		
		log(LogLevelType.DEBUG, msg);
	}

	public void info(String msg) {
		log(LogLevelType.INFO, msg);
	}

	public void warn(String msg) {
		log(LogLevelType.WARN, msg);
	}

	public void error(String msg) {
		log(LogLevelType.ERROR, msg);
	}
	
	public void write(String msg) {
		System.out.println(msg);
		writeToFile(msg);
	}
	
	public void writeToFile(String msg) {
		if (isLogToFile) {
			try {
				logFile.write(msg);
				logFile.newLine();
				logFile.flush();
			} catch (IOException e) {
				//
			}
		}
	}
	
	public void logToFile(boolean isLogToFile) {		
		this.isLogToFile = isLogToFile;
	}
	
	public boolean appendLogFile(String logPath) {
		File createPath = new File(logPath);

		if (!createPath.exists() && !createPath.mkdirs()) {
			error("Could not create folder '" + createPath + "' for log file.");
			return false;
		}

		File file = new File(createPath.getAbsolutePath() + File.separator + getDefaultLogFile());
		try {
			info("Writing log messages to file: '" + file.getAbsolutePath() + "'");
			detachLogFile();
			
			logFile = new BufferedWriter(new FileWriter(file, file.exists()));
			isLogToFile = true;
		} catch (IOException e) {
			error("Failed to open log file '" + logFile + "': " + e.getMessage());
			error("Not writing log messages to file");
			return false;
		}
				
		return true;
	}

	public void detachLogFile() {
		if (logFile != null) {
			try {
				warn("Stopped writing log messages to log file.");
				logFile.close();
			} catch (IOException e) {
				//
			} finally {
				logFile = null;
				isLogToFile = false;
			}
		}
	}
	
	private String getDefaultLogFile() {
		cal = Calendar.getInstance();

		int m = cal.get(Calendar.MONTH) + 1;
		int d = cal.get(Calendar.DATE);
		int y = cal.get(Calendar.YEAR);

		StringBuffer defaultLog = new StringBuffer("log_3dcitydb_impexp_");
		defaultLog.append(y);
		defaultLog.append('-');
		defaultLog.append(df.format(m));
		defaultLog.append('-');
		defaultLog.append(df.format(d));
		defaultLog.append(".log");

		return defaultLog.toString();
	}

}
