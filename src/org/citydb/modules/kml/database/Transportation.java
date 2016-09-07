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
package org.citydb.modules.kml.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import org.citydb.api.database.BalloonTemplateHandler;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.api.geometry.GeometryType;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.common.event.GeometryCounterEvent;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.LineStringType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PointType;

public class Transportation extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Transportation";

	public Transportation(Connection connection,
			KmlExporterManager kmlExporterManager,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			AbstractDatabaseAdapter databaseAdapter,
			BlobExportAdapter textureExportAdapter,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandler balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

		super(connection,
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
		return config.getProject().getKmlExporter().getTransportationDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getTransportationColladaOptions();
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getTransportationBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {
		return Queries.getTransportationHighlightingQuery(currentLod);
	}

	public void read(KmlSplittingResult work) {

		PreparedStatement psQuery = null;
		ResultSet rs = null;

		boolean reversePointOrder = false;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if(!work.getDisplayForm().isAchievableFromLoD(currentLod)) break;

				try {
					psQuery = connection.prepareStatement(Queries.getTransportationQuery(currentLod, work.getDisplayForm()),
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);

					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setLong(i, work.getId());
					}

					rs = psQuery.executeQuery();
					if (rs.isBeforeFirst()) {
						break; // result set not empty
					}
					else {
						try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
						rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
						try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
					}
				}
				catch (Exception e2) {
					try { if (rs != null) rs.close(); } catch (SQLException sqle) {}
					rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
					try { if (psQuery != null) psQuery.close(); } catch (SQLException sqle) {}
				}

				currentLod--;
			}

			if (rs == null) { // result empty, give up
				String fromMessage = " from LoD" + lodToExportFrom;
				if (lodToExportFrom == 5) {
					if (work.getDisplayForm().getForm() == DisplayForm.COLLADA)
						fromMessage = ". LoD1 or higher required";
					else
						fromMessage = " from any LoD";
				}
				Logger.getInstance().info("Could not display object " + work.getGmlId() 
						+ " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else { // result not empty
				kmlExporterManager.updateFeatureTracker(work);

				// get the proper displayForm (for highlighting)
				int indexOfDf = getDisplayForms().indexOf(work.getDisplayForm());
				if (indexOfDf != -1) {
					work.setDisplayForm(getDisplayForms().get(indexOfDf));
				}

				if (currentLod == 0) { // LoD0_Network
					kmlExporterManager.print(createPlacemarksForLoD0Network(rs, work),
							work,
							getBalloonSettings().isBalloonContentInSeparateFile());
				}
				else {
					switch (work.getDisplayForm().getForm()) {
					case DisplayForm.FOOTPRINT:
						kmlExporterManager.print(createPlacemarksForFootprint(rs, work),
								work,
								getBalloonSettings().isBalloonContentInSeparateFile());
						break;
					case DisplayForm.EXTRUDED:

						PreparedStatement psQuery2 = connection.prepareStatement(Queries.GET_EXTRUDED_HEIGHT(databaseAdapter.getDatabaseType()));
						for (int i = 1; i <= psQuery2.getParameterMetaData().getParameterCount(); i++) {
							psQuery2.setLong(i, work.getId());
						}
						ResultSet rs2 = psQuery2.executeQuery();
						rs2.next();
						double measuredHeight = rs2.getDouble("envelope_measured_height");
						try { rs2.close(); /* release cursor on DB */ } catch (SQLException e) {}
						try { psQuery2.close(); /* release cursor on DB */ } catch (SQLException e) {}

						kmlExporterManager.print(createPlacemarksForExtruded(rs, work, measuredHeight, reversePointOrder),
								work,
								getBalloonSettings().isBalloonContentInSeparateFile());
						break;
					case DisplayForm.GEOMETRY:
						setGmlId(work.getGmlId());
						setId(work.getId());
						if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter()) { // region
							if (work.getDisplayForm().isHighlightingEnabled()) {
								kmlExporterManager.print(createPlacemarksForHighlighting(work),
										work,
										getBalloonSettings().isBalloonContentInSeparateFile());
							}
							kmlExporterManager.print(createPlacemarksForGeometry(rs, work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
						}
						else { // reverse order for single buildings
							kmlExporterManager.print(createPlacemarksForGeometry(rs, work),
									work,
									getBalloonSettings().isBalloonContentInSeparateFile());
							//							kmlExporterManager.print(createPlacemarkForEachSurfaceGeometry(rs, work.getGmlId(), false));
							if (work.getDisplayForm().isHighlightingEnabled()) {
								//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work),
								//							 						 work,
								//							 						 getBalloonSetings().isBalloonContentInSeparateFile());
								kmlExporterManager.print(createPlacemarksForHighlighting(work),
										work,
										getBalloonSettings().isBalloonContentInSeparateFile());
							}
						}
						break;
					case DisplayForm.COLLADA:
						fillGenericObjectForCollada(rs, config.getProject().getKmlExporter().getTransportationColladaOptions().isGenerateTextureAtlases());
						String currentgmlId = getGmlId();
						setGmlId(work.getGmlId());
						setId(work.getId());

						if (currentgmlId != work.getGmlId() && getGeometryAmount() > GEOMETRY_AMOUNT_WARNING) {
							Logger.getInstance().info("Object " + work.getGmlId() + " has more than " + GEOMETRY_AMOUNT_WARNING + " geometries. This may take a while to process...");
						}

						List<Point3d> anchorCandidates = getOrigins(); // setOrigins() called mainly for the side-effect
						double zOffset = getZOffsetFromConfigOrDB(work.getId());
						if (zOffset == Double.MAX_VALUE) {
							zOffset = getZOffsetFromGEService(work.getId(), anchorCandidates);
						}
						setZOffset(zOffset);

						ColladaOptions colladaOptions = getColladaOptions();
						setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
						try {
							if (work.getDisplayForm().isHighlightingEnabled()) {
								//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work),
								//													 work,
								//													 getBalloonSetings().isBalloonContentInSeparateFile());
								kmlExporterManager.print(createPlacemarksForHighlighting(work),
										work,
										getBalloonSettings().isBalloonContentInSeparateFile());
							}
						}
						catch (Exception ioe) {
							ioe.printStackTrace();
						}

						break;
					}
				}				
			}
		}
		catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
			return;
		}
		catch (JAXBException jaxbEx) {
			return;
		}
		finally {
			if (rs != null)
				try { rs.close(); } catch (SQLException e) {}
			if (psQuery != null)
				try { psQuery.close(); } catch (SQLException e) {}
		}
	}

	public PlacemarkType createPlacemarkForColladaModel() throws SQLException {
		// undo trick for very close coordinates
		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {getOrigin().x,
				getOrigin().y,
				getOrigin().z});
		setLocation(reducePrecisionForXorY(originInWGS84[0]),
				reducePrecisionForXorY(originInWGS84[1]),
				reducePrecisionForZ(originInWGS84[2]));

		return super.createPlacemarkForColladaModel();
	}

	private List<PlacemarkType> createPlacemarksForLoD0Network(ResultSet rs,
			KmlSplittingResult work) throws SQLException {

		DisplayForm footprintSettings = new DisplayForm(DisplayForm.FOOTPRINT, -1, -1);
		int indexOfDf = getDisplayForms().indexOf(footprintSettings);
		if (indexOfDf != -1) {
			footprintSettings = getDisplayForms().get(indexOfDf);
		}

		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();
		
		double zOffset = getZOffsetFromConfigOrDB(work.getId());
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
		rs.beforeFirst(); // return cursor to beginning
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);
		}

		while (rs.next()) {
			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			placemark.setName(work.getGmlId());
			placemark.setId(/* DisplayForm.FOOTPRINT_PLACEMARK_ID + */ placemark.getName());
			if (footprintSettings.isHighlightingEnabled())
				placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.FOOTPRINT_STR + "Style");
			else
				placemark.setStyleUrl("#" + getStyleBasisName() + DisplayForm.FOOTPRINT_STR + "Normal");

			Object buildingGeometryObj = rs.getObject(1); 
			if (!rs.wasNull() && buildingGeometryObj != null) {
				GeometryObject pointOrCurveGeometry = geometryConverterAdapter.getGeometry(buildingGeometryObj);
				eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

				if (pointOrCurveGeometry.getGeometryType() == GeometryType.POINT) { // point
					double[] ordinatesArray = pointOrCurveGeometry.getCoordinates(0);
					ordinatesArray = super.convertPointCoordinatesToWGS84(ordinatesArray);

					PointType point = kmlFactory.createPointType();
					point.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[0]) + "," 
							+ reducePrecisionForXorY(ordinatesArray[1]) + ","
							+ reducePrecisionForZ(ordinatesArray[2]) + zOffset));

					placemark.setAbstractGeometryGroup(kmlFactory.createPoint(point));
				}
				else { // curve
					pointOrCurveGeometry = super.convertToWGS84(pointOrCurveGeometry);
					LineStringType lineString = kmlFactory.createLineStringType();
					
					for (int i = 0; i < pointOrCurveGeometry.getNumElements(); i++) {
						double[] ordinatesArray = pointOrCurveGeometry.getCoordinates(i);
						
						// order points clockwise
						for (int j = 0; j < ordinatesArray.length; j = j+3) {
							lineString.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + "," 
									+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
									+ reducePrecisionForZ(ordinatesArray[j+2] + zOffset)));
						}
					}
					
					switch (config.getProject().getKmlExporter().getAltitudeMode()) {
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
			}
			// replace default BalloonTemplateHandler with a brand new one, this costs resources!
			if (getBalloonSettings().isIncludeDescription()) {
				addBalloonContents(placemark, work.getId());
			}
			placemarkList.add(placemark);
		}
		return placemarkList;
	}

}
