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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BalloonStyleType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LineStyleType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.PairType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolyStyleType;
import net.opengis.kml._2.PolygonType;
import net.opengis.kml._2.StyleMapType;
import net.opengis.kml._2.StyleStateEnumType;
import net.opengis.kml._2.StyleType;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.kmlExporter.Balloon;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.GeometryCounterEvent;

public class CityObjectGroup extends KmlGenericObject{

	private static final String STYLE_BASIS_NAME = "Group";

	public CityObjectGroup(Connection connection,
			KmlExporterManager kmlExporterManager,
			CityGMLFactory cityGMLFactory,
			net.opengis.kml._2.ObjectFactory kmlFactory,
			EventDispatcher eventDispatcher,
			DatabaseSrs dbSrs,
			Config config) {

		super(connection,
			  kmlExporterManager,
			  cityGMLFactory,
			  kmlFactory,
			  eventDispatcher,
			  dbSrs,
			  config);
	}

	protected PreparedStatement getQueryForObjectType (KmlSplittingResult work) throws SQLException {
		PreparedStatement psQuery = connection.prepareStatement(Queries.CITYOBJECTGROUP_FOOTPRINT,
				  												ResultSet.TYPE_SCROLL_INSENSITIVE,
				  												ResultSet.CONCUR_READ_ONLY);

		return psQuery;
	}

	protected Balloon getBalloonSetings() {
		return config.getProject().getKmlExporter().getCityObjectGroupBalloon();
	}

	public void read(KmlSplittingResult work) {

		PreparedStatement psQuery = null;
		OracleResultSet rs = null;

		try {
			psQuery = getQueryForObjectType(work);

			for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
				psQuery.setString(i, work.getGmlId());
			}
				
			rs = (OracleResultSet)psQuery.executeQuery();
			if (!rs.isBeforeFirst()) {
				try { rs.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
				rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
				try { psQuery.close(); /* release cursor on DB */ } catch (SQLException sqle) {}
			}

			if (rs == null) { // result empty, give up
				Logger.getInstance().info("Could not display CityObjectGroup " + work.getGmlId());
			}
			else { // result not empty
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));

				// get the proper displayForm (colors, highlighting) when not building
				DisplayForm displayForm = null;
				for (DisplayForm dl : config.getProject().getKmlExporter().getCityObjectGroupDisplayForms()) {
					if (dl.getForm() == DisplayForm.FOOTPRINT) {
						displayForm = dl;
						break;
					}
				}

				addGroupStyle(displayForm);
				work.setDisplayForm(displayForm);
				kmlExporterManager.print(createPlacemarksForFootprint(rs, work),
										 work,
										 getBalloonSetings().isBalloonContentInSeparateFile());
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
	
	protected List<PlacemarkType> createPlacemarksForFootprint(OracleResultSet rs, KmlSplittingResult work) throws SQLException {

		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(work.getGmlId());
		placemark.setId(DisplayForm.FOOTPRINT_PLACEMARK_ID + placemark.getName());

		if (work.getDisplayForm().isHighlightingEnabled()) {
			placemark.setStyleUrl("#" + STYLE_BASIS_NAME + "Style");
		}
		else {
			placemark.setStyleUrl("#" + STYLE_BASIS_NAME + "Normal");
		}

		if (getBalloonSetings().isIncludeDescription()) {
			addBalloonContents(placemark, work.getGmlId());
		}
		MultiGeometryType multiGeometry = kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PolygonType polygon = null; 
		while (rs.next()) {
			STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 

			if (!rs.wasNull() && buildingGeometryObj != null) {
				eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

				polygon = kmlFactory.createPolygonType();
				polygon.setTessellate(true);
				polygon.setExtrude(false);
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.CLAMP_TO_GROUND));

				JGeometry groundSurface = convertToWGS84(JGeometry.load(buildingGeometryObj));
				int dim = groundSurface.getDimensions();
				for (int i = 0; i < groundSurface.getElemInfo().length; i = i+3) {
					LinearRingType linearRing = kmlFactory.createLinearRingType();
					BoundaryType boundary = kmlFactory.createBoundaryType();
					boundary.setLinearRing(linearRing);
					switch (groundSurface.getElemInfo()[i+1]) {
					case EXTERIOR_POLYGON_RING:
						polygon.setOuterBoundaryIs(boundary);
						break;
					case INTERIOR_POLYGON_RING:
						polygon.getInnerBoundaryIs().add(boundary);
						break;
					case POINT:
					case LINE_STRING:
						continue;
					default:
						Logger.getInstance().warn("Unknown geometry for " + work.getGmlId());
						continue;
					}

					double[] ordinatesArray = groundSurface.getOrdinatesArray();
					int startNextGeometry = ((i+3) < groundSurface.getElemInfo().length) ? 
							groundSurface.getElemInfo()[i+3] - 1: // still more geometries
								ordinatesArray.length; // default
							// order points counter-clockwise
							for (int j = startNextGeometry - dim; j >= groundSurface.getElemInfo()[i] - 1; j = j-dim) {
								linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," + ordinatesArray[j+1] + ",0"));
							}
				}
				multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));
			}
		}
		if (polygon != null) { // if there is at least some content
			placemarkList.add(placemark);
		}
		return placemarkList;
	}

	private void addGroupStyle(DisplayForm displayForm) throws JAXBException {

		String fillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_COLOR);
		String lineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_COLOR);
		String hlFillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
		String hlLineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);

		if (displayForm.isSetRgba0()) {
			fillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(displayForm.getRgba0()));
		}
		if (displayForm.isSetRgba1()) {
			lineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(displayForm.getRgba1()));
		}
		if (displayForm.isSetRgba4()) {
			hlFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(displayForm.getRgba4()));
		}
		if (displayForm.isSetRgba5()) {
			hlLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(displayForm.getRgba5()));
		}

		BalloonStyleType balloonStyle = new BalloonStyleType();
		balloonStyle.setText("$[description]");

		LineStyleType lineStyleFootprintNormal = kmlFactory.createLineStyleType();
		lineStyleFootprintNormal.setColor(hexStringToByteArray(lineColor));
		lineStyleFootprintNormal.setWidth(1.5);
		PolyStyleType polyStyleFootprintNormal = kmlFactory.createPolyStyleType();
		polyStyleFootprintNormal.setColor(hexStringToByteArray(fillColor));
		StyleType styleFootprintNormal = kmlFactory.createStyleType();
		styleFootprintNormal.setId(STYLE_BASIS_NAME + "Normal");
		styleFootprintNormal.setLineStyle(lineStyleFootprintNormal);
		styleFootprintNormal.setPolyStyle(polyStyleFootprintNormal);
		styleFootprintNormal.setBalloonStyle(balloonStyle);

		kmlExporterManager.print(styleFootprintNormal);

		if (displayForm.isHighlightingEnabled()) {
			LineStyleType lineStyleFootprintHighlight = kmlFactory.createLineStyleType();
			lineStyleFootprintHighlight.setColor(hexStringToByteArray(hlLineColor));
			lineStyleFootprintHighlight.setWidth(1.5);
			PolyStyleType polyStyleFootprintHighlight = kmlFactory.createPolyStyleType();
			polyStyleFootprintHighlight.setColor(hexStringToByteArray(hlFillColor));
			StyleType styleFootprintHighlight = kmlFactory.createStyleType();
			styleFootprintHighlight.setId(STYLE_BASIS_NAME + "Highlight");
			styleFootprintHighlight.setLineStyle(lineStyleFootprintHighlight);
			styleFootprintHighlight.setPolyStyle(polyStyleFootprintHighlight);
			styleFootprintHighlight.setBalloonStyle(balloonStyle);

			PairType pairFootprintNormal = kmlFactory.createPairType();
			pairFootprintNormal.setKey(StyleStateEnumType.NORMAL);
			pairFootprintNormal.setStyleUrl("#" + styleFootprintNormal.getId());
			PairType pairFootprintHighlight = kmlFactory.createPairType();
			pairFootprintHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
			pairFootprintHighlight.setStyleUrl("#" + styleFootprintHighlight.getId());
			StyleMapType styleMapFootprint = kmlFactory.createStyleMapType();
			styleMapFootprint.setId(STYLE_BASIS_NAME + "Style");
			styleMapFootprint.getPair().add(pairFootprintNormal);
			styleMapFootprint.getPair().add(pairFootprintHighlight);

			kmlExporterManager.print(styleFootprintHighlight);
			kmlExporterManager.print(styleMapFootprint);
		}
	}

	private byte[] hexStringToByteArray(String hex) {
		// padding if needed
		if (hex.length()/2 != (hex.length()+1)/2) {
			hex = "0" + hex;
		}
			
		byte[] bytes = new byte[hex.length()/2];
		try {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			return null;
		}
		return bytes;
	}


}
