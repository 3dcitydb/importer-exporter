/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.kmlExporter.AltitudeMode;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.BalloonContentMode;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.KmlExporter;
import org.citydb.config.project.kmlExporter.PointAndCurve;
import org.citydb.config.project.kmlExporter.PointDisplayMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.GeometryCounterEvent;
import org.citydb.log.Logger;
import org.citydb.modules.kml.util.AffineTransformer;
import org.citydb.modules.kml.util.BalloonTemplateHandler;
import org.citydb.query.Query;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.geometry.Point;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LineStringType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PointType;
import net.opengis.kml._2.PolygonType;

public class GenericCityObject extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Generic";
	public static final String POINT = "Point";
	public static final String CURVE = "Curve";

	private AffineTransformer transformer;
	private boolean isPointOrCurve;
	private boolean isPoint;

	public GenericCityObject(Connection connection,
			Query query,
			KmlExporterManager kmlExporterManager,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			AbstractDatabaseAdapter databaseAdapter,
			BlobExportAdapter textureExportAdapter,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandler balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

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
	}

	protected List<DisplayForm> getDisplayForms() {
		return config.getProject().getKmlExporter().getGenericCityObjectDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getGenericCityObjectColladaOptions();
	}

	public Balloon getBalloonSettings() {
		if (isPointOrCurve) {
			if (isPoint)
				return config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getPointBalloon();
			else
				return config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveBalloon();
		}
		// default
		return config.getProject().getKmlExporter().getGenericCityObject3DBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if (!work.getDisplayForm().isAchievableFromLoD(currentLod)) 
					break;

				try {
					String query = queries.getGenericCityObjectBasisData(currentLod);
					psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= getParameterCount(query); i++)
						psQuery.setLong(i, work.getId());

					rs = psQuery.executeQuery();
					if (rs.isBeforeFirst()) {
						rs.next();
						if (rs.getLong(4) != 0 || rs.getLong(1) != 0)
							break; // result set not empty
					}

					try { rs.close(); } catch (SQLException sqle) {} 
					try { psQuery.close(); } catch (SQLException sqle) {}
					rs = null;

					// check for point or curve
					query = queries.getGenericCityObjectPointAndCurveQuery(currentLod);
					psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= getParameterCount(query); i++)
						psQuery.setLong(i, work.getId());

					rs = psQuery.executeQuery();
					if (rs.isBeforeFirst()) {					
						isPointOrCurve = true;
						break; // result set not empty
					}

					try { rs.close(); } catch (SQLException sqle) {} 
					try { psQuery.close(); } catch (SQLException sqle) {}
					rs = null;
				} catch (Exception e) {
					Logger.getInstance().error("SQL error while querying the highest available LOD: " + e.getMessage());
					try { if (rs != null) rs.close(); } catch (SQLException sqle) {} 
					try { if (psQuery != null) psQuery.close(); } catch (SQLException sqle) {}
					try { connection.commit(); } catch (SQLException sqle) {}
					rs = null;
				}

				currentLod--;
			}

			if ((rs == null) || // result empty
					((!isPointOrCurve) && !work.getDisplayForm().isAchievableFromLoD(currentLod))) { // give up	
				String fromMessage = " from LoD" + lodToExportFrom;
				if (lodToExportFrom == 5) {
					if (work.getDisplayForm().getForm() == DisplayForm.COLLADA)
						fromMessage = ". LoD1 or higher required";
					else
						fromMessage = " from any LoD";
				}
				Logger.getInstance().info("Could not display object " + work.getGmlId() + " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else { // result not empty
				kmlExporterManager.updateFeatureTracker(work);

				if (isPointOrCurve) { // point or curve geometry

					kmlExporterManager.print(createPlacemarksForPointOrCurve(rs, work),
							work,
							getBalloonSettings().isBalloonContentInSeparateFile());
				}
				else {					
					// decide whether explicit or implicit geometry
					long sgRootId = rs.getLong(4);
					if (sgRootId == 0) {
						sgRootId = rs.getLong(1);
						if (sgRootId != 0) {
							GeometryObject point = geometryConverterAdapter.getPoint(rs.getObject(2));
							String transformationString = rs.getString(3);
							if (point != null && transformationString != null) {
								double[] ordinatesArray = point.getCoordinates(0);
								Point referencePoint = new Point(ordinatesArray[0], ordinatesArray[1], ordinatesArray[2]);						
								List<Double> m = Util.string2double(transformationString, "\\s+");
								if (m != null && m.size() >= 16)
									transformer = new AffineTransformer(new Matrix(m.subList(0, 16), 4), referencePoint, databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid());
							}
						}
					}

					try { rs.close(); } catch (SQLException sqle) {} 
					try { psQuery.close(); } catch (SQLException sqle) {}
					rs = null;

					String query = queries.getGenericCityObjectQuery(currentLod, 
							work.getDisplayForm(),
							transformer != null, 
							work.getDisplayForm().getForm() == DisplayForm.COLLADA && config.getProject().getKmlExporter().getAppearanceTheme() != KmlExporter.THEME_NONE);
					psQuery = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					psQuery.setLong(1, sgRootId);
					rs = psQuery.executeQuery();

					// get the proper displayForm (for highlighting)
					int indexOfDf = getDisplayForms().indexOf(work.getDisplayForm());
					if (indexOfDf != -1)
						work.setDisplayForm(getDisplayForms().get(indexOfDf));

					switch (work.getDisplayForm().getForm()) {
					case DisplayForm.FOOTPRINT:
						kmlExporterManager.print(createPlacemarksForFootprint(rs, work, transformer),
								work,
								getBalloonSettings().isBalloonContentInSeparateFile());
						break;
						
					case DisplayForm.EXTRUDED:
						PreparedStatement psQuery2 = null;
						ResultSet rs2 = null;

						try {
							query = queries.getExtrusionHeight();
							psQuery2 = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
							for (int i = 1; i <= getParameterCount(query); i++)
								psQuery2.setLong(i, work.getId());

							rs2 = psQuery2.executeQuery();
							rs2.next();

							double measuredHeight = rs2.getDouble("envelope_measured_height");
							kmlExporterManager.print(createPlacemarksForExtruded(rs, work, measuredHeight, false, transformer),
									work, getBalloonSettings().isBalloonContentInSeparateFile());
							break;
						} finally {
							try { if (rs2 != null) rs2.close(); } catch (SQLException e) {}
							try { if (psQuery2 != null) psQuery2.close(); } catch (SQLException e) {}
						}
						
					case DisplayForm.GEOMETRY:
						setGmlId(work.getGmlId());
						setId(work.getId());
						if (this.query.isSetTiling()) { // region
							if (work.getDisplayForm().isHighlightingEnabled())
								kmlExporterManager.print(createPlacemarksForHighlighting(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());

							kmlExporterManager.print(createPlacemarksForGeometry(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
						} else { // reverse order for single objects
							kmlExporterManager.print(createPlacemarksForGeometry(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
							if (work.getDisplayForm().isHighlightingEnabled())
								kmlExporterManager.print(createPlacemarksForHighlighting(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
						}
						break;
						
					case DisplayForm.COLLADA:
					String currentgmlId = getGmlId();
					setGmlId(work.getGmlId());
					setId(work.getId());
					fillGenericObjectForCollada(rs, config.getProject().getKmlExporter().getGenericCityObjectColladaOptions().isGenerateTextureAtlases(), transformer);

					if (currentgmlId != work.getGmlId() && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING)
						Logger.getInstance().info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");

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
							kmlExporterManager.print(createPlacemarksForHighlighting(rs, work, transformer), work, getBalloonSettings().isBalloonContentInSeparateFile());
					} catch (Exception ioe) {
						Util.logStackTrace(ioe);
					}

					break;
					}
				}				
			}
		} catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
			return;
		} catch (JAXBException jaxbEx) {
			Logger.getInstance().error("XML error while working on city object " + work.getGmlId() + ": " + jaxbEx.getMessage());
			return;
		} finally {
			if (rs != null)
				try { rs.close(); } catch (SQLException e) {}
			if (psQuery != null)
				try { psQuery.close(); } catch (SQLException e) {}
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
		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();
		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();

		double zOffset = getZOffsetFromConfigOrDB(work.getId());
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
		rs.beforeFirst(); // return cursor to beginning
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);
		}
		while (rs.next()) {

			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			LineStringType lineString = kmlFactory.createLineStringType();
			PointType pointString = kmlFactory.createPointType();

			Object buildingGeometryObj = rs.getObject(1); 

			GeometryObject pointOrCurveGeometry = geometryConverterAdapter.getGeometry(buildingGeometryObj);			

			eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

			if (pointOrCurveGeometry.getGeometryType() == GeometryType.POINT) { // point
				isPoint = true; // dirty hack, don't try this at home
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
					lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
							+ reducePrecisionForZ(zOrdinate)));

					lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
							+ reducePrecisionForZ(zOrdinate)));

					lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArray[1]) + ","
							+ reducePrecisionForZ(zOrdinate)));

					lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
							+ reducePrecisionForZ(zOrdinate)));

					lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
							+ reducePrecisionForZ(zOrdinate)));

					if (pacSettings.isPointHighlightingEnabled())
						placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.POINT + "Style");
					else
						placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.POINT + "Normal");

					switch (pacSettings.getPointAltitudeMode()) {
					case ABSOLUTE:
						lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
						break;
					case RELATIVE:
					case CLAMP_TO_GROUND: // to make point geometry over curve geometry
						lineString.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
						break;
					}
					placemark.setAbstractGeometryGroup(kmlFactory.createLineString(lineString));
				}
				else if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
					pointString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArray[1]) + ","
							+ reducePrecisionForZ(zOrdinate)));
					placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.POINT + "Normal");
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
					placemark.setAbstractGeometryGroup(kmlFactory.createPoint(pointString));
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

					String topLeftFootNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
							+ reducePrecisionForZ(zOrdinate));
					String bottomLeftFootNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
							+ reducePrecisionForZ(zOrdinate));
					String bottomRightFootNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
							+ reducePrecisionForZ(zOrdinate));
					String topRightFootNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
							+ reducePrecisionForZ(zOrdinate));
					String topLeftRoofNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
							+ reducePrecisionForZ(zOrdinate + sideLength));
					String bottomLeftRoofNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayTopLeft[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
							+ reducePrecisionForZ(zOrdinate + sideLength));
					String bottomRightRoofNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayBottomRight[1]) + ","
							+ reducePrecisionForZ(zOrdinate + sideLength)); 
					String topRightRoofNode = String.valueOf(reducePrecisionForXorY(ordinatesArrayBottomRight[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArrayTopLeft[1]) + ","
							+ reducePrecisionForZ(zOrdinate + sideLength));				

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

					// setting Style references
					if (pacSettings.isPointCubeHighlightingEnabled())
						placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.POINT + "Style");
					else
						placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.POINT + "Normal");


					placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));					
				}

				placemark.setName(work.getGmlId());
				// replace default BalloonTemplateHandler with a brand new one, this costs resources!
				if (pacSettings.getPointBalloon() != null && pacSettings.getPointBalloon().isIncludeDescription() &&
						pacSettings.getPointBalloon().getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
					String balloonTemplateFilename = pacSettings.getPointBalloon().getBalloonContentTemplateFile();
					if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
						setBalloonTemplateHandler(new BalloonTemplateHandler(new File(balloonTemplateFilename), databaseAdapter));
					}
					addBalloonContents(placemark, work.getId());
				}

			}
			else { // curve
				pointOrCurveGeometry = convertToWGS84(pointOrCurveGeometry);
				double[] ordinatesArray = pointOrCurveGeometry.getCoordinates(0);
				for (int j = 0; j < ordinatesArray.length; j = j+3){
					lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + "," 
							+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
							+ reducePrecisionForZ(ordinatesArray[j+2] + zOffset)));
				}

				placemark.setName(work.getGmlId());

				// replace default BalloonTemplateHandler with a brand new one, this costs resources!
				if (pacSettings.isCurveHighlightingEnabled())
					placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.CURVE + "Style");
				else
					placemark.setStyleUrl("#" + getStyleBasisName() + GenericCityObject.CURVE + "Normal");

				if (pacSettings.getCurveBalloon() != null && pacSettings.getCurveBalloon().isIncludeDescription() &&
						pacSettings.getCurveBalloon().getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
					String balloonTemplateFilename = pacSettings.getCurveBalloon().getBalloonContentTemplateFile();
					if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
						setBalloonTemplateHandler(new BalloonTemplateHandler(new File(balloonTemplateFilename), databaseAdapter));
					}
					// this is the reason for the isPoint dirty hack
					addBalloonContents(placemark, work.getId());
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
				placemark.setAbstractGeometryGroup(kmlFactory.createLineString(lineString));
			}

			placemark.setId(/* DisplayForm.GEOMETRY_PLACEMARK_ID + */ placemark.getName());
			placemarkList.add(placemark);
		}

		return placemarkList;		
	}	

}
