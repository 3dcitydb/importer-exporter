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
	
	public boolean isSupportedBy(DatabaseVersionSupport... supportedVersions) {
		for (DatabaseVersionSupport supportedVersion : supportedVersions) {
			if (supportedVersion.contains(this))
				return true;
		}
		
		return false;
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
	
	public int compareTo(int major, int minor) {
		return compareTo(major, minor, revision);
	}
	
	public int compareTo(int major) {
		return compareTo(major, minor, revision);
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
