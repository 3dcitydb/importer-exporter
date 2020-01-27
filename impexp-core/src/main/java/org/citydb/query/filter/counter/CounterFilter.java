/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.query.filter.counter;

public class CounterFilter {
	private long count = -1;
	private long startIndex = -1;
	private long startId = -1;

	public CounterFilter() {
	}

	public boolean isSetCount() {
		return count != -1;
	}
	
	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = Math.max(count, -1);
	}

	public boolean isSetStartIndex() {
		return startIndex != -1;
	}

	public long getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(long startIndex) {
		this.startIndex = Math.max(startIndex, -1);
		startId = -1;
	}

	public boolean isSetStartId() {
		return startId != -1;
	}

	public long getStartId() {
		return startId;
	}

	public void setStartId(long startId) {
		this.startId = Math.max(startId, -1);
		startIndex = -1;
	}
}
