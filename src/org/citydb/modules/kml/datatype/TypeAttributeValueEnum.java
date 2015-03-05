/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.kml.datatype;

import org.citygml4j.model.citygml.CityGMLClass;

public enum TypeAttributeValueEnum {
	X3D_MATERIAL("X3DMaterial"),
	GEOREFERENCED_TEXTURE("GeoreferencedTexture"),
	PARAMETERIZED_TEXTURE("ParameterizedTexture"),
	
	BUILDING_CEILING_SURFACE("CeilingSurface"),
	BUILDING_CLOSURE_SURFACE("ClosureSurface"),
	BUILDING_FLOOR_SURFACE("FloorSurface"),
	BUILDING_GROUND_SURFACE("GroundSurface"),
	INTERIOR_BUILDING_WALL_SURFACE("InteriorWallSurface"),
	BUILDING_ROOF_SURFACE("RoofSurface"),
	BUILDING_WALL_SURFACE("WallSurface"),
	OUTER_BUILDING_CEILING_SURFACE("OuterCeilingSurface"),
	OUTER_BUILDING_FLOOR_SURFACE("OuterFloorSurface"),
	
	DOOR("Door"),
	WINDOW("Window"),
	
	BRIDGE_ROOF_SURFACE("BridgeRoofSurface"),
	BRIDGE_WALL_SURFACE("BridgeWallSurface"),
	BRIDGE_GROUND_SURFACE("BridgeGroundSurface"),
	BRIDGE_CLOSURE_SURFACE("BridgeClosureSurface"),
	INTERIOR_BRIDGE_WALL_SURFACE("InteriorBridgeWallSurface"),
	BRIDGE_CEILING_SURFACE("BridgeCeilingSurface"),
	BRIDGE_FLOOR_SURFACE("BridgeFloorSurface"),
	OUTER_BRIDGE_CEILING_SURFACE("OuterBridgeCeilingSurface"),
	OUTER_BRIDGE_FLOOR_SURFACE("OuterBridgeFloorSurface"),
	
	TUNNEL_ROOF_SURFACE("TunnelRoofSurface"),
	TUNNEL_WALL_SURFACE("TunnelWallSurface"),
	TUNNEL_GROUND_SURFACE("TunnelGroundSurface"),
	TUNNEL_CLOSURE_SURFACE("TunnelClosureSurface"),
	INTERIOR_TUNNEL_WALL_SURFACE("InteriorTunnelWallSurface"),
	TUNNEL_CEILING_SURFACE("TunnelCeilingSurface"),
	TUNNEL_FLOOR_SURFACE("TunnelFloorSurface"),
	OUTER_TUNNEL_CEILING_SURFACE("OuterTunnelCeilingSurface"),
	OUTER_TUNNEL_FLOOR_SURFACE("OuterTunnelFloorSurface"),
	
	WATER_CLOSURE_SURFACE("WaterClosureSurface"),
	WATER_GROUND_SURFACE("WaterGroundSurface"),
	WATER_SURFACE("WaterSurface"),
	
	TRANSPORTATION_COMPLEX("TransportationComplex"),
	RAILWAY("Railway"),
	ROAD("Road"),
	SQUARE("Square"),
	TRACK("Track"),
	
	OTHER("Dummy");

	private String value;
	
	private TypeAttributeValueEnum(String value) {
		this.value = value;
	}
	
	public static TypeAttributeValueEnum fromCityGMLClass(CityGMLClass type) {
		switch (type) {
		case X3D_MATERIAL:
			return X3D_MATERIAL;
		case GEOREFERENCED_TEXTURE:
			return GEOREFERENCED_TEXTURE;
		case PARAMETERIZED_TEXTURE:
			return PARAMETERIZED_TEXTURE;
			
		// Building Boundary Surface	
		case BUILDING_CEILING_SURFACE:
			return BUILDING_CEILING_SURFACE;
		case BUILDING_CLOSURE_SURFACE:
			return BUILDING_CLOSURE_SURFACE;
		case BUILDING_FLOOR_SURFACE:
			return BUILDING_FLOOR_SURFACE;
		case BUILDING_GROUND_SURFACE:
			return BUILDING_GROUND_SURFACE;
		case INTERIOR_BUILDING_WALL_SURFACE:
			return INTERIOR_BUILDING_WALL_SURFACE;
		case BUILDING_ROOF_SURFACE:
			return BUILDING_ROOF_SURFACE;
		case BUILDING_WALL_SURFACE:
			return BUILDING_WALL_SURFACE;
		case OUTER_BUILDING_CEILING_SURFACE:
			return OUTER_BUILDING_CEILING_SURFACE;
		case OUTER_BUILDING_FLOOR_SURFACE:
			return OUTER_BUILDING_FLOOR_SURFACE;
			
		// Bridge Boundary Surface	
		case BRIDGE_ROOF_SURFACE:
			return BRIDGE_ROOF_SURFACE;
		case BRIDGE_WALL_SURFACE:
			return BRIDGE_WALL_SURFACE;
		case BRIDGE_GROUND_SURFACE:
			return BRIDGE_GROUND_SURFACE;
		case BRIDGE_CLOSURE_SURFACE:
			return BRIDGE_CLOSURE_SURFACE;
		case INTERIOR_BRIDGE_WALL_SURFACE:
			return INTERIOR_BRIDGE_WALL_SURFACE;
		case BRIDGE_CEILING_SURFACE:
			return BRIDGE_CEILING_SURFACE;
		case BRIDGE_FLOOR_SURFACE:
			return BRIDGE_FLOOR_SURFACE;
		case OUTER_BRIDGE_CEILING_SURFACE:
			return OUTER_BRIDGE_CEILING_SURFACE;
		case OUTER_BRIDGE_FLOOR_SURFACE:
			return OUTER_BRIDGE_FLOOR_SURFACE;		
			
		// Tunnel Boundary Surface				
		case TUNNEL_ROOF_SURFACE:
			return TUNNEL_ROOF_SURFACE;
		case TUNNEL_WALL_SURFACE:
			return TUNNEL_WALL_SURFACE;
		case TUNNEL_GROUND_SURFACE:
			return TUNNEL_GROUND_SURFACE;
		case TUNNEL_CLOSURE_SURFACE:
			return TUNNEL_CLOSURE_SURFACE;
		case INTERIOR_TUNNEL_WALL_SURFACE:
			return INTERIOR_TUNNEL_WALL_SURFACE;
		case TUNNEL_CEILING_SURFACE:
			return TUNNEL_CEILING_SURFACE;
		case TUNNEL_FLOOR_SURFACE:
			return TUNNEL_FLOOR_SURFACE;
		case OUTER_TUNNEL_CEILING_SURFACE:
			return OUTER_TUNNEL_CEILING_SURFACE;
		case OUTER_TUNNEL_FLOOR_SURFACE:
			return OUTER_TUNNEL_FLOOR_SURFACE;
			
		case BUILDING_DOOR:
			return DOOR;
		case BUILDING_WINDOW:
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
			return OTHER;
		}
	}

	@Override
	public String toString() {
		return value;
	}
	
}
