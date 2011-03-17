package de.tub.citydb.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.citygml4j.impl.jaxb.gml._3_1_1.CodeImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.AbstractFeature;
import org.citygml4j.model.gml.Code;
import org.citygml4j.model.gml.GMLClass;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Workspace;

public class Util {

	public static int cityObject2classId(CityGMLClass cityGMLClass) {
		int classId = 0;

		switch (cityGMLClass) {
		case BUILDING:
			classId = 26;
			break;
		case BUILDINGFURNITURE:
			classId = 40;
			break;
		case BUILDINGINSTALLATION:
			classId = 27;
			break;
		case BUILDINGPART:
			classId = 25;
			break;
		case CEILINGSURFACE:
			classId = 30;
			break;
		case CLOSURESURFACE:
			classId = 36;
			break;
		case DOOR:
			classId = 39;
			break;
		case FLOORSURFACE:
			classId = 32;
			break;
		case GENERICCITYOBJECT:
			classId = 5;
			break;
		case GROUNDSURFACE:
			classId = 35;
			break;
		case INTBUILDINGINSTALLATION:
			classId = 28;
			break;
		case INTERIORWALLSURFACE:
			classId = 31;
			break;
		case ROOFSURFACE:
			classId = 33;
			break;
		case ROOM:
			classId = 41;
			break;
		case WALLSURFACE:
			classId = 34;
			break;
		case WINDOW:
			classId = 38;
			break;
		case CITYFURNITURE:
			classId = 21;
			break;
		case LANDUSE:
			classId = 4;
			break;
		case WATERBODY:
			classId = 9;
			break;
		case WATERSURFACE:
			classId = 11;
			break;
		case WATERGROUNDSURFACE:
			classId = 12;
			break;
		case WATERCLOSURESURFACE:
			classId = 13;
			break;
		case SOLITARYVEGETATIONOBJECT:
			classId = 7;
			break;
		case PLANTCOVER:
			classId = 8;
			break;
		case TRANSPORTATIONCOMPLEX:
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
		case TRAFFICAREA:
			classId = 47;
			break;
		case AUXILIARYTRAFFICAREA:
			classId = 48;
			break;
		case CITYOBJECTGROUP:
			classId = 23;
			break;
		case RELIEFFEATURE:
			classId = 14;
			break;
		case TINRELIEF:
			classId = 16;
			break;
		case MASSPOINTRELIEF:
			classId = 17;
			break;
		case BREAKLINERELIEF:
			classId = 18;
			break;
		case RASTERRELIEF:
			classId = 19;
			break;
		}

		return classId;
	}

	public static CityGMLClass classId2cityObject(int classId) {
		CityGMLClass cityObjectType = CityGMLClass.UNDEFINED;

		switch (classId) {
		case 4:
			cityObjectType = CityGMLClass.LANDUSE;
			break;
		case 21:
			cityObjectType = CityGMLClass.CITYFURNITURE;
			break;
		case 26:
			cityObjectType = CityGMLClass.BUILDING;
			break;
		case 9:
			cityObjectType = CityGMLClass.WATERBODY;
			break;
		case 8:
			cityObjectType = CityGMLClass.PLANTCOVER;
			break;
		case 7:
			cityObjectType = CityGMLClass.SOLITARYVEGETATIONOBJECT;
			break;
		case 42:
			cityObjectType = CityGMLClass.TRANSPORTATIONCOMPLEX;
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
			cityObjectType = CityGMLClass.GENERICCITYOBJECT;
			break;
		case 23:
			cityObjectType = CityGMLClass.CITYOBJECTGROUP;
			break;
		case 14:
			cityObjectType = CityGMLClass.RELIEFFEATURE;
			break;
		case 16:
			cityObjectType = CityGMLClass.TINRELIEF;
			break;
		case 17:
			cityObjectType = CityGMLClass.MASSPOINTRELIEF;
			break;
		case 18:
			cityObjectType = CityGMLClass.BREAKLINERELIEF;
			break;
		case 19:
			cityObjectType = CityGMLClass.RASTERRELIEF;
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

}
