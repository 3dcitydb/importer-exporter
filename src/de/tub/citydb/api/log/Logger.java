/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.api.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import de.tub.citydb.api.controller.LogController;

public class Logger implements LogController {
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
