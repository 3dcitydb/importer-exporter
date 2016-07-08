/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.citydb.api.controller.LogController;
import org.citydb.api.log.LogLevel;

public class Logger implements LogController {
	private static Logger instance = new Logger();

	private LogLevel consoleLogLevel = LogLevel.INFO;
	private LogLevel fileLogLevel = LogLevel.INFO;

	private Calendar cal;
	private DecimalFormat df = new DecimalFormat("00");

	private boolean isLogToConsole = true;
	private boolean isLogToFile = false;
	private BufferedWriter logFile;

	private Logger() {
		// just to thwart instantiation
	}

	public static Logger getInstance() {
		return instance;
	}

	public void setDefaultConsoleLogLevel(LogLevel consoleLogLevel) {
		this.consoleLogLevel = consoleLogLevel;
	}

	public void setDefaultFileLogLevel(LogLevel fileLogLevel) {
		this.fileLogLevel = fileLogLevel;
	}

	@Override
	public LogLevel getDefaultConsoleLogLevel() {
		return consoleLogLevel;
	}

	@Override
	public LogLevel getDefaultFileLogLevel() {
		return fileLogLevel;
	}

	private String getPrefix(LogLevel type) {
		cal = Calendar.getInstance();

		int h = cal.get(Calendar.HOUR_OF_DAY);
		int m = cal.get(Calendar.MINUTE);
		int s = cal.get(Calendar.SECOND);

		StringBuffer prefix = new StringBuffer()
		.append("[")
		.append(df.format(h))
		.append(":")
		.append(df.format(m))
		.append(":")
		.append(df.format(s))
		.append(" ")
		.append(type.value())
		.append("] ");

		return prefix.toString();
	}

	@Override
	public void log(LogLevel type, String msg) {
		StringBuffer buffer = new StringBuffer(getPrefix(type));
		buffer.append(msg);

		if (isLogToConsole && consoleLogLevel.ordinal() >= type.ordinal())
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

	@Override
	public void debug(String msg) {		
		log(LogLevel.DEBUG, msg);
	}

	@Override
	public void info(String msg) {
		log(LogLevel.INFO, msg);
	}

	@Override
	public void warn(String msg) {
		log(LogLevel.WARN, msg);
	}

	@Override
	public void error(String msg) {
		log(LogLevel.ERROR, msg);
	}

	@Override
	public void all(LogLevel type, String message) {
		StringBuffer buffer = new StringBuffer(getPrefix(type));
		buffer.append(message);

		if (isLogToConsole && consoleLogLevel.ordinal() >= type.ordinal())
			System.out.println(buffer.toString());

		if (fileLogLevel.ordinal() >= type.ordinal())
			writeToFile(buffer.toString());
	}

	@Override
	public void print(String msg) {
		if (isLogToConsole)
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
	
	public void logToConsole(boolean isLogToConsole) {
		this.isLogToConsole = isLogToConsole;
	}

	public void logToFile(boolean isLogToFile) {		
		this.isLogToFile = isLogToFile;
	}

	public boolean appendLogFile(String logFile, boolean isDirectory) {
		File file = new File(logFile);
		
		File path = isDirectory ? file : new File(file.getParent());
		if (!path.exists() && !path.mkdirs()) {
			error("Could not create folder '" + path.getAbsolutePath() + "' for log file.");
			return false;
		}

		if (isDirectory)
			file = new File(file.getAbsolutePath() + File.separator + getDefaultLogFile());
		
		try {
			info("Writing log messages to file: '" + file.getAbsolutePath() + "'");
			detachLogFile();

			this.logFile = new BufferedWriter(new FileWriter(file, file.exists()));
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
