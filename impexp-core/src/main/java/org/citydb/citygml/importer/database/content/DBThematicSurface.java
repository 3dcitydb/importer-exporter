/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBThematicSurface implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psThematicSurface;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOpening openingImporter;
	private int batchCounter;

	public DBThematicSurface(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".thematic_surface (id, objectclass_id, building_id, room_id, building_installation_id, lod2_multi_surface_id, lod3_multi_surface_id, lod4_multi_surface_id) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?)";
		psThematicSurface = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		openingImporter = importer.getImporter(DBOpening.class);
	}

	protected long doImport(AbstractBoundarySurface boundarySurface) throws CityGMLImportException, SQLException {
		return doImport(boundarySurface, null, 0);
	}

	public long doImport(AbstractBoundarySurface boundarySurface, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(boundarySurface);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long boundarySurfaceId = cityObjectImporter.doImport(boundarySurface, featureType);

		// import boundary surface information
		// primary id
		psThematicSurface.setLong(1, boundarySurfaceId);

		// objectclass id
		psThematicSurface.setInt(2, featureType.getObjectClassId());

		// parent id
		if (parent instanceof AbstractBuilding) {
			psThematicSurface.setLong(3, parentId);
			psThematicSurface.setNull(4, Types.NULL);
			psThematicSurface.setNull(5, Types.NULL);
		} else if (parent instanceof Room) {
			psThematicSurface.setNull(3, Types.NULL);
			psThematicSurface.setLong(4, parentId);
			psThematicSurface.setNull(5, Types.NULL);
		} else if (parent instanceof BuildingInstallation
				|| parent instanceof IntBuildingInstallation) {
			psThematicSurface.setNull(3, Types.NULL);
			psThematicSurface.setNull(4, Types.NULL);
			psThematicSurface.setLong(5, parentId);
		} else {
			psThematicSurface.setNull(3, Types.NULL);
			psThematicSurface.setNull(4, Types.NULL);
			psThematicSurface.setNull(5, Types.NULL);
		}

		// bldg:lodXMultiSurface
		for (int i = 0; i < 3; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = boundarySurface.getLod2MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = boundarySurface.getLod3MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = boundarySurface.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), boundarySurfaceId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.THEMATIC_SURFACE.getName(),
								boundarySurfaceId, 
								href, 
								"lod" + (i + 2) + "_multi_surface_id"));
					}
				}
			}

			if (multiSurfaceId != 0)
				psThematicSurface.setLong(6 + i, multiSurfaceId);
			else
				psThematicSurface.setNull(6 + i, Types.NULL);
		}

		psThematicSurface.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.THEMATIC_SURFACE);

		// bldg:opening
		if (boundarySurface.isSetOpening()) {
			for (OpeningProperty property : boundarySurface.getOpening()) {
				AbstractOpening opening = property.getOpening();

				if (opening != null) {
					openingImporter.doImport(opening, boundarySurface, boundarySurfaceId);
					property.unsetOpening();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.OPENING_TO_THEM_SURFACE.getName(),								
								boundarySurfaceId,
								"THEMATIC_SURFACE_ID",
								href,
								"OPENING_ID"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(boundarySurface, boundarySurfaceId, featureType);

		return boundarySurfaceId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psThematicSurface.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psThematicSurface.close();
	}

}
