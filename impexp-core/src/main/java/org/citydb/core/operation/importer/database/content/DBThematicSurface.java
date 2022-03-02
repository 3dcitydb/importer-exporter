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
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.common.property.FeatureProperty;
import org.citydb.core.operation.common.property.SurfaceGeometryProperty;
import org.citydb.core.operation.common.xlink.DBXlinkBasic;
import org.citydb.core.operation.common.xlink.DBXlinkSurfaceGeometry;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import java.sql.Connection;
import java.sql.SQLException;

public class DBThematicSurface implements DBImporter {
	private final CityGMLImportManager importer;

	private DBFeature featureImporter;
	private DBProperty propertyImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOpening openingImporter;

	public DBThematicSurface(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;
		propertyImporter = importer.getImporter(DBProperty.class);
		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		featureImporter = importer.getImporter(DBFeature.class);
		openingImporter = importer.getImporter(DBOpening.class);
	}

	protected long doImport(AbstractBoundarySurface boundarySurface) throws CityGMLImportException, SQLException {
		return doImport(boundarySurface, null, 0);
	}

	public long doImport(AbstractBoundarySurface boundarySurface, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(boundarySurface);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import feature information
		long boundarySurfaceId = featureImporter.doImport(boundarySurface, featureType);

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
					multiSurfaceId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), 0);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.PROPERTY.getName(),
								boundarySurfaceId, 
								href, 
								"val_surface_geometry"));
					}
				}
			}

			if (multiSurfaceId != 0) {
				SurfaceGeometryProperty surfaceGeometryProperty = new SurfaceGeometryProperty();
				surfaceGeometryProperty.setName("lod" + (i + 2) + "MultiSurface");
				surfaceGeometryProperty.setNamespace("core");
				surfaceGeometryProperty.setDataType("gml:MultiSurface");
				surfaceGeometryProperty.setValue(multiSurfaceId);
				propertyImporter.doImport(surfaceGeometryProperty, boundarySurfaceId);
			}
		}

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
		// nothing to do...
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		// nothing to do...
	}

}
