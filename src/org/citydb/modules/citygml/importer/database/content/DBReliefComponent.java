/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.log.Logger;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.BreaklineRelief;
import org.citygml4j.model.citygml.relief.MassPointRelief;
import org.citygml4j.model.citygml.relief.TINRelief;
import org.citygml4j.model.citygml.relief.TinProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;

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
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBReliefComponent(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		psReliefComponent = batchConn.prepareStatement("insert into RELIEF_COMPONENT (ID, OBJECTCLASS_ID, LOD, EXTENT) values (?, ?, ?, ?)");
		psTinRelief = batchConn.prepareStatement("insert into TIN_RELIEF (ID, MAX_LENGTH, MAX_LENGTH_UNIT, STOP_LINES, BREAK_LINES, CONTROL_POINTS, SURFACE_GEOMETRY_ID) values (?, ?, ?, ?, ?, ?, ?)");
		psMassPointRelief = batchConn.prepareStatement("insert into MASSPOINT_RELIEF (ID, RELIEF_POINTS) values (?, ?)");
		psBreaklineRelief = batchConn.prepareStatement("insert into BREAKLINE_RELIEF (ID, RIDGE_OR_VALLEY_LINES, BREAK_LINES) values (?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		reliefFeatToRelComp = (DBReliefFeatToRelComp)dbImporterManager.getDBImporter(DBImporterEnum.RELIEF_FEAT_TO_REL_COMP);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
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

		// OBJECTCLASS_ID
		psReliefComponent.setLong(2, Util.cityObject2classId(reliefComponent.getCityGMLClass()));

		// dem:lod
		psReliefComponent.setInt(3, reliefComponent.getLod());

		// dem:extent
		GeometryObject extent = null;
		if (reliefComponent.isSetExtent())
			extent = otherGeometryImporter.get2DPolygon(reliefComponent.getExtent());

		if (extent != null)
			psReliefComponent.setObject(4, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(extent, batchConn));
		else
			psReliefComponent.setNull(4, nullGeometryType, nullGeometryTypeName);

		psReliefComponent.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.RELIEF_COMPONENT);

		// fill sub-tables according to relief component type
		if (reliefComponent.getCityGMLClass() == CityGMLClass.TIN_RELIEF) {
			TINRelief tinRelief = (TINRelief)reliefComponent;

			// ID
			psTinRelief.setLong(1, reliefComponentId);

			double maxLength = -Double.MAX_VALUE;
			String maxLengthUnit = null;
			GeometryObject stopLines, breakLines, controlPoints;
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
						if (tin.isSetMaxLength()) {
							maxLength = tin.getMaxLength().getValue();
							maxLengthUnit = tin.getMaxLength().getUom();
						}

						// stopLines
						if (tin.isSetStopLines())
							stopLines = otherGeometryImporter.getMultiCurve(tin.getStopLines());

						// breakLines
						if (tin.isSetBreakLines())
							breakLines = otherGeometryImporter.getMultiCurve(tin.getBreakLines());

						// controlPoints
						if (tin.isSetControlPoint())
							controlPoints = otherGeometryImporter.getMultiPoint(tin.getControlPoint());
					}

					tinProperty.unsetTriangulatedSurface();

				} else {
					// xlink
					String href = tinProperty.getHref();

					if (href != null && href.length() != 0)
						LOG.error("XLink reference '" + href + "' to " + GMLClass.TRIANGULATED_SURFACE + " element is not supported");
				}
			}

			// maxLength
			if (maxLength != -Double.MAX_VALUE) {
				psTinRelief.setDouble(2, maxLength);
				psTinRelief.setString(3, maxLengthUnit);
			} else {
				psTinRelief.setNull(2, Types.DOUBLE);
				psTinRelief.setNull(3, Types.VARCHAR);
			}

			// stopLines
			if (stopLines != null)
				psTinRelief.setObject(4, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(stopLines, batchConn));
			else
				psTinRelief.setNull(4, nullGeometryType, nullGeometryTypeName);

			// breakLines
			if (breakLines != null)
				psTinRelief.setObject(5, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(breakLines, batchConn));
			else
				psTinRelief.setNull(5, nullGeometryType, nullGeometryTypeName);

			// controlPoints
			if (controlPoints != null)
				psTinRelief.setObject(6, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(controlPoints, batchConn));
			else
				psTinRelief.setNull(6, nullGeometryType, nullGeometryTypeName);

			// triangle patches
			if (geometryId != 0)
				psTinRelief.setLong(7, geometryId);
			else
				psTinRelief.setNull(7, 0);

			psTinRelief.addBatch();
		}

		else if (reliefComponent.getCityGMLClass() == CityGMLClass.MASSPOINT_RELIEF) {
			MassPointRelief massPointRelief = (MassPointRelief)reliefComponent;

			// ID
			psMassPointRelief.setLong(1, reliefComponentId);

			// reliefPoints
			GeometryObject reliefPoints = null;
			if (massPointRelief.isSetReliefPoints()) {
				reliefPoints = otherGeometryImporter.getMultiPoint(massPointRelief.getReliefPoints());
				massPointRelief.unsetReliefPoints();
			}

			if (reliefPoints != null)
				psMassPointRelief.setObject(2, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(reliefPoints, batchConn));
			else
				psMassPointRelief.setNull(2, nullGeometryType, nullGeometryTypeName);

			psMassPointRelief.addBatch();
		}

		else if (reliefComponent.getCityGMLClass() == CityGMLClass.BREAKLINE_RELIEF) {
			BreaklineRelief breakLineRelief = (BreaklineRelief)reliefComponent;

			// ID
			psBreaklineRelief.setLong(1, reliefComponentId);

			GeometryObject ridgeOrValleyLines, breakLines;
			ridgeOrValleyLines = breakLines = null;

			if (breakLineRelief.isSetRidgeOrValleyLines()) {
				ridgeOrValleyLines = otherGeometryImporter.getMultiCurve(breakLineRelief.getRidgeOrValleyLines());
				breakLineRelief.unsetRidgeOrValleyLines();
			}

			if (breakLineRelief.isSetBreaklines()) {
				breakLines = otherGeometryImporter.getMultiCurve(breakLineRelief.getBreaklines());
				breakLineRelief.unsetBreaklines();
			}

			if (ridgeOrValleyLines != null)
				psBreaklineRelief.setObject(2, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(ridgeOrValleyLines, batchConn));
			else
				psBreaklineRelief.setNull(2, nullGeometryType, nullGeometryTypeName);

			if (breakLines != null)
				psBreaklineRelief.setObject(3, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(breakLines, batchConn));
			else
				psBreaklineRelief.setNull(3, nullGeometryType, nullGeometryTypeName);

			psBreaklineRelief.addBatch();
		}

		// reliefComponent2reliefFeature
		reliefFeatToRelComp.insert(reliefComponentId, parentId);

		// insert local appearance
		cityObjectImporter.insertAppearance(reliefComponent, reliefComponentId);

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
