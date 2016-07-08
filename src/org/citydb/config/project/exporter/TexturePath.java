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
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="TexturePathType", propOrder={
		"relativePath",
		"absolutePath",
		"noOfBuckets"
})
public class TexturePath {
	@XmlAttribute
	private TexturePathMode mode = TexturePathMode.RELATIVE;
	@XmlAttribute
	private boolean useBuckets = false;
	private String relativePath = "appearance";
	private String absolutePath = "";
	private Integer noOfBuckets;

	public boolean isAbsolute() {
		return mode == TexturePathMode.ABSOLUTE;
	}

	public boolean isRelative() {
		return mode == TexturePathMode.RELATIVE;
	}

	public TexturePathMode getMode() {
		return mode;
	}

	public void setMode(TexturePathMode mode) {
		this.mode = mode;
	}

	public boolean isUseBuckets() {
		return useBuckets;
	}

	public void setUseBuckets(boolean useBuckets) {
		this.useBuckets = useBuckets;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public int getNoOfBuckets() {
		if (noOfBuckets == null)
			return 0;

		return noOfBuckets.intValue();
	}

	public void setNoOfBuckets(Integer noOfBuckets) {
		if (noOfBuckets != null)
			this.noOfBuckets = noOfBuckets;
	}

}
