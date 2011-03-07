package de.tub.citydb.sax.events;

public class Location {
	private long lineNumber;
	private long columnNumber;
	
	public Location(long lineNumber, long columnNumber) {
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	public long getColumnNumber() {
		return columnNumber;
	}
	
}
