package org.citydb.api.database;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseVersion implements Comparable<DatabaseVersion> {
	private final int major;
	private final int minor;
	private final int revision;
	private final String productVersion;

	public DatabaseVersion(int major, int minor, int revision, String productVersion) {
		this.major = major;
		this.minor = minor;
		this.revision = revision;
		this.productVersion = productVersion;
	}

	public DatabaseVersion(int major, int minor, int revision) {
		this(major, minor, revision, null);
	}

	public DatabaseVersion(String version) {
		Matcher matcher = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)?$").matcher(version);
		if (matcher.matches()) {
			major = Integer.valueOf(matcher.group(1));
			minor = Integer.valueOf(matcher.group(2));
			revision = Integer.valueOf(matcher.group(3));			
			productVersion = null;
		} else
			throw new IllegalArgumentException("Version string '" + version + "' must follow a 'major.minor.revision' pattern.");
	}

	public int getMajorVersion() {
		return major;
	}

	public int getMinorVersion() {
		return minor;
	}

	public int getRevisionVersion() {
		return revision;
	}
	
	public String getProductVersion() {
		return productVersion;
	}

	public String getVersionNumber() {
		return new StringBuilder().append(major).append('.').append(minor).append('.').append(revision).toString();
	}

	@Override
	public int compareTo(DatabaseVersion other) {
		return compareTo(other.major, other.minor, other.revision);
	}
	
	public int compareTo(int major, int minor, int revision) {
		int result = this.major - major;
		if (result != 0)
			return result;

		result = this.minor - minor;
		if (result != 0)
			return result;

		return this.revision - revision;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof DatabaseVersion))
			return false;

		DatabaseVersion other = (DatabaseVersion)obj;
		return (major == other.major) && (minor == other.minor) && (revision == other.revision);
	}

	@Override
	public String toString() {
		return productVersion != null ? productVersion : getVersionNumber();
	}

}
