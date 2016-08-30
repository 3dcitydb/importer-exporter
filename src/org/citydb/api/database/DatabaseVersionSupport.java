package org.citydb.api.database;

public class DatabaseVersionSupport {
	private final DatabaseVersion targetVersion;

	private DatabaseVersion fromVersion;
	private boolean isRevisionForwardCompatible;

	private DatabaseVersionSupport(DatabaseVersion targetVersion) {
		this.targetVersion = targetVersion;
	}

	public static DatabaseVersionSupport targetVersion(DatabaseVersion targetVersion) {
		return new DatabaseVersionSupport(targetVersion);
	}

	public static DatabaseVersionSupport targetVersion(int major, int minor, int revision) {
		return new DatabaseVersionSupport(new DatabaseVersion(major, minor, revision));
	}

	public DatabaseVersion getTargetVersion() {
		return targetVersion;
	}

	public DatabaseVersion getFromVersion() {
		return fromVersion;
	}

	public boolean isRevisionForwardCompatible() {
		return isRevisionForwardCompatible;
	}

	public DatabaseVersionSupport withBackwardsCompatibility(DatabaseVersion fromVersion) {
		if (targetVersion.compareTo(fromVersion) < 0)
			throw new IllegalArgumentException("The backwards compatible version must not be greater than the target version.");

		if (targetVersion.compareTo(fromVersion) > 0)
			this.fromVersion = fromVersion;

		return this;
	}
	
	public DatabaseVersionSupport withBackwardsCompatibility(int major, int minor, int revision) {
		return withBackwardsCompatibility(new DatabaseVersion(major, minor, revision));
	}

	public DatabaseVersionSupport withRevisionForwardCompatibility(boolean enable) {
		isRevisionForwardCompatible = enable;
		return this;
	}

	public boolean contains(DatabaseVersion version) {
		int result = isRevisionForwardCompatible ? targetVersion.compareTo(version.getMajorVersion(), version.getMinorVersion()) : targetVersion.compareTo(version);
	
		if (fromVersion == null && result != 0)
			return false;
	
		else if (fromVersion != null && (result < 0 || fromVersion.compareTo(version) > 0))
			return false;

		return true;
	}

	private String printTargetVersion() {
		if (isRevisionForwardCompatible) { 
			return new StringBuilder()
					.append(targetVersion.getMajorVersion()).append('.')
					.append(targetVersion.getMinorVersion()).append(".x").toString();
		} else
			return targetVersion.getVersionNumber();
	}

	@Override
	public String toString() {
		if (fromVersion != null) {
			return new StringBuilder()
					.append(printTargetVersion()).append(" - ")
					.append(fromVersion.getVersionNumber()).toString();
		} else
			return printTargetVersion();
	}
	
}
