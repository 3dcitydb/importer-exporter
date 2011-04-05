package de.tub.citydb.database;

import org.citygml4j.model.citygml.CityGMLClass;

public enum DBTypeValueEnum {
	X3D_MATERIAL("X3DMaterial"),
	GEOREFERENCED_TEXTURE("GeoreferencedTexture"),
	PARAMETERIZED_TEXTURE("ParameterizedTexture"),
	
	CEILING_SURFACE("CeilingSurface"),
	CLOSURE_SURFACE("ClosureSurface"),
	FLOOR_SURFACE("FloorSurface"),
	GROUND_SURFACE("GroundSurface"),
	INTERIOR_WALL_SURFACE("InteriorWallSurface"),
	ROOF_SURFACE("RoofSurface"),
	WALL_SURFACE("WallSurface"),
	DOOR("Door"),
	WINDOW("Window"),
	
	WATER_CLOSURE_SURFACE("WaterClosureSurface"),
	WATER_GROUND_SURFACE("WaterGroundSurface"),
	WATER_SURFACE("WaterSurface"),
	
	TRANSPORTATION_COMPLEX("TransportationComplex"),
	RAILWAY("Railway"),
	ROAD("Road"),
	SQUARE("Square"),
	TRACK("Track");

	private String value;
	
	private DBTypeValueEnum(String value) {
		this.value = value;
	}
	
	public static DBTypeValueEnum fromCityGMLClass(CityGMLClass type) {
		switch (type) {
		case X3D_MATERIAL:
			return X3D_MATERIAL;
		case GEOREFERENCED_TEXTURE:
			return GEOREFERENCED_TEXTURE;
		case PARAMETERIZED_TEXTURE:
			return PARAMETERIZED_TEXTURE;
		case CEILING_SURFACE:
			return CEILING_SURFACE;
		case CLOSURE_SURFACE:
			return CLOSURE_SURFACE;
		case FLOOR_SURFACE:
			return FLOOR_SURFACE;
		case GROUND_SURFACE:
			return GROUND_SURFACE;
		case INTERIOR_WALL_SURFACE:
			return INTERIOR_WALL_SURFACE;
		case ROOF_SURFACE:
			return ROOF_SURFACE;
		case WALL_SURFACE:
			return WALL_SURFACE;
		case DOOR:
			return DOOR;
		case WINDOW:
			return WINDOW;		
		case WATER_CLOSURE_SURFACE:
			return WATER_CLOSURE_SURFACE;
		case WATER_GROUND_SURFACE:
			return WATER_GROUND_SURFACE;
		case WATER_SURFACE:
			return WATER_SURFACE;
		case TRANSPORTATION_COMPLEX:
			return TRANSPORTATION_COMPLEX;
		case RAILWAY:
			return RAILWAY;
		case ROAD:
			return ROAD;
		case SQUARE:
			return SQUARE;
		case TRACK:
			return TRACK;
		default:
			return null;
		}
	}

	@Override
	public String toString() {
		return value;
	}
	
}
