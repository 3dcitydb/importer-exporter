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
package org.citydb.query.builder.config;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.kmlExporter.KmlTiling;
import org.citydb.config.project.kmlExporter.KmlTilingMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.tiling.Tiling;

import java.sql.SQLException;

public class TilingFilterBuilder {
	private final AbstractDatabaseAdapter databaseAdapter;

	protected TilingFilterBuilder(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	protected Tiling buildTilingFilter(org.citydb.config.project.query.filter.tiling.AbstractTiling tilingConfig) throws QueryBuildException {
		try {
			// adapt tiling settings in case of KML exports
			if (tilingConfig instanceof KmlTiling) {
				KmlTiling kmlTilingConfig = (KmlTiling) tilingConfig;

				// calculate tile size if required
				if (kmlTilingConfig.getMode() == KmlTilingMode.AUTOMATIC) {
					BoundingBox extent = kmlTilingConfig.getExtent();
					double autoTileSideLength = kmlTilingConfig.getTilingOptions().getAutoTileSideLength();

					// transform extent into the database srs if required
					DatabaseSrs dbSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();
					DatabaseSrs extentSrs = extent.isSetSrs() ? extent.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();			
					if (extentSrs.getSrid() != dbSrs.getSrid()) {
						try {
							extent = databaseAdapter.getUtil().transformBoundingBox(extent, extent.getSrs(), dbSrs);
						} catch (SQLException e) {
							throw new QueryBuildException("Failed to automatically calculate tile size.", e);
						}
					}

					tilingConfig.setRows((int)((extent.getUpperCorner().getY() - extent.getLowerCorner().getY()) / autoTileSideLength) + 1);
					tilingConfig.setColumns((int)((extent.getUpperCorner().getX() - extent.getLowerCorner().getX()) / autoTileSideLength) + 1);
				} 

				// internally map no tiling to manual tiling mode
				else if (kmlTilingConfig.getMode() == KmlTilingMode.NO_TILING) {
					tilingConfig.setRows(1);
					tilingConfig.setColumns(1);
				}
			}

			Tiling tiling = new Tiling(tilingConfig.getExtent(), tilingConfig.getRows(), tilingConfig.getColumns());
			tiling.setTilingOptions(tilingConfig.getTilingOptions());

			return tiling;
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build the tiling filter.", e);
		}
	}
}
