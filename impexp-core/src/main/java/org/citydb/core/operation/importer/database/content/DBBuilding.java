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
package org.citydb.core.operation.importer.database.content;

import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.common.property.*;
import org.citydb.core.operation.common.xlink.DBXlinkBasic;
import org.citydb.core.operation.common.xlink.DBXlinkSurfaceGeometry;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.util.AttributeValueJoiner;
import org.citydb.core.operation.importer.util.GeometryConverter;
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

import java.sql.Connection;
import java.sql.SQLException;

public class DBBuilding implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;


	private DBFeature featureImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBAddress addressImporter;
	private DBProperty propertyImporter;

	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;	
	private int batchCounter;

	private boolean hasObjectClassIdColumn;
	private int nullGeometryType;
	private String nullGeometryTypeName;	

	public DBBuilding(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		featureImporter = importer.getImporter(DBFeature.class);
		thematicSurfaceImporter = importer.getImporter(DBThematicSurface.class);
		addressImporter = importer.getImporter(DBAddress.class);
		propertyImporter = importer.getImporter(DBProperty.class);

		geometryConverter = importer.getGeometryConverter();

		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(AbstractBuilding building) throws CityGMLImportException, SQLException {
		return doImport(building, 0, 0);
	}

	public long doImport(AbstractBuilding building, long parentId, long rootId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(building);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long buildingId = featureImporter.doImport(building, featureType);
		if (rootId == 0)
			rootId = buildingId;

		// function
		if (building.isSetFunction()) {
			for (Code function: building.getFunction()) {
				CodeListProperty functionProperty = new CodeListProperty();
				functionProperty.setDataType("gml:CodeList");
				functionProperty.setValue(function.getValue());
				functionProperty.setNamespace("bldg");
				functionProperty.setName("function");
				propertyImporter.doImport(functionProperty, buildingId);
			}
		}

		// bldg:roofType
		if (building.isSetRoofType() && building.getRoofType().isSetValue()) {
			CodeListProperty roofType = new CodeListProperty();
			roofType.setDataType("gml:CodeList");
			roofType.setValue(building.getRoofType().getValue());
			roofType.setNamespace("bldg");
			roofType.setName("roofType");
			propertyImporter.doImport(roofType, buildingId);
		}

		// bldg:measuredHeight
		if (building.isSetMeasuredHeight() && building.getMeasuredHeight().isSetValue()) {
			MeasureProperty measureProperty = new MeasureProperty();
			measureProperty.setDataType("gml:LengthType ");
			measureProperty.setValue(building.getMeasuredHeight().getValue());
			measureProperty.setUom(building.getMeasuredHeight().getUom());
			measureProperty.setNamespace("bldg");
			measureProperty.setName("measuredHeight");
			propertyImporter.doImport(measureProperty, buildingId);
		}

		// bldg:lodXTerrainIntersectionCurve
		for (int i = 0; i < 4; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = building.getLod1TerrainIntersection();
				break;
			case 1:
				multiCurveProperty = building.getLod2TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = building.getLod3TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = building.getLod4TerrainIntersection();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				GeometryProperty multiPLineProperty = new GeometryProperty();
				multiPLineProperty.setName("lod" + (i + 2) + "TerrainIntersection");
				multiPLineProperty.setNamespace("core");
				multiPLineProperty.setDataType("gml:MultiCurve");
				multiPLineProperty.setValue(multiLineObj);
				propertyImporter.doImport(multiPLineProperty, buildingId);
			}
		}

		// bldg:lodXMultiCurve
		for (int i = 0; i < 3; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = building.getLod2MultiCurve();
				break;
			case 1:
				multiCurveProperty = building.getLod3MultiCurve();
				break;
			case 2:
				multiCurveProperty = building.getLod4MultiCurve();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				GeometryProperty multiPLineProperty = new GeometryProperty();
				multiPLineProperty.setName("lod" + (i + 2) + "MultiCurve");
				multiPLineProperty.setNamespace("core");
				multiPLineProperty.setDataType("gml:MultiCurve");
				multiPLineProperty.setValue(multiLineObj);
				propertyImporter.doImport(multiPLineProperty, buildingId);
			}
		}

		// bldg:lod0FootPrint and bldg:lod0RoofEdge
		for (int i = 0; i < 2; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = building.getLod0FootPrint();
				break;
			case 1:
				multiSurfaceProperty = building.getLod0RoofEdge();
				break;			
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), buildingId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.PROPERTY.getName(),
								buildingId, 
								href, 
								"val_surface_geometry"));
					}
				}
			}

			if (multiSurfaceId != 0) {
				SurfaceGeometryProperty surfaceGeometryProperty = new SurfaceGeometryProperty();
				surfaceGeometryProperty.setName(i == 0 ? "lod0_footprint_id" : "lod0_roofprint_id");
				surfaceGeometryProperty.setNamespace("core");
				surfaceGeometryProperty.setDataType("gml:MultiSurface");
				surfaceGeometryProperty.setValue(multiSurfaceId);
				propertyImporter.doImport(surfaceGeometryProperty, buildingId);
			}
		}

		// bldg:lodXMultiSurface
		for (int i = 0; i < 4; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = building.getLod1MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = building.getLod2MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = building.getLod3MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = building.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
				    multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), 0);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.PROPERTY.getName(),
								buildingId, 
								href, 
								"val_surface_geometry"));
					}
				}
			}

			if (multiGeometryId != 0) {
				SurfaceGeometryProperty surfaceGeometryProperty = new SurfaceGeometryProperty();
				surfaceGeometryProperty.setName("lod" + (i + 1) + "MultiSurface");
				surfaceGeometryProperty.setNamespace("core");
				surfaceGeometryProperty.setDataType("gml:MultiSurface");
				surfaceGeometryProperty.setValue(multiGeometryId);
				propertyImporter.doImport(surfaceGeometryProperty, buildingId);
			}
		}

		// bldg:lodXSolid
		for (int i = 0; i < 4; i++) {
			SolidProperty solidProperty = null;
			long solidGeometryId = 0;

			switch (i) {
			case 0:
				solidProperty = building.getLod1Solid();
				break;
			case 1:
				solidProperty = building.getLod2Solid();
				break;
			case 2:
				solidProperty = building.getLod3Solid();
				break;
			case 3:
				solidProperty = building.getLod4Solid();
				break;
			}

			String solidTypeName = null;
			if (solidProperty != null) {
				if (solidProperty.isSetSolid()) {
					solidGeometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), 0);
					solidTypeName = solidProperty.getSolid().getClass().getSimpleName();
					solidProperty.unsetSolid();
				} else {
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.PROPERTY.getName(),
								buildingId, 
								href, 
								"val_surface_geometry"));
					}
				}
			}

			if (solidGeometryId != 0) {
				SurfaceGeometryProperty solidGeometryProperty = new SurfaceGeometryProperty();
				solidGeometryProperty.setName("lod" + (i + 1) + "Solid");
				solidGeometryProperty.setNamespace("core");
				solidGeometryProperty.setDataType("gml:" + solidTypeName);
				solidGeometryProperty.setValue(solidGeometryId);
				propertyImporter.doImport(solidGeometryProperty, buildingId);
			}
		}

		// bldg:boundedBy
		if (building.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : building.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					long boundarySurfaceId= thematicSurfaceImporter.doImport(boundarySurface, building, buildingId);
					FeatureProperty boundarySurfaceProperty = new FeatureProperty();
					boundarySurfaceProperty.setName("boundedBy");
					boundarySurfaceProperty.setNamespace("bldg");
					boundarySurfaceProperty.setDataType("bldg:" + boundarySurface.getClass().getSimpleName());
					boundarySurfaceProperty.setValue(boundarySurfaceId);
					propertyImporter.doImport(boundarySurfaceProperty, buildingId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.PROPERTY.getName(),
								buildingId,
								"feature_id",
								href,
								"val_feature"));
					}
				}
			}
		}

		// bldg:outerBuildingInstallation
/*		if (building.isSetOuterBuildingInstallation()) {
			for (BuildingInstallationProperty property : building.getOuterBuildingInstallation()) {
				BuildingInstallation installation = property.getBuildingInstallation();

				if (installation != null) {
				//	buildingInstallationImporter.doImport(installation, building, buildingId);
					property.unsetBuildingInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BUILDING_INSTALLATION.getName(),
								href,
								buildingId,
								"building_id"));
					}
				}
			}
		}

		// bldg:interiorBuildingInstallation
		if (building.isSetInteriorBuildingInstallation()) {
			for (IntBuildingInstallationProperty property : building.getInteriorBuildingInstallation()) {
				IntBuildingInstallation installation = property.getIntBuildingInstallation();

				if (installation != null) {
				//	buildingInstallationImporter.doImport(installation, building, buildingId);
					property.unsetIntBuildingInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BUILDING_INSTALLATION.getName(),
								href,
								buildingId,
								"building_id"));
					}
				}
			}
		}*/

		// bldg:interiorRoom
/*		if (building.isSetInteriorRoom()) {
			for (InteriorRoomProperty property : building.getInteriorRoom()) {
				Room room = property.getRoom();

				if (room != null) {
				//	roomImporter.doImport(room, buildingId);
					property.unsetRoom();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.ROOM.getName(),
								href,
								buildingId,
								"building_id"));
					}
				}
			}
		}*/

		// bldg:consistsOfBuildingPart
		if (building.isSetConsistsOfBuildingPart()) {
			for (BuildingPartProperty property : building.getConsistsOfBuildingPart()) {
				BuildingPart buildingPart = property.getBuildingPart();
				if (buildingPart != null) {
					long buildingPartId = doImport(buildingPart, buildingId, rootId);
					FeatureProperty boundarySurfaceProperty = new FeatureProperty();
					boundarySurfaceProperty.setName("consistsOfBuildingPart");
					boundarySurfaceProperty.setNamespace("bldg");
					boundarySurfaceProperty.setDataType("bldg:BuildingPart");
					boundarySurfaceProperty.setValue(buildingPartId);
					propertyImporter.doImport(boundarySurfaceProperty, buildingId);

					property.unsetBuildingPart();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0)
						importer.logOrThrowUnsupportedXLinkMessage(building, BuildingPart.class, href);
				}
			}
		}

		// bldg:address
		if (building.isSetAddress()) {
			for (AddressProperty property : building.getAddress()) {
				Address address = property.getAddress();

				if (address != null) {
					long addressId = addressImporter.doImport(address, buildingId);

					FeatureProperty addressProperty = new FeatureProperty();
					addressProperty.setName("address");
					addressProperty.setNamespace("core");
					addressProperty.setDataType("core:Address");
					addressProperty.setValue(addressId);
					propertyImporter.doImport(addressProperty, buildingId);

					property.unsetAddress();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.PROPERTY.getName(),
								buildingId,
								"FEATURE_ID",
								href,
								"VAR_FEATURE"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(building, buildingId, featureType);

		return buildingId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		// nothing to do
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		// nothing to do
	}

}
