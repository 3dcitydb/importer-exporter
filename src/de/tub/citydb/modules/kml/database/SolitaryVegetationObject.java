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
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.xml.bind.JAXBException;

import net.opengis.kml._2.PlacemarkType;
import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.Matrix;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.kmlExporter.Balloon;
import de.tub.citydb.config.project.kmlExporter.ColladaOptions;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.util.Util;

public class SolitaryVegetationObject extends KmlGenericObject{

	public static final String STYLE_BASIS_NAME = "Vegetation";

	private long sgRootId;
	private Matrix transformation;

	private double refPointX;
	private double refPointY;
	private double refPointZ;

	public SolitaryVegetationObject(Connection connection,
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

	protected Balloon getBalloonSetings() {
		return config.getProject().getKmlExporter().getVegetationBalloon();
	}

	public String getStyleBasisName() {
		return STYLE_BASIS_NAME;
	}

	public void read(KmlSplittingResult work) {
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if(!work.getDisplayForm().isAchievableFromLoD(currentLod)) break;

				try {
					psQuery = connection.prepareStatement(Queries.getSolitaryVegetationObjectBasisData(currentLod));

					for (int i = 1; i <= psQuery.getParameterMetaData().getParameterCount(); i++) {
						psQuery.setString(i, work.getGmlId());
					}

					rs = (OracleResultSet)psQuery.executeQuery();
					if (rs.isBeforeFirst()) {
						break; // result set not empty
					}
					else {
						try { rs.close(); // release cursor on DB
						} catch (SQLException sqle) {}
						rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
						try { psQuery.close(); // release cursor on DB
					 	} catch (SQLException sqle) {}
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
				String fromMessage = lodToExportFrom == 5 ? " from any LoD": " from LoD" + lodToExportFrom;
				Logger.getInstance().info("Could not display object " + work.getGmlId() 
						+ " as " + work.getDisplayForm().getName() + fromMessage + ".");
			}
			else { // result not empty
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));
				
				// decide whether explicit or implicit geometry
				rs.next();
				sgRootId = rs.getLong(4);
				if (sgRootId == 0) {
					sgRootId = rs.getLong(1);
					double[] ordinatesArray = (JGeometry.load((STRUCT)rs.getObject(2))).getPoint();
					refPointX = ordinatesArray[0];
					refPointY = ordinatesArray[1];
					refPointZ = ordinatesArray[2];

					String transformationString = rs.getString(3);
					if (transformationString != null) {
						List<Double> m = Util.string2double(transformationString, "\\s+");
						if (m != null && m.size() >= 16) {
							transformation = new Matrix(4, 4);
							transformation.setMatrix(m.subList(0, 16));
							transformation = transformation.getMatrix(3, 4);
						}
					}
				}

				try { rs.close(); // release cursor on DB
				} catch (SQLException sqle) {}
				rs = null; // workaround for jdbc library: rs.isClosed() throws SQLException!
				try { psQuery.close(); // release cursor on DB
			 	} catch (SQLException sqle) {}

				psQuery = connection.prepareStatement(Queries.getSolitaryVegetationObjectGeometryContents(work.getDisplayForm()),
						   							  ResultSet.TYPE_SCROLL_INSENSITIVE,
						   							  ResultSet.CONCUR_READ_ONLY);
				psQuery.setLong(1, sgRootId);
				rs = (OracleResultSet)psQuery.executeQuery();
			 	
				// get the proper displayForm (for highlighting)
				int indexOfDf = config.getProject().getKmlExporter().getVegetationDisplayForms().indexOf(work.getDisplayForm());
				if (indexOfDf != -1) {
					work.setDisplayForm(config.getProject().getKmlExporter().getVegetationDisplayForms().get(indexOfDf));
				}

				switch (work.getDisplayForm().getForm()) {
				case DisplayForm.FOOTPRINT:
					kmlExporterManager.print(createPlacemarksForFootprint(rs, work),
											 work,
											 getBalloonSetings().isBalloonContentInSeparateFile());
					break;
				case DisplayForm.EXTRUDED:

					PreparedStatement psQuery2 = connection.prepareStatement(Queries.GET_EXTRUDED_HEIGHT);
					for (int i = 1; i <= psQuery2.getParameterMetaData().getParameterCount(); i++) {
						psQuery2.setString(i, work.getGmlId());
					}
					OracleResultSet rs2 = (OracleResultSet)psQuery2.executeQuery();
					rs2.next();
					double measuredHeight = rs2.getDouble("envelope_measured_height");
					try { rs2.close(); // release cursor on DB
					} catch (SQLException e) {}
					try { psQuery2.close(); // release cursor on DB
					} catch (SQLException e) {}

					kmlExporterManager.print(createPlacemarksForExtruded(rs, work, measuredHeight, false),
											 work,
											 getBalloonSetings().isBalloonContentInSeparateFile());
					break;
				case DisplayForm.GEOMETRY:
					if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter()) { // region
						if (work.getDisplayForm().isHighlightingEnabled()) {
							kmlExporterManager.print(createPlacemarksForHighlighting(work),
													 work,
													 getBalloonSetings().isBalloonContentInSeparateFile());
						}
						kmlExporterManager.print(createPlacemarksForGeometry(rs, work),
												 work,
												 getBalloonSetings().isBalloonContentInSeparateFile());
					}
					else { // reverse order for single buildings
						kmlExporterManager.print(createPlacemarksForGeometry(rs, work),
												 work,
												 getBalloonSetings().isBalloonContentInSeparateFile());
//						kmlExporterManager.print(createPlacemarkForEachSurfaceGeometry(rs, work.getGmlId(), false));
						if (work.getDisplayForm().isHighlightingEnabled()) {
//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work),
//							 						 work,
//							 						 getBalloonSetings().isBalloonContentInSeparateFile());
							kmlExporterManager.print(createPlacemarksForHighlighting(work),
													 work,
													 getBalloonSetings().isBalloonContentInSeparateFile());
						}
					}
					break;
				case DisplayForm.COLLADA:
					fillGenericObjectForCollada(rs, work.getGmlId(), transformation);
					List<Point3d> anchorCandidates = setOrigins(); // setOrigins() called mainly for the side-effect
					double zOffset = getZOffsetFromConfigOrDB(work.getGmlId());
					if (zOffset == Double.MAX_VALUE) {
						if (transformation != null) {
							anchorCandidates.clear();
							anchorCandidates.add(new Point3d(0,0,0)); // will be turned into refPointX,Y,Z by convertToWGS84
						}
						zOffset = getZOffsetFromGEService(work.getGmlId(), anchorCandidates);
					}
					setZOffset(zOffset);

					ColladaOptions colladaOptions = config.getProject().getKmlExporter().getVegetationColladaOptions();
					setIgnoreSurfaceOrientation(colladaOptions.isIgnoreSurfaceOrientation());
					try {
						double imageScaleFactor = 1;
						if (work.getDisplayForm().isHighlightingEnabled()) {
//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work),
//													 work,
//													 getBalloonSetings().isBalloonContentInSeparateFile());
							kmlExporterManager.print(createPlacemarksForHighlighting(work),
													 work,
													 getBalloonSetings().isBalloonContentInSeparateFile());
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
	
	protected JGeometry convertToWGS84(JGeometry jGeometry) throws SQLException {
		double[] originalCoords = jGeometry.getOrdinatesArray();
		if (transformation != null) {
			for (int i = 0; i < originalCoords.length; i += 3) {
				double[] vals = new double[]{originalCoords[i], originalCoords[i+1], originalCoords[i+2], 1};
				Matrix v = new Matrix(vals, 4);
		
				v = transformation.times(v);
				originalCoords[i] = v.get(0, 0) + refPointX;
				originalCoords[i+1] = v.get(1, 0) + refPointY;
				originalCoords[i+2] = v.get(2, 0) + refPointZ;
			}
		}
		return super.convertToWGS84(jGeometry);
	}

	public PlacemarkType createPlacemarkFromGenericObject(KmlGenericObject kmlGenericObject,
			  KmlSplittingResult work) throws SQLException {

		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {0, 0, 0}); // will be turned into refPointX,Y,Z by convertToWGS84
		kmlGenericObject.setLocationX(reducePrecisionForXorY(originInWGS84[0]));
		kmlGenericObject.setLocationY(reducePrecisionForXorY(originInWGS84[1]));
		kmlGenericObject.setLocationZ(reducePrecisionForZ(originInWGS84[2]));

		return super.createPlacemarkFromGenericObject(kmlGenericObject, work);
	}

    protected List<Point3d> setOrigins() {
		List<Point3d> coords = new ArrayList<Point3d>();
		if (transformation != null) {
	    	setOriginX(Double.MAX_VALUE);
	    	setOriginY(Double.MAX_VALUE);
			VertexInfo vertexInfoIterator = firstVertexInfo;
			while (vertexInfoIterator != null) {
				if (vertexInfoIterator.getX() < getOriginX() && vertexInfoIterator.getY() < getOriginY()) {
					// for implicit geometries origin must be on the left bottom
					setOriginX(vertexInfoIterator.getX());
					setOriginY(vertexInfoIterator.getY());
					setOriginZ(vertexInfoIterator.getZ());
					coords.clear();
					Point3d point3d = new Point3d(getOriginX(), getOriginY(), getOriginZ());
					coords.add(point3d);
				}
				if (vertexInfoIterator.getX() == getOriginX() && vertexInfoIterator.getY() == getOriginY()) {
					Point3d point3d = new Point3d(vertexInfoIterator.getX(), vertexInfoIterator.getY(), vertexInfoIterator.getZ());
					coords.add(point3d);
				}
				vertexInfoIterator = vertexInfoIterator.getNextVertexInfo();
			}
		}
		else {
			coords = super.setOrigins();
		}
		
		return coords;
    }
    

}
