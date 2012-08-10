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
package de.tub.citydb.modules.kml.database;

import java.io.IOException;
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
import de.tub.citydb.util.database.DBUtil;

public class Building extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = ""; // "Building"

	public Building(Connection connection,
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

	protected Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getBuildingBalloon();
	}
	
	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	public void read(KmlSplittingResult work) {

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
					psQuery = connection.prepareStatement(Queries.getSingleBuildingQuery(currentLod, work.getDisplayForm()),
							   							  ResultSet.TYPE_SCROLL_INSENSITIVE,
							   							  ResultSet.CONCUR_READ_ONLY);

					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setString(i, work.getGmlId());
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

				// when for EXTRUDED or FOOTPRINT there is no ground surface modelled, try to find it out indirectly
				if (rs == null && (work.getDisplayForm().getForm() <= DisplayForm.EXTRUDED)) {

					reversePointOrder = true;

					int groupBasis = 4;
					try {
						psQuery = connection.prepareStatement(Queries.getBuildingAggregateGeometries(0.001,
																									 DBUtil.get2DSrid(dbSrs),
																									 currentLod,
																									 Math.pow(groupBasis, 4),
																									 Math.pow(groupBasis, 3),
																									 Math.pow(groupBasis, 2)),
															  ResultSet.TYPE_SCROLL_INSENSITIVE,
															  ResultSet.CONCUR_READ_ONLY);

						for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
							psQuery.setString(i, work.getGmlId());
						}
						rs = (OracleResultSet)psQuery.executeQuery();
						if (rs.isBeforeFirst()) {
							rs.next();
							if(rs.getObject(1) != null) {
								rs.beforeFirst();
								break; // result set not empty
							}
						}
//						else {
							try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
							rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
							try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
//						}
					}
					catch (Exception e2) {
						try { if (rs != null) rs.close(); } catch (SQLException sqle) {}
						rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
						try { if (psQuery != null) psQuery.close(); } catch (SQLException sqle) {}
					}
				}

				currentLod--;
				reversePointOrder = false;
			}

			if (rs == null) { // result empty, give up
				String fromMessage = lodToExportFrom == 5 ? " from any LoD": " from LoD" + lodToExportFrom;
				Logger.getInstance().info("Could not display object " + work.getGmlId() 
						+ " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else { // result not empty
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));

				switch (work.getDisplayForm().getForm()) {
				case DisplayForm.FOOTPRINT:
					kmlExporterManager.print(createPlacemarksForFootprint(rs, work),
											 work,
											 getBalloonSettings().isBalloonContentInSeparateFile());
					break;
				case DisplayForm.EXTRUDED:

					PreparedStatement psQuery2 = connection.prepareStatement(Queries.GET_EXTRUDED_HEIGHT);
					for (int i = 1; i <= psQuery2.getParameterMetaData().getParameterCount(); i++) {
						psQuery2.setString(i, work.getGmlId());
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
					fillGenericObjectForCollada(rs, work.getGmlId());
					List<Point3d> anchorCandidates = setOrigins(); // setOrigins() called mainly for the side-effect
					double zOffset = getZOffsetFromConfigOrDB(work.getGmlId());
					if (zOffset == Double.MAX_VALUE) {
						zOffset = getZOffsetFromGEService(work.getGmlId(), anchorCandidates);
					}
					setZOffset(zOffset);

					ColladaOptions colladaOptions = config.getProject().getKmlExporter().getBuildingColladaOptions();
					setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
					try {
						double imageScaleFactor = 1;
						if (work.getDisplayForm().isHighlightingEnabled()) {
//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work),
//													 work,
//													 getBalloonSetings().isBalloonContentInSeparateFile());
							kmlExporterManager.print(createPlacemarksForHighlighting(work),
													 work,
													 getBalloonSettings().isBalloonContentInSeparateFile());
						}
						if (colladaOptions.isGenerateTextureAtlases()) {
//							eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("kmlExport.dialog.creatingAtlases")));
							if (colladaOptions.isScaleImages()) {
								imageScaleFactor = colladaOptions.getImageScaleFactor();
							}
							createTextureAtlas(colladaOptions.getPackingAlgorithm(),
											   imageScaleFactor,
											   colladaOptions.isTextureAtlasPots());
						}
						else if (colladaOptions.isScaleImages()) {
							imageScaleFactor = colladaOptions.getImageScaleFactor();
							if (imageScaleFactor < 1) {
								resizeAllImagesByFactor(imageScaleFactor);
							}
						}
					}
					catch (IOException ioe) {
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

	public PlacemarkType createPlacemarkFromGenericObject(KmlGenericObject kmlGenericObject,
			  											  KmlSplittingResult work) throws SQLException {
		// undo trick for very close coordinates
		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {kmlGenericObject.getOriginX()/100, kmlGenericObject.getOriginY()/100, kmlGenericObject.getOriginZ()/100});
		kmlGenericObject.setLocationX(reducePrecisionForXorY(originInWGS84[0]));
		kmlGenericObject.setLocationY(reducePrecisionForXorY(originInWGS84[1]));
		kmlGenericObject.setLocationZ(reducePrecisionForZ(originInWGS84[2]));

		return super.createPlacemarkFromGenericObject(kmlGenericObject, work);
	}

}
