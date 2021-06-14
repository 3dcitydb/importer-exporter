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
package org.citydb.operation.importer.util;

import org.citygml4j.model.citygml.ade.ADEClass;
import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.binding.ADEModelObject;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.Bridge;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgePart;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.building.AbstractBuilding;
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
import org.citygml4j.model.gml.coverage.AbstractDiscreteCoverage;
import org.citygml4j.model.gml.coverage.RectifiedGridCoverage;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.util.walker.FeatureWalker;

import java.util.ArrayList;
import java.util.List;

public class ADEPropertyCollector {
	private final ADEHookWalker walker;

	public ADEPropertyCollector() {
		walker = new ADEHookWalker();
	}
	
	public List<ADEModelObject> getADEProperties(AbstractFeature root) {
		walker.root = root;		
		root.accept(walker);
		
		List<ADEModelObject> result = walker.properties;
		walker.root = null;
		walker.properties = null;
		
		return result;
	}

	private final class ADEHookWalker extends FeatureWalker {
		private AbstractFeature root;
		private List<ADEModelObject> properties;
		
		public void visit(org.citygml4j.model.citygml.bridge.AbstractBoundarySurface abstractBoundarySurface) {
			visit((AbstractCityObject)abstractBoundarySurface);

			if (abstractBoundarySurface.isSetGenericApplicationPropertyOfBoundarySurface())
				for (ADEComponent ade : abstractBoundarySurface.getGenericApplicationPropertyOfBoundarySurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.building.AbstractBoundarySurface abstractBoundarySurface) {
			visit((AbstractCityObject)abstractBoundarySurface);

			if (abstractBoundarySurface.isSetGenericApplicationPropertyOfBoundarySurface())
				for (ADEComponent ade : abstractBoundarySurface.getGenericApplicationPropertyOfBoundarySurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface abstractBoundarySurface) {
			visit((AbstractCityObject)abstractBoundarySurface);

			if (abstractBoundarySurface.isSetGenericApplicationPropertyOfBoundarySurface())
				for (ADEComponent ade : abstractBoundarySurface.getGenericApplicationPropertyOfBoundarySurface())
					visit(ade);
		}

		public void visit(AbstractBridge abstractBridge) {
			visit((AbstractSite)abstractBridge);

			if (abstractBridge.isSetGenericApplicationPropertyOfAbstractBridge())
				for (ADEComponent ade : abstractBridge.getGenericApplicationPropertyOfAbstractBridge())
					visit(ade);
		}

		public void visit(AbstractBuilding abstractBuilding) {
			visit((AbstractSite)abstractBuilding);

			if (abstractBuilding.isSetGenericApplicationPropertyOfAbstractBuilding())
				for (ADEComponent ade : abstractBuilding.getGenericApplicationPropertyOfAbstractBuilding())
					visit(ade);
		}

		public void visit(AbstractCityObject abstractCityObject) {
			if (abstractCityObject.isSetGenericApplicationPropertyOfCityObject())
				for (ADEComponent ade : abstractCityObject.getGenericApplicationPropertyOfCityObject())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.AbstractOpening abstractOpening) {
			visit((AbstractCityObject)abstractOpening);

			if (abstractOpening.isSetGenericApplicationPropertyOfOpening())
				for (ADEComponent ade : abstractOpening.getGenericApplicationPropertyOfOpening())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.building.AbstractOpening abstractOpening) {
			visit((AbstractCityObject)abstractOpening);

			if (abstractOpening.isSetGenericApplicationPropertyOfOpening())
				for (ADEComponent ade : abstractOpening.getGenericApplicationPropertyOfOpening())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.AbstractOpening abstractOpening) {
			visit((AbstractCityObject)abstractOpening);

			if (abstractOpening.isSetGenericApplicationPropertyOfOpening())
				for (ADEComponent ade : abstractOpening.getGenericApplicationPropertyOfOpening())
					visit(ade);
		}

		public void visit(AbstractReliefComponent abstractReliefComponent) {
			visit((AbstractCityObject)abstractReliefComponent);

			if (abstractReliefComponent.isSetGenericApplicationPropertyOfReliefComponent())
				for (ADEComponent ade : abstractReliefComponent.getGenericApplicationPropertyOfReliefComponent())
					visit(ade);
		}

		public void visit(AbstractSite abstractSite) {
			visit((AbstractCityObject)abstractSite);

			if (abstractSite.isSetGenericApplicationPropertyOfSite())
				for (ADEComponent ade : abstractSite.getGenericApplicationPropertyOfSite())
					visit(ade);
		}

		public void visit(AbstractSurfaceData abstractSurfaceData) {
			if (abstractSurfaceData.isSetGenericApplicationPropertyOfSurfaceData())
				for (ADEComponent ade : abstractSurfaceData.getGenericApplicationPropertyOfSurfaceData())
					visit(ade);
		}

		public void visit(AbstractTexture abstractTexture) {
			visit((AbstractSurfaceData)abstractTexture);

			if (abstractTexture.isSetGenericApplicationPropertyOfTexture())
				for (ADEComponent ade : abstractTexture.getGenericApplicationPropertyOfTexture())
					visit(ade);
		}

		public void visit(AbstractTransportationObject abstractTransportationObject) {
			visit((AbstractCityObject)abstractTransportationObject);

			if (abstractTransportationObject.isSetGenericApplicationPropertyOfTransportationObject())
				for (ADEComponent ade : abstractTransportationObject.getGenericApplicationPropertyOfTransportationObject())
					visit(ade);
		}

		public void visit(AbstractTunnel abstractTunnel) {
			visit((AbstractSite)abstractTunnel);

			if (abstractTunnel.isSetGenericApplicationPropertyOfAbstractTunnel())
				for (ADEComponent ade : abstractTunnel.getGenericApplicationPropertyOfAbstractTunnel())
					visit(ade);
		}

		public void visit(AbstractVegetationObject abstractVegetationObject) {
			visit((AbstractCityObject)abstractVegetationObject);

			if (abstractVegetationObject.isSetGenericApplicationPropertyOfVegetationObject())
				for (ADEComponent ade : abstractVegetationObject.getGenericApplicationPropertyOfVegetationObject())
					visit(ade);
		}

		public void visit(AbstractWaterObject abstractWaterObject) {
			visit((AbstractCityObject)abstractWaterObject);

			if (abstractWaterObject.isSetGenericApplicationPropertyOfWaterObject())
				for (ADEComponent ade : abstractWaterObject.getGenericApplicationPropertyOfWaterObject())
					visit(ade);
		}

		public void visit(AbstractWaterBoundarySurface abstractWaterBoundarySurface) {
			visit((AbstractCityObject)abstractWaterBoundarySurface);

			if (abstractWaterBoundarySurface.isSetGenericApplicationPropertyOfWaterBoundarySurface())
				for (ADEComponent ade : abstractWaterBoundarySurface.getGenericApplicationPropertyOfWaterBoundarySurface())
					visit(ade);
		}

		public void visit(Appearance appearance) {
			if (appearance.isSetGenericApplicationPropertyOfAppearance())
				for (ADEComponent ade : appearance.getGenericApplicationPropertyOfAppearance())
					visit(ade);
		}

		public void visit(GeoreferencedTexture georeferencedTexture) {
			visit((AbstractTexture)georeferencedTexture);	

			if (georeferencedTexture.isSetGenericApplicationPropertyOfGeoreferencedTexture())
				for (ADEComponent ade : georeferencedTexture.getGenericApplicationPropertyOfGeoreferencedTexture())
					visit(ade);
		}

		public void visit(ParameterizedTexture parameterizedTexture) {
			visit((AbstractTexture)parameterizedTexture);

			if (parameterizedTexture.isSetGenericApplicationPropertyOfParameterizedTexture())
				for (ADEComponent ade : parameterizedTexture.getGenericApplicationPropertyOfParameterizedTexture())
					visit(ade);
		}

		public void visit(X3DMaterial x3dMaterial) {
			visit((AbstractSurfaceData)x3dMaterial);

			if (x3dMaterial.isSetGenericApplicationPropertyOfX3DMaterial())
				for (ADEComponent ade : x3dMaterial.getGenericApplicationPropertyOfX3DMaterial())
					visit(ade);
		}

		public void visit(Bridge bridge) {
			visit((AbstractBridge)bridge);

			if (bridge.isSetGenericApplicationPropertyOfBridge())
				for (ADEComponent ade : bridge.getGenericApplicationPropertyOfBridge())
					visit(ade);
		}

		public void visit(BridgeConstructionElement bridgeConstructionElement) {
			visit((AbstractCityObject)bridgeConstructionElement);

			if (bridgeConstructionElement.isSetGenericApplicationPropertyOfBridgeConstructionElement())
				for (ADEComponent ade : bridgeConstructionElement.getGenericApplicationPropertyOfBridgeConstructionElement())
					visit(ade);
		}

		public void visit(BridgeFurniture bridgeFurniture) {
			visit((AbstractCityObject)bridgeFurniture);

			if (bridgeFurniture.isSetGenericApplicationPropertyOfBridgeFurniture())
				for (ADEComponent ade : bridgeFurniture.getGenericApplicationPropertyOfBridgeFurniture())
					visit(ade);
		}

		public void visit(BridgeInstallation bridgeInstallation) {
			visit((AbstractCityObject)bridgeInstallation);

			if (bridgeInstallation.isSetGenericApplicationPropertyOfBridgeInstallation())
				for (ADEComponent ade : bridgeInstallation.getGenericApplicationPropertyOfBridgeInstallation())
					visit(ade);
		}

		public void visit(BridgePart bridgePart) {
			visit((AbstractBridge)bridgePart);

			if (bridgePart.isSetGenericApplicationPropertyOfBridgePart())
				for (ADEComponent ade : bridgePart.getGenericApplicationPropertyOfBridgePart())
					visit(ade);
		}

		public void visit(BridgeRoom bridgeRoom) {
			visit((AbstractCityObject)bridgeRoom);

			if (bridgeRoom.isSetGenericApplicationPropertyOfBridgeRoom())
				for (ADEComponent ade : bridgeRoom.getGenericApplicationPropertyOfBridgeRoom())
					visit(ade);
		}

		public void visit(IntBridgeInstallation intBridgeInstallation) {
			visit((AbstractCityObject)intBridgeInstallation);

			if (intBridgeInstallation.isSetGenericApplicationPropertyOfIntBridgeInstallation())
				for (ADEComponent ade : intBridgeInstallation.getGenericApplicationPropertyOfIntBridgeInstallation())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.CeilingSurface ceilingSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)ceilingSurface);

			if (ceilingSurface.isSetGenericApplicationPropertyOfCeilingSurface())
				for (ADEComponent ade : ceilingSurface.getGenericApplicationPropertyOfCeilingSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.OuterCeilingSurface outerCeilingSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)outerCeilingSurface);

			if (outerCeilingSurface.isSetGenericApplicationPropertyOfOuterCeilingSurface())
				for (ADEComponent ade : outerCeilingSurface.getGenericApplicationPropertyOfOuterCeilingSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.ClosureSurface closureSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)closureSurface);

			if (closureSurface.isSetGenericApplicationPropertyOfClosureSurface())
				for (ADEComponent ade : closureSurface.getGenericApplicationPropertyOfClosureSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.FloorSurface floorSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)floorSurface);

			if (floorSurface.isSetGenericApplicationPropertyOfFloorSurface())
				for (ADEComponent ade : floorSurface.getGenericApplicationPropertyOfFloorSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.OuterFloorSurface outerFloorSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)outerFloorSurface);

			if (outerFloorSurface.isSetGenericApplicationPropertyOfOuterFloorSurface())
				for (ADEComponent ade : outerFloorSurface.getGenericApplicationPropertyOfOuterFloorSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.GroundSurface groundSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)groundSurface);

			if (groundSurface.isSetGenericApplicationPropertyOfGroundSurface())
				for (ADEComponent ade : groundSurface.getGenericApplicationPropertyOfGroundSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.InteriorWallSurface interiorWallSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)interiorWallSurface);

			if (interiorWallSurface.isSetGenericApplicationPropertyOfInteriorWallSurface())
				for (ADEComponent ade : interiorWallSurface.getGenericApplicationPropertyOfInteriorWallSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.RoofSurface roofSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)roofSurface);

			if (roofSurface.isSetGenericApplicationPropertyOfRoofSurface())
				for (ADEComponent ade : roofSurface.getGenericApplicationPropertyOfRoofSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.WallSurface wallSurface) {
			visit((org.citygml4j.model.citygml.bridge.AbstractBoundarySurface)wallSurface);

			if (wallSurface.isSetGenericApplicationPropertyOfWallSurface())
				for (ADEComponent ade : wallSurface.getGenericApplicationPropertyOfWallSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.Door door) {
			visit((org.citygml4j.model.citygml.bridge.AbstractOpening)door);

			if (door.isSetGenericApplicationPropertyOfDoor())
				for (ADEComponent ade : door.getGenericApplicationPropertyOfDoor())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.bridge.Window window) {
			visit((org.citygml4j.model.citygml.bridge.AbstractOpening)window);

			if (window.isSetGenericApplicationPropertyOfWindow())
				for (ADEComponent ade : window.getGenericApplicationPropertyOfWindow())
					visit(ade);
		}

		public void visit(Building building) {
			visit((AbstractBuilding)building);

			if (building.isSetGenericApplicationPropertyOfBuilding())
				for (ADEComponent ade : building.getGenericApplicationPropertyOfBuilding())
					visit(ade);
		}

		public void visit(BuildingFurniture buildingFurniture) {
			visit((AbstractCityObject)buildingFurniture);

			if (buildingFurniture.isSetGenericApplicationPropertyOfBuildingFurniture())
				for (ADEComponent ade : buildingFurniture.getGenericApplicationPropertyOfBuildingFurniture())
					visit(ade);
		}

		public void visit(BuildingInstallation buildingInstallation) {
			visit((AbstractCityObject)buildingInstallation);

			if (buildingInstallation.isSetGenericApplicationPropertyOfBuildingInstallation())
				for (ADEComponent ade : buildingInstallation.getGenericApplicationPropertyOfBuildingInstallation())
					visit(ade);
		}

		public void visit(BuildingPart buildingPart) {
			visit((AbstractBuilding)buildingPart);

			if (buildingPart.isSetGenericApplicationPropertyOfBuildingPart())
				for (ADEComponent ade : buildingPart.getGenericApplicationPropertyOfBuildingPart())
					visit(ade);
		}

		public void visit(IntBuildingInstallation intBuildingInstallation) {
			visit((AbstractCityObject)intBuildingInstallation);

			if (intBuildingInstallation.isSetGenericApplicationPropertyOfIntBuildingInstallation())
				for (ADEComponent ade : intBuildingInstallation.getGenericApplicationPropertyOfIntBuildingInstallation())
					visit(ade);
		}

		public void visit(Room room) {
			visit((AbstractCityObject)room);

			if (room.isSetGenericApplicationPropertyOfRoom())
				for (ADEComponent ade : room.getGenericApplicationPropertyOfRoom())
					visit(ade);
		}

		public void visit(CeilingSurface ceilingSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)ceilingSurface);

			if (ceilingSurface.isSetGenericApplicationPropertyOfCeilingSurface())
				for (ADEComponent ade : ceilingSurface.getGenericApplicationPropertyOfCeilingSurface())
					visit(ade);
		}

		public void visit(OuterCeilingSurface outerCeilingSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)outerCeilingSurface);

			if (outerCeilingSurface.isSetGenericApplicationPropertyOfOuterCeilingSurface())
				for (ADEComponent ade : outerCeilingSurface.getGenericApplicationPropertyOfOuterCeilingSurface())
					visit(ade);
		}

		public void visit(ClosureSurface closureSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)closureSurface);

			if (closureSurface.isSetGenericApplicationPropertyOfClosureSurface())
				for (ADEComponent ade : closureSurface.getGenericApplicationPropertyOfClosureSurface())
					visit(ade);
		}

		public void visit(FloorSurface floorSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)floorSurface);

			if (floorSurface.isSetGenericApplicationPropertyOfFloorSurface())
				for (ADEComponent ade : floorSurface.getGenericApplicationPropertyOfFloorSurface())
					visit(ade);
		}

		public void visit(OuterFloorSurface outerFloorSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)outerFloorSurface);

			if (outerFloorSurface.isSetGenericApplicationPropertyOfOuterFloorSurface())
				for (ADEComponent ade : outerFloorSurface.getGenericApplicationPropertyOfOuterFloorSurface())
					visit(ade);
		}

		public void visit(GroundSurface groundSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)groundSurface);

			if (groundSurface.isSetGenericApplicationPropertyOfGroundSurface())
				for (ADEComponent ade : groundSurface.getGenericApplicationPropertyOfGroundSurface())
					visit(ade);
		}

		public void visit(InteriorWallSurface interiorWallSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)interiorWallSurface);

			if (interiorWallSurface.isSetGenericApplicationPropertyOfInteriorWallSurface())
				for (ADEComponent ade : interiorWallSurface.getGenericApplicationPropertyOfInteriorWallSurface())
					visit(ade);
		}

		public void visit(RoofSurface roofSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)roofSurface);

			if (roofSurface.isSetGenericApplicationPropertyOfRoofSurface())
				for (ADEComponent ade : roofSurface.getGenericApplicationPropertyOfRoofSurface())
					visit(ade);
		}

		public void visit(WallSurface wallSurface) {
			visit((org.citygml4j.model.citygml.building.AbstractBoundarySurface)wallSurface);

			if (wallSurface.isSetGenericApplicationPropertyOfWallSurface())
				for (ADEComponent ade : wallSurface.getGenericApplicationPropertyOfWallSurface())
					visit(ade);
		}

		public void visit(Door door) {
			visit((org.citygml4j.model.citygml.building.AbstractOpening)door);

			if (door.isSetGenericApplicationPropertyOfDoor())
				for (ADEComponent ade : door.getGenericApplicationPropertyOfDoor())
					visit(ade);
		}

		public void visit(Window window) {
			visit((org.citygml4j.model.citygml.building.AbstractOpening)window);

			if (window.isSetGenericApplicationPropertyOfWindow())
				for (ADEComponent ade : window.getGenericApplicationPropertyOfWindow())
					visit(ade);
		}

		public void visit(HollowSpace hollowSpace) {
			visit((AbstractCityObject)hollowSpace);

			if (hollowSpace.isSetGenericApplicationPropertyOfHollowSpace())
				for (ADEComponent ade : hollowSpace.getGenericApplicationPropertyOfHollowSpace())
					visit(ade);
		}

		public void visit(IntTunnelInstallation intTunnelInstallation) {
			visit((AbstractCityObject)intTunnelInstallation);

			if (intTunnelInstallation.isSetGenericApplicationPropertyOfIntTunnelInstallation())
				for (ADEComponent ade : intTunnelInstallation.getGenericApplicationPropertyOfIntTunnelInstallation())
					visit(ade);
		}

		public void visit(Tunnel tunnel) {
			visit((AbstractTunnel)tunnel);

			if (tunnel.isSetGenericApplicationPropertyOfTunnel())
				for (ADEComponent ade : tunnel.getGenericApplicationPropertyOfTunnel())
					visit(ade);
		}

		public void visit(TunnelFurniture tunnelFurniture) {
			visit((AbstractCityObject)tunnelFurniture);

			if (tunnelFurniture.isSetGenericApplicationPropertyOfTunnelFurniture())
				for (ADEComponent ade : tunnelFurniture.getGenericApplicationPropertyOfTunnelFurniture())
					visit(ade);
		}

		public void visit(TunnelInstallation tunnelInstallation) {
			visit((AbstractCityObject)tunnelInstallation);

			if (tunnelInstallation.isSetGenericApplicationPropertyOfTunnelInstallation())
				for (ADEComponent ade : tunnelInstallation.getGenericApplicationPropertyOfTunnelInstallation())
					visit(ade);
		}

		public void visit(TunnelPart tunnelPart) {
			visit((AbstractTunnel)tunnelPart);

			if (tunnelPart.isSetGenericApplicationPropertyOfTunnelPart())
				for (ADEComponent ade : tunnelPart.getGenericApplicationPropertyOfTunnelPart())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.CeilingSurface ceilingSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)ceilingSurface);

			if (ceilingSurface.isSetGenericApplicationPropertyOfCeilingSurface())
				for (ADEComponent ade : ceilingSurface.getGenericApplicationPropertyOfCeilingSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.OuterCeilingSurface outerCeilingSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)outerCeilingSurface);

			if (outerCeilingSurface.isSetGenericApplicationPropertyOfOuterCeilingSurface())
				for (ADEComponent ade : outerCeilingSurface.getGenericApplicationPropertyOfOuterCeilingSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.ClosureSurface closureSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)closureSurface);

			if (closureSurface.isSetGenericApplicationPropertyOfClosureSurface())
				for (ADEComponent ade : closureSurface.getGenericApplicationPropertyOfClosureSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.FloorSurface floorSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)floorSurface);

			if (floorSurface.isSetGenericApplicationPropertyOfFloorSurface())
				for (ADEComponent ade : floorSurface.getGenericApplicationPropertyOfFloorSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.OuterFloorSurface outerFloorSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)outerFloorSurface);

			if (outerFloorSurface.isSetGenericApplicationPropertyOfOuterFloorSurface())
				for (ADEComponent ade : outerFloorSurface.getGenericApplicationPropertyOfOuterFloorSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.GroundSurface groundSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)groundSurface);

			if (groundSurface.isSetGenericApplicationPropertyOfGroundSurface())
				for (ADEComponent ade : groundSurface.getGenericApplicationPropertyOfGroundSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.InteriorWallSurface interiorWallSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)interiorWallSurface);

			if (interiorWallSurface.isSetGenericApplicationPropertyOfInteriorWallSurface())
				for (ADEComponent ade : interiorWallSurface.getGenericApplicationPropertyOfInteriorWallSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.RoofSurface roofSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)roofSurface);

			if (roofSurface.isSetGenericApplicationPropertyOfRoofSurface())
				for (ADEComponent ade : roofSurface.getGenericApplicationPropertyOfRoofSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.WallSurface wallSurface) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractBoundarySurface)wallSurface);

			if (wallSurface.isSetGenericApplicationPropertyOfWallSurface())
				for (ADEComponent ade : wallSurface.getGenericApplicationPropertyOfWallSurface())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.Door door) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractOpening)door);

			if (door.isSetGenericApplicationPropertyOfDoor())
				for (ADEComponent ade : door.getGenericApplicationPropertyOfDoor())
					visit(ade);
		}

		public void visit(org.citygml4j.model.citygml.tunnel.Window window) {
			visit((org.citygml4j.model.citygml.tunnel.AbstractOpening)window);

			if (window.isSetGenericApplicationPropertyOfWindow())
				for (ADEComponent ade : window.getGenericApplicationPropertyOfWindow())
					visit(ade);
		}

		public void visit(CityFurniture cityFurniture) {
			visit((AbstractCityObject)cityFurniture);

			if (cityFurniture.isSetGenericApplicationPropertyOfCityFurniture())
				for (ADEComponent ade : cityFurniture.getGenericApplicationPropertyOfCityFurniture())
					visit(ade);
		}

		public void visit(CityObjectGroup cityObjectGroup) {
			visit((AbstractCityObject)cityObjectGroup);

			if (cityObjectGroup.isSetGenericApplicationPropertyOfCityObjectGroup())
				for (ADEComponent ade : cityObjectGroup.getGenericApplicationPropertyOfCityObjectGroup())
					visit(ade);
		}

		public void visit(Address address) {
			if (address.isSetGenericApplicationPropertyOfAddress())
				for (ADEComponent ade : address.getGenericApplicationPropertyOfAddress())
					visit(ade);
		}

		public void visit(CityModel cityModel) {
			if (cityModel.isSetGenericApplicationPropertyOfCityModel())
				for (ADEComponent ade : cityModel.getGenericApplicationPropertyOfCityModel())
					visit(ade);
		}

		public void visit(GenericCityObject genericCityObject) {
			visit((AbstractCityObject)genericCityObject);
		}

		public void visit(LandUse landUse) {
			visit((AbstractCityObject)landUse);

			if (landUse.isSetGenericApplicationPropertyOfLandUse())
				for (ADEComponent ade : landUse.getGenericApplicationPropertyOfLandUse())
					visit(ade);
		}

		public void visit(BreaklineRelief breaklineRelief) {
			visit((AbstractReliefComponent)breaklineRelief);

			if (breaklineRelief.isSetGenericApplicationPropertyOfBreaklineRelief())
				for (ADEComponent ade : breaklineRelief.getGenericApplicationPropertyOfBreaklineRelief())
					visit(ade);
		}

		public void visit(MassPointRelief massPointRelief) {
			visit((AbstractReliefComponent)massPointRelief);

			if (massPointRelief.isSetGenericApplicationPropertyOfMassPointRelief())
				for (ADEComponent ade : massPointRelief.getGenericApplicationPropertyOfMassPointRelief())
					visit(ade);
		}

		public void visit(RasterRelief rasterRelief) {
			visit((AbstractReliefComponent)rasterRelief);

			if (rasterRelief.isSetGenericApplicationPropertyOfRasterRelief())
				for (ADEComponent ade : rasterRelief.getGenericApplicationPropertyOfRasterRelief())
					visit(ade);
		}

		public void visit(ReliefFeature reliefFeature) {
			visit((AbstractCityObject)reliefFeature);

			if (reliefFeature.isSetGenericApplicationPropertyOfReliefFeature())
				for (ADEComponent ade : reliefFeature.getGenericApplicationPropertyOfReliefFeature())
					visit(ade);
		}

		public void visit(TINRelief tinRelief) {
			visit((AbstractReliefComponent)tinRelief);

			if (tinRelief.isSetGenericApplicationPropertyOfTinRelief())
				for (ADEComponent ade : tinRelief.getGenericApplicationPropertyOfTinRelief())
					visit(ade);
		}

		public void visit(AuxiliaryTrafficArea auxiliaryTrafficArea) {
			visit((AbstractTransportationObject)auxiliaryTrafficArea);

			if (auxiliaryTrafficArea.isSetGenericApplicationPropertyOfAuxiliaryTrafficArea())
				for (ADEComponent ade : auxiliaryTrafficArea.getGenericApplicationPropertyOfAuxiliaryTrafficArea())
					visit(ade);
		}

		public void visit(Railway railway) {
			visit((TransportationComplex)railway);

			if (railway.isSetGenericApplicationPropertyOfRailway())
				for (ADEComponent ade : railway.getGenericApplicationPropertyOfRailway())
					visit(ade);
		}

		public void visit(RectifiedGridCoverage rectifiedGridCoverage) {
			visit((AbstractDiscreteCoverage)rectifiedGridCoverage);
		}

		public void visit(Road road) {
			visit((TransportationComplex)road);

			if (road.isSetGenericApplicationPropertyOfRoad())
				for (ADEComponent ade : road.getGenericApplicationPropertyOfRoad())
					visit(ade);
		}

		public void visit(Square square) {
			visit((TransportationComplex)square);

			if (square.isSetGenericApplicationPropertyOfSquare())
				for (ADEComponent ade : square.getGenericApplicationPropertyOfSquare())
					visit(ade);
		}

		public void visit(Track track) {
			visit((TransportationComplex)track);

			if (track.isSetGenericApplicationPropertyOfTrack())
				for (ADEComponent ade : track.getGenericApplicationPropertyOfTrack())
					visit(ade);
		}

		public void visit(TrafficArea trafficArea) {
			visit((AbstractTransportationObject)trafficArea);

			if (trafficArea.isSetGenericApplicationPropertyOfTrafficArea())
				for (ADEComponent ade : trafficArea.getGenericApplicationPropertyOfTrafficArea())
					visit(ade);
		}

		public void visit(TransportationComplex transportationComplex) {
			visit((AbstractTransportationObject)transportationComplex);

			if (transportationComplex.isSetGenericApplicationPropertyOfTransportationComplex())
				for (ADEComponent ade : transportationComplex.getGenericApplicationPropertyOfTransportationComplex())
					visit(ade);
		}

		public void visit(PlantCover plantCover) {
			visit((AbstractVegetationObject)plantCover);

			if (plantCover.isSetGenericApplicationPropertyOfPlantCover())
				for (ADEComponent ade : plantCover.getGenericApplicationPropertyOfPlantCover())
					visit(ade);
		}

		public void visit(SolitaryVegetationObject solitaryVegetationObject) {
			visit((AbstractVegetationObject)solitaryVegetationObject);

			if (solitaryVegetationObject.isSetGenericApplicationPropertyOfSolitaryVegetationObject())
				for (ADEComponent ade : solitaryVegetationObject.getGenericApplicationPropertyOfVegetationObject())
					visit(ade);
		}

		public void visit(WaterBody waterBody) {
			visit((AbstractWaterObject)waterBody);

			if (waterBody.isSetGenericApplicationPropertyOfWaterBody())
				for (ADEComponent ade : waterBody.getGenericApplicationPropertyOfWaterBody())
					visit(ade);
		}

		public void visit(WaterClosureSurface waterClosureSurface) {
			visit((AbstractWaterBoundarySurface)waterClosureSurface);

			if (waterClosureSurface.isSetGenericApplicationPropertyOfWaterClosureSurface())
				for (ADEComponent ade : waterClosureSurface.getGenericApplicationPropertyOfWaterClosureSurface())
					visit(ade);
		}

		public void visit(WaterGroundSurface waterGroundSurface) {
			visit((AbstractWaterBoundarySurface)waterGroundSurface);

			if (waterGroundSurface.isSetGenericApplicationPropertyOfWaterGroundSurface())
				for (ADEComponent ade : waterGroundSurface.getGenericApplicationPropertyOfWaterGroundSurface())
					visit(ade);
		}

		public void visit(WaterSurface waterSurface) {
			visit((AbstractWaterBoundarySurface)waterSurface);

			if (waterSurface.isSetGenericApplicationPropertyOfWaterSurface())
				for (ADEComponent ade : waterSurface.getGenericApplicationPropertyOfWaterSurface())
					visit(ade);
		}

		public void visit(ADEComponent adeComponent) {
			if (adeComponent.getADEClass() == ADEClass.MODEL_OBJECT)
				visit((ADEModelObject)adeComponent);
		}

		public void visit(ADEModelObject adeModelObject) {
			if (adeModelObject == root)
				super.visit(adeModelObject);		
			else if (adeModelObject.getParent() == root) {
				if (properties == null)
					properties = new ArrayList<>();
				
				properties.add(adeModelObject);
			}
		}
	}

}
