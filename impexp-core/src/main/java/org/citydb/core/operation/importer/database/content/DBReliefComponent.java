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
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.BreaklineRelief;
import org.citygml4j.model.citygml.relief.MassPointRelief;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.relief.TINRelief;
import org.citygml4j.model.citygml.relief.TinProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBReliefComponent implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psReliefComponent;
	private PreparedStatement psTinRelief;
	private PreparedStatement psMassPointRelief;
	private PreparedStatement psBreaklineRelief;
	private DBCityObject cityObjectImporter;
	private DBReliefFeatToRelComp reliefFeatToRelComp;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBReliefComponent(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String componentStmt = "insert into " + schema + ".relief_component (id, objectclass_id, lod, extent) values " +
				"(?, ?, ?, ?)";
		psReliefComponent = batchConn.prepareStatement(componentStmt);

		String tinStmt = "insert into " + schema + ".tin_relief (id, max_length, max_length_unit, stop_lines, break_lines, control_points, surface_geometry_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psTinRelief = batchConn.prepareStatement(tinStmt);

		String masspointStmt = "insert into " + schema + ".masspoint_relief (id, relief_points" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psMassPointRelief = batchConn.prepareStatement(masspointStmt);

		String breaklineStmt = "insert into " + schema + ".breakline_relief (id, ridge_or_valley_lines, break_lines" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psBreaklineRelief = batchConn.prepareStatement(breaklineStmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		reliefFeatToRelComp = importer.getImporter(DBReliefFeatToRelComp.class);
		geometryConverter = importer.getGeometryConverter();
	}

	protected long doImport(AbstractReliefComponent reliefComponent) throws CityGMLImportException, SQLException {
		return doImport(reliefComponent, null, 0);
	}

	public long doImport(AbstractReliefComponent reliefComponent, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(reliefComponent);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long reliefComponentId = cityObjectImporter.doImport(reliefComponent, featureType);

		// import relief component information
		// primary id
		psReliefComponent.setLong(1, reliefComponentId);

		// objectclass id
		psReliefComponent.setLong(2, featureType.getObjectClassId());

		// dem:lod
		psReliefComponent.setInt(3, reliefComponent.getLod());

		// dem:extent
		GeometryObject extent = null;
		if (reliefComponent.isSetExtent())
			extent = geometryConverter.get2DPolygon(reliefComponent.getExtent());

		if (extent != null)
			psReliefComponent.setObject(4, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(extent, batchConn));
		else
			psReliefComponent.setNull(4, nullGeometryType, nullGeometryTypeName);

		psReliefComponent.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.RELIEF_COMPONENT);

		// fill sub-tables according to relief component type
		if (reliefComponent instanceof TINRelief) {
			TINRelief tinRelief = (TINRelief)reliefComponent;

			// primary id
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
					geometryId = surfaceGeometryImporter.doImport(triangulatedSurface, reliefComponentId);

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
							stopLines = geometryConverter.getMultiCurve(tin.getStopLines());

						// breakLines
						if (tin.isSetBreakLines())
							breakLines = geometryConverter.getMultiCurve(tin.getBreakLines());

						// controlPoints
						if (tin.isSetControlPoint())
							controlPoints = geometryConverter.getMultiPoint(tin.getControlPoint());
					}

					tinProperty.unsetTriangulatedSurface();

				} else {
					String href = tinProperty.getHref();
					if (href != null && href.length() != 0)
						importer.logOrThrowUnsupportedXLinkMessage(tinRelief, TriangulatedSurface.class, href);
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
				psTinRelief.setObject(4, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(stopLines, batchConn));
			else
				psTinRelief.setNull(4, nullGeometryType, nullGeometryTypeName);

			// breakLines
			if (breakLines != null)
				psTinRelief.setObject(5, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(breakLines, batchConn));
			else
				psTinRelief.setNull(5, nullGeometryType, nullGeometryTypeName);

			// controlPoints
			if (controlPoints != null)
				psTinRelief.setObject(6, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(controlPoints, batchConn));
			else
				psTinRelief.setNull(6, nullGeometryType, nullGeometryTypeName);

			// triangle patches
			if (geometryId != 0)
				psTinRelief.setLong(7, geometryId);
			else
				psTinRelief.setNull(7, 0);

			// objectclass id
			if (hasObjectClassIdColumn)
				psTinRelief.setLong(8, featureType.getObjectClassId());

			psTinRelief.addBatch();
		}

		else if (reliefComponent instanceof MassPointRelief) {
			MassPointRelief massPointRelief = (MassPointRelief)reliefComponent;

			// primary id
			psMassPointRelief.setLong(1, reliefComponentId);

			// dem:reliefPoints
			GeometryObject reliefPoints = null;
			if (massPointRelief.isSetReliefPoints()) {
				reliefPoints = geometryConverter.getMultiPoint(massPointRelief.getReliefPoints());
				massPointRelief.unsetReliefPoints();
			}

			if (reliefPoints != null)
				psMassPointRelief.setObject(2, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(reliefPoints, batchConn));
			else
				psMassPointRelief.setNull(2, nullGeometryType, nullGeometryTypeName);

			// objectclass id
			if (hasObjectClassIdColumn)
				psMassPointRelief.setLong(3, featureType.getObjectClassId());

			psMassPointRelief.addBatch();
		}

		else if (reliefComponent instanceof BreaklineRelief) {
			BreaklineRelief breakLineRelief = (BreaklineRelief)reliefComponent;

			// primary id
			psBreaklineRelief.setLong(1, reliefComponentId);

			// dem:ridgeOrValleyLines and dem:breakLines
			GeometryObject ridgeOrValleyLines, breakLines;
			ridgeOrValleyLines = breakLines = null;

			if (breakLineRelief.isSetRidgeOrValleyLines()) {
				ridgeOrValleyLines = geometryConverter.getMultiCurve(breakLineRelief.getRidgeOrValleyLines());
				breakLineRelief.unsetRidgeOrValleyLines();
			}

			if (breakLineRelief.isSetBreaklines()) {
				breakLines = geometryConverter.getMultiCurve(breakLineRelief.getBreaklines());
				breakLineRelief.unsetBreaklines();
			}

			if (ridgeOrValleyLines != null)
				psBreaklineRelief.setObject(2, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(ridgeOrValleyLines, batchConn));
			else
				psBreaklineRelief.setNull(2, nullGeometryType, nullGeometryTypeName);

			if (breakLines != null)
				psBreaklineRelief.setObject(3, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(breakLines, batchConn));
			else
				psBreaklineRelief.setNull(3, nullGeometryType, nullGeometryTypeName);

			// objectclass id
			if (hasObjectClassIdColumn)
				psBreaklineRelief.setLong(4, featureType.getObjectClassId());

			psBreaklineRelief.addBatch();
		}

		// relief component to relief feature
		if (parent instanceof ReliefFeature)
			reliefFeatToRelComp.doImport(reliefComponentId, parentId);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(reliefComponent, reliefComponentId, featureType);

		return reliefComponentId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psReliefComponent.executeBatch();
			psTinRelief.executeBatch();
			psMassPointRelief.executeBatch();
			psBreaklineRelief.executeBatch();		
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psReliefComponent.close();
		psTinRelief.close();
		psMassPointRelief.close();
		psBreaklineRelief.close();
	}

}
