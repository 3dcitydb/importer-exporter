package de.tub.citydb.config.project.exporter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import de.tub.citygml4j.model.citygml.CityGMLModule;
import de.tub.citygml4j.model.citygml.CityGMLModuleType;
import de.tub.citygml4j.model.citygml.CityGMLModuleVersion;
import de.tub.citygml4j.model.citygml.appearance.AppearanceModule;
import de.tub.citygml4j.model.citygml.building.BuildingModule;
import de.tub.citygml4j.model.citygml.cityfurniture.CityFurnitureModule;
import de.tub.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupModule;
import de.tub.citygml4j.model.citygml.core.CoreModule;
import de.tub.citygml4j.model.citygml.generics.GenericsModule;
import de.tub.citygml4j.model.citygml.landuse.LandUseModule;
import de.tub.citygml4j.model.citygml.relief.ReliefModule;
import de.tub.citygml4j.model.citygml.texturedsurface.TexturedSurfaceModule;
import de.tub.citygml4j.model.citygml.transportation.TransportationModule;
import de.tub.citygml4j.model.citygml.vegetation.VegetationModule;
import de.tub.citygml4j.model.citygml.waterbody.WaterBodyModule;

@XmlType(name="ExportModuleVersionType", propOrder={
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
public class ExpModuleVersion {
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
	
	@XmlType(name="CoreMode")
	@XmlEnum	
	public enum CoreMode implements ExpModuleVersionMode<CoreModule> {
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
	
	@XmlType(name="AppearanceMode")
	@XmlEnum	
	public enum AppearanceMode implements ExpModuleVersionMode<AppearanceModule> {
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

	@XmlType(name="BuildingMode")
	@XmlEnum	
	public enum BuildingMode implements ExpModuleVersionMode<BuildingModule> {
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
	
	@XmlType(name="CityFurnitureMode")
	@XmlEnum	
	public enum CityFurnitureMode implements ExpModuleVersionMode<CityFurnitureModule> {
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
	
	@XmlType(name="CityObjectGroupMode")
	@XmlEnum	
	public enum CityObjectGroupMode implements ExpModuleVersionMode<CityObjectGroupModule> {
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
	
	@XmlType(name="GenericsMode")
	@XmlEnum	
	public enum GenericsMode implements ExpModuleVersionMode<GenericsModule> {
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
	
	@XmlType(name="LandUseMode")
	@XmlEnum	
	public enum LandUseMode implements ExpModuleVersionMode<LandUseModule> {
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
	
	@XmlType(name="ReliefMode")
	@XmlEnum	
	public enum ReliefMode implements ExpModuleVersionMode<ReliefModule> {
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
	
	@XmlType(name="TransportationMode")
	@XmlEnum	
	public enum TransportationMode implements ExpModuleVersionMode<TransportationModule> {
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
	
	@XmlType(name="VegetationMode")
	@XmlEnum	
	public enum VegetationMode implements ExpModuleVersionMode<VegetationModule> {
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
	
	@XmlType(name="WaterBodyMode")
	@XmlEnum	
	public enum WaterBodyMode implements ExpModuleVersionMode<WaterBodyModule> {
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
	
	@XmlType(name="TexturedSurfaceMode")
	@XmlEnum	
	public enum TexturedSurfaceMode implements ExpModuleVersionMode<TexturedSurfaceModule> {
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
