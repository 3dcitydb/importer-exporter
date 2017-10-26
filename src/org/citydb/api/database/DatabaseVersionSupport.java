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
