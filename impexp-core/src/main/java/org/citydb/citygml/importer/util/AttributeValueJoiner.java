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
package org.citydb.citygml.importer.util;

import org.citydb.util.CoreConstants;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

public class AttributeValueJoiner {
	private StringJoiner[] joiner;

	@SafeVarargs
	public final <T> AttributeValueJoiner join(String delimiter, List<T> values, Function<T, String>... mapper) {
		joiner = new StringJoiner[mapper.length];
		Arrays.setAll(joiner, i -> new StringJoiner(delimiter));

		if (values != null) {
			for (T value : values) {
				if (value == null)
					continue;

				for (int i = 0; i < mapper.length; i++) {
					String item = mapper[i].apply(value);
					if (i == 0 && (item == null || item.length() == 0))
						break;

					joiner[i].add(item != null ? item.trim() : "");
				}
			}
		}

		return this;
	}

	@SafeVarargs
	public final <T> AttributeValueJoiner join(List<T> values, Function<T, String>... mapper) {
		return join(CoreConstants.DEFAULT_DELIMITER, values, mapper);
	}

	public <T> String join(String delimiter, List<T> values) {
		return join(delimiter, values, Object::toString).result(0);
	}

	public <T> String join(List<T> values) {
		return join(CoreConstants.DEFAULT_DELIMITER, values, Object::toString).result(0);
	}

	public String result(int i) {
		if (joiner == null)
			throw new IllegalStateException("No join results found");
		if (i < 0 || i >= joiner.length)
			throw new IndexOutOfBoundsException("No join result " + i);

		String result = joiner[i].length() != 0 ? joiner[i].toString() : null;
		joiner[i] = null;		
		return result;
	}

}
