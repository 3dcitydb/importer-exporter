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
import java.util.List;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import org.citydb.api.database.BalloonTemplateHandler;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.log.Logger;

import net.opengis.kml._2.PlacemarkType;

public class LandUse extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "LandUse";

	public LandUse(Connection connection,
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
		return config.getProject().getKmlExporter().getLandUseDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getLandUseColladaOptions();
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getLandUseBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {
		return Queries.getLandUseHighlightingQuery(currentLod);
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
					psQuery = connection.prepareStatement(Queries.getLandUseQuery(currentLod, work.getDisplayForm()),
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
					fillGenericObjectForCollada(rs, config.getProject().getKmlExporter().getLandUseColladaOptions().isGenerateTextureAtlases());
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

}
