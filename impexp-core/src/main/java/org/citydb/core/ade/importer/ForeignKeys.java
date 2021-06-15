/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.ade.importer;

import java.util.HashMap;

public class ForeignKeys {
	public static final ForeignKeys EMPTY_SET = new ForeignKeys(true);	
	private HashMap<String, Long> foreignKeys;

	private ForeignKeys(boolean isDefault) {
		if (!isDefault)
			foreignKeys = new HashMap<>();
	}

	public static final synchronized ForeignKeys create() {
		return new ForeignKeys(false);
	}

	public ForeignKeys with(String name, long value) {
		if (this != EMPTY_SET)
			foreignKeys.put(name, value);

		return this;
	}

	public boolean isEmpty() {
		return this != EMPTY_SET ? foreignKeys.isEmpty() : true;
	}

	public boolean contains(String name) {
		return this != EMPTY_SET ? foreignKeys.containsKey(name) : false;
	}

	public long get(String name) {
		if (this != EMPTY_SET) {
			Long value = foreignKeys.get(name);
			return value != null ? value.longValue() : 0;
		} else
			return 0;
	}
}
