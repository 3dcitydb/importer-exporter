/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.citygml4j.impl.gml.basicTypes.CodeImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.feature.AbstractFeature;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Workspace;
import java.util.GregorianCalendar;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.child.Child;

public class Util {

	public static int cityObject2classId(CityGMLClass cityGMLClass) {
		int classId = 0;

		switch (cityGMLClass) {
		case BUILDING:
			classId = 26;
			break;
		case BUILDING_FURNITURE:
			classId = 40;
			break;
		case BUILDING_INSTALLATION:
			classId = 27;
			break;
		case BUILDING_PART:
			classId = 25;
			break;
		case CEILING_SURFACE:
			classId = 30;
			break;
		case CLOSURE_SURFACE:
			classId = 36;
			break;
		case DOOR:
			classId = 39;
			break;
		case FLOOR_SURFACE:
			classId = 32;
			break;
		case GENERIC_CITY_OBJECT:
			classId = 5;
			break;
		case GROUND_SURFACE:
			classId = 35;
			break;
		case INT_BUILDING_INSTALLATION:
			classId = 28;
			break;
		case INTERIOR_WALL_SURFACE:
			classId = 31;
			break;
		case ROOF_SURFACE:
			classId = 33;
			break;
		case ROOM:
			classId = 41;
			break;
		case WALL_SURFACE:
			classId = 34;
			break;
		case WINDOW:
			classId = 38;
			break;
		case CITY_FURNITURE:
			classId = 21;
			break;
		case LAND_USE:
			classId = 4;
			break;
		case WATER_BODY:
			classId = 9;
			break;
		case WATER_SURFACE:
			classId = 11;
			break;
		case WATER_GROUND_SURFACE:
			classId = 12;
			break;
		case WATER_CLOSURE_SURFACE:
			classId = 13;
			break;
		case SOLITARY_VEGETATION_OBJECT:
			classId = 7;
			break;
		case PLANT_COVER:
			classId = 8;
			break;
		case TRANSPORTATION_COMPLEX:
			classId = 42;
			break;
		case TRACK:
			classId = 43;
			break;
		case RAILWAY:
			classId = 44;
			break;
		case ROAD:
			classId = 45;
			break;
		case SQUARE:
			classId = 46;
			break;
		case TRAFFIC_AREA:
			classId = 47;
			break;
		case AUXILIARY_TRAFFIC_AREA:
			classId = 48;
			break;
		case CITY_OBJECT_GROUP:
			classId = 23;
			break;
		case RELIEF_FEATURE:
			classId = 14;
			break;
		case TIN_RELIEF:
			classId = 16;
			break;
		case MASSPOINT_RELIEF:
			classId = 17;
			break;
		case BREAKLINE_RELIEF:
			classId = 18;
			break;
		case RASTER_RELIEF:
			classId = 19;
			break;
		}

		return classId;
	}

	public static CityGMLClass classId2cityObject(int classId) {
		CityGMLClass cityObjectType = CityGMLClass.UNDEFINED;

		switch (classId) {
		case 4:
			cityObjectType = CityGMLClass.LAND_USE;
			break;
		case 21:
			cityObjectType = CityGMLClass.CITY_FURNITURE;
			break;
		case 26:
			cityObjectType = CityGMLClass.BUILDING;
			break;
		case 9:
			cityObjectType = CityGMLClass.WATER_BODY;
			break;
		case 8:
			cityObjectType = CityGMLClass.PLANT_COVER;
			break;
		case 7:
			cityObjectType = CityGMLClass.SOLITARY_VEGETATION_OBJECT;
			break;
		case 42:
			cityObjectType = CityGMLClass.TRANSPORTATION_COMPLEX;
			break;
		case 43:
			cityObjectType = CityGMLClass.TRACK;
			break;
		case 44:
			cityObjectType = CityGMLClass.RAILWAY;
			break;
		case 45:
			cityObjectType = CityGMLClass.ROAD;
			break;
		case 46:
			cityObjectType = CityGMLClass.SQUARE;
			break;
		case 5:
			cityObjectType = CityGMLClass.GENERIC_CITY_OBJECT;
			break;
		case 23:
			cityObjectType = CityGMLClass.CITY_OBJECT_GROUP;
			break;
		case 14:
			cityObjectType = CityGMLClass.RELIEF_FEATURE;
			break;
		case 16:
			cityObjectType = CityGMLClass.TIN_RELIEF;
			break;
		case 17:
			cityObjectType = CityGMLClass.MASSPOINT_RELIEF;
			break;
		case 18:
			cityObjectType = CityGMLClass.BREAKLINE_RELIEF;
			break;
		case 19:
			cityObjectType = CityGMLClass.RASTER_RELIEF;
			break;
		}

		return cityObjectType;
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
		StringBuffer string = new StringBuffer();

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

	public static String[] gmlName2dbString(AbstractFeature feature) {
		String[] dbGmlName = new String[2];

		dbGmlName[0] = null;
		dbGmlName[1] = null;

		if (feature.isSetName()) {
			List<String> gmlNameList = new ArrayList<String>();
			List<String> gmlNameCodespaceList = new ArrayList<String>();

			for (Code code : feature.getName()) {
				String name = code.getValue();
				String codespace = code.getCodeSpace();

				if (name != null)
					name = name.trim();

				gmlNameList.add(name);
				gmlNameCodespaceList.add(codespace);
			}

			dbGmlName[0] = Util.collection2string(gmlNameList, Internal.GML_NAME_DELIMITER);
			dbGmlName[1] = Util.collection2string(gmlNameCodespaceList, Internal.GML_NAME_DELIMITER);
		}

		return dbGmlName;
	}

	public static void dbGmlName2featureName(AbstractFeature feature, String dbGmlName, String dbGmlCodeSpace) {
		// this is weird, isn't it...
		String delimiter = Internal.GML_NAME_DELIMITER.replaceAll("\\\\", "\\\\\\\\");

		// decompose gml:name
		List<String> gmlNameList = Util.string2string(dbGmlName, delimiter);
		List<String> gmlNameCodespaceList = Util.string2string(dbGmlCodeSpace, delimiter);

		if (gmlNameList != null && gmlNameList.size() != 0) {
			for (int i = 0; i < gmlNameList.size(); i++) {
				Code code = new CodeImpl();
				code.setValue(gmlNameList.get(i));

				if (gmlNameCodespaceList != null && gmlNameCodespaceList.size() >= i + 1) {
					String codeSpace = gmlNameCodespaceList.get(i);

					if (codeSpace != null && codeSpace.length() != 0)
						code.setCodeSpace(codeSpace);
				}

				feature.addName(code);
			}
		}
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
		if (null == cityObject) return null;

		if (cityObject.isSetCreationDate()) {
			return cityObject.getCreationDate();
		}

		if (checkParents && cityObject.isSetParent()) {
			Object parent = cityObject.getParent();
			while (null != parent) {
				if (parent instanceof AbstractCityObject) {
					return getCreationDate((AbstractCityObject)parent, true);
				}

				if (!(parent instanceof Child)) {
					break;
				}

				Child child = (Child)parent;
				if (!child.isSetParent()) {
					break;
				}

				parent = child.getParent();
			}
		}

		return null;
	}

	public static GregorianCalendar getTerminationDate(AbstractCityObject cityObject, boolean checkParents) {
		if (null == cityObject) return null;

		if (cityObject.isSetTerminationDate()) {
			return cityObject.getTerminationDate();
		}

		if (checkParents && cityObject.isSetParent()) {
			Object parent = cityObject.getParent();
			while (null != parent) {
				if (parent instanceof AbstractCityObject) {
					return getTerminationDate((AbstractCityObject)parent, true);
				}

				if (!(parent instanceof Child)) {
					break;
				}

				Child child = (Child)parent;
				if (!child.isSetParent()) {
					break;
				}

				parent = child.getParent();
			}
		}

		return null;
	}
}
