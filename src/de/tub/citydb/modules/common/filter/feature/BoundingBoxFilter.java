/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.modules.common.filter.feature;

import java.sql.SQLException;
import java.util.List;

import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.api.gui.BoundingBoxCorner;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;
import de.tub.citydb.config.project.filter.FilterBoundingBox;
import de.tub.citydb.config.project.filter.TiledBoundingBox;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.filter.Filter;
import de.tub.citydb.modules.common.filter.FilterMode;
import de.tub.citydb.util.database.DBUtil;

public class BoundingBoxFilter implements Filter<Envelope> {
	private final Logger LOG = Logger.getInstance();
	private final AbstractFilterConfig filterConfig;
	private final FilterMode mode;
	private final Config config;

	private boolean isActive;
	private boolean useTiling;
	private FilterBoundingBox boundingBoxConfig;

	private BoundingBox boundingBox;
	private BoundingBox activeBoundingBox;

	private double rowHeight = 0;  
	private double columnWidth = 0;
	private int rows = 1;  
	private int columns = 1;
	private int activeRow = 0;
	private int activeColumn = 0;
	private DatabaseSrs srs;

	public BoundingBoxFilter(Config config, FilterMode mode) {
		this.mode = mode;
		this.config = config;

		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else if (mode == FilterMode.KML_EXPORT)
			filterConfig = config.getProject().getKmlExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();			

		init();
	}

	private void init() {
		isActive = filterConfig.isSetComplexFilter() &&
		filterConfig.getComplexFilter().getBoundingBox().isSet();

		if (isActive) {
			boundingBoxConfig = filterConfig.getComplexFilter().getBoundingBox();
			if (mode == FilterMode.EXPORT || mode == FilterMode.KML_EXPORT)
				useTiling = ((TiledBoundingBox)boundingBoxConfig).getTiling().getMode() != TilingMode.NO_TILING;

			if (boundingBoxConfig.getLowerLeftCorner().getX() != null && 
					boundingBoxConfig.getLowerLeftCorner().getY() != null &&
					boundingBoxConfig.getUpperRightCorner().getX() != null && 
					boundingBoxConfig.getUpperRightCorner().getY() != null) {
				boundingBox = new BoundingBox(boundingBoxConfig);

				// check whether we have to transform coordinate values of bounding box
				DatabaseSrs bboxSrs = boundingBoxConfig.getSrs();
				srs = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem();

				// target db srid differs if another coordinate transformation is
				// applied to the CityGML export
				if (mode == FilterMode.EXPORT) {
					DatabaseSrs targetSRS = config.getProject().getExporter().getTargetSRS();
					if (targetSRS.isSupported() && targetSRS.getSrid() != srs.getSrid())
						srs = targetSRS;
				}
				
				if (bboxSrs != null && bboxSrs.isSupported() && bboxSrs.getSrid() != srs.getSrid()) {			
					try {
						boundingBox = DBUtil.transformBBox(boundingBox, bboxSrs, srs);
					} catch (SQLException sqlEx) {
						LOG.error("Failed to initialize bounding box filter.");
					}
				}

				activeBoundingBox = boundingBox;

				if (useTiling) {
					Tiling tiling = ((TiledBoundingBox)boundingBoxConfig).getTiling();					
					rows = tiling.getRows();
					columns = tiling.getColumns();
					rowHeight = (boundingBox.getUpperRightCorner().getY() - boundingBox.getLowerLeftCorner().getY()) / rows;  
					columnWidth = (boundingBox.getUpperRightCorner().getX() - boundingBox.getLowerLeftCorner().getX()) / columns;
				}
			} 
			else
				isActive = false;
		}
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}

	public boolean filter(Envelope envelope) {
		if (isActive) {
			if (!envelope.isSetLowerCorner() || !envelope.isSetUpperCorner())
				return true;

			DirectPosition lowerCorner = envelope.getLowerCorner();
			DirectPosition upperCorner = envelope.getUpperCorner();

			if (!lowerCorner.isSetValue() || !upperCorner.isSetValue())
				return true;

			List<Double> lowerCornerValue = lowerCorner.getValue();
			List<Double> upperCornerValue = upperCorner.getValue();

			if (lowerCornerValue.size() < 2 || upperCornerValue.size() < 2)
				return true;

			Double minX = lowerCornerValue.get(0);
			Double minY = lowerCornerValue.get(1);

			Double maxX = upperCornerValue.get(0);
			Double maxY = upperCornerValue.get(1);

			if (!useTiling) { // no tiling
				if (boundingBoxConfig.isSetContainMode()) {
					if (minX >= activeBoundingBox.getLowerLeftCorner().getX() &&
							minY >= activeBoundingBox.getLowerLeftCorner().getY() &&
							maxX <= activeBoundingBox.getUpperRightCorner().getX() &&
							maxY <= activeBoundingBox.getUpperRightCorner().getY())
						return false;
					else
						return true;
				}

				else if (boundingBoxConfig.isSetOverlapMode()) {
					if (minX >= activeBoundingBox.getUpperRightCorner().getX() ||
							maxX <= activeBoundingBox.getLowerLeftCorner().getX() ||
							minY >= activeBoundingBox.getUpperRightCorner().getY() ||
							maxY <= activeBoundingBox.getLowerLeftCorner().getY())
						return true;
					else 
						return false;
				}
			}
			else { // manual tiling
				double centroidX = (minX + maxX) / 2;
				double centroidY = (minY + maxY) / 2;
				if (centroidX >= activeBoundingBox.getLowerLeftCorner().getX() &&
						centroidY > activeBoundingBox.getLowerLeftCorner().getY() &&
						centroidX < activeBoundingBox.getUpperRightCorner().getX() &&
						centroidY <= activeBoundingBox.getUpperRightCorner().getY())
					return false;
				else
					return true;
			}
		}

		return false;
	}

	public BoundingBox getFilterState() {
		return activeBoundingBox;
	}

	public int getSrid() {
		return srs.getSrid();
	}
	
	public void setActiveTile(int activeRow, int activeColumn) {
		if (!useTiling || 
				activeRow < 0 || activeRow > rows ||
				activeColumn < 0 || activeColumn > columns)
			return;

		this.activeRow = activeRow;
		this.activeColumn = activeColumn;

		double lowerLeftX = boundingBox.getLowerLeftCorner().getX() + (activeColumn * columnWidth);
		double lowerLeftY = boundingBox.getLowerLeftCorner().getY() + (activeRow * rowHeight);
		double upperRightX = lowerLeftX + columnWidth;
		double upperRightY = lowerLeftY + rowHeight;

		activeBoundingBox = new BoundingBox(
				new BoundingBoxCorner(lowerLeftX, lowerLeftY),
				new BoundingBoxCorner(upperRightX, upperRightY),
				boundingBox.getSrs()
		);
	}

	public int getTileRow() {
		return activeRow;
	}

	public int getTileColumn() {
		return activeColumn;
	}
}
