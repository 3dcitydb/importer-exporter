/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.config.project.exporter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.model.citygml.CityGMLModule;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.CityGMLModuleVersion;
import org.citygml4j.model.citygml.appearance.AppearanceModule;
import org.citygml4j.model.citygml.building.BuildingModule;
import org.citygml4j.model.citygml.cityfurniture.CityFurnitureModule;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupModule;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.generics.GenericsModule;
import org.citygml4j.model.citygml.landuse.LandUseModule;
import org.citygml4j.model.citygml.relief.ReliefModule;
import org.citygml4j.model.citygml.texturedsurface.TexturedSurfaceModule;
import org.citygml4j.model.citygml.transportation.TransportationModule;
import org.citygml4j.model.citygml.vegetation.VegetationModule;
import org.citygml4j.model.citygml.waterbody.WaterBodyModule;

@XmlType(name="ModuleVersionType", propOrder={
		"core",
		"appearance",
		"building",
		"cityFurniture",
		"cityObjectGroup",
		"generics",
		"landUse",
		"relief",
		"transportation",
		"vegetation",
		"waterBody",
		"texturedSurface"
		})
public class ModuleVersion {
	@XmlElement(required=true)
	public CoreMode core = CoreMode.v1_0_0;
	@XmlElement(required=true)
	public AppearanceMode appearance = AppearanceMode.v1_0_0;
	@XmlElement(required=true)
	public BuildingMode building = BuildingMode.v1_0_0;
	@XmlElement(required=true)
	public CityFurnitureMode cityFurniture = CityFurnitureMode.v1_0_0;
	@XmlElement(required=true)
	public CityObjectGroupMode cityObjectGroup = CityObjectGroupMode.v1_0_0;
	@XmlElement(required=true)
	public GenericsMode generics = GenericsMode.v1_0_0;
	@XmlElement(required=true)
	public LandUseMode landUse = LandUseMode.v1_0_0;
	@XmlElement(required=true)
	public ReliefMode relief = ReliefMode.v1_0_0;
	@XmlElement(required=true)
	public TransportationMode transportation = TransportationMode.v1_0_0;
	@XmlElement(required=true)
	public VegetationMode vegetation = VegetationMode.v1_0_0;
	@XmlElement(required=true)
	public WaterBodyMode waterBody = WaterBodyMode.v1_0_0;
	@XmlElement(required=true)
	public TexturedSurfaceMode texturedSurface = TexturedSurfaceMode.v1_0_0;	
	
	@XmlType(name="CoreModeType")
	@XmlEnum	
	public enum CoreMode implements ModuleVersionMode<CoreModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
	    public CoreModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return CoreModule.v0_4_0;
	    	default:
	    		return CoreModule.v1_0_0;
	    	}
	    }
	    
	    public static CoreMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="AppearanceModeType")
	@XmlEnum	
	public enum AppearanceMode implements ModuleVersionMode<AppearanceModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public AppearanceModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return AppearanceModule.v0_4_0;
	    	default:
	    		return AppearanceModule.v1_0_0;
	    	}
	    }
	    
	    public static AppearanceMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}

	@XmlType(name="BuildingModeType")
	@XmlEnum	
	public enum BuildingMode implements ModuleVersionMode<BuildingModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public BuildingModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return BuildingModule.v0_4_0;
	    	default:
	    		return BuildingModule.v1_0_0;
	    	}
	    }
	    
	    public static BuildingMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="CityFurnitureModeType")
	@XmlEnum	
	public enum CityFurnitureMode implements ModuleVersionMode<CityFurnitureModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public CityFurnitureModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return CityFurnitureModule.v0_4_0;
	    	default:
	    		return CityFurnitureModule.v1_0_0;
	    	}
	    }
	    
	    public static CityFurnitureMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="CityObjectGroupModeType")
	@XmlEnum	
	public enum CityObjectGroupMode implements ModuleVersionMode<CityObjectGroupModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public CityObjectGroupModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return CityObjectGroupModule.v0_4_0;
	    	default:
	    		return CityObjectGroupModule.v1_0_0;
	    	}
	    }
	    
	    public static CityObjectGroupMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="GenericsModeType")
	@XmlEnum	
	public enum GenericsMode implements ModuleVersionMode<GenericsModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public GenericsModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return GenericsModule.v0_4_0;
	    	default:
	    		return GenericsModule.v1_0_0;
	    	}
	    }
	    
	    public static GenericsMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="LandUseModeType")
	@XmlEnum	
	public enum LandUseMode implements ModuleVersionMode<LandUseModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public LandUseModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return LandUseModule.v0_4_0;
	    	default:
	    		return LandUseModule.v1_0_0;
	    	}
	    }
	    
	    public static LandUseMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="ReliefModeType")
	@XmlEnum	
	public enum ReliefMode implements ModuleVersionMode<ReliefModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public ReliefModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return ReliefModule.v0_4_0;
	    	default:
	    		return ReliefModule.v1_0_0;
	    	}
	    }
	    
	    public static ReliefMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="TransportationModeType")
	@XmlEnum	
	public enum TransportationMode implements ModuleVersionMode<TransportationModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public TransportationModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return TransportationModule.v0_4_0;
	    	default:
	    		return TransportationModule.v1_0_0;
	    	}
	    }
	    
	    public static TransportationMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="VegetationModeType")
	@XmlEnum	
	public enum VegetationMode implements ModuleVersionMode<VegetationModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public VegetationModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return VegetationModule.v0_4_0;
	    	default:
	    		return VegetationModule.v1_0_0;
	    	}
	    }
	    
	    public static VegetationMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="WaterBodyModeType")
	@XmlEnum	
	public enum WaterBodyMode implements ModuleVersionMode<WaterBodyModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public WaterBodyModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return WaterBodyModule.v0_4_0;
	    	default:
	    		return WaterBodyModule.v1_0_0;
	    	}
	    }
	    
	    public static WaterBodyMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	@XmlType(name="TexturedSurfaceModeType")
	@XmlEnum	
	public enum TexturedSurfaceMode implements ModuleVersionMode<TexturedSurfaceModule> {
		@XmlEnumValue("v0.4.0")
		v0_4_0,
		@XmlEnumValue("v1.0.0")
		v1_0_0;
		
		public TexturedSurfaceModule getModule() {
	    	switch (this) {
	    	case v0_4_0:
	    		return TexturedSurfaceModule.v0_4_0;
	    	default:
	    		return TexturedSurfaceModule.v1_0_0;
	    	}
	    }
	    
	    public static TexturedSurfaceMode fromVersion(CityGMLModuleVersion version) {
	    	switch (version) {
	    	case v0_4_0:
	    		return v0_4_0;
	    	default:
	    		return v1_0_0;
	    	}
	    }
	}
	
	public CoreMode getCore() {
		return core;
	}

	public void setCore(CoreMode core) {
		this.core = core;
	}

	public AppearanceMode getAppearance() {
		return appearance;
	}

	public void setAppearance(AppearanceMode appearance) {
		this.appearance = appearance;
	}

	public BuildingMode getBuilding() {
		return building;
	}

	public void setBuilding(BuildingMode building) {
		this.building = building;
	}

	public CityFurnitureMode getCityFurniture() {
		return cityFurniture;
	}

	public void setCityFurniture(CityFurnitureMode cityFurniture) {
		this.cityFurniture = cityFurniture;
	}

	public CityObjectGroupMode getCityObjectGroup() {
		return cityObjectGroup;
	}

	public void setCityObjectGroup(CityObjectGroupMode cityObjectGroup) {
		this.cityObjectGroup = cityObjectGroup;
	}

	public GenericsMode getGenerics() {
		return generics;
	}

	public void setGenerics(GenericsMode generics) {
		this.generics = generics;
	}

	public LandUseMode getLandUse() {
		return landUse;
	}

	public void setLandUse(LandUseMode landUse) {
		this.landUse = landUse;
	}

	public ReliefMode getRelief() {
		return relief;
	}

	public void setRelief(ReliefMode relief) {
		this.relief = relief;
	}

	public TransportationMode getTransportation() {
		return transportation;
	}

	public void setTransportation(TransportationMode transportation) {
		this.transportation = transportation;
	}

	public VegetationMode getVegetation() {
		return vegetation;
	}

	public void setVegetation(VegetationMode vegetation) {
		this.vegetation = vegetation;
	}

	public WaterBodyMode getWaterBody() {
		return waterBody;
	}

	public void setWaterBody(WaterBodyMode waterBody) {
		this.waterBody = waterBody;
	}

	public TexturedSurfaceMode getTexturedSurface() {
		return texturedSurface;
	}

	public void setTexturedSurface(TexturedSurfaceMode texturedSurface) {
		this.texturedSurface = texturedSurface;
	}
	
	public List<CityGMLModule> getModules() {
		List<CityGMLModule> moduleList = new ArrayList<CityGMLModule>();
		
		moduleList.add(getCore().getModule());
		moduleList.add(getAppearance().getModule());
		moduleList.add(getBuilding().getModule());
		moduleList.add(getCityFurniture().getModule());
		moduleList.add(getCityObjectGroup().getModule());
		moduleList.add(getGenerics().getModule());
		moduleList.add(getLandUse().getModule());
		moduleList.add(getRelief().getModule());
		moduleList.add(getTransportation().getModule());
		moduleList.add(getVegetation().getModule());
		moduleList.add(getWaterBody().getModule());
		moduleList.add(getTexturedSurface().getModule());
		
		return moduleList;
	}
	
	public void setModuleVersion(CityGMLModuleType type, CityGMLModuleVersion version) {
		switch (type) {
		case APPEARANCE:
			setAppearance(AppearanceMode.fromVersion(version));
			break;
		case BUILDING:
			setBuilding(BuildingMode.fromVersion(version));
			break;
		case CORE:
			setCore(CoreMode.fromVersion(version));
			break;
		case CITYFURNITURE:
			setCityFurniture(CityFurnitureMode.fromVersion(version));
			break;
		case CITYOBJECTGROUP:
			setCityObjectGroup(CityObjectGroupMode.fromVersion(version));
			break;
		case GENERICS:
			setGenerics(GenericsMode.fromVersion(version));
			break;
		case LANDUSE:
			setLandUse(LandUseMode.fromVersion(version));
			break;
		case RELIEF:
			setRelief(ReliefMode.fromVersion(version));
			break;
		case TRANSPORTATION:
			setTransportation(TransportationMode.fromVersion(version));
			break;
		case VEGETATION:
			setVegetation(VegetationMode.fromVersion(version));
			break;
		case WATERBODY:
			setWaterBody(WaterBodyMode.fromVersion(version));
			break;
		case TEXTUREDSURFACE:
			setTexturedSurface(TexturedSurfaceMode.fromVersion(version));
			break;
		}
	}
}
