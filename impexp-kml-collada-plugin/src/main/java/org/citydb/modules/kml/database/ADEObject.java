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
package org.citydb.modules.kml.database;

import net.opengis.kml._2.*;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.ade.ADEKmlExporterPreference;
import org.citydb.config.project.kmlExporter.*;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.GeometryCounterEvent;
import org.citydb.log.Logger;
import org.citydb.modules.kml.ade.ADEKmlExportException;
import org.citydb.modules.kml.ade.ADEKmlExportExtensionManager;
import org.citydb.modules.kml.ade.ADEKmlExportManager;
import org.citydb.modules.kml.ade.ADEKmlExportQueries;
import org.citydb.modules.kml.util.BalloonTemplateHandler;
import org.citydb.modules.kml.util.ElevationServiceHandler;
import org.citydb.query.Query;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ADEObject extends KmlGenericObject{
	private final Logger log = Logger.getInstance();

	public static final String POINT = "Point";
	public static final String CURVE = "Curve";

	private final int adeObjectClassId;

	public ADEObject(Connection connection,
	                 Query query,
	                 KmlExporterManager kmlExporterManager,
	                 net.opengis.kml._2.ObjectFactory kmlFactory,
	                 AbstractDatabaseAdapter databaseAdapter,
	                 BlobExportAdapter textureExportAdapter,
	                 ElevationServiceHandler elevationServiceHandler,
	                 BalloonTemplateHandler balloonTemplateHandler,
	                 EventDispatcher eventDispatcher,
	                 Config config,
	                 int adeObjectClassId) {

		super(connection,
				query,
				kmlExporterManager,
				kmlFactory,
				databaseAdapter,
				textureExportAdapter,
				elevationServiceHandler,
				balloonTemplateHandler,
				eventDispatcher,
				config);

		this.adeObjectClassId = adeObjectClassId;
	}

	private ADEKmlExporterPreference getPreference() {
		return ADEKmlExportExtensionManager.getInstance().getPreference(config, adeObjectClassId);
	}
	protected List<DisplayForm> getDisplayForms() {
		return getPreference().getDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return getPreference().getColladaOptions();
	}

	public PointAndCurve getPointAndCurve() {
		return getPreference().getPointAndCurve();
	}

	public Balloon getBalloonSettings() {
		return getPreference().getBalloon();
	}

	public String getStyleBasisName() {
		return ADEKmlExportExtensionManager.getInstance().getPreference(config, adeObjectClassId).getTarget();
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement pointAndCurveQueryPs = null;
		ResultSet pointAndCurveQueryRs = null;
		boolean hasPointAndCurve = false;
		PreparedStatement brepIdsQueryPs = null;
		ResultSet brepIdsQueryRs = null;
		boolean hasBrep = false;
		PreparedStatement brepGeometriesQueryPs = null;
		ResultSet brepGeometriesQueryRs = null;

		try {
			ADEKmlExportManager adeKmlExportManager = kmlExporterManager.getADEKmlExportManager(adeObjectClassId);
			ADEKmlExportQueries adeQueries = adeKmlExportManager.getKmlExporter(adeObjectClassId).getQueries();

			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if (!work.getDisplayForm().isAchievableFromLoD(currentLod)) 
					break;

				try {
					String query = adeQueries.getSurfaceGeometryQuery(currentLod);
					brepIdsQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= getParameterCount(query); i++)
						brepIdsQueryPs.setLong(i, work.getId());

					brepIdsQueryRs = brepIdsQueryPs.executeQuery();

					if (brepIdsQueryRs.isBeforeFirst()) {
						hasBrep = true; // result set not empty
					}

					// check for point or curve
					query = adeQueries.getPointAndCurveQuery(currentLod);
					if (query != null) {
						pointAndCurveQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
						for (int i = 1; i <= getParameterCount(query); i++)
							pointAndCurveQueryPs.setLong(i, work.getId());

						pointAndCurveQueryRs = pointAndCurveQueryPs.executeQuery();
						if (pointAndCurveQueryRs.next())
							hasPointAndCurve = true;
					}
				} catch (Exception e) {
					log.error("SQL error while querying the highest available LOD: " + e.getMessage());
				}

				if (hasBrep || hasPointAndCurve)
					break;

				currentLod--;
			}

			if (!hasBrep && !hasPointAndCurve) {
				String fromMessage = " from LoD" + lodToExportFrom;
				if (lodToExportFrom == 5) {
					if (work.getDisplayForm().getForm() == DisplayForm.COLLADA)
						fromMessage = ". LoD1 or higher required";
					else
						fromMessage = " from any LoD";
				}
				log.info("Could not display object " + work.getGmlId() + " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else { // result not empty
				kmlExporterManager.updateFeatureTracker(work);

				if (hasPointAndCurve) { // point or curve geometry
					kmlExporterManager.print(createPlacemarksForPointOrCurve(pointAndCurveQueryRs, work),
							work,
							getBalloonSettings().isBalloonContentInSeparateFile());
				}

				if (hasBrep) {
					String query;
					if (work.getDisplayForm().getForm() == DisplayForm.FOOTPRINT || work.getDisplayForm().getForm() == DisplayForm.EXTRUDED) {
						query = adeQueries.getSurfaceGeometryQuery(currentLod);
					} else {
						query = adeQueries.getSurfaceGeometryRefIdsQuery(currentLod);
					}
					brepGeometriesQueryPs = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= getParameterCount(query); i++)
						brepGeometriesQueryPs.setLong(i, work.getId());
					brepGeometriesQueryRs = brepGeometriesQueryPs.executeQuery();

					// get the proper displayForm (for highlighting)
					int indexOfDf = getDisplayForms().indexOf(work.getDisplayForm());
					if (indexOfDf != -1)
						work.setDisplayForm(getDisplayForms().get(indexOfDf));

					switch (work.getDisplayForm().getForm()) {
						case DisplayForm.FOOTPRINT:
							kmlExporterManager.print(createPlacemarksForFootprint(brepGeometriesQueryRs, work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
							break;

						case DisplayForm.EXTRUDED:
							PreparedStatement psQuery = null;
							ResultSet rs = null;

							try {
								query = queries.getExtrusionHeight();
								psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
								for (int i = 1; i <= getParameterCount(query); i++)
									psQuery.setLong(i, work.getId());

								rs = psQuery.executeQuery();
								rs.next();

								double measuredHeight = rs.getDouble("envelope_measured_height");
								kmlExporterManager.print(createPlacemarksForExtruded(brepGeometriesQueryRs, work, measuredHeight, false),
										work, getBalloonSettings().isBalloonContentInSeparateFile());
								break;
							} finally {
								try { if (psQuery != null) psQuery.close(); } catch (SQLException e) {}
							}

						case DisplayForm.GEOMETRY:
							setGmlId(work.getGmlId());
							setId(work.getId());
							if (this.query.isSetTiling()) { // region
								if (work.getDisplayForm().isHighlightingEnabled())
									kmlExporterManager.print(createPlacemarksForHighlighting(brepGeometriesQueryRs, work, true), work, getBalloonSettings().isBalloonContentInSeparateFile());

								kmlExporterManager.print(createPlacemarksForGeometry(brepGeometriesQueryRs, work, true), work, getBalloonSettings().isBalloonContentInSeparateFile());
							} else { // reverse order for single objects
								kmlExporterManager.print(createPlacemarksForGeometry(brepGeometriesQueryRs, work, true), work, getBalloonSettings().isBalloonContentInSeparateFile());
								if (work.getDisplayForm().isHighlightingEnabled())
									kmlExporterManager.print(createPlacemarksForHighlighting(brepGeometriesQueryRs, work, true), work, getBalloonSettings().isBalloonContentInSeparateFile());
							}
							break;

						case DisplayForm.COLLADA:
							String currentgmlId = getGmlId();
							setGmlId(work.getGmlId());
							setId(work.getId());
							fillGenericObjectForCollada(brepGeometriesQueryRs, getColladaOptions().isGenerateTextureAtlases(), true);

							if (currentgmlId != null && !currentgmlId.equals(work.getGmlId()) && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING)
								log.info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");

							List<Point3d> anchorCandidates = getOrigins();
							double zOffset = getZOffsetFromConfigOrDB(work.getId());
							if (zOffset == Double.MAX_VALUE) {
								zOffset = getZOffsetFromGEService(work.getId(), anchorCandidates);
							}
							setZOffset(zOffset);

							ColladaOptions colladaOptions = getColladaOptions();
							setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
							try {
								if (work.getDisplayForm().isHighlightingEnabled())
									kmlExporterManager.print(createPlacemarksForHighlighting(brepGeometriesQueryRs, work, true), work, getBalloonSettings().isBalloonContentInSeparateFile());
							} catch (Exception ioe) {
								log.logStackTrace(ioe);
							}

							break;
					}
				}				
			}
		} catch (SQLException sqlEx) {
			log.error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
		} catch (JAXBException jaxbEx) {
			log.error("XML error while working on city object " + work.getGmlId() + ": " + jaxbEx.getMessage());
		} catch (ADEKmlExportException e) {
			log.error("ADE Kml-Export error while working on city object " + work.getGmlId() + ": " + e.getMessage());
		} finally {
			if (brepGeometriesQueryPs != null)
				try { brepGeometriesQueryPs.close(); } catch (SQLException e) {}
			if (brepGeometriesQueryPs != null)
				try { brepGeometriesQueryPs.close(); } catch (SQLException e) {}
			if (pointAndCurveQueryPs != null)
				try { pointAndCurveQueryPs.close(); } catch (SQLException e) {}
		}
	}

	public PlacemarkType createPlacemarkForColladaModel() throws SQLException {
		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {getOrigin().x,
				getOrigin().y,
				getOrigin().z});
		setLocation(reducePrecisionForXorY(originInWGS84[0]),
				reducePrecisionForXorY(originInWGS84[1]),
				reducePrecisionForZ(originInWGS84[2]));

		return super.createPlacemarkForColladaModel();
	}

	protected List<PlacemarkType> createPlacemarksForPointOrCurve(ResultSet rs,
			KmlSplittingResult work) throws SQLException {
		PointAndCurve pacSettings = getPointAndCurve();
		List<PlacemarkType> placemarkList= new ArrayList<>();

		double zOffset = getZOffsetFromConfigOrDB(work.getId());
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, null, (zOffset == Double.MAX_VALUE));
		rs.beforeFirst(); // return cursor to beginning
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);
		}
		while (rs.next()) {
			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			Object buildingGeometryObj = rs.getObject(1); 

			GeometryObject pointOrCurveGeometry = geometryConverterAdapter.getGeometry(buildingGeometryObj);			

			eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));
			MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();

			if (pointOrCurveGeometry.getGeometryType() == GeometryType.MULTI_POINT ||
					pointOrCurveGeometry.getGeometryType() == GeometryType.POINT) { // point

				for (int i = 0; i < pointOrCurveGeometry.getNumElements(); i++) {
					GeometryObject pointGeometry = GeometryObject.createPoint(pointOrCurveGeometry.getCoordinates(i), pointOrCurveGeometry.getDimension(), pointOrCurveGeometry.getSrid());
					multiGeometry.getAbstractGeometryGroup().add(createPointGeometryElement(pointGeometry, pacSettings, zOffset));
				}

				if (pacSettings.getPointDisplayMode() == PointDisplayMode.CROSS_LINE){
					if (pacSettings.isPointHighlightingEnabled() || pacSettings.isPointCubeHighlightingEnabled())
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Style");
					else
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Normal");
				} else if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
					placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Normal");
				} else if (pacSettings.getPointDisplayMode() == PointDisplayMode.CUBE) {
					// setting Style references
					if (pacSettings.isPointCubeHighlightingEnabled())
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Style");
					else
						placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.POINT + "Normal");
				}
			}
			else if (pointOrCurveGeometry.getGeometryType() == GeometryType.MULTI_LINE_STRING ||
					pointOrCurveGeometry.getGeometryType() == GeometryType.LINE_STRING){ // curve
				pointOrCurveGeometry = convertToWGS84(pointOrCurveGeometry);
				for (int i = 0; i < pointOrCurveGeometry.getNumElements(); i++) {
					LineStringType lineString = kmlFactory.createLineStringType();
					double[] ordinatesArray = pointOrCurveGeometry.getCoordinates(i);
					for (int j = 0; j < ordinatesArray.length; j = j+3){
						lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArray[j]) + ","
								+ reducePrecisionForXorY(ordinatesArray[j + 1]) + ","
								+ reducePrecisionForZ(ordinatesArray[j + 2] + zOffset));
					}

					switch (pacSettings.getCurveAltitudeMode()) {
						case ABSOLUTE:
							lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
							break;
						case RELATIVE:
							lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
							break;
						case CLAMP_TO_GROUND:
							lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
							break;
					}
					multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createLineString(lineString));
				}

				if (pacSettings.isCurveHighlightingEnabled())
					placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.CURVE + "Style");
				else
					placemark.setStyleUrl("#" + getStyleBasisName() + ADEObject.CURVE + "Normal");
			}

			if (getBalloonSettings().isIncludeDescription() &&
					!work.getDisplayForm().isHighlightingEnabled()) { // avoid double description
				addBalloonContents(placemark, work.getId());
			}

			placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));
			placemark.setId(/* DisplayForm.GEOMETRY_PLACEMARK_ID + */ placemark.getName());
			placemark.setName(work.getGmlId());
			placemarkList.add(placemark);
		}

		return placemarkList;		
	}

	private JAXBElement<? extends AbstractGeometryType> createPointGeometryElement(GeometryObject pointOrCurveGeometry, PointAndCurve pacSettings, double zOffset) throws SQLException {
		LineStringType lineString = kmlFactory.createLineStringType();
		PointType pointString = kmlFactory.createPointType();
		double[] ordinatesArray = super.convertPointCoordinatesToWGS84(pointOrCurveGeometry.getCoordinates(0));
		double zOrdinate = ordinatesArray[2] + zOffset;

		if (pacSettings.getPointDisplayMode() == PointDisplayMode.CROSS_LINE){
			double[] ordinatesArrayTopLeft = new double[2];
			ordinatesArrayTopLeft[0] = pointOrCurveGeometry.getCoordinates(0)[0] - 1;
			ordinatesArrayTopLeft[1] = pointOrCurveGeometry.getCoordinates(0)[1] + 1;
			ordinatesArrayTopLeft = super.convertPointCoordinatesToWGS84(ordinatesArrayTopLeft);

			double[] ordinatesArrayBottomRight = new double[2];
			ordinatesArrayBottomRight[0] = pointOrCurveGeometry.getCoordinates(0)[0] + 1;
			ordinatesArrayBottomRight[1] = pointOrCurveGeometry.getCoordinates(0)[1] - 1;
			ordinatesArrayBottomRight = super.convertPointCoordinatesToWGS84(ordinatesArrayBottomRight);


			if (pacSettings.getCurveAltitudeMode() == AltitudeMode.CLAMP_TO_GROUND) {
				// tiny extrude above the ground
				zOrdinate = 0.1;
			}
			// draw an X
			lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
					+ reducePrecisionForZ(zOrdinate));

			lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
					+ reducePrecisionForZ(zOrdinate));

			lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArray[0]) + ","
					+ reducePrecisionForXorY(ordinatesArray[1]) + ","
					+ reducePrecisionForZ(zOrdinate));

			lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
					+ reducePrecisionForZ(zOrdinate));

			lineString.getCoordinates().add(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
					+ reducePrecisionForZ(zOrdinate));

			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
				case CLAMP_TO_GROUND: // to make point geometry over curve geometry
					lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
			}
			return kmlFactory.createLineString(lineString);
		}
		else if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
			pointString.getCoordinates().add(reducePrecisionForXorY(ordinatesArray[0]) + ","
					+ reducePrecisionForXorY(ordinatesArray[1]) + ","
					+ reducePrecisionForZ(zOrdinate));

			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					pointString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
					pointString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
				case CLAMP_TO_GROUND:
					pointString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));
					break;
			}
			return kmlFactory.createPoint(pointString);
		}
		else if (pacSettings.getPointDisplayMode() == PointDisplayMode.CUBE) {

			double sideLength = pacSettings.getPointCubeLengthOfSide();
			MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();
			double[] ordinatesArrayTopLeft = new double[2];
			ordinatesArrayTopLeft[0] = pointOrCurveGeometry.getCoordinates(0)[0] - sideLength/2;
			ordinatesArrayTopLeft[1] = pointOrCurveGeometry.getCoordinates(0)[1] + sideLength/2;
			ordinatesArrayTopLeft = super.convertPointCoordinatesToWGS84(ordinatesArrayTopLeft);

			double[] ordinatesArrayBottomRight = new double[2];
			ordinatesArrayBottomRight[0] = pointOrCurveGeometry.getCoordinates(0)[0] + sideLength/2;
			ordinatesArrayBottomRight[1] = pointOrCurveGeometry.getCoordinates(0)[1] - sideLength/2;
			ordinatesArrayBottomRight = super.convertPointCoordinatesToWGS84(ordinatesArrayBottomRight);

			if (pacSettings.getPointAltitudeMode() == AltitudeMode.CLAMP_TO_GROUND) {
				zOrdinate = 0.0;
			}

			String topLeftFootNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
					+ reducePrecisionForZ(zOrdinate);
			String bottomLeftFootNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
					+ reducePrecisionForZ(zOrdinate);
			String bottomRightFootNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
					+ reducePrecisionForZ(zOrdinate);
			String topRightFootNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
					+ reducePrecisionForZ(zOrdinate);
			String topLeftRoofNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
					+ reducePrecisionForZ(zOrdinate + sideLength);
			String bottomLeftRoofNode = reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
					+ reducePrecisionForZ(zOrdinate + sideLength);
			String bottomRightRoofNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
					+ reducePrecisionForZ(zOrdinate + sideLength);
			String topRightRoofNode = reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + ","
					+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
					+ reducePrecisionForZ(zOrdinate + sideLength);

			LinearRingType LinearRingElement = kmlFactory.createLinearRingType();

			// bottom side
			LinearRingElement.getCoordinates().add(topLeftFootNode);
			LinearRingElement.getCoordinates().add(bottomLeftFootNode);
			LinearRingElement.getCoordinates().add(bottomRightFootNode);
			LinearRingElement.getCoordinates().add(topRightFootNode);
			LinearRingElement.getCoordinates().add(topLeftFootNode);
			BoundaryType boundary = kmlFactory.createBoundaryType();
			boundary.setLinearRing(LinearRingElement);
			PolygonType polygon = kmlFactory.createPolygonType();
			polygon.setOuterBoundaryIs(boundary);
			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
				case CLAMP_TO_GROUND:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

			// top side
			LinearRingElement = kmlFactory.createLinearRingType();
			LinearRingElement.getCoordinates().add(topLeftRoofNode);
			LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
			LinearRingElement.getCoordinates().add(bottomRightRoofNode);
			LinearRingElement.getCoordinates().add(topRightRoofNode);
			LinearRingElement.getCoordinates().add(topLeftRoofNode);
			boundary = kmlFactory.createBoundaryType();
			boundary.setLinearRing(LinearRingElement);
			polygon = kmlFactory.createPolygonType();
			polygon.setOuterBoundaryIs(boundary);
			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
				case CLAMP_TO_GROUND:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

			// left side
			LinearRingElement = kmlFactory.createLinearRingType();
			LinearRingElement.getCoordinates().add(topLeftRoofNode);
			LinearRingElement.getCoordinates().add(topLeftFootNode);
			LinearRingElement.getCoordinates().add(bottomLeftFootNode);
			LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
			LinearRingElement.getCoordinates().add(topLeftRoofNode);
			boundary = kmlFactory.createBoundaryType();
			boundary.setLinearRing(LinearRingElement);
			polygon = kmlFactory.createPolygonType();
			polygon.setOuterBoundaryIs(boundary);
			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
				case CLAMP_TO_GROUND:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

			// right side
			LinearRingElement = kmlFactory.createLinearRingType();
			LinearRingElement.getCoordinates().add(topRightRoofNode);
			LinearRingElement.getCoordinates().add(bottomRightRoofNode);
			LinearRingElement.getCoordinates().add(bottomRightFootNode);
			LinearRingElement.getCoordinates().add(topRightFootNode);
			LinearRingElement.getCoordinates().add(topRightRoofNode);
			boundary = kmlFactory.createBoundaryType();
			boundary.setLinearRing(LinearRingElement);
			polygon = kmlFactory.createPolygonType();
			polygon.setOuterBoundaryIs(boundary);
			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
				case CLAMP_TO_GROUND:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

			// front side
			LinearRingElement = kmlFactory.createLinearRingType();
			LinearRingElement.getCoordinates().add(topLeftRoofNode);
			LinearRingElement.getCoordinates().add(topRightRoofNode);
			LinearRingElement.getCoordinates().add(topRightFootNode);
			LinearRingElement.getCoordinates().add(topLeftFootNode);
			LinearRingElement.getCoordinates().add(topLeftRoofNode);
			boundary = kmlFactory.createBoundaryType();
			boundary.setLinearRing(LinearRingElement);
			polygon = kmlFactory.createPolygonType();
			polygon.setOuterBoundaryIs(boundary);
			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
				case CLAMP_TO_GROUND:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

			// back side
			LinearRingElement = kmlFactory.createLinearRingType();
			LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
			LinearRingElement.getCoordinates().add(bottomLeftFootNode);
			LinearRingElement.getCoordinates().add(bottomRightFootNode);
			LinearRingElement.getCoordinates().add(bottomRightRoofNode);
			LinearRingElement.getCoordinates().add(bottomLeftRoofNode);
			boundary = kmlFactory.createBoundaryType();
			boundary.setLinearRing(LinearRingElement);
			polygon = kmlFactory.createPolygonType();
			polygon.setOuterBoundaryIs(boundary);
			switch (pacSettings.getPointAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
				case CLAMP_TO_GROUND:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

			return kmlFactory.createMultiGeometry(multiGeometry);
		}

		return null;
	}

}
