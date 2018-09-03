/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import org.citydb.config.project.global.LogLevel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	private static Logger instance = new Logger();

	private ConsoleLogger consoleLogger;
	private LogLevel consoleLevel = LogLevel.INFO;
	private LogLevel fileLevel = LogLevel.INFO;

	private boolean isLogToConsole = true;
	private boolean isLogToFile = false;
	private BufferedWriter logFile;

	private Logger() {
		consoleLogger = new DefaultConsoleLogger();
	}

	public static Logger getInstance() {
		return instance;
	}

	public void setConsoleLogger(ConsoleLogger consoleLogger) {
		if (consoleLogger != null)
			this.consoleLogger = consoleLogger;
	}

	public void setDefaultConsoleLogLevel(LogLevel level) {
		consoleLevel = level;
	}

	public void setDefaultFileLogLevel(LogLevel level) {
		fileLevel = level;
	}

	public LogLevel getDefaultConsoleLogLevel() {
		return consoleLevel;
	}

	public LogLevel getDefaultFileLogLevel() {
		return fileLevel;
	}

	private String getPrefix(LogLevel level) {
		return "[" +
				LocalDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_TIME) +
				" " +
				level.value() +
				"] ";
	}

	public void log(LogLevel level, String msg) {
		msg = getPrefix(level) + msg;

		if (isLogToConsole && consoleLevel.ordinal() >= level.ordinal())
			consoleLogger.log(level, msg);

		if (isLogToFile && fileLevel.ordinal() >= level.ordinal()) {
			try {
				logFile.write(msg);
				logFile.newLine();
				logFile.flush();
			} catch (IOException e) {
				//
			}
		}
	}

	public void debug(String msg) {		
		log(LogLevel.DEBUG, msg);
	}

	public void info(String msg) {
		log(LogLevel.INFO, msg);
	}

	public void warn(String msg) {
		log(LogLevel.WARN, msg);
	}

	public void error(String msg) {
		log(LogLevel.ERROR, msg);
	}

	public void print(String msg) {
		if (isLogToConsole)
			consoleLogger.log(msg);

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
		return "log_3dcitydb_impexp_" +
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) +
				".log";
	}

}
