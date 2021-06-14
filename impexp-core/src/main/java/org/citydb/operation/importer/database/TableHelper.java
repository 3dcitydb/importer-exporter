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
package org.citydb.operation.importer.database;

import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.AbstractAttribute;
import org.citydb.database.schema.mapping.AbstractExtension;
import org.citydb.database.schema.mapping.AbstractJoin;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AbstractType;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.ComplexAttribute;
import org.citydb.database.schema.mapping.ComplexAttributeType;
import org.citydb.database.schema.mapping.ComplexProperty;
import org.citydb.database.schema.mapping.GeometryProperty;
import org.citydb.database.schema.mapping.InjectedProperty;
import org.citydb.database.schema.mapping.Join;
import org.citydb.database.schema.mapping.JoinTable;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.TableRole;
import org.citydb.operation.importer.database.content.DBAddress;
import org.citydb.operation.importer.database.content.DBAddressToBridge;
import org.citydb.operation.importer.database.content.DBAddressToBuilding;
import org.citydb.operation.importer.database.content.DBAppearToSurfaceData;
import org.citydb.operation.importer.database.content.DBAppearance;
import org.citydb.operation.importer.database.content.DBBridge;
import org.citydb.operation.importer.database.content.DBBridgeConstrElement;
import org.citydb.operation.importer.database.content.DBBridgeFurniture;
import org.citydb.operation.importer.database.content.DBBridgeInstallation;
import org.citydb.operation.importer.database.content.DBBridgeOpenToThemSrf;
import org.citydb.operation.importer.database.content.DBBridgeOpening;
import org.citydb.operation.importer.database.content.DBBridgeRoom;
import org.citydb.operation.importer.database.content.DBBridgeThematicSurface;
import org.citydb.operation.importer.database.content.DBBuilding;
import org.citydb.operation.importer.database.content.DBBuildingFurniture;
import org.citydb.operation.importer.database.content.DBBuildingInstallation;
import org.citydb.operation.importer.database.content.DBCityFurniture;
import org.citydb.operation.importer.database.content.DBCityObject;
import org.citydb.operation.importer.database.content.DBCityObjectGenericAttrib;
import org.citydb.operation.importer.database.content.DBCityObjectGroup;
import org.citydb.operation.importer.database.content.DBExternalReference;
import org.citydb.operation.importer.database.content.DBGenericCityObject;
import org.citydb.operation.importer.database.content.DBImplicitGeometry;
import org.citydb.operation.importer.database.content.DBImporter;
import org.citydb.operation.importer.database.content.DBLandUse;
import org.citydb.operation.importer.database.content.DBOpening;
import org.citydb.operation.importer.database.content.DBOpeningToThemSurface;
import org.citydb.operation.importer.database.content.DBPlantCover;
import org.citydb.operation.importer.database.content.DBReliefComponent;
import org.citydb.operation.importer.database.content.DBReliefFeatToRelComp;
import org.citydb.operation.importer.database.content.DBReliefFeature;
import org.citydb.operation.importer.database.content.DBRoom;
import org.citydb.operation.importer.database.content.DBSolitaryVegetatObject;
import org.citydb.operation.importer.database.content.DBSurfaceData;
import org.citydb.operation.importer.database.content.DBSurfaceGeometry;
import org.citydb.operation.importer.database.content.DBTexImage;
import org.citydb.operation.importer.database.content.DBTextureParam;
import org.citydb.operation.importer.database.content.DBThematicSurface;
import org.citydb.operation.importer.database.content.DBTrafficArea;
import org.citydb.operation.importer.database.content.DBTransportationComplex;
import org.citydb.operation.importer.database.content.DBTunnel;
import org.citydb.operation.importer.database.content.DBTunnelFurniture;
import org.citydb.operation.importer.database.content.DBTunnelHollowSpace;
import org.citydb.operation.importer.database.content.DBTunnelInstallation;
import org.citydb.operation.importer.database.content.DBTunnelOpenToThemSrf;
import org.citydb.operation.importer.database.content.DBTunnelOpening;
import org.citydb.operation.importer.database.content.DBTunnelThematicSurface;
import org.citydb.operation.importer.database.content.DBWaterBodToWaterBndSrf;
import org.citydb.operation.importer.database.content.DBWaterBody;
import org.citydb.operation.importer.database.content.DBWaterBoundarySurface;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class TableHelper {
	private final SchemaMapping schemaMapping;
	private final HashMap<String, Set<String>> dependencyMap;
	private List<String> weightedTables;
	private Set<Object> visited;	

	public TableHelper(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
		dependencyMap = new HashMap<>();
		
		init();
	}
		
	private void init() {
		visited = new HashSet<>();		
	
		// build dependencies between tables
		List<AbstractType<?>> types = schemaMapping.getAbstractTypes();
		for (AbstractType<?> type : types)
			addDependency(type.getTable(), type);
		
		// add dependencies that are not available from 
		// the 3dcitydb schema mapping file
		addDependency("surface_geometry", "cityobject");
		addDependency("textureparam", "surface_data");
		addDependency("textureparam", "surface_geometry");
		addDependency("implicit_geometry", "surface_geometry");

		// weight tables according to their dependencies
		HashMap<String, Integer> weights = new HashMap<>();
		for (String table : dependencyMap.keySet())
			calcWeights(table, weights);

		weightedTables = weights.entrySet().stream()
				.sorted(Entry.<String, Integer>comparingByValue().reversed())
				.map(e -> e.getKey())
				.collect(Collectors.toList());

		// clean up
		visited = null;
	}
	
	public List<String> getCommitOrder() {
		return weightedTables;
	}

	public List<String> getCommitOrder(String table) {
		Set<String> dependencies = getTransitiveDependencies(getTableName(table));		
		return weightedTables.stream()
				.filter(t -> dependencies.contains(t))
				.collect(Collectors.toList());
	}

	private Set<String> getTransitiveDependencies(String table) {
		Set<String> result = new HashSet<>();
		result.add(table);

		Set<String> dependencies = dependencyMap.get(table);		
		if (dependencies != null) {
			for (String dependency : dependencies)
				result.addAll(getTransitiveDependencies(dependency));
		}

		return result;
	}
	
	private void calcWeights(String table, HashMap<String, Integer> weights) {
		if (!weights.containsKey(table))
			weights.put(table, 0);

		int score = weights.get(table) + 1;

		Set<String> dependencies = dependencyMap.get(table);
		if (dependencies != null) {
			for (String dependency : dependencies) {
				if (!weights.containsKey(dependency))
					weights.put(dependency, score);
				else
					weights.put(dependency, weights.get(dependency) + score);

				calcWeights(dependency, weights);
			}
		}		
	}

	private void addDependency(String table, AbstractType<?> type) {
		if (type.isSetExtension()) {
			AbstractExtension<?> extension = type.getExtension();			
			if (extension.isSetJoin())
				addDependency(table, extension.getJoin(), type.getSchema());
		}

		for (AbstractProperty property : type.getProperties())
			addDependency(table, property);
	}

	private void addDependency(String table, ComplexAttributeType type) {
		if (visited.add(type)) {
			for (AbstractAttribute attribute : type.getAttributes())
				addDependency(table, attribute);
		}
	}

	private void addDependency(String table, AbstractProperty property) {
		String joinTable = table;

		if (property instanceof InjectedProperty) {
			InjectedProperty injectedProperty = (InjectedProperty)property;
			if (injectedProperty.isSetBaseJoin())
				joinTable = addDependency(joinTable, injectedProperty.getBaseJoin(), property.getSchema());
		}

		if (property.isSetJoin())
			joinTable = addDependency(joinTable, property.getJoin(), property.getSchema());

		if (property instanceof ComplexProperty) {
			ComplexProperty complexProperty = (ComplexProperty)property;
			if (complexProperty.isSetType() && visited.add(complexProperty.getType()))
				addDependency(joinTable, complexProperty.getType());
		}

		else if (property instanceof GeometryProperty) {
			GeometryProperty geometryProperty = (GeometryProperty)property;
			if (geometryProperty.isSetRefColumn())
				addDependency(table, "surface_geometry");
		}

		else if (property instanceof ComplexAttribute) {
			ComplexAttribute complexAttribute = (ComplexAttribute)property;
			if (complexAttribute.isSetType())
				addDependency(joinTable, complexAttribute.getType());
		}
	}

	private String addDependency(String table, AbstractJoin join, AppSchema schema) {
		if (join instanceof Join) {
			Join simpleJoin = (Join)join;
			String joinTable = simpleJoin.getTable();

			if (simpleJoin.getToRole() == TableRole.PARENT)
				addDependency(table, joinTable);
			else
				addDependency(joinTable, table);
			
			return joinTable;
		}

		else if (join instanceof JoinTable) {
			JoinTable joinTable = (JoinTable)join;
			String fromTable = joinTable.getTable();
						
			for (Join simpleJoin : joinTable.getJoins())
				addDependency(fromTable, simpleJoin, schema);

			return table;
		}

		// we do not consider reverse joins...
		return table;
	}

	private void addDependency(String table, String dependency) {
		table = getTableName(table);
		dependency = getTableName(dependency);

		if (dependency.equals(table))
			return;

		Set<String> dependencies = dependencyMap.get(table);
		if (dependencies == null) {
			dependencies = new HashSet<>();
			dependencyMap.put(table, dependencies);
		}

		dependencies.add(dependency);
	}

	private String getTableName(String table) {
		if (table.equals(MappingConstants.TARGET_TABLE_TOKEN))
			table = "cityobject";

		return table.toLowerCase();
	}
	
	public Class<? extends DBImporter> getImporterClass(TableEnum table) throws SQLException {
		switch (table) {
		case ADDRESS:
			return DBAddress.class;
		case ADDRESS_TO_BRIDGE:
			return DBAddressToBridge.class;
		case ADDRESS_TO_BUILDING:
			return DBAddressToBuilding.class;
		case APPEAR_TO_SURFACE_DATA:
			return DBAppearToSurfaceData.class;
		case APPEARANCE:
			return DBAppearance.class;
		case BRIDGE_OPEN_TO_THEM_SRF:
			return DBBridgeOpenToThemSrf.class;
		case BREAKLINE_RELIEF:
			return DBReliefComponent.class;
		case BRIDGE:
			return DBBridge.class;
		case BRIDGE_CONSTR_ELEMENT:
			return DBBridgeConstrElement.class;
		case BRIDGE_FURNITURE:
			return DBBridgeFurniture.class;
		case BRIDGE_INSTALLATION:
			return DBBridgeInstallation.class;
		case BRIDGE_OPENING:
			return DBBridgeOpening.class;
		case BRIDGE_ROOM:
			return DBBridgeRoom.class;
		case BRIDGE_THEMATIC_SURFACE:
			return DBBridgeThematicSurface.class;
		case BUILDING:
			return DBBuilding.class;
		case BUILDING_FURNITURE:
			return DBBuildingFurniture.class;
		case BUILDING_INSTALLATION:
			return DBBuildingInstallation.class;
		case CITY_FURNITURE:
			return DBCityFurniture.class;
		case CITYOBJECT:
			return DBCityObject.class;
		case CITYOBJECT_GENERICATTRIB:
			return DBCityObjectGenericAttrib.class;
		case CITYOBJECTGROUP:
			return DBCityObjectGroup.class;
		case EXTERNAL_REFERENCE:
			return DBExternalReference.class;
		case GENERALIZATION:
			return null;
		case GENERIC_CITYOBJECT:
			return DBGenericCityObject.class;
		case GROUP_TO_CITYOBJECT:
			return null;
		case IMPLICIT_GEOMETRY:
			return DBImplicitGeometry.class;
		case LAND_USE:
			return DBLandUse.class;
		case MASSPOINT_RELIEF:
			return DBReliefComponent.class;
		case OPENING:
			return DBOpening.class;
		case OPENING_TO_THEM_SURFACE:
			return DBOpeningToThemSurface.class;
		case PLANT_COVER:
			return DBPlantCover.class;
		case RASTER_RELIEF:
			return DBReliefComponent.class;
		case RELIEF_COMPONENT:
			return DBReliefComponent.class;
		case RELIEF_FEAT_TO_REL_COMP:
			return DBReliefFeatToRelComp.class;
		case RELIEF_FEATURE:
			return DBReliefFeature.class;
		case ROOM:
			return DBRoom.class;
		case SOLITARY_VEGETAT_OBJECT:
			return DBSolitaryVegetatObject.class;
		case SURFACE_DATA:
			return DBSurfaceData.class;
		case SURFACE_GEOMETRY:
			return DBSurfaceGeometry.class;
		case TEX_IMAGE:
			return DBTexImage.class;
		case TEXTUREPARAM:
			return DBTextureParam.class;
		case THEMATIC_SURFACE:
			return DBThematicSurface.class;
		case TIN_RELIEF:
			return DBReliefComponent.class;
		case TRAFFIC_AREA:
			return DBTrafficArea.class;
		case TRANSPORTATION_COMPLEX:
			return DBTransportationComplex.class;
		case TUNNEL:
			return DBTunnel.class;
		case TUNNEL_FURNITURE:
			return DBTunnelFurniture.class;
		case TUNNEL_HOLLOW_SPACE:
			return DBTunnelHollowSpace.class;
		case TUNNEL_INSTALLATION:
			return DBTunnelInstallation.class;
		case TUNNEL_OPEN_TO_THEM_SRF:
			return DBTunnelOpenToThemSrf.class;
		case TUNNEL_OPENING:
			return DBTunnelOpening.class;
		case TUNNEL_THEMATIC_SURFACE:
			return DBTunnelThematicSurface.class;
		case WATERBOD_TO_WATERBND_SRF:
			return DBWaterBodToWaterBndSrf.class;
		case WATERBODY:
			return DBWaterBody.class;
		case WATERBOUNDARY_SURFACE:
			return DBWaterBoundarySurface.class;
		case UNDEFINED:
			return null;
		}
		
		return null;
	}
	
}
