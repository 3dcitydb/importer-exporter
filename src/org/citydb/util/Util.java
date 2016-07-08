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
package org.citydb.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import org.citydb.config.internal.Internal;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.exporter.CityGMLVersionType;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLVersion;

public class Util {
	private static final EnumMap<CityGMLClass, Integer> cityGMLClassMap = new EnumMap<CityGMLClass, Integer>(CityGMLClass.class);

	static {
		cityGMLClassMap.put(CityGMLClass.LAND_USE, 4);
		cityGMLClassMap.put(CityGMLClass.GENERIC_CITY_OBJECT, 5);
		cityGMLClassMap.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, 7);
		cityGMLClassMap.put(CityGMLClass.PLANT_COVER, 8);
		cityGMLClassMap.put(CityGMLClass.WATER_BODY, 9);
		cityGMLClassMap.put(CityGMLClass.WATER_SURFACE, 11);
		cityGMLClassMap.put(CityGMLClass.WATER_GROUND_SURFACE, 12);
		cityGMLClassMap.put(CityGMLClass.WATER_CLOSURE_SURFACE, 13);
		cityGMLClassMap.put(CityGMLClass.RELIEF_FEATURE, 14);
		cityGMLClassMap.put(CityGMLClass.TIN_RELIEF, 16);
		cityGMLClassMap.put(CityGMLClass.MASSPOINT_RELIEF, 17);
		cityGMLClassMap.put(CityGMLClass.BREAKLINE_RELIEF, 18);
		cityGMLClassMap.put(CityGMLClass.RASTER_RELIEF, 19);
		cityGMLClassMap.put(CityGMLClass.CITY_FURNITURE, 21);
		cityGMLClassMap.put(CityGMLClass.CITY_OBJECT_GROUP, 23);		
		cityGMLClassMap.put(CityGMLClass.BUILDING_PART, 25);
		cityGMLClassMap.put(CityGMLClass.BUILDING, 26);		
		cityGMLClassMap.put(CityGMLClass.BUILDING_INSTALLATION, 27);
		cityGMLClassMap.put(CityGMLClass.INT_BUILDING_INSTALLATION, 28);
		cityGMLClassMap.put(CityGMLClass.BUILDING_CEILING_SURFACE, 30);
		cityGMLClassMap.put(CityGMLClass.INTERIOR_BUILDING_WALL_SURFACE, 31);
		cityGMLClassMap.put(CityGMLClass.BUILDING_FLOOR_SURFACE, 32);
		cityGMLClassMap.put(CityGMLClass.BUILDING_ROOF_SURFACE, 33);
		cityGMLClassMap.put(CityGMLClass.BUILDING_WALL_SURFACE, 34);
		cityGMLClassMap.put(CityGMLClass.BUILDING_GROUND_SURFACE, 35);
		cityGMLClassMap.put(CityGMLClass.BUILDING_CLOSURE_SURFACE, 36);
		cityGMLClassMap.put(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE, 60);
		cityGMLClassMap.put(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE, 61);
		cityGMLClassMap.put(CityGMLClass.BUILDING_WINDOW, 38);
		cityGMLClassMap.put(CityGMLClass.BUILDING_DOOR, 39);
		cityGMLClassMap.put(CityGMLClass.BUILDING_FURNITURE, 40);
		cityGMLClassMap.put(CityGMLClass.BUILDING_ROOM, 41);
		cityGMLClassMap.put(CityGMLClass.TRANSPORTATION_COMPLEX, 42);
		cityGMLClassMap.put(CityGMLClass.TRACK, 43);
		cityGMLClassMap.put(CityGMLClass.RAILWAY, 44);
		cityGMLClassMap.put(CityGMLClass.ROAD, 45);
		cityGMLClassMap.put(CityGMLClass.SQUARE, 46);
		cityGMLClassMap.put(CityGMLClass.TRAFFIC_AREA, 47);
		cityGMLClassMap.put(CityGMLClass.AUXILIARY_TRAFFIC_AREA, 48);
		cityGMLClassMap.put(CityGMLClass.APPEARANCE, 50);
		cityGMLClassMap.put(CityGMLClass.X3D_MATERIAL, 53);
		cityGMLClassMap.put(CityGMLClass.PARAMETERIZED_TEXTURE, 54);
		cityGMLClassMap.put(CityGMLClass.GEOREFERENCED_TEXTURE, 55);
		cityGMLClassMap.put(CityGMLClass.CITY_MODEL, 57);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_PART, 63);
		cityGMLClassMap.put(CityGMLClass.BRIDGE, 64);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_INSTALLATION, 65);
		cityGMLClassMap.put(CityGMLClass.INT_BRIDGE_INSTALLATION, 66);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_CEILING_SURFACE, 68);
		cityGMLClassMap.put(CityGMLClass.INTERIOR_BRIDGE_WALL_SURFACE, 69);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_FLOOR_SURFACE, 70);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_ROOF_SURFACE, 71);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_WALL_SURFACE, 72);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_GROUND_SURFACE, 73);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_CLOSURE_SURFACE, 74);
		cityGMLClassMap.put(CityGMLClass.OUTER_BRIDGE_CEILING_SURFACE, 75);
		cityGMLClassMap.put(CityGMLClass.OUTER_BRIDGE_FLOOR_SURFACE, 76);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_WINDOW, 78);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_DOOR, 79);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_FURNITURE, 80);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_ROOM, 81);
		cityGMLClassMap.put(CityGMLClass.BRIDGE_CONSTRUCTION_ELEMENT, 82);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_PART, 84);
		cityGMLClassMap.put(CityGMLClass.TUNNEL, 85);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_INSTALLATION, 86);
		cityGMLClassMap.put(CityGMLClass.INT_TUNNEL_INSTALLATION, 87);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_CEILING_SURFACE, 89);
		cityGMLClassMap.put(CityGMLClass.INTERIOR_TUNNEL_WALL_SURFACE, 90);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_FLOOR_SURFACE, 91);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_ROOF_SURFACE, 92);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_WALL_SURFACE, 93);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_GROUND_SURFACE, 94);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_CLOSURE_SURFACE, 95);
		cityGMLClassMap.put(CityGMLClass.OUTER_TUNNEL_CEILING_SURFACE, 96);
		cityGMLClassMap.put(CityGMLClass.OUTER_TUNNEL_FLOOR_SURFACE, 97);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_WINDOW, 99);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_DOOR, 100);
		cityGMLClassMap.put(CityGMLClass.TUNNEL_FURNITURE, 101);
		cityGMLClassMap.put(CityGMLClass.HOLLOW_SPACE, 102);
	}

	public static int cityObject2classId(CityGMLClass cityGMLClass) {
		try {
			return cityGMLClassMap.get(cityGMLClass);
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public static CityGMLClass classId2cityObject(int classId) {
		for (Entry<CityGMLClass, Integer> entry : cityGMLClassMap.entrySet())
			if (entry.getValue() == classId)
				return entry.getKey();

		return CityGMLClass.UNDEFINED;
	}

	public static List<Double> string2double(String input, String delimiter) {
		if (input == null || input.length() == 0)
			return null;

		List<Double> values = new ArrayList<Double>();

		try {
			String[] split = input.split(delimiter);
			if (split != null && split.length != 0) {
				for (String s : split) {
					Double value = null;

					try {
						value = Double.parseDouble(s);
					} catch (NumberFormatException nfe) {
						//
					}

					if (value != null)
						values.add(value);
				}
			}
		} catch (PatternSyntaxException pE) {
			//
		}

		if (values.size() != 0)
			return values;
		else
			return null;
	}

	public static List<Integer> string2int(String input, String delimiter) {
		if (input == null || input.length() == 0)
			return null;

		List<Integer> values = new ArrayList<Integer>();

		try {
			String[] split = input.split(delimiter);
			if (split != null && split.length != 0) {
				for (String s : split) {
					Integer value = null;

					try {
						value = Integer.parseInt(s);
					} catch (NumberFormatException nfe) {
						//
					}

					if (value != null)
						values.add(value);
				}
			}
		} catch (PatternSyntaxException pE) {
			//
		}

		if (values.size() != 0)
			return values;
		else
			return null;
	}

	public static List<String> string2string(String input, String delimiter) {
		if (input == null || input.length() == 0)
			return null;

		List<String> values = new ArrayList<String>();

		try {
			String[] split = input.split(delimiter);
			if (split != null && split.length != 0) {
				for (String s : split)
					values.add(s);
			}
		} catch (PatternSyntaxException pE) {
			//
		}

		if (values.size() != 0)
			return values;
		else
			return null;
	}

	public static <T> String collection2string(Collection<T> list, String delimiter) {
		StringBuilder string = new StringBuilder();

		int i = 1;
		for (T object : list) {
			if (object != null)
				string.append(object.toString());

			if (i < list.size())
				string.append(delimiter);

			i++;
		}

		return string.toString();
	}

	public static boolean isRemoteXlink(String xlink) {
		URL url = null;

		try {
			url = new URL(xlink);
		} catch (MalformedURLException e) {
			//
		}

		return url != null;
	}

	public static String[] codeList2string(List<Code> codeList) {
		String[] result = new String[2];

		if (!codeList.isEmpty()) {
			List<String> values = new ArrayList<String>(codeList.size());
			List<String> codespaces = new ArrayList<String>(codeList.size());

			for (Code code : codeList) {
				String value = code.getValue().trim();
				String codespace = code.getCodeSpace();
				if (value != null && value.length() != 0) {
					values.add(value.trim());
					codespaces.add(codespace);
				}
			}

			if (!values.isEmpty()) {
				result[0] = collection2string(values, Internal.CODELIST_DELIMITER);
				result[1] = collection2string(codespaces, Internal.CODELIST_DELIMITER);
			}
		}

		return result;
	}

	public static List<Code> string2codeList(String values, String codespaces) {
		List<Code> codeList = null;
		String delimiter = Internal.CODELIST_DELIMITER.replaceAll("\\\\", "\\\\\\\\");

		// decompose values and codespaces
		List<String> valueList = string2string(values, delimiter);
		List<String> codespaceList = Util.string2string(codespaces, delimiter);

		if (valueList != null && valueList.size() > 0) {
			codeList = new ArrayList<Code>(valueList.size());

			for (int i = 0; i < valueList.size(); i++) {
				Code code = new Code();
				code.setValue(valueList.get(i));

				if (codespaceList != null && codespaceList.size() > i) {
					String codeSpace = codespaceList.get(i);
					if (codeSpace != null && codeSpace.length() > 0)
						code.setCodeSpace(codeSpace);
				}

				codeList.add(code);
			}
		} else
			codeList = Collections.emptyList();

		return codeList;
	}

	public static String buildInOperator(Collection<? extends Object> items, String columnName, String logicalOperator, int maxItems) {		
		StringBuilder predicate = new StringBuilder();
		if (items.size() == 1)
			return predicate.append(columnName).append(" = ").append(items.iterator().next()).toString();

		if (items.size() > maxItems)
			predicate.append("(");

		predicate.append(columnName).append(" in (");
		Iterator<? extends Object> iter = items.iterator();
		int i = 0;

		while (iter.hasNext()) {
			predicate.append(iter.next());

			if (iter.hasNext()) {
				if (++i == maxItems) {
					predicate.append(") ").append(logicalOperator).append(" ").append(columnName).append(" in (");
					i = 0;
				} else
					predicate.append(", ");
			}
		}

		predicate.append(")");
		if (items.size() > maxItems)
			predicate.append(")");

		return predicate.toString();
	}

	public static String getFeatureSignature(CityGMLClass featureType, String gmlId) {
		StringBuilder sig = new StringBuilder(featureType.toString());

		if (gmlId != null) {
			sig.append(" '");
			sig.append(gmlId);
			sig.append('\'');
		} else
			sig.append(" (unknown gml:id)");

		return sig.toString();	
	}

	public static String getGeometrySignature(GMLClass geometryType, String gmlId) {
		StringBuilder sig = new StringBuilder("gml:");
		sig.append(geometryType.toString());

		if (gmlId != null) {
			sig.append(" '");
			sig.append(gmlId);
			sig.append('\'');
		} else
			sig.append(" (unknown gml:id)");

		return sig.toString();	
	}

	public static String getFileExtension(String file) {
		String ext = null;
		int i = file.lastIndexOf('.'); 
		if (i > 0 &&  i < file.length() - 1)
			ext = file.substring(i + 1).toLowerCase();

		return ext;
	}

	public static String stripFileExtension(String file) {
		int i = file.lastIndexOf('.'); 
		if (i > 0 &&  i < file.length() - 1)
			file = file.substring(0, i);

		return file;
	}

	public static String formatElapsedTime(long millis) {
		long d = TimeUnit.MILLISECONDS.toDays(millis);
		long h = TimeUnit.MILLISECONDS.toHours(millis) % TimeUnit.DAYS.toHours(1);
		long m = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
		long s = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);

		if (d > 0)
			return String.format("%02d d, %02d h, %02d m, %02d s", d, h, m, s);
		if (h > 0)
			return String.format("%02d h, %02d m, %02d s", h, m, s);
		if (m > 0)
			return String.format("%02d m, %02d s", m, s);

		return String.format("%02d s", s);
	}

	public static boolean checkWorkspaceTimestamp(Workspace workspace) {
		String timestamp = workspace.getTimestamp().trim();
		boolean success = true;

		if (timestamp.length() > 0) {		
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			format.setLenient(false);
			try {
				format.parse(timestamp);				
			} catch (java.text.ParseException e) {
				success = false;
			}
		}

		workspace.setTimestamp(timestamp);
		return success;
	}

	public static GregorianCalendar getCreationDate(AbstractCityObject cityObject, boolean checkParents) {
		if (cityObject == null)
			return null;

		if (cityObject.isSetCreationDate())
			return cityObject.getCreationDate();

		if (checkParents) {
			Child child = cityObject;
			ModelObject parent = null;

			while ((parent = child.getParent()) != null) {
				if (parent instanceof AbstractCityObject && ((AbstractCityObject)parent).isSetCreationDate())
					return ((AbstractCityObject)parent).getCreationDate();
				else if (parent instanceof Child)
					child = (Child)parent;
				else 
					break;
			}
		}

		return null;
	}

	public static GregorianCalendar getTerminationDate(AbstractCityObject cityObject, boolean checkParents) {
		if (cityObject == null)
			return null;

		if (cityObject.isSetTerminationDate())
			return cityObject.getTerminationDate();

		if (checkParents) {
			Child child = cityObject;
			ModelObject parent = null;

			while ((parent = child.getParent()) != null) {
				if (parent instanceof AbstractCityObject && ((AbstractCityObject)parent).isSetTerminationDate())
					return ((AbstractCityObject)parent).getTerminationDate();
				else if (parent instanceof Child)
					child = (Child)parent;
				else 
					break;
			}
		}

		return null;
	}

	public static CityGMLVersion toCityGMLVersion(CityGMLVersionType version) {
		switch (version) {
		case v1_0_0:
			return CityGMLVersion.v1_0_0;
		default:
			return CityGMLVersion.v2_0_0;
		}
	}

	public static CityGMLVersionType fromCityGMLVersion(CityGMLVersion version) {
		if (version == CityGMLVersion.v1_0_0)
			return CityGMLVersionType.v1_0_0;
		else
			return CityGMLVersionType.v2_0_0;
	}

}
