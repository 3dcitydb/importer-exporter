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
package org.citydb.modules.kml.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolygonType;

import org.citydb.api.event.EventDispatcher;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;

public class Bridge extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Bridge"; // "Bridge"

	public Bridge(Connection connection,
			KmlExporterManager kmlExporterManager,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			AbstractDatabaseAdapter databaseAdapter,
			BlobExportAdapter textureExportAdapter,
			ElevationServiceHandler elevationServiceHandler,
			BalloonTemplateHandlerImpl balloonTemplateHandler,
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
		return config.getProject().getKmlExporter().getBridgeDisplayForms();
	}

	public ColladaOptions getColladaOptions() {
		return config.getProject().getKmlExporter().getBridgeColladaOptions();
	}

	public Balloon getBalloonSettings() {
		return config.getProject().getKmlExporter().getBridgeBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	protected String getHighlightingQuery() {

		return Queries.getBridgePartHighlightingQuery(currentLod);
	}

	public void read(KmlSplittingResult work) {
		List<PlacemarkType> placemarks = new ArrayList<PlacemarkType>();
		PreparedStatement psQuery = null;
		ResultSet rs = null;
		try {

			psQuery = connection.prepareStatement(Queries.BRIDGE_PARTS_FROM_BRIDGE);

			for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
				psQuery.setLong(i, work.getId());
			}

			rs = psQuery.executeQuery();

			while (rs.next()) {
				long bridgePartId = rs.getLong(1);
				List<PlacemarkType> placemarkBPart = readBridgePart(bridgePartId, work);
				if (placemarkBPart != null){
					placemarks.addAll(placemarkBPart);
				} 

			}
		}
		catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while getting bridge parts for bridge " + work.getGmlId() + ": " + sqlEx.getMessage());
			return;
		}
		finally {
			try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
			rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
			try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
		}

		if (placemarks.size() == 0) {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
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
		else {
			try {
				// compact list before exporting
				for (int i = 0; i < placemarks.size(); i++) {
					PlacemarkType placemark1 = placemarks.get(i);
					if (placemark1 == null) continue;
					MultiGeometryType multiGeometry1 = (MultiGeometryType) placemark1.getAbstractGeometryGroup().getValue();
					for (int j = i+1; j < placemarks.size(); j++) {
						PlacemarkType placemark2 = placemarks.get(j);
						if (placemark2 == null || !placemark1.getId().equals(placemark2.getId())) continue;
						// compact since ids are identical
						MultiGeometryType multiGeometry2 = (MultiGeometryType) placemark2.getAbstractGeometryGroup().getValue();
						multiGeometry1.getAbstractGeometryGroup().addAll(multiGeometry2.getAbstractGeometryGroup());
						placemarks.set(j, null); // polygons transfered, placemark exhausted
					}
				}

				kmlExporterManager.print(placemarks,
						work,
						getBalloonSettings().isBalloonContentInSeparateFile());

				kmlExporterManager.updateFeatureTracker(work);
			}
			catch (JAXBException jaxbEx) {}
		}
	}

	private List<PlacemarkType> readBridgePart(long bridgePartId, KmlSplittingResult work) {

		PreparedStatement psQuery = null;
		ResultSet rs = null;

		boolean reversePointOrder = false;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 1: lodToExportFrom;
			while (currentLod >= minLod) {
				if(!work.getDisplayForm().isAchievableFromLoD(currentLod)) break;
				try {

					psQuery = connection.prepareStatement(Queries.getBridgePartQuery(currentLod, work.getDisplayForm(), databaseAdapter.getDatabaseType()),
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setLong(i, bridgePartId);
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

				// when for EXTRUDED or FOOTPRINT there is no ground surface modelled, try to find it out indirectly
				if (rs == null && (work.getDisplayForm().getForm() <= DisplayForm.EXTRUDED)) {
					reversePointOrder = true;
					int groupBasis = 4;
					try {
						psQuery = connection.prepareStatement(Queries.getBridgePartAggregateGeometries(0.001,
								DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getUtil().get2DSrid(dbSrs),
								currentLod,
								Math.pow(groupBasis, 4),
								Math.pow(groupBasis, 3),
								Math.pow(groupBasis, 2),
								databaseAdapter.getDatabaseType()),
								ResultSet.TYPE_SCROLL_INSENSITIVE,
								ResultSet.CONCUR_READ_ONLY);

						for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
							psQuery.setLong(i, bridgePartId);
						}
						rs = psQuery.executeQuery();
						if (rs.isBeforeFirst()) {
							rs.next();
							if(rs.getObject(1) != null) {
								rs.beforeFirst();
								break; // result set not empty
							}
						}

						try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
						rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
						try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}

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

			if (rs != null) { // result not empty
				// get the proper displayForm (for highlighting)
				int indexOfDf = getDisplayForms().indexOf(work.getDisplayForm());
				if (indexOfDf != -1) {
					work.setDisplayForm(getDisplayForms().get(indexOfDf));
				}				
				switch (work.getDisplayForm().getForm()) {
				case DisplayForm.FOOTPRINT:
					return createPlacemarksForFootprint(rs, work);

				case DisplayForm.EXTRUDED:
					PreparedStatement psQuery2 = connection.prepareStatement(Queries.GET_EXTRUDED_HEIGHT(databaseAdapter.getDatabaseType()));
					for (int i = 1; i <= psQuery2.getParameterMetaData().getParameterCount(); i++) {
						psQuery2.setLong(i, bridgePartId);
					}
					ResultSet rs2 = psQuery2.executeQuery();
					rs2.next();

					double measuredHeight = rs2.getDouble("envelope_measured_height");
					try { rs2.close(); /* release cursor on DB */ } catch (SQLException e) {}
					try { psQuery2.close(); /* release cursor on DB */ } catch (SQLException e) {}
					return createPlacemarksForExtruded(rs, work, measuredHeight, reversePointOrder);

				case DisplayForm.GEOMETRY:
					setGmlId(work.getGmlId());
					setId(work.getId());
					if (work.getDisplayForm().isHighlightingEnabled()) {
						if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter()) { // region
							List<PlacemarkType> hlPlacemarks = createPlacemarksForHighlighting(bridgePartId, work);
							hlPlacemarks.addAll(createPlacemarksForGeometry(rs, work));
							return hlPlacemarks;
						}
						else { // reverse order for single bridges
							List<PlacemarkType> placemarks = createPlacemarksForGeometry(rs, work);
							placemarks.addAll(createPlacemarksForHighlighting(bridgePartId, work));
							return placemarks;
						}
					}
					return createPlacemarksForGeometry(rs, work);

				case DisplayForm.COLLADA:
					fillGenericObjectForCollada(rs, config.getProject().getKmlExporter().getBridgeColladaOptions().isGenerateTextureAtlases()); // fill and refill
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
							return createPlacemarksForHighlighting(bridgePartId, work);
						}
						// just COLLADA, no KML
						List<PlacemarkType> dummy = new ArrayList<PlacemarkType>();
						dummy.add(null);
						return dummy;
					}
					catch (Exception ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
		catch (SQLException sqlEx) {
			Logger.getInstance().error("SQL error while querying city object " + work.getGmlId() + ": " + sqlEx.getMessage());
			return null;
		}
		finally {
			if (rs != null)
				try { rs.close(); } catch (SQLException e) {}
			if (psQuery != null)
				try { psQuery.close(); } catch (SQLException e) {}
		}

		return null; // nothing found 
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

	// overloaded for just one line, but this is safest
	protected List<PlacemarkType> createPlacemarksForHighlighting(long bridgePartId, KmlSplittingResult work) throws SQLException {

		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setStyleUrl("#" + getStyleBasisName() + work.getDisplayForm().getName() + "Style");
		placemark.setName(work.getGmlId());
		placemark.setId(DisplayForm.GEOMETRY_HIGHLIGHTED_PLACEMARK_ID + placemark.getName());
		placemarkList.add(placemark);

		if (getBalloonSettings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getId());
		}

		MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PreparedStatement getGeometriesStmt = null;
		ResultSet rs = null;

		double hlDistance = work.getDisplayForm().getHighlightingDistance();

		try {
			getGeometriesStmt = connection.prepareStatement(getHighlightingQuery(),
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			for (int i = 1; i <= getGeometriesStmt.getParameterMetaData().getParameterCount(); i++) {
				// this is THE LINE
				getGeometriesStmt.setLong(i, bridgePartId);
			}
			rs = getGeometriesStmt.executeQuery();

			double zOffset = getZOffsetFromConfigOrDB(work.getId());
			if (zOffset == Double.MAX_VALUE) {
				List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
				rs.beforeFirst(); // return cursor to beginning
				zOffset = getZOffsetFromGEService(work.getId(), lowestPointCandidates);
			}

			while (rs.next()) {
				Object unconvertedObj = rs.getObject(1);
				GeometryObject unconvertedSurface = geometryConverterAdapter.getPolygon(unconvertedObj);
				if (unconvertedSurface == null || unconvertedSurface.getNumElements() == 0)
					return null;

				double[] ordinatesArray = unconvertedSurface.getCoordinates(0);
				double nx = 0;
				double ny = 0;
				double nz = 0;

				for (int current = 0; current < ordinatesArray.length - 3; current = current+3) {
					int next = current+3;
					if (next >= ordinatesArray.length - 3) next = 0;
					nx = nx + ((ordinatesArray[current+1] - ordinatesArray[next+1]) * (ordinatesArray[current+2] + ordinatesArray[next+2])); 
					ny = ny + ((ordinatesArray[current+2] - ordinatesArray[next+2]) * (ordinatesArray[current] + ordinatesArray[next])); 
					nz = nz + ((ordinatesArray[current] - ordinatesArray[next]) * (ordinatesArray[current+1] + ordinatesArray[next+1])); 
				}

				double value = Math.sqrt(nx * nx + ny * ny + nz * nz);
				if (value == 0) { // not a surface, but a line
					continue;
				}
				nx = nx / value;
				ny = ny / value;
				nz = nz / value;

				for (int i = 0; i < unconvertedSurface.getNumElements(); i++) {
					ordinatesArray = unconvertedSurface.getCoordinates(i);
					for (int j = 0; j < ordinatesArray.length; j = j + 3) {
						// coordinates = coordinates + hlDistance * (dot product of normal vector and unity vector)
						ordinatesArray[j] = ordinatesArray[j] + hlDistance * nx;
						ordinatesArray[j+1] = ordinatesArray[j+1] + hlDistance * ny;
						ordinatesArray[j+2] = ordinatesArray[j+2] + zOffset + hlDistance * nz;
					}
				}

				// now convert to WGS84
				GeometryObject surface = convertToWGS84(unconvertedSurface);

				PolygonType polygon = kmlFactory.createPolygonType();
				switch (config.getProject().getKmlExporter().getAltitudeMode()) {
				case ABSOLUTE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
					polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
				}
				multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

				for (int i = 0; i < surface.getNumElements(); i++) {
					LinearRingType linearRing = kmlFactory.createLinearRingType();
					BoundaryType boundary = kmlFactory.createBoundaryType();
					boundary.setLinearRing(linearRing);

					if (i == 0)
						polygon.setOuterBoundaryIs(boundary);
					else
						polygon.getInnerBoundaryIs().add(boundary);

					// order points clockwise
					ordinatesArray = surface.getCoordinates(i);
					for (int j = 0; j < ordinatesArray.length; j = j+3)
						linearRing.getCoordinates().add(String.valueOf(reducePrecisionForXorY(ordinatesArray[j]) + "," 
								+ reducePrecisionForXorY(ordinatesArray[j+1]) + ","
								+ reducePrecisionForZ(ordinatesArray[j+2])));
				}
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn("Exception when generating highlighting geometry of object " + work.getGmlId());
			e.printStackTrace();
		}
		finally {
			if (rs != null) rs.close();
			if (getGeometriesStmt != null) getGeometriesStmt.close();
		}

		return placemarkList;
	}

}
