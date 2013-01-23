/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import de.tub.citydb.api.controller.LogController;
import de.tub.citydb.api.log.LogLevel;

public class Logger implements LogController {
	private static Logger INSTANCE = new Logger();

	private LogLevel consoleLogLevel = LogLevel.INFO;
	private LogLevel fileLogLevel = LogLevel.INFO;

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
		
		System.out.println(buffer.toString());
		writeToFile(buffer.toString());
	}

	@Override
	public void print(String msg) {
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
