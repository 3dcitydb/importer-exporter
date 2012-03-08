/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.BreaklineRelief;
import org.citygml4j.model.citygml.relief.MassPointRelief;
import org.citygml4j.model.citygml.relief.TINRelief;
import org.citygml4j.model.citygml.relief.TinProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.postgis.PGgeometry;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBReliefComponent implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psReliefComponent;
	private PreparedStatement psTinRelief;
	private PreparedStatement psMassPointRelief;
	private PreparedStatement psBreaklineRelief;
	private DBCityObject cityObjectImporter;
	private DBReliefFeatToRelComp reliefFeatToRelComp;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBStGeometry stGeometry;
	
	private int batchCounter;

	public DBReliefComponent(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psReliefComponent = batchConn.prepareStatement("insert into RELIEF_COMPONENT (ID, NAME, NAME_CODESPACE, DESCRIPTION, LOD, EXTENT) values " +
		"(?, ?, ?, ?, ?, ?)");
		psTinRelief = batchConn.prepareStatement("insert into TIN_RELIEF (ID, MAX_LENGTH, STOP_LINES, BREAK_LINES, CONTROL_POINTS, SURFACE_GEOMETRY_ID) values " +
		"(?, ?, ?, ?, ?, ?)");
		psMassPointRelief = batchConn.prepareStatement("insert into MASSPOINT_RELIEF (ID, RELIEF_POINTS) values " +
		"(?, ?)");
		psBreaklineRelief = batchConn.prepareStatement("insert into BREAKLINE_RELIEF (ID, RIDGE_OR_VALLEY_LINES, BREAK_LINES) values " +
		"(?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		reliefFeatToRelComp = (DBReliefFeatToRelComp)dbImporterManager.getDBImporter(DBImporterEnum.RELIEF_FEAT_TO_REL_COMP);
		stGeometry = (DBStGeometry)dbImporterManager.getDBImporter(DBImporterEnum.ST_GEOMETRY);
	}

	public long insert(AbstractReliefComponent reliefComponent, long parentId) throws SQLException {
		long reliefComponentId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (reliefComponentId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(reliefComponent, reliefComponentId);

		// ReliefComponent
		// ID
		psReliefComponent.setLong(1, reliefComponentId);

		// gml:name
		if (reliefComponent.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(reliefComponent);

			psReliefComponent.setString(2, dbGmlName[0]);
			psReliefComponent.setString(3, dbGmlName[1]);
		} else {
			psReliefComponent.setNull(2, Types.VARCHAR);
			psReliefComponent.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (reliefComponent.isSetDescription()) {
			String description = reliefComponent.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psReliefComponent.setString(4, description);
		} else {
			psReliefComponent.setNull(4, Types.VARCHAR);
		}

		// lod
		psReliefComponent.setInt(5, reliefComponent.getLod());

		// extent
		if (reliefComponent.isSetExtent()) {
			PGgeometry extent = stGeometry.get2DPolygon(reliefComponent.getExtent());

			if (extent != null) {
				psReliefComponent.setObject(6, extent);
			} else
				psReliefComponent.setNull(6, Types.OTHER, "ST_GEOMETRY");
		} else
			psReliefComponent.setNull(6, Types.OTHER, "ST_GEOMETRY");

		psReliefComponent.addBatch();
		if (++batchCounter == Internal.POSTGRESQL_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.RELIEF_COMPONENT);

		// fill sub-tables according to relief component type
		if (reliefComponent.getCityGMLClass() == CityGMLClass.TIN_RELIEF) {
			TINRelief tinRelief = (TINRelief)reliefComponent;

			// ID
			psTinRelief.setLong(1, reliefComponentId);

			double maxLength = -Double.MAX_VALUE;
			PGgeometry stopLines, breakLines, controlPoints;
			stopLines = breakLines = controlPoints = null;
			long geometryId = 0;

			// gml:TriangulatedSurface
			if (tinRelief.isSetTin()) {
				TinProperty tinProperty = tinRelief.getTin();
				TriangulatedSurface triangulatedSurface = tinProperty.getObject();
				
				if (triangulatedSurface != null) {
					geometryId = surfaceGeometryImporter.insert(triangulatedSurface, reliefComponentId);

					// gml:Tin
					if (triangulatedSurface.getGMLClass() == GMLClass.TIN) {
						Tin tin = (Tin)triangulatedSurface;

						// maxLength
						if (tin.isSetMaxLength()) 
							maxLength = tin.getMaxLength().getValue();

						// stopLines
						if (tin.isSetStopLines())
							stopLines = stGeometry.getMultiCurve(tin.getStopLines());

						// breakLines
						if (tin.isSetBreakLines())
							breakLines = stGeometry.getMultiCurve(tin.getBreakLines());

						// controlPoints
						if (tin.isSetControlPoint())
							controlPoints = stGeometry.getMultiPoint(tin.getControlPoint());
					}

				} else {
					// xlink
					String href = tinProperty.getHref();

					if (href != null && href.length() != 0)
						LOG.error("XLink reference '" + href + "' to gml:TriangulatedSurface element is not supported");
				}
			}

			// maxLength
			if (maxLength != -Double.MAX_VALUE)
				psTinRelief.setDouble(2, maxLength);
			else
				psTinRelief.setNull(2, Types.DOUBLE);

			// stopLines
			if (stopLines != null) {
				psTinRelief.setObject(3, stopLines);
			} else
				psTinRelief.setNull(3, Types.OTHER, "ST_GEOMETRY");

			// breakLines
			if (breakLines != null) {
				psTinRelief.setObject(4, breakLines);
			} else
				psTinRelief.setNull(4, Types.OTHER, "ST_GEOMETRY");

			// controlPoints
			if (controlPoints != null) {
				psTinRelief.setObject(5, controlPoints);
			} else
				psTinRelief.setNull(5, Types.OTHER, "ST_GEOMETRY");

			// triangle patches
			if (geometryId != 0)
				psTinRelief.setLong(6, geometryId);
			else
				psTinRelief.setNull(6, 0);

			psTinRelief.addBatch();
		}

		else if (reliefComponent.getCityGMLClass() == CityGMLClass.MASSPOINT_RELIEF) {
			MassPointRelief massPointRelief = (MassPointRelief)reliefComponent;

			// ID
			psMassPointRelief.setLong(1, reliefComponentId);

			// reliefPoints
			PGgeometry reliefPoints = null;
			if (massPointRelief.isSetReliefPoints())
				reliefPoints = stGeometry.getMultiPoint(massPointRelief.getReliefPoints());

			if (reliefPoints != null) {
				psMassPointRelief.setObject(2, reliefPoints);
			} else
				psMassPointRelief.setNull(2, Types.OTHER, "ST_GEOMETRY");

			psMassPointRelief.addBatch();
		}

		else if (reliefComponent.getCityGMLClass() == CityGMLClass.BREAKLINE_RELIEF) {
			BreaklineRelief breakLineRelief = (BreaklineRelief)reliefComponent;

			// ID
			psBreaklineRelief.setLong(1, reliefComponentId);

			PGgeometry ridgeOrValleyLines, breakLines;
			ridgeOrValleyLines = breakLines = null;

			if (breakLineRelief.isSetRidgeOrValleyLines())
				ridgeOrValleyLines = stGeometry.getMultiCurve(breakLineRelief.getRidgeOrValleyLines());

			if (breakLineRelief.isSetBreaklines())
				breakLines = stGeometry.getMultiCurve(breakLineRelief.getBreaklines());

			if (ridgeOrValleyLines != null) {
				psBreaklineRelief.setObject(2, ridgeOrValleyLines);
			} else
				psBreaklineRelief.setNull(2, Types.OTHER, "ST_GEOMETRY");

			if (breakLines != null) {
				psBreaklineRelief.setObject(3, breakLines);
			} else
				psBreaklineRelief.setNull(3, Types.OTHER, "ST_GEOMETRY");

			psBreaklineRelief.addBatch();
		}

		// reliefComponent2reliefFeature
		reliefFeatToRelComp.insert(reliefComponentId, parentId);

		return reliefComponentId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psReliefComponent.executeBatch();
		psTinRelief.executeBatch();
		psMassPointRelief.executeBatch();
		psBreaklineRelief.executeBatch();		
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psReliefComponent.close();
		psTinRelief.close();
		psMassPointRelief.close();
		psBreaklineRelief.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.RELIEF_COMPONENT;
	}

}
