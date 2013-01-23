/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.kml.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import net.opengis.kml._2.PlacemarkType;
import oracle.jdbc.OracleResultSet;

import org.citygml4j.factory.CityGMLFactory;

import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.kmlExporter.Balloon;
import de.tub.citydb.config.project.kmlExporter.ColladaOptions;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;

public class Relief extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Relief";
	private static final int FIRST_RELIEF_QUERY = Queries.RELIEF_TIN_QUERY;
	private static final int LAST_RELIEF_QUERY = Queries.RELIEF_TIN_QUERY;
	private int currentReliefQuery = FIRST_RELIEF_QUERY;
	private boolean alreadyCounted = false;

	public Relief(Connection connection,
			KmlExporterManager kmlExporterManager,
			CityGMLFactory cityGMLFactory,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandlerImpl balloonTemplateHandler,
			EventDispatcher eventDispatcher,
			Config config) {

		super(connection,
			  kmlExporterManager,
			  cityGMLFactory,
			  kmlFactory,
			  elevationServiceHandler,
			  balloonTemplateHandler,
			  eventDispatcher,
			  config);
	}

	protected List<DisplayForm> getDisplayForms() {
		return config.getProject().getKmlExporter().getReliefDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getReliefColladaOptions();
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getReliefBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {
		return Queries.getReliefHighlightingQuery(currentLod, currentReliefQuery);
	}

	public void read(KmlSplittingResult work) {
		alreadyCounted = false;
		boolean resultNotEmpty = false;
		for (currentReliefQuery = FIRST_RELIEF_QUERY; currentReliefQuery <= LAST_RELIEF_QUERY; currentReliefQuery++) {
			resultNotEmpty = read(work, currentReliefQuery) || resultNotEmpty;
		}

		if (!resultNotEmpty) { // result IS empty
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			String fromMessage = " from LoD" + lodToExportFrom;
			if (lodToExportFrom == 5) {
				if (work.getDisplayForm().getForm() == DisplayForm.COLLADA)
					fromMessage = ". LoD2 or higher required";
				else
					fromMessage = " from any LoD";
			}
			Logger.getInstance().info("Could not display object " + work.getGmlId() 
									+ " as " + work.getDisplayForm().getName() + fromMessage + ".");
		}
	}

	public boolean read(KmlSplittingResult work, int reliefQueryNumber) {

		PreparedStatement psQuery = null;
		OracleResultSet rs = null;
		
		boolean reversePointOrder = false;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 1: lodToExportFrom;

			while (currentLod >= minLod) {
				if(!work.getDisplayForm().isAchievableFromLoD(currentLod)) break;

				try {
					psQuery = connection.prepareStatement(Queries.getReliefQuery(currentLod, work.getDisplayForm(), reliefQueryNumber),
							   							  ResultSet.TYPE_SCROLL_INSENSITIVE,
							   							  ResultSet.CONCUR_READ_ONLY);

					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setLong(i, work.getId());
					}
				
					rs = (OracleResultSet)psQuery.executeQuery();
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
				return false;
			}
			else { // result not empty
				if (!alreadyCounted) { // in order to display it immediately
					eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));
					alreadyCounted = true;
				}

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

					PreparedStatement psQuery2 = connection.prepareStatement(Queries.GET_EXTRUDED_HEIGHT);
					for (int i = 1; i <= psQuery2.getParameterMetaData().getParameterCount(); i++) {
						psQuery2.setLong(i, work.getId());
					}
					OracleResultSet rs2 = (OracleResultSet)psQuery2.executeQuery();
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
					if (reliefQueryNumber == Queries.RELIEF_TIN_QUERY) { // all others not supported since they have no texture
						fillGenericObjectForCollada(rs);
						setGmlId(work.getGmlId());
						setId(work.getId());
						List<Point3d> anchorCandidates = setOrigins(); // setOrigins() called mainly for the side-effect
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
					}
					break;
				}
			}
		}
		catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
			return false;
		}
		catch (JAXBException jaxbEx) {
			return false;
		}
		finally {
			if (rs != null)
				try { rs.close(); } catch (SQLException e) {}
			if (psQuery != null)
				try { psQuery.close(); } catch (SQLException e) {}
		}
		return true;
	}

	public PlacemarkType createPlacemarkForColladaModel() throws SQLException {
		// undo trick for very close coordinates
		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {getOriginX()/100, getOriginY()/100, getOriginZ()/100});
		setLocationX(reducePrecisionForXorY(originInWGS84[0]));
		setLocationY(reducePrecisionForXorY(originInWGS84[1]));
		setLocationZ(reducePrecisionForZ(originInWGS84[2]));

		return super.createPlacemarkForColladaModel();
	}

}
