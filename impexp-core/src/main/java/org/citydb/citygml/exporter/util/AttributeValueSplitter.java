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
package org.citydb.citygml.exporter.util;

import org.citydb.util.CoreConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AttributeValueSplitter {
	private final Pattern defaultPattern = Pattern.compile(CoreConstants.DEFAULT_DELIMITER.replaceAll("\\\\", "\\\\\\\\"));
	private List<SplitValue> results = new ArrayList<>();	

	public List<SplitValue> split(Pattern pattern, String... values) {
		results.clear(); 
		if (values == null || values[0] == null)
			return results;

		String[][] items = new String[values.length][];
		for (int i = 0; i < values.length; i++) {
			items[i] = values[i] != null ? pattern.split(values[i]) : null;
		}

		if (items[0].length == 0) 
			return results;
		
		for (int i = 0; i < items[0].length; i++) {
			SplitValue splitValue = new SplitValue(values.length);
			for (int j = 0; j < values.length; j++) {
				if (j < items.length && items[j] != null) {
					String value = i < items[j].length ? items[j][i] : null;				
					splitValue.values[j] = value != null && value.length() > 0 ? value.trim() : null;
				} else
					splitValue.values[j] = null;
			}

			results.add(splitValue);
		}

		return results;
	}

	public List<SplitValue> split(String... values) {
		return split(defaultPattern, values);
	}
	
	public List<Double> splitDoubleList(Pattern pattern, String doubleList) {
		if (doubleList == null || doubleList.length() == 0)
			return null;

		List<Double> values = new ArrayList<Double>();
		String[] items = pattern.split(doubleList);
		if (items.length == 0)
			return values;
		
		for (String item : items) {
			try {
				values.add(Double.parseDouble(item));
			} catch (NumberFormatException e) {
				//
			}
		}
		
		return values;
	}
	
	public List<Double> splitDoubleList(String doubleList) {
		return splitDoubleList(Pattern.compile("\\s+"), doubleList);
	}

	public static class SplitValue {
		private String[] values;

		private SplitValue(int length) {
			values = new String[length];
		}

		public String result(int i) {
			if (i < 0 || i >= values.length)
				throw new IndexOutOfBoundsException("No split result " + i);

			return values[i];
		}

		public Double asDouble(int i) {
			try {
				return Double.parseDouble(result(i));
			} catch (NumberFormatException e) {
				return null;
			}
		}

		public Integer asInteger(int i) {
			try {
				return Integer.parseInt(result(i));
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}

}
