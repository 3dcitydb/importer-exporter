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
package org.citydb.core.util;

import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.citygml.appearance.*;
import org.citygml4j.model.citygml.bridge.*;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.CeilingSurface;
import org.citygml4j.model.citygml.building.ClosureSurface;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.FloorSurface;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.InteriorWallSurface;
import org.citygml4j.model.citygml.building.OuterCeilingSurface;
import org.citygml4j.model.citygml.building.OuterFloorSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.building.Window;
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.core.*;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.*;
import org.citygml4j.model.citygml.transportation.*;
import org.citygml4j.model.citygml.tunnel.*;
import org.citygml4j.model.citygml.vegetation.AbstractVegetationObject;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.*;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.AbstractFeatureCollection;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

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
			return objectClassId != null ? objectClassId : 0;
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
						return entry.getKey().getDeclaredConstructor().newInstance();
					} catch (Exception e) {
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
			String typeName = objectClassId != MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID ?
					schemaMapping.getAbstractObjectType(objectClassId).toString() :
					CoreModule.v2_0_0.getNamespacePrefix() + ":" + MappingConstants.IMPLICIT_GEOMETRY_PATH;
			
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

		List<Double> values = new ArrayList<>();

		try {
			String[] split = input.split(delimiter);
			if (split.length != 0) {
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

		List<String> values = new ArrayList<>();

		try {
			String[] split = input.split(delimiter);
			if (split.length != 0)
				Collections.addAll(values, split);
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
		return getFileExtension(file, true);
	}

	public static String getFileExtension(Path file) {
		return file.getFileName() != null ? getFileExtension(file.getFileName().toString(), false) : "";
	}

	private static String getFileExtension(String file, boolean checkSeparator) {
		int extension = indexOfExtension(file, checkSeparator);
		return extension > 0 ? file.substring(extension + 1).toLowerCase(Locale.ROOT) : "";
	}

	public static String stripFileExtension(String file) {
		int extension = indexOfExtension(file, true);
		return extension > 0 ? file.substring(0, extension) : file;
	}

	private static int indexOfExtension(String file, boolean checkSeparator) {
		int separator = checkSeparator ? Math.max(file.lastIndexOf(File.separator), file.lastIndexOf('\\')) : -1;
		int extension = file.lastIndexOf('.');
		return extension > separator ? extension : -1;
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

	public static ZonedDateTime getCreationDate(AbstractCityObject cityObject, boolean checkParents) {
		if (cityObject == null)
			return null;

		if (cityObject.isSetCreationDate())
			return cityObject.getCreationDate();

		if (checkParents) {
			Child child = cityObject;
			ModelObject parent;

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

	public static ZonedDateTime getTerminationDate(AbstractCityObject cityObject, boolean checkParents) {
		if (cityObject == null)
			return null;

		if (cityObject.isSetTerminationDate())
			return cityObject.getTerminationDate();

		if (checkParents) {
			Child child = cityObject;
			ModelObject parent;

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
		return version == CityGMLVersionType.v1_0_0 ? CityGMLVersion.v1_0_0 : CityGMLVersion.v2_0_0;
	}

	public static CityGMLVersionType fromCityGMLVersion(CityGMLVersion version) {
		return version == CityGMLVersion.v1_0_0 ? CityGMLVersionType.v1_0_0 : CityGMLVersionType.v2_0_0;
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
