package de.tub.citydb.log;

import java.text.DecimalFormat;
import java.util.Calendar;

public class Logger {
	private static Logger INSTANCE = new Logger();
		
	private LogLevelType logLevel = LogLevelType.INFO;
	private Calendar cal;
	private DecimalFormat df = new DecimalFormat("00");
	
	private Logger() {
		// just to thwart instantiation
	}

	public static Logger getInstance() {
		return INSTANCE;
	}
	
	public void setLogLevel(LogLevelType logLevel) {
		this.logLevel = logLevel;
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
        prefix.append(type.value);
        prefix.append("] ");
        
        return prefix.toString();
	}
	
	private void log(LogLevelType type, String msg) {
		System.out.println(getPrefix(type) + msg);
	}
	
	public void debug(String msg) {
		if (logLevel.ordinal() <= LogLevelType.DEBUBG.ordinal())
			log(LogLevelType.DEBUBG, msg);
	}
	
	public void info(String msg) {
		if (logLevel.ordinal() <= LogLevelType.INFO.ordinal())
			log(LogLevelType.INFO, msg);
	}
	
	public void warn(String msg) {
		if (logLevel.ordinal() <= LogLevelType.WARN.ordinal())
			log(LogLevelType.WARN, msg);
	}
	
	public void error(String msg) {
		if (logLevel.ordinal() <= LogLevelType.ERROR.ordinal())
			log(LogLevelType.ERROR, msg);
	}
	
	public enum LogLevelType {
		DEBUBG("DEBUG"),
		INFO("INFO"),
		WARN("WARN"),
		ERROR("ERROR");
		
		private final String value;

		LogLevelType(String v) {
			value = v;
		}

		public String value() {
			return value;
		}
	};
}
