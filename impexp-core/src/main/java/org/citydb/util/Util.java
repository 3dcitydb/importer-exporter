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
package org.citydb.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.log.Logger;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.AbstractTextureParameterization;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.Bridge;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgePart;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.CeilingSurface;
import org.citygml4j.model.citygml.building.ClosureSurface;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.FloorSurface;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.InteriorWallSurface;
import org.citygml4j.model.citygml.building.OuterCeilingSurface;
import org.citygml4j.model.citygml.building.OuterFloorSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.building.Window;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AbstractSite;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.BreaklineRelief;
import org.citygml4j.model.citygml.relief.MassPointRelief;
import org.citygml4j.model.citygml.relief.RasterRelief;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.relief.TINRelief;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.Railway;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.Square;
import org.citygml4j.model.citygml.transportation.Track;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.Tunnel;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.tunnel.TunnelPart;
import org.citygml4j.model.citygml.vegetation.AbstractVegetationObject;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.AbstractWaterObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.citygml.waterbody.WaterClosureSurface;
import org.citygml4j.model.citygml.waterbody.WaterGroundSurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.AbstractFeatureCollection;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;

public class Util {
	private static final HashMap<Class<? extends AbstractGML>, Integer> objectClassIds = new HashMap<>();

	static {
		objectClassIds.put(AbstractGML.class, 1);
		objectClassIds.put(AbstractFeature.class, 2);
		objectClassIds.put(AbstractCityObject.class, 3);
		objectClassIds.put(LandUse.class, 4);
		objectClassIds.put(GenericCityObject.class, 5);
		objectClassIds.put(AbstractVegetationObject.class, 6);		
		objectClassIds.put(SolitaryVegetationObject.class, 7);
		objectClassIds.put(PlantCover.class, 8);
		objectClassIds.put(WaterBody.class, 9);
		objectClassIds.put(AbstractWaterBoundarySurface.class, 10);
		objectClassIds.put(WaterSurface.class, 11);
		objectClassIds.put(WaterGroundSurface.class, 12);
		objectClassIds.put(WaterClosureSurface.class, 13);
		objectClassIds.put(ReliefFeature.class, 14);
		objectClassIds.put(AbstractReliefComponent.class, 15);
		objectClassIds.put(TINRelief.class, 16);
		objectClassIds.put(MassPointRelief.class, 17);
		objectClassIds.put(BreaklineRelief.class, 18);
		objectClassIds.put(RasterRelief.class, 19);
		objectClassIds.put(AbstractSite.class, 20);
		objectClassIds.put(CityFurniture.class, 21);
		objectClassIds.put(AbstractTransportationObject.class, 22);
		objectClassIds.put(CityObjectGroup.class, 23);
		objectClassIds.put(AbstractBuilding.class, 24);
		objectClassIds.put(BuildingPart.class, 25);
		objectClassIds.put(Building.class, 26);		
		objectClassIds.put(BuildingInstallation.class, 27);
		objectClassIds.put(IntBuildingInstallation.class, 28);
		objectClassIds.put(AbstractBoundarySurface.class, 29);
		objectClassIds.put(CeilingSurface.class, 30);
		objectClassIds.put(InteriorWallSurface.class, 31);
		objectClassIds.put(FloorSurface.class, 32);
		objectClassIds.put(RoofSurface.class, 33);
		objectClassIds.put(WallSurface.class, 34);
		objectClassIds.put(GroundSurface.class, 35);
		objectClassIds.put(ClosureSurface.class, 36);
		objectClassIds.put(AbstractOpening.class, 37);
		objectClassIds.put(Window.class, 38);
		objectClassIds.put(Door.class, 39);
		objectClassIds.put(BuildingFurniture.class, 40);
		objectClassIds.put(Room.class, 41);
		objectClassIds.put(TransportationComplex.class, 42);
		objectClassIds.put(Track.class, 43);
		objectClassIds.put(Railway.class, 44);
		objectClassIds.put(Road.class, 45);
		objectClassIds.put(Square.class, 46);
		objectClassIds.put(TrafficArea.class, 47);
		objectClassIds.put(AuxiliaryTrafficArea.class, 48);
		objectClassIds.put(AbstractFeatureCollection.class, 49);
		objectClassIds.put(Appearance.class, 50);
		objectClassIds.put(AbstractSurfaceData.class, 51);
		objectClassIds.put(AbstractTexture.class, 52);
		objectClassIds.put(X3DMaterial.class, 53);
		objectClassIds.put(ParameterizedTexture.class, 54);
		objectClassIds.put(GeoreferencedTexture.class, 55);
		objectClassIds.put(AbstractTextureParameterization.class, 56);
		objectClassIds.put(CityModel.class, 57);
		objectClassIds.put(Address.class, 58);
		objectClassIds.put(ImplicitGeometry.class, 59);		
		objectClassIds.put(OuterCeilingSurface.class, 60);
		objectClassIds.put(OuterFloorSurface.class, 61);
		objectClassIds.put(AbstractBridge.class, 62);
		objectClassIds.put(BridgePart.class, 63);
		objectClassIds.put(Bridge.class, 64);
		objectClassIds.put(BridgeInstallation.class, 65);
		objectClassIds.put(IntBridgeInstallation.class, 66);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.AbstractBoundarySurface.class, 67);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.CeilingSurface.class, 68);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.InteriorWallSurface.class, 69);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.FloorSurface.class, 70);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.RoofSurface.class, 71);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.WallSurface.class, 72);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.GroundSurface.class, 73);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.ClosureSurface.class, 74);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.OuterCeilingSurface.class, 75);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.OuterFloorSurface.class, 76);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.AbstractOpening.class, 77);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.Window.class, 78);
		objectClassIds.put(org.citygml4j.model.citygml.bridge.Door.class, 79);
		objectClassIds.put(BridgeFurniture.class, 80);
		objectClassIds.put(BridgeRoom.class, 81);
		objectClassIds.put(BridgeConstructionElement.class, 82);
		objectClassIds.put(AbstractTunnel.class, 83);
		objectClassIds.put(TunnelPart.class, 84);
		objectClassIds.put(Tunnel.class, 85);
		objectClassIds.put(TunnelInstallation.class, 86);
		objectClassIds.put(IntTunnelInstallation.class, 87);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface.class, 88);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.CeilingSurface.class, 89);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.InteriorWallSurface.class, 90);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.FloorSurface.class, 91);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.RoofSurface.class, 92);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.WallSurface.class, 93);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.GroundSurface.class, 94);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.ClosureSurface.class, 95);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.OuterCeilingSurface.class, 96);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.OuterFloorSurface.class, 97);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.AbstractOpening.class, 98);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.Window.class, 99);
		objectClassIds.put(org.citygml4j.model.citygml.tunnel.Door.class, 100);
		objectClassIds.put(TunnelFurniture.class, 101);
		objectClassIds.put(HollowSpace.class, 102);
		objectClassIds.put(TexCoordList.class, 103);
		objectClassIds.put(TexCoordGen.class, 104);
		objectClassIds.put(AbstractWaterObject.class, 105);
	}
	
	public static int getObjectClassId(Class<? extends AbstractGML> objectClass) {
		if (ADEModelObject.class.isAssignableFrom(objectClass)) {
			ADEExtension extension = ADEExtensionManager.getInstance().getExtensionByObject(objectClass.asSubclass(ADEModelObject.class));
			return extension.getADEObjectMapper().getObjectClassId(objectClass);
		} else {
			Integer objectClassId = objectClassIds.get(objectClass);
			return objectClassId != null ? objectClassId.intValue() : 0;
		}
	}
		
	public static AbstractGML createObject(int objectClassId, CityGMLVersion version) {
		ADEExtension extension = ADEExtensionManager.getInstance().getExtensionByObjectClassId(objectClassId);
		if (extension != null)
			return extension.getADEObjectMapper().createObject(objectClassId, version);
		else {
			for (Entry<Class<? extends AbstractGML>, Integer> entry : objectClassIds.entrySet()) {
				if (entry.getValue() == objectClassId && !Modifier.isAbstract(entry.getKey().getModifiers())) {
					try {
						return entry.getKey().newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						// 
					}
				}
			}
		}
		
		return null;
	}
	
	public static CityGMLClass getCityGMLClass(int objectClassId) {
		for (Entry<Class<? extends AbstractGML>, Integer> entry : objectClassIds.entrySet()) {
			if (entry.getValue() == objectClassId) {
				Class<? extends AbstractGML> typeClass = entry.getKey();
				if (CityGML.class.isAssignableFrom(typeClass))				
					return CityGMLClass.fromModelClass(typeClass.asSubclass(CityGML.class));
			}
		}
		
		ADEExtension extension = ADEExtensionManager.getInstance().getExtensionByObjectClassId(objectClassId);
		if (extension != null)
			return CityGMLClass.ADE_COMPONENT;
		
		return CityGMLClass.UNDEFINED;
	}
	
	public static TreeMap<String, Long> mapObjectCounter(Map<Integer, Long> objectCounter, SchemaMapping schemaMapping) {
		TreeMap<String, Long> mapping = new TreeMap<>();
		for (Entry<Integer, Long> entry : objectCounter.entrySet()) {
			int objectClassId = entry.getKey();
			String typeName = objectClassId != MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID ? schemaMapping.getAbstractObjectType(objectClassId).toString()
					: new StringBuilder(CoreModule.v2_0_0.getNamespacePrefix()).append(":").append(MappingConstants.IMPLICIT_GEOMETRY_PATH).toString();
			
			mapping.put(typeName, entry.getValue());
		}
		
		return mapping;
	}
	
	public static CityGMLClass genericAttributeType2cityGMLClass(int typeId) {
		switch (typeId) {
		case 1: return CityGMLClass.STRING_ATTRIBUTE;
		case 2: return CityGMLClass.INT_ATTRIBUTE;
		case 3: return CityGMLClass.DOUBLE_ATTRIBUTE;
		case 4: return CityGMLClass.URI_ATTRIBUTE;
		case 5: return CityGMLClass.DATE_ATTRIBUTE;
		case 6: return CityGMLClass.MEASURE_ATTRIBUTE;
		case 7: return CityGMLClass.GENERIC_ATTRIBUTE_SET;
		default: return CityGMLClass.UNDEFINED;
		}
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
		StringJoiner joiner = new StringJoiner(delimiter);		
		for (T item : list)
			joiner.add(item != null ? item.toString() : "");

		return joiner.length() != 0 ? joiner.toString() : null;
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
	
	public static void logStackTrace(Throwable t) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			t.printStackTrace(new PrintStream(baos));
			baos.flush();
			Logger.getInstance().error(new String(baos.toByteArray()));
		} catch (IOException e) {
			Logger.getInstance().error("Failed to print stack trace. Check the console instead.");
			e.printStackTrace();
		}
	}
	
	public static class URLClassLoader extends java.net.URLClassLoader {
		
		public URLClassLoader(ClassLoader parentLoader) {
			super(new URL[]{}, parentLoader);
		}
		
		public URLClassLoader() {
			this(Util.class.getClassLoader());
		}

		public void addPath(Path path) {
			try {
				super.addURL(path.toUri().toURL());
			} catch (MalformedURLException e) {
				// 
			}
		}
	}

}
