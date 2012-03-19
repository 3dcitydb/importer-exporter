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
package de.tub.citydb.modules.kml.concurrent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import net.opengis.kml._2.AltitudeModeEnumType;
import net.opengis.kml._2.BoundaryType;
import net.opengis.kml._2.LinearRingType;
import net.opengis.kml._2.LinkType;
import net.opengis.kml._2.LocationType;
import net.opengis.kml._2.ModelType;
import net.opengis.kml._2.MultiGeometryType;
import net.opengis.kml._2.ObjectFactory;
import net.opengis.kml._2.OrientationType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolygonType;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.util.xml.SAXEventBuffer;

import com.sun.j3d.utils.geometry.GeometryInfo;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.kmlExporter.BalloonContentMode;
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.GeometryCounterEvent;
import de.tub.citydb.modules.kml.database.BalloonTemplateHandlerImpl;
import de.tub.citydb.modules.kml.database.Building;
import de.tub.citydb.modules.kml.database.ColladaBundle;
import de.tub.citydb.modules.kml.database.ElevationServiceHandler;
import de.tub.citydb.modules.kml.database.KmlExporterManager;
import de.tub.citydb.modules.kml.database.KmlSplittingResult;
import de.tub.citydb.modules.kml.database.TexCoords;
import de.tub.citydb.modules.kml.database.TileQueries;
import de.tub.citydb.util.Util;
import de.tub.citydb.util.database.DBUtil;

public class KmlExportWorker implements Worker<KmlSplittingResult> {

	private static final int POINT = 1;
	private static final int LINE_STRING = 2;
	private static final int EXTERIOR_POLYGON_RING = 1003;
	private static final int INTERIOR_POLYGON_RING = 2003;

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<KmlSplittingResult> workQueue = null;
	private KmlSplittingResult firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final DatabaseConnectionPool dbConnectionPool;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final ObjectFactory kmlFactory; 
	private final CityGMLFactory cityGMLFactory; 
	private final ConcurrentLinkedQueue<ColladaBundle> buildingQueue;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private Connection connection;
	private KmlExporterManager kmlExporterManager;
	private int currentLod;
	private Building buildingGroup = null;
	private int buildingGroupCounter = 0;
	private int buildingGroupSize = 1;

	private BalloonTemplateHandlerImpl balloonTemplateHandler = null;

	private ElevationServiceHandler elevationServiceHandler;
	private SimpleDateFormat dateFormatter;
	private double hlDistance = 0.75; 
	private X3DMaterial defaultX3dMaterial;

	public KmlExportWorker(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DatabaseConnectionPool dbConnectionPool,
			WorkerPool<SAXEventBuffer> ioWriterPool,
			ObjectFactory kmlFactory,
			CityGMLFactory cityGMLFactory,
			ConcurrentLinkedQueue<ColladaBundle> buildingQueue,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.dbConnectionPool = dbConnectionPool;
		this.ioWriterPool = ioWriterPool;
		this.kmlFactory = kmlFactory;
		this.cityGMLFactory = cityGMLFactory;
		this.buildingQueue = buildingQueue;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for both connections if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(connection, 
				database.getWorkspaces().getKmlExportWorkspace());

		kmlExporterManager = new KmlExporterManager(jaxbKmlContext,
				jaxbColladaContext,
				ioWriterPool,
				kmlFactory,
				buildingQueue,
				config);

		if (config.getProject().getKmlExporter().isGroupBuildings()) {
			buildingGroupSize = config.getProject().getKmlExporter().getGroupSize();
		}

		if (config.getProject().getKmlExporter().isIncludeDescription() &&
			config.getProject().getKmlExporter().getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
			String balloonTemplateFilename = config.getProject().getKmlExporter().getBalloonContentTemplateFile();
			if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
				balloonTemplateHandler = new BalloonTemplateHandlerImpl(new File(balloonTemplateFilename), connection);
			}
		}

		elevationServiceHandler = new ElevationServiceHandler();

		dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		defaultX3dMaterial = cityGMLFactory.createX3DMaterial();
		defaultX3dMaterial.setAmbientIntensity(0.2d);
		defaultX3dMaterial.setShininess(0.2d);
		defaultX3dMaterial.setTransparency(0d);
		defaultX3dMaterial.setDiffuseColor(getX3dColorFromString("0.8 0.8 0.8"));
		defaultX3dMaterial.setSpecularColor(getX3dColorFromString("1.0 1.0 1.0"));
		defaultX3dMaterial.setEmissiveColor(getX3dColorFromString("0.0 0.0 0.0"));
	}

	@Override
	public Thread getThread() {
		return workerThread;
	}

	@Override
	public void interrupt() {
		shouldRun = false;
		workerThread.interrupt();
	}

	@Override
	public void interruptIfIdle() {
		final ReentrantLock runLock = this.runLock;
		shouldRun = false;

		if (runLock.tryLock()) {
			try {
				workerThread.interrupt();
			} finally {
				runLock.unlock();
			}
		}
	}

	@Override
	public void setFirstWork(KmlSplittingResult firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<KmlSplittingResult> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public void run() {
		try {
			if (firstWork != null && shouldRun) {
				doWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					KmlSplittingResult work = workQueue.take();
					doWork(work);
				}
				catch (InterruptedException ie) {
					// re-check state
				}
			}

			// last buildingGroup may not be big enough
			if (buildingGroupCounter != 0) {
				try {
					ColladaBundle colladaBundle = new ColladaBundle();
					colladaBundle.setCollada(buildingGroup.generateColladaTree());
					colladaBundle.setTexImages(buildingGroup.getTexImages());
					colladaBundle.setTexOrdImages(buildingGroup.getTexOrdImages());
					colladaBundle.setPlacemark(createPlacemarkFromBuilding(buildingGroup));
					colladaBundle.setBuildingId(buildingGroup.getId());
					kmlExporterManager.print(colladaBundle);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				buildingGroup = null;
				buildingGroupCounter = 0;
			}

		}
		finally {
			if (connection != null) {
				try {
					connection.commit(); // for all possible GE_LoDn_zOffset values
					connection.close();
				}
				catch (SQLException e) {}

				connection = null;
			}
		}
	}

	private void doWork(KmlSplittingResult work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		PreparedStatement psQuery = null;
		OracleResultSet rs = null;

		boolean reversePointOrder = false;

		try {
			int lodToExportFrom = config.getProject().getKmlExporter().getLodToExportFrom();
			currentLod = lodToExportFrom == 5 ? 4: lodToExportFrom;
			int minLod = lodToExportFrom == 5 ? 0: lodToExportFrom;

			while (currentLod >= minLod) {
				if(!work.getDisplayLevel().isAchievableFromLoD(currentLod)) break;

				try {
					psQuery = connection.prepareStatement(
							TileQueries.getSingleBuildingQuery(currentLod, work.getDisplayLevel()),
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
				if (rs == null && (work.getDisplayLevel().getLevel() == DisplayLevel.FOOTPRINT || 
						   			work.getDisplayLevel().getLevel() == DisplayLevel.EXTRUDED)) {

					reversePointOrder = true;

					int groupBasis = 4;
					try {
						psQuery = connection.prepareStatement(TileQueries.
								  	QUERY_GET_AGGREGATE_GEOMETRIES_FOR_LOD.replace("<TOLERANCE>", "0.001")
								  										  .replace("<2D_SRID>", String.valueOf(DBUtil.get2DSrid(dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem())))
								  										  .replace("<LoD>", String.valueOf(currentLod))
																		  .replace("<GROUP_BY_1>", String.valueOf(Math.pow(groupBasis, 4)))
																		  .replace("<GROUP_BY_2>", String.valueOf(Math.pow(groupBasis, 3)))
																		  .replace("<GROUP_BY_3>", String.valueOf(Math.pow(groupBasis, 2))),
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
						+ " as " + work.getDisplayLevel().getName() + fromMessage + ".");
			}
			else { // result not empty
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));

				switch (work.getDisplayLevel().getLevel()) {
				case DisplayLevel.FOOTPRINT:
					kmlExporterManager.print(createPlacemarksForFootprint(rs, work.getGmlId()));
					break;
				case DisplayLevel.EXTRUDED:

					PreparedStatement psQuery2 = connection.prepareStatement(TileQueries.QUERY_EXTRUDED_HEIGHTS);
					for (int i = 1; i <= psQuery2.getParameterMetaData().getParameterCount(); i++) {
						psQuery2.setString(i, work.getGmlId());
					}
					OracleResultSet rs2 = (OracleResultSet)psQuery2.executeQuery();
					rs2.next();
					double measuredHeight = rs2.getDouble("envelope_measured_height");
					try { rs2.close(); /* release cursor on DB */ } catch (SQLException e) {}
					try { psQuery2.close(); /* release cursor on DB */ } catch (SQLException e) {}
					
					kmlExporterManager.print(createPlacemarksForExtruded(rs, work.getGmlId(), measuredHeight, reversePointOrder));
					break;
				case DisplayLevel.GEOMETRY:
					if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter()) { // region
						if (config.getProject().getKmlExporter().isGeometryHighlighting()) {
							kmlExporterManager.print(createPlacemarksForHighlighting(work.getGmlId(),
									work.getDisplayLevel()));
						}
						kmlExporterManager.print(createPlacemarksForGeometry(rs, work.getGmlId()));
					}
					else { // reverse order for single buildings
						kmlExporterManager.print(createPlacemarksForGeometry(rs, work.getGmlId()));
//							kmlExporterManager.print(createPlacemarkForEachSurfaceGeometry(rs, work.getGmlId(), false));
						if (config.getProject().getKmlExporter().isGeometryHighlighting()) {
//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work.getGmlId(),
//																								work.getDisplayLevel()));
							kmlExporterManager.print(createPlacemarksForHighlighting(work.getGmlId(),
									work.getDisplayLevel()));
						}
					}
					break;
				case DisplayLevel.COLLADA:
					Building currentBuilding = createBuildingForCollada(rs, work.getGmlId());
					if (currentBuilding == null) return;
					currentBuilding.setIgnoreSurfaceOrientation(config.getProject().getKmlExporter().isIgnoreSurfaceOrientation());
					try {
						double imageScaleFactor = 1;
						if (config.getProject().getKmlExporter().isColladaHighlighting()) {
//							kmlExporterManager.print(createPlacemarkForEachHighlingtingGeometry(work.getGmlId(),
//									   															work.getDisplayLevel()));
							kmlExporterManager.print(createPlacemarksForHighlighting(work.getGmlId(),
																					 work.getDisplayLevel()));
						}
						if (config.getProject().getKmlExporter().isGenerateTextureAtlases()) {
//							eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("kmlExport.dialog.creatingAtlases")));
							if (config.getProject().getKmlExporter().isScaleImages()) {
								imageScaleFactor = config.getProject().getKmlExporter().getImageScaleFactor();
							}
							currentBuilding.createTextureAtlas(config.getProject().getKmlExporter().getPackingAlgorithm(),
															   imageScaleFactor,
															   config.getProject().getKmlExporter().isTextureAtlasPots());
						}
						else if (config.getProject().getKmlExporter().isScaleImages()) {
							imageScaleFactor = config.getProject().getKmlExporter().getImageScaleFactor();
							if (imageScaleFactor < 1) {
								currentBuilding.resizeAllImagesByFactor(imageScaleFactor);
							}
						}
					}
					catch (IOException ioe) {
						ioe.printStackTrace();
					}

					if (buildingGroup == null) {
						buildingGroup = currentBuilding; 
					}
					else {
						buildingGroup.appendBuilding(currentBuilding);
					}

					buildingGroupCounter++;
					if (buildingGroupCounter == buildingGroupSize) {
						try {
							ColladaBundle colladaBundle = new ColladaBundle();
							colladaBundle.setCollada(buildingGroup.generateColladaTree());
							colladaBundle.setTexImages(buildingGroup.getTexImages());
							colladaBundle.setTexOrdImages(buildingGroup.getTexOrdImages());
							colladaBundle.setPlacemark(createPlacemarkFromBuilding(buildingGroup));
							colladaBundle.setBuildingId(buildingGroup.getId());
							kmlExporterManager.print(colladaBundle);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						buildingGroup = null;
						buildingGroupCounter = 0;
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

			runLock.unlock();
		}
	}

	private List<PlacemarkType> createPlacemarksForFootprint(OracleResultSet rs, String gmlId) throws SQLException {

		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(gmlId);
		placemark.setId(DisplayLevel.FOOTPRINT_PLACEMARK_ID + placemark.getName());

		if (config.getProject().getKmlExporter().isFootprintHighlighting()) {
			placemark.setStyleUrl("#" + DisplayLevel.FOOTPRINT_STR + "Style");
		}
		else {
			placemark.setStyleUrl("#" + DisplayLevel.FOOTPRINT_STR + "Normal");
		}

		if (config.getProject().getKmlExporter().isIncludeDescription()) {
			addBalloonContents(placemark, gmlId);
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
						Logger.getInstance().warn("Unknown geometry for " + gmlId);
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

	private List<PlacemarkType> createPlacemarksForExtruded(OracleResultSet rs,
															String gmlId,
															double measuredHeight,
															boolean reversePointOrder) throws SQLException {

		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(gmlId);
		placemark.setId(DisplayLevel.EXTRUDED_PLACEMARK_ID + placemark.getName());
		if (config.getProject().getKmlExporter().isFootprintHighlighting()) {
			placemark.setStyleUrl("#" + DisplayLevel.EXTRUDED_STR + "Style");
		}
		else {
			placemark.setStyleUrl("#" + DisplayLevel.EXTRUDED_STR + "Normal");
		}
		if (config.getProject().getKmlExporter().isIncludeDescription()) {
			addBalloonContents(placemark, gmlId);
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
				polygon.setExtrude(true);
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));

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
						Logger.getInstance().warn("Unknown geometry for " + gmlId);
						continue;
					}
					double[] ordinatesArray = groundSurface.getOrdinatesArray();
					int startNextGeometry = ((i+3) < groundSurface.getElemInfo().length) ? 
							groundSurface.getElemInfo()[i+3] - 1: // still more geometries
								ordinatesArray.length; // default

					if (reversePointOrder) {
						for (int j = groundSurface.getElemInfo()[i] - 1; j < startNextGeometry; j = j+dim) {
							linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," 
									+ ordinatesArray[j+1] + ","
									+ measuredHeight));
						}
					}
					else {
						// order points counter-clockwise
						for (int j = startNextGeometry - dim; j >= groundSurface.getElemInfo()[i] - 1; j = j-dim) {
							linearRing.getCoordinates().add(String.valueOf(ordinatesArray[j] + "," 
									+ ordinatesArray[j+1] + ","
									+ measuredHeight));
						}
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


	private List<PlacemarkType> createPlacemarksForGeometry(OracleResultSet rs, String gmlId) throws SQLException{
		return createPlacemarksForGeometry(rs, gmlId, false, false);
	}

	private List<PlacemarkType> createPlacemarksForGeometry(OracleResultSet rs,
			String gmlId,
			boolean includeGroundSurface,
			boolean includeClosureSurface) throws SQLException {

		HashMap<String, MultiGeometryType> multiGeometries = new HashMap<String, MultiGeometryType>();
		MultiGeometryType multiGeometry = null;
		PolygonType polygon = null;

		double zOffset = getZOffsetFromConfigOrDB(gmlId);
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
		rs.beforeFirst(); // return cursor to beginning
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(gmlId, lowestPointCandidates);
		}
		double lowestZCoordinate = convertPointCoordinatesToWGS84(new double[] {
				lowestPointCandidates.get(0).x/100, // undo trick for very close coordinates
				lowestPointCandidates.get(0).y/100,	
				lowestPointCandidates.get(0).z/100}) [2];

		while (rs.next()) {
//			Long surfaceId = rs.getLong("id");

			String surfaceType = rs.getString("type");
			if (surfaceType != null && !surfaceType.endsWith("Surface")) {
				surfaceType = surfaceType + "Surface";
			}

			if ((!includeGroundSurface && TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.GROUND_SURFACE).toString().equalsIgnoreCase(surfaceType)) ||
					(!includeClosureSurface && TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.CLOSURE_SURFACE).toString().equalsIgnoreCase(surfaceType)))	{
				continue;
			}

			STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
			JGeometry surface = convertToWGS84(JGeometry.load(buildingGeometryObj));
			double[] ordinatesArray = surface.getOrdinatesArray();

			eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));

			polygon = kmlFactory.createPolygonType();
			switch (config.getProject().getKmlExporter().getAltitudeMode()) {
			case ABSOLUTE:
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
				break;
			case RELATIVE:
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
				break;
			}

			// just in case surfaceType == null
			boolean probablyRoof = true;
			double nx = 0;
			double ny = 0;
			double nz = 0;

			for (int i = 0; i < surface.getElemInfo().length; i = i+3) {
				LinearRingType linearRing = kmlFactory.createLinearRingType();
				BoundaryType boundary = kmlFactory.createBoundaryType();
				boundary.setLinearRing(linearRing);
				if (surface.getElemInfo()[i+1] == EXTERIOR_POLYGON_RING) {
					polygon.setOuterBoundaryIs(boundary);
				}
				else { // INTERIOR_POLYGON_RING
					polygon.getInnerBoundaryIs().add(boundary);
				}

				int startNextRing = ((i+3) < surface.getElemInfo().length) ? 
						surface.getElemInfo()[i+3] - 1: // still holes to come
						ordinatesArray.length; // default

						// order points clockwise
						for (int j = surface.getElemInfo()[i] - 1; j < startNextRing; j = j+3) {
							linearRing.getCoordinates().add(String.valueOf(Building.reducePrecisionForXorY(ordinatesArray[j]) + "," 
									+ Building.reducePrecisionForXorY(ordinatesArray[j+1]) + ","
									+ Building.reducePrecisionForZ(ordinatesArray[j+2] + zOffset)));

							probablyRoof = probablyRoof && (Building.reducePrecisionForZ(ordinatesArray[j+2] - lowestZCoordinate) > 0);
							// not touching the ground

							if (currentLod == 1) { // calculate normal
								int current = j;
								int next = j+3;
								if (next >= startNextRing) next = surface.getElemInfo()[i] - 1;
								nx = nx + ((ordinatesArray[current+1] - ordinatesArray[next+1]) * (ordinatesArray[current+2] + ordinatesArray[next+2])); 
								ny = ny + ((ordinatesArray[current+2] - ordinatesArray[next+2]) * (ordinatesArray[current] + ordinatesArray[next])); 
								nz = nz + ((ordinatesArray[current] - ordinatesArray[next]) * (ordinatesArray[current+1] + ordinatesArray[next+1]));
							}
						}
			}

			if (currentLod == 1) { // calculate normal
				double value = Math.sqrt(nx * nx + ny * ny + nz * nz);
				if (value == 0) { // not a surface, but a line
					continue;
				}
				nx = nx / value;
				ny = ny / value;
				nz = nz / value;
			}

			if (surfaceType == null) {
				surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();
				switch (currentLod) {
					case 1:
						if (probablyRoof && (nz > 0.999)) {
							surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.ROOF_SURFACE).toString();
						}
						break;
					case 2:
						if (probablyRoof) {
							surfaceType = TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.ROOF_SURFACE).toString();
						}
						break;
				}
			}

			multiGeometry = multiGeometries.get(surfaceType);
			if (multiGeometry == null) {
				multiGeometry = kmlFactory.createMultiGeometryType();
				multiGeometries.put(surfaceType, multiGeometry);
			}
			multiGeometry.getAbstractGeometryGroup().add(kmlFactory.createPolygon(polygon));

		}

		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();
		Set<String> keySet = multiGeometries.keySet();
		Iterator<String> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			String surfaceType = iterator.next();
			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			placemark.setName(gmlId + "_" + surfaceType);
			placemark.setId(DisplayLevel.GEOMETRY_PLACEMARK_ID + placemark.getName());
			placemark.setStyleUrl("#" + surfaceType + "Normal");
			if (config.getProject().getKmlExporter().isIncludeDescription() &&
					!config.getProject().getKmlExporter().isGeometryHighlighting()) { // avoid double description
				addBalloonContents(placemark, gmlId);
			}
			multiGeometry = multiGeometries.get(surfaceType);
			placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));
			placemarkList.add(placemark);
		}
		return placemarkList;
	}

	private Building createBuildingForCollada(OracleResultSet rs, String gmlId) throws SQLException {

		String selectedTheme = config.getProject().getKmlExporter().getAppearanceTheme();

		Building currentBuilding = new Building();
		currentBuilding.setId(gmlId);
		int texImageCounter = 0;
		STRUCT buildingGeometryObj = null;

		while (rs.next()) {
			long surfaceRootId = rs.getLong(1);
			for (String colladaQuery: TileQueries.QUERIES_COLLADA_GET_BUILDING_DATA) { // parent surfaces come first
				PreparedStatement psQuery = null;
				OracleResultSet rs2 = null;
				try {
					psQuery = connection.prepareStatement(colladaQuery);
					psQuery.setLong(1, surfaceRootId);
					//				psQuery.setString(2, selectedTheme);
					rs2 = (OracleResultSet)psQuery.executeQuery();
	
					while (rs2.next()) {
						String theme = rs2.getString("theme");
	
						buildingGeometryObj = (STRUCT)rs2.getObject(1); 
						// surfaceId is the key to all Hashmaps in building
						long surfaceId = rs2.getLong("id");
	
						if (buildingGeometryObj == null) { // root or parent
							if (selectedTheme.equalsIgnoreCase(theme)) {
								X3DMaterial x3dMaterial = cityGMLFactory.createX3DMaterial();
								fillX3dMaterialValues(x3dMaterial, rs2);
								// x3dMaterial will only added if not all x3dMaterial members are null
								currentBuilding.addX3dMaterial(surfaceId, x3dMaterial);
							}
							continue; 
						}
	
						// from hier on it is a surfaceMember
						eventDispatcher.triggerEvent(new GeometryCounterEvent(null, this));
						long parentId = rs2.getLong("parent_id");
	
						String texImageUri = null;
						OrdImage texImage = null;
						StringTokenizer texCoordsTokenized = null;
	
						if (selectedTheme.equals(KmlExporter.THEME_NONE)) {
							currentBuilding.addX3dMaterial(surfaceId, defaultX3dMaterial);
						}
						else if	(!selectedTheme.equalsIgnoreCase(theme) && // no surface data for this surface and theme
								currentBuilding.getX3dMaterial(parentId) != null) { // material for parent surface known
							currentBuilding.addX3dMaterial(surfaceId, currentBuilding.getX3dMaterial(parentId));
						}
						else {
							texImageUri = rs2.getString("tex_image_uri");
							texImage = (OrdImage)rs2.getORAData("tex_image", OrdImage.getORADataFactory());
							String texCoords = rs2.getString("texture_coordinates");
	
							if (texImageUri != null && texImageUri.trim().length() != 0
									&&  texCoords != null && texCoords.trim().length() != 0
									&&	texImage != null) {
	
								texImageCounter++;
								if (texImageCounter > 20) {
									eventDispatcher.triggerEvent(new CounterEvent(CounterType.TEXTURE_IMAGE, texImageCounter, this));
									texImageCounter = 0;
								}
	
								int fileSeparatorIndex = Math.max(texImageUri.lastIndexOf("\\"), texImageUri.lastIndexOf("/")); 
								texImageUri = "_" + texImageUri.substring(fileSeparatorIndex + 1);
	
								currentBuilding.addTexImageUri(surfaceId, texImageUri);
								if (currentBuilding.getTexOrdImage(texImageUri) == null) { // not already marked as wrapping texture
									BufferedImage bufferedImage = null;
									try {
										bufferedImage = ImageIO.read(texImage.getDataInStream());
									}
									catch (IOException ioe) {}
									if (bufferedImage != null) { // image in JPEG, PNG or another usual format
										currentBuilding.addTexImage(texImageUri, bufferedImage);
									}
									else {
										currentBuilding.addTexOrdImage(texImageUri, texImage);
									}
								}
	
								texCoords = texCoords.replaceAll(";", " "); // substitute of ; for internal ring
								texCoordsTokenized = new StringTokenizer(texCoords, " ");
							}
							else {
								X3DMaterial x3dMaterial = cityGMLFactory.createX3DMaterial();
								fillX3dMaterialValues(x3dMaterial, rs2);
								// x3dMaterial will only added if not all x3dMaterial members are null
								currentBuilding.addX3dMaterial(surfaceId, x3dMaterial);
								if (currentBuilding.getX3dMaterial(surfaceId) == null) {
									// untextured surface and no x3dMaterial -> default x3dMaterial (gray)
									currentBuilding.addX3dMaterial(surfaceId, defaultX3dMaterial);
								}
							}
						}
	
						JGeometry surface = JGeometry.load(buildingGeometryObj);
						double[] ordinatesArray = surface.getOrdinatesArray();
	
						GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
						int contourCount = surface.getElemInfo().length/3;
						// last point of polygons in gml is identical to first and useless for GeometryInfo
						double[] giOrdinatesArray = new double[ordinatesArray.length - (contourCount*3)];
	
						int[] stripCountArray = new int[contourCount];
						int[] countourCountArray = {contourCount};
	
						for (int currentContour = 1; currentContour <= contourCount; currentContour++) {
							int startOfCurrentRing = surface.getElemInfo()[(currentContour-1)*3] - 1;
							int startOfNextRing = (currentContour == contourCount) ? 
									ordinatesArray.length: // last
										surface.getElemInfo()[currentContour*3] - 1; // still holes to come
	
							for (int j = startOfCurrentRing; j < startOfNextRing - 3; j = j+3) {
	
								giOrdinatesArray[(j-(currentContour-1)*3)] = ordinatesArray[j] * 100; // trick for very close coordinates
								giOrdinatesArray[(j-(currentContour-1)*3)+1] = ordinatesArray[j+1] * 100;
								giOrdinatesArray[(j-(currentContour-1)*3)+2] = ordinatesArray[j+2] * 100;
	
								TexCoords texCoordsForThisSurface = null;
								if (texCoordsTokenized != null) {
									double s = Double.parseDouble(texCoordsTokenized.nextToken());
									double t = Double.parseDouble(texCoordsTokenized.nextToken());
									if (s > 1.1 || s < -0.1 || t < -0.1 || t > 1.1) { // texture wrapping -- it conflicts with texture atlas
										currentBuilding.removeTexImage(texImageUri);
										currentBuilding.addTexOrdImage(texImageUri, texImage);
									}
									texCoordsForThisSurface = new TexCoords(s, t);
								}
								currentBuilding.setVertexInfoForXYZ(surfaceId,
										giOrdinatesArray[(j-(currentContour-1)*3)],
										giOrdinatesArray[(j-(currentContour-1)*3)+1],
										giOrdinatesArray[(j-(currentContour-1)*3)+2],
										texCoordsForThisSurface);
							}
							stripCountArray[currentContour-1] = (startOfNextRing -3 - startOfCurrentRing)/3;
							if (texCoordsTokenized != null) {
								texCoordsTokenized.nextToken(); // geometryInfo ignores last point in a polygon
								texCoordsTokenized.nextToken(); // keep texture coordinates in sync
							}
						}
						gi.setCoordinates(giOrdinatesArray);
						gi.setContourCounts(countourCountArray);
						gi.setStripCounts(stripCountArray);
						currentBuilding.addGeometryInfo(surfaceId, gi);
					}
				}
				catch (SQLException sqlEx) {
					Logger.getInstance().error("SQL error while querying city object: " + sqlEx.getMessage());
					return null;
				}
				finally {
					if (rs2 != null)
						try { rs2.close(); } catch (SQLException e) {}
						if (psQuery != null)
							try { psQuery.close(); } catch (SQLException e) {}
				}
			}
		}

		List<Point3d> anchorCandidates = currentBuilding.setOrigins();
		double zOffset = getZOffsetFromConfigOrDB(gmlId);
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(gmlId, anchorCandidates);
		}
		currentBuilding.setZOffset(zOffset);

		return currentBuilding;
	}

	private PlacemarkType createPlacemarkFromBuilding(Building building) throws SQLException {
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName(building.getId());
		placemark.setId(DisplayLevel.COLLADA_PLACEMARK_ID + placemark.getName());

		if (config.getProject().getKmlExporter().isIncludeDescription() &&
				!config.getProject().getKmlExporter().isColladaHighlighting() && // avoid double description
				(!config.getProject().getKmlExporter().isGroupBuildings() ||
						config.getProject().getKmlExporter().getGroupSize() == 1)) {
			addBalloonContents(placemark, building.getId());
		}

		ModelType model = kmlFactory.createModelType();
		LocationType location = kmlFactory.createLocationType();

		// undo trick for very close coordinates
		double[] originInWGS84 = convertPointCoordinatesToWGS84(new double[] {building.getOriginX()/100, building.getOriginY()/100, building.getOriginZ()/100});
		building.setLocationX(Building.reducePrecisionForXorY(originInWGS84[0]));
		building.setLocationY(Building.reducePrecisionForXorY(originInWGS84[1]));

		switch (config.getProject().getKmlExporter().getAltitudeMode()) {
		case ABSOLUTE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
			break;
		case RELATIVE:
			model.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
			break;
		}

		location.setLatitude(building.getLocationY());
		location.setLongitude(building.getLocationX());
		location.setAltitude(Building.reducePrecisionForZ(originInWGS84[2] + building.getZOffset()));
		model.setLocation(location);

		// correct heading value
		double lat1 = originInWGS84[1];
		// undo trick for very close coordinates
		double[] dummy = convertPointCoordinatesToWGS84(new double[] {building.getOriginX()/100, building.getOriginY()/100 - 20, building.getOriginZ()/100});
		double lat2 = dummy[1];
		double dLon = dummy[0] - originInWGS84[0];
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		double bearing = Math.toDegrees(Math.atan2(y, x));
		bearing = (bearing + 180) % 360;
		if (originInWGS84[0] > 0) { // East
			bearing = -bearing;
		}

		OrientationType orientation = kmlFactory.createOrientationType();
		orientation.setHeading(Building.reducePrecisionForZ(bearing));
		model.setOrientation(orientation);

		LinkType link = kmlFactory.createLinkType();
		if (config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getActive().booleanValue() &&
				config.getProject().getKmlExporter().isOneFilePerObject()) {
			link.setHref(building.getId() + ".dae");
		}
		else {
			// File.separator would be wrong here, it MUST be "/"
			link.setHref(building.getId() + "/" + building.getId() + ".dae");
		}
		model.setLink(link);

		placemark.setAbstractGeometryGroup(kmlFactory.createModel(model));
		return placemark;
	}


	private List<PlacemarkType> createPlacemarksForHighlighting(String gmlId,
			DisplayLevel displayLevel) throws SQLException {

		List<PlacemarkType> placemarkList= new ArrayList<PlacemarkType>();

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setStyleUrl("#" + displayLevel.getName() + "Style");
		placemark.setName(gmlId);
		placemark.setId(DisplayLevel.GEOMETRY_HIGHLIGHTED_PLACEMARK_ID + placemark.getName());
		placemarkList.add(placemark);

		if (config.getProject().getKmlExporter().isIncludeDescription()) {
			addBalloonContents(placemark, gmlId);
		}

		MultiGeometryType multiGeometry =  kmlFactory.createMultiGeometryType();
		placemark.setAbstractGeometryGroup(kmlFactory.createMultiGeometry(multiGeometry));

		PreparedStatement getGeometriesStmt = null;
		OracleResultSet rs = null;

		hlDistance = config.getProject().getKmlExporter().getColladaHighlightingDistance();
		if (displayLevel.getLevel() == DisplayLevel.GEOMETRY) {
			hlDistance = config.getProject().getKmlExporter().getGeometryHighlightingDistance();
		}

		try {
			getGeometriesStmt = connection.prepareStatement(TileQueries.getSingleBuildingHighlightingQuery(currentLod),
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			for (int i = 1; i <= getGeometriesStmt.getParameterMetaData().getParameterCount(); i++) {
				getGeometriesStmt.setString(i, gmlId);
			}
			rs = (OracleResultSet)getGeometriesStmt.executeQuery();

			double zOffset = getZOffsetFromConfigOrDB(gmlId);
			if (zOffset == Double.MAX_VALUE) {
				List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
				rs.beforeFirst(); // return cursor to beginning
				zOffset = getZOffsetFromGEService(gmlId, lowestPointCandidates);
			}

			while (rs.next()) {
				STRUCT unconverted = (STRUCT)rs.getObject(1);
				JGeometry unconvertedSurface = JGeometry.load(unconverted);
				double[] ordinatesArray = unconvertedSurface.getOrdinatesArray();
				if (ordinatesArray == null) {
					continue;
				}

				int contourCount = unconvertedSurface.getElemInfo().length/3;
				// remove normal-irrelevant points
				int startContour1 = unconvertedSurface.getElemInfo()[0] - 1;
				int endContour1 = (contourCount == 1) ? 
						ordinatesArray.length: // last
							unconvertedSurface.getElemInfo()[3] - 1; // holes are irrelevant for normal calculation
				// last point of polygons in gml is identical to first and useless for GeometryInfo
				endContour1 = endContour1 - 3;

				double nx = 0;
				double ny = 0;
				double nz = 0;

				for (int current = startContour1; current < endContour1; current = current+3) {
					int next = current+3;
					if (next >= endContour1) next = 0;
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

				for (int i = 0; i < ordinatesArray.length; i = i + 3) {
					// coordinates = coordinates + hlDistance * (dot product of normal vector and unity vector)
					ordinatesArray[i] = ordinatesArray[i] + hlDistance * nx;
					ordinatesArray[i+1] = ordinatesArray[i+1] + hlDistance * ny;
					ordinatesArray[i+2] = ordinatesArray[i+2] + zOffset + hlDistance * nz;
				}

				// now convert to WGS84
				JGeometry surface = convertToWGS84(unconvertedSurface);
				ordinatesArray = surface.getOrdinatesArray();

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

				for (int i = 0; i < surface.getElemInfo().length; i = i+3) {
					LinearRingType linearRing = kmlFactory.createLinearRingType();
					BoundaryType boundary = kmlFactory.createBoundaryType();
					boundary.setLinearRing(linearRing);
					if (surface.getElemInfo()[i+1] == EXTERIOR_POLYGON_RING) {
						polygon.setOuterBoundaryIs(boundary);
					}
					else { // INTERIOR_POLYGON_RING
						polygon.getInnerBoundaryIs().add(boundary);
					}

					int startNextRing = ((i+3) < surface.getElemInfo().length) ? 
							surface.getElemInfo()[i+3] - 1: // still holes to come
								ordinatesArray.length; // default

							// order points clockwise
							for (int j = surface.getElemInfo()[i] - 1; j < startNextRing; j = j+3) {
								linearRing.getCoordinates().add(String.valueOf(Building.reducePrecisionForXorY(ordinatesArray[j]) + "," 
										+ Building.reducePrecisionForXorY(ordinatesArray[j+1]) + ","
										+ Building.reducePrecisionForZ(ordinatesArray[j+2])));
							}
				}
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn("Exception when generating highlighting geometry of building " + gmlId);
			e.printStackTrace();
		}
		finally {
			if (rs != null) rs.close();
			if (getGeometriesStmt != null) getGeometriesStmt.close();
		}

		return placemarkList;
	}

	private String getBalloonContentGenericAttribute(String gmlId) {

		String balloonContent = null;
		String genericAttribName = "Balloon_Content"; 
		PreparedStatement selectQuery = null;
		OracleResultSet rs = null;

		try {
			// look for the value in the DB
			selectQuery = connection.prepareStatement(TileQueries.QUERY_GET_STRVAL_GENERICATTRIB_FROM_GML_ID);
			selectQuery.setString(1, genericAttribName);
			selectQuery.setString(2, gmlId);
			rs = (OracleResultSet)selectQuery.executeQuery();
			if (rs.next()) {
				balloonContent = rs.getString(1);
			}
		}
		catch (Exception e) {}
		finally {
			try {
				if (rs != null) rs.close();
				if (selectQuery != null) selectQuery.close();
			}
			catch (Exception e2) {}
		}
		return balloonContent;
	}

	private void addBalloonContents(PlacemarkType placemark, String gmlId) {
		try {
			switch (config.getProject().getKmlExporter().getBalloonContentMode()) {
			case GEN_ATTRIB:
				String balloonTemplate = getBalloonContentGenericAttribute(gmlId);
				if (balloonTemplate != null) {
					if (balloonTemplateHandler == null) { // just in case
						balloonTemplateHandler = new BalloonTemplateHandlerImpl((File) null, connection);
					}
					placemark.setDescription(balloonTemplateHandler.getBalloonContent(balloonTemplate, gmlId, currentLod));
				}
				break;
			case GEN_ATTRIB_AND_FILE:
				balloonTemplate = getBalloonContentGenericAttribute(gmlId);
				if (balloonTemplate != null) {
					placemark.setDescription(balloonTemplateHandler.getBalloonContent(balloonTemplate, gmlId, currentLod));
					break;
				}
			case FILE :
				if (balloonTemplateHandler != null) {
					placemark.setDescription(balloonTemplateHandler.getBalloonContent(gmlId, currentLod));
				}
				break;
			}
		}
		catch (Exception e) { } // invalid balloons are silently discarded
	}

	private void fillX3dMaterialValues (X3DMaterial x3dMaterial, OracleResultSet rs) throws SQLException {

		double ambientIntensity = rs.getDouble("x3d_ambient_intensity");
		if (!rs.wasNull()) {
			x3dMaterial.setAmbientIntensity(ambientIntensity);
		}
		double shininess = rs.getDouble("x3d_shininess");
		if (!rs.wasNull()) {
			x3dMaterial.setShininess(shininess);
		}
		double transparency = rs.getDouble("x3d_transparency");
		if (!rs.wasNull()) {
			x3dMaterial.setTransparency(transparency);
		}
		Color color = getX3dColorFromString(rs.getString("x3d_diffuse_color"));
		if (color != null) {
			x3dMaterial.setDiffuseColor(color);
		}
		color = getX3dColorFromString(rs.getString("x3d_specular_color"));
		if (color != null) {
			x3dMaterial.setSpecularColor(color);
		}
		color = getX3dColorFromString(rs.getString("x3d_emissive_color"));
		if (color != null) {
			x3dMaterial.setEmissiveColor(color);
		}
		x3dMaterial.setIsSmooth(rs.getInt("x3d_is_smooth") == 1);
	}

	private Color getX3dColorFromString(String colorString) {
		Color color = null;
		if (colorString != null) {
			List<Double> colorList = Util.string2double(colorString, "\\s+");

			if (colorList != null && colorList.size() >= 3) {
				color = cityGMLFactory.createColor(colorList.get(0), colorList.get(1), colorList.get(2));
			}
		}
		return color;
	}

	private double getZOffsetFromConfigOrDB (String gmlId) {

		double zOffset = Double.MAX_VALUE;;

		switch (config.getProject().getKmlExporter().getAltitudeOffsetMode()) {
		case NO_OFFSET:
			zOffset = 0;
			break;
		case CONSTANT:
			zOffset = config.getProject().getKmlExporter().getAltitudeOffsetValue();
			break;
		case GENERIC_ATTRIBUTE:
			PreparedStatement selectQuery = null;
			OracleResultSet rs = null;
			String genericAttribName = "GE_LoD" + currentLod + "_zOffset";
			try {
				// first look for the value in the DB
				selectQuery = connection.prepareStatement(TileQueries.QUERY_GET_STRVAL_GENERICATTRIB_FROM_GML_ID);
				selectQuery.setString(1, genericAttribName);
				selectQuery.setString(2, gmlId);
				rs = (OracleResultSet)selectQuery.executeQuery();
				if (rs.next()) {
					String strVal = rs.getString(1);
					if (strVal != null) { // use value in DB 
						StringTokenizer attributeTokenized = new StringTokenizer(strVal, "|");
						attributeTokenized.nextToken(); // skip mode
						zOffset = Double.parseDouble(attributeTokenized.nextToken());
					}
				}
			}
			catch (Exception e) {}
			finally {
				try {
					if (rs != null) rs.close();
					if (selectQuery != null) selectQuery.close();
				}
				catch (Exception e2) {}
			}
		}

		return zOffset;
	}

	private double getZOffsetFromGEService (String gmlId, List<Point3d> candidates) {

		double zOffset = 0;

		if (config.getProject().getKmlExporter().isCallGElevationService()) { // allowed to query
			PreparedStatement insertQuery = null;
			OracleResultSet rs = null;
			try {
				// convert candidate points to WGS84
				double[] coords = new double[candidates.size()*3];
				int index = 0;
				for (Point3d point3d: candidates) {
					coords[index++] = point3d.x / 100; // undo trick for very close coordinates
					coords[index++] = point3d.y / 100;
					coords[index++] = point3d.z / 100;
				}
				JGeometry jGeometry = JGeometry.createLinearLineString(coords, 3, dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem().getSrid());
				coords = convertToWGS84(jGeometry).getOrdinatesArray();

				Logger.getInstance().info("Getting zOffset from Google's elevation API for " + gmlId + " with " + candidates.size() + " points.");
				zOffset = elevationServiceHandler.getZOffset(coords);

				// save result in DB for next time
				String genericAttribName = "GE_LoD" + currentLod + "_zOffset";
				insertQuery = connection.prepareStatement(TileQueries.QUERY_INSERT_GE_ZOFFSET);
				insertQuery.setString(1, genericAttribName);
				String strVal = "Auto|" + zOffset + "|" + dateFormatter.format(new Date(System.currentTimeMillis()));
				insertQuery.setString(2, strVal);
				insertQuery.setString(3, gmlId);
				rs = (OracleResultSet)insertQuery.executeQuery();
			}
			catch (Exception e) {
				if (e.getMessage().startsWith("ORA-01427")) { // single-row subquery returns more than one row 
					Logger.getInstance().warn("gml:id value " + gmlId + " is used for more than one object in the 3DCityDB; zOffset was not stored.");
				}
			}
			finally {
				try {
					if (rs != null) rs.close();
					if (insertQuery != null) insertQuery.close();
				}
				catch (Exception e2) {}
			}
		}

		return zOffset;
	}

	private List<Point3d> getLowestPointsCoordinates(OracleResultSet rs, boolean willCallGEService) throws SQLException {
		double currentlyLowestZCoordinate = Double.MAX_VALUE;
		List<Point3d> coords = new ArrayList<Point3d>();

		rs.next();
		STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
		JGeometry surface = JGeometry.load(buildingGeometryObj);
		double[] ordinatesArray = surface.getOrdinatesArray();

		do {
			// we are only interested in the z coordinate 
			for (int j = 2; j < ordinatesArray.length; j = j+3) {
				if (ordinatesArray[j] < currentlyLowestZCoordinate) {
					coords.clear();
					Point3d point3d = new Point3d(ordinatesArray[j-2], ordinatesArray[j-1], ordinatesArray[j]);
					coords.add(point3d);
					currentlyLowestZCoordinate = point3d.z;
				}
				if (willCallGEService && ordinatesArray[j] == currentlyLowestZCoordinate) {
					Point3d point3d = new Point3d(ordinatesArray[j-2], ordinatesArray[j-1], ordinatesArray[j]);
					if (!coords.contains(point3d)) {
						coords.add(point3d);
					}
				}
			}
			if (!rs.next())	break;
			STRUCT unconverted = (STRUCT)rs.getObject(1); 
			surface = JGeometry.load(unconverted);
			ordinatesArray = surface.getOrdinatesArray();
		}
		while (true);

		for (Point3d point3d: coords) {
			point3d.x = point3d.x * 100; // trick for very close coordinates
			point3d.y = point3d.y * 100;
			point3d.z = point3d.z * 100;
		}
		return coords;
	}

	private double[] convertPointCoordinatesToWGS84(double[] coords) throws SQLException {

		double[] pointCoords = null; 
		// createLinearLineString is a workaround for Oracle11g!
		JGeometry jGeometry = JGeometry.createLinearLineString(coords, coords.length, dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem().getSrid());
		JGeometry convertedPointGeom = convertToWGS84(jGeometry);
		if (convertedPointGeom != null) {
			pointCoords = convertedPointGeom.getFirstPoint();
		}
		return pointCoords;
	}

	private JGeometry convertToWGS84(JGeometry jGeometry) throws SQLException {

		double[] originalCoords = jGeometry.getOrdinatesArray();

		JGeometry convertedPointGeom = null;
		PreparedStatement convertStmt = null;
		OracleResultSet rs2 = null;
		try {
			convertStmt = (dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem().is3D() && 
						   jGeometry.getDimensions() == 3) ?
					  	  connection.prepareStatement(TileQueries.TRANSFORM_GEOMETRY_TO_WGS84_3D):
					  	  connection.prepareStatement(TileQueries.TRANSFORM_GEOMETRY_TO_WGS84);
			// now convert to WGS84
			STRUCT unconverted = SyncJGeometry.syncStore(jGeometry, connection);
			convertStmt.setObject(1, unconverted);
			rs2 = (OracleResultSet)convertStmt.executeQuery();
			while (rs2.next()) {
				// ColumnName is SDO_CS.TRANSFORM(JGeometry, 4326)
				STRUCT converted = (STRUCT)rs2.getObject(1); 
				convertedPointGeom = JGeometry.load(converted);
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn("Exception when converting geometry to WGS84");
			e.printStackTrace();
		}
		finally {
			try {
				if (rs2 != null) rs2.close();
				if (convertStmt != null) convertStmt.close();
			}
			catch (Exception e2) {}
		}

		if (config.getProject().getKmlExporter().isUseOriginalZCoords()) {
			double[] convertedCoords = convertedPointGeom.getOrdinatesArray();
			for (int i = 2; i < originalCoords.length; i = i + 3) {
				convertedCoords[i] = originalCoords[i];
			}
		}

		return convertedPointGeom;
	}


	private List<PlacemarkType> createPlacemarkForEachSurfaceGeometry(OracleResultSet rs,
			String gmlId,
			boolean includeGroundSurface) throws SQLException {

		PlacemarkType placemark = null; 
		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();

		double zOffset = getZOffsetFromConfigOrDB(gmlId);
		List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
		rs.beforeFirst(); // return cursor to beginning
		if (zOffset == Double.MAX_VALUE) {
			zOffset = getZOffsetFromGEService(gmlId, lowestPointCandidates);
		}
		double lowestZCoordinate = convertPointCoordinatesToWGS84(new double[] {
				lowestPointCandidates.get(0).x/100, // undo trick for very close coordinates
				lowestPointCandidates.get(0).y/100,	
				lowestPointCandidates.get(0).z/100}) [2];

		while (rs.next()) {
			String surfaceType = rs.getString("type");
			if (surfaceType != null && !surfaceType.endsWith("Surface")) {
				surfaceType = surfaceType + "Surface";
			}

			STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
			long surfaceId = rs.getLong("id");
			// results are ordered by surface type
			if (!includeGroundSurface && TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.GROUND_SURFACE).toString().equalsIgnoreCase(surfaceType)) {
				continue;
			}

			JGeometry originalSurface = JGeometry.load(buildingGeometryObj);
			double[] originalOrdinatesArray = originalSurface.getOrdinatesArray();
			if (originalOrdinatesArray == null) {
				continue;
			}

			// convert original surface to WGS84
			JGeometry originalSurfaceWGS84 = convertToWGS84(originalSurface);
			double[] originalOrdinatesArrayWGS84 = originalSurfaceWGS84.getOrdinatesArray();

			// create Placemark for every Polygon
			placemark = kmlFactory.createPlacemarkType();
			placemark.setName(gmlId + "_" + String.valueOf(surfaceId));
			placemark.setId(DisplayLevel.GEOMETRY_PLACEMARK_ID + placemark.getName());
			placemark.setStyleUrl("#" + surfaceType + "Normal");

			//			if (config.getProject().getKmlExporter().isIncludeDescription() &&
			//					!config.getProject().getKmlExporter().isGeometryHighlighting()) { // avoid double description
			//				addBalloonContents(placemark, gmlId);
			//			}

			placemarkList.add(placemark);

			PolygonType polygon = kmlFactory.createPolygonType();
			switch (config.getProject().getKmlExporter().getAltitudeMode()) {
			case ABSOLUTE:
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
				break;
			case RELATIVE:
				polygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
				break;
			}
			placemark.setAbstractGeometryGroup(kmlFactory.createPolygon(polygon));

			boolean probablyRoof = true;

			for (int i = 0; i < originalSurfaceWGS84.getElemInfo().length; i = i+3) {
				LinearRingType linearRing = kmlFactory.createLinearRingType();
				BoundaryType boundary = kmlFactory.createBoundaryType();
				boundary.setLinearRing(linearRing);
				if (originalSurfaceWGS84.getElemInfo()[i+1] == EXTERIOR_POLYGON_RING) {
					polygon.setOuterBoundaryIs(boundary);
				}
				else { // INTERIOR_POLYGON_RING
					polygon.getInnerBoundaryIs().add(boundary);
				}

				int startNextRing = ((i+3) < originalSurfaceWGS84.getElemInfo().length) ? 
						originalSurfaceWGS84.getElemInfo()[i+3] - 1: // still holes to come
							originalOrdinatesArrayWGS84.length; // default

						// order points clockwise
						for (int j = originalSurfaceWGS84.getElemInfo()[i] - 1; j < startNextRing; j = j+3) {
							linearRing.getCoordinates().add(String.valueOf(Building.reducePrecisionForXorY(originalOrdinatesArrayWGS84[j]) + "," 
									+ Building.reducePrecisionForXorY(originalOrdinatesArrayWGS84[j+1]) + ","
									+ Building.reducePrecisionForZ(originalOrdinatesArrayWGS84[j+2] + zOffset)));

							probablyRoof = probablyRoof && (Building.reducePrecisionForZ(originalOrdinatesArrayWGS84[j+2] - lowestZCoordinate) > 0);
							// not touching the ground
						}

						if (surfaceType == null) {
							String likelySurfaceType = (probablyRoof && currentLod < 3) ?
									TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.ROOF_SURFACE).toString().toString() :
										TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString();
									placemark.setStyleUrl("#" + likelySurfaceType + "Normal");
						}

			}

		}

		return placemarkList;
	}


	private List<PlacemarkType> createPlacemarkForEachHighlingtingGeometry(String gmlId,
			DisplayLevel displayLevel) throws SQLException {

		PlacemarkType highlightingPlacemark = null; 
		List<PlacemarkType> placemarkList = new ArrayList<PlacemarkType>();

		PreparedStatement getGeometriesStmt = null;
		OracleResultSet rs = null;

		hlDistance = config.getProject().getKmlExporter().getColladaHighlightingDistance();
		if (displayLevel.getLevel() == DisplayLevel.GEOMETRY) {
			hlDistance = config.getProject().getKmlExporter().getGeometryHighlightingDistance();
		}

		try {
			getGeometriesStmt = connection.prepareStatement(TileQueries.getSingleBuildingHighlightingQuery(currentLod),
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			for (int i = 1; i <= getGeometriesStmt.getParameterMetaData().getParameterCount(); i++) {
				getGeometriesStmt.setString(i, gmlId);
			}
			rs = (OracleResultSet)getGeometriesStmt.executeQuery();

			double zOffset = getZOffsetFromConfigOrDB(gmlId);
			List<Point3d> lowestPointCandidates = getLowestPointsCoordinates(rs, (zOffset == Double.MAX_VALUE));
			rs.beforeFirst(); // return cursor to beginning
			if (zOffset == Double.MAX_VALUE) {
				zOffset = getZOffsetFromGEService(gmlId, lowestPointCandidates);
			}

			while (rs.next()) {
				//				String surfaceType = rs.getString("type");
				//				if (!surfaceType.endsWith("Surface")) {
				//					surfaceType = surfaceType + "Surface";
				//				}
				// results are ordered by surface type
				//				if (!includeGroundSurface && CityGMLClass.GROUNDSURFACE.toString().equalsIgnoreCase(surfaceType)) {
				//					continue;
				//				}

				STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
				long surfaceId = rs.getLong("id");

				JGeometry originalSurface = JGeometry.load(buildingGeometryObj);
				double[] ordinatesArray = originalSurface.getOrdinatesArray();
				if (ordinatesArray == null) {
					continue;
				}

				int contourCount = originalSurface.getElemInfo().length/3;
				// remove normal-irrelevant points
				int startContour1 = originalSurface.getElemInfo()[0] - 1;
				int endContour1 = (contourCount == 1) ? 
						ordinatesArray.length: // last
							originalSurface.getElemInfo()[3] - 1; // holes are irrelevant for normal calculation
				// last point of polygons in gml is identical to first and useless for GeometryInfo
				endContour1 = endContour1 - 3;

				double nx = 0;
				double ny = 0;
				double nz = 0;

				for (int current = startContour1; current < endContour1; current = current+3) {
					int next = current+3;
					if (next >= endContour1) next = 0;
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

				double factor = 1.5; // 0.5 inside Global Highlighting; 1.5 outside Global Highlighting;

				for (int i = 0; i < ordinatesArray.length; i = i + 3) {
					// coordinates = coordinates + hlDistance * (dot product of normal vector and unity vector)
					ordinatesArray[i] = ordinatesArray[i] + hlDistance * factor * nx;
					ordinatesArray[i+1] = ordinatesArray[i+1] + hlDistance * factor * ny;
					ordinatesArray[i+2] = ordinatesArray[i+2] + hlDistance * factor * nz;
				}

				// now convert highlighting to WGS84
				JGeometry highlightingSurfaceWGS84 = convertToWGS84(originalSurface);
				double[] highlightingOrdinatesArrayWGS84 = highlightingSurfaceWGS84.getOrdinatesArray();

				// create highlighting Placemark for every Polygon
				highlightingPlacemark = kmlFactory.createPlacemarkType();
				highlightingPlacemark.setName(gmlId + "_" + String.valueOf(surfaceId));
				highlightingPlacemark.setId(DisplayLevel.GEOMETRY_HIGHLIGHTED_PLACEMARK_ID + highlightingPlacemark.getName());
				highlightingPlacemark.setStyleUrl("#" + displayLevel.getName() + "Style");

				//				if (config.getProject().getKmlExporter().isIncludeDescription() &&
				//						!config.getProject().getKmlExporter().isGeometryHighlighting()) { // avoid double description
				//					addBalloonContents(placemark, gmlId);
				//				}

				placemarkList.add(highlightingPlacemark);

				PolygonType highlightingPolygon = kmlFactory.createPolygonType();
				switch (config.getProject().getKmlExporter().getAltitudeMode()) {
				case ABSOLUTE:
					highlightingPolygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.ABSOLUTE));
					break;
				case RELATIVE:
					highlightingPolygon.setAltitudeModeGroup(kmlFactory.createAltitudeMode(AltitudeModeEnumType.RELATIVE_TO_GROUND));
					break;
				}
				highlightingPlacemark.setAbstractGeometryGroup(kmlFactory.createPolygon(highlightingPolygon));

				for (int i = 0; i < highlightingSurfaceWGS84.getElemInfo().length; i = i+3) {
					LinearRingType highlightingLinearRing = kmlFactory.createLinearRingType();
					BoundaryType highlightingBoundary = kmlFactory.createBoundaryType();
					highlightingBoundary.setLinearRing(highlightingLinearRing);
					if (highlightingSurfaceWGS84.getElemInfo()[i+1] == EXTERIOR_POLYGON_RING) {
						highlightingPolygon.setOuterBoundaryIs(highlightingBoundary);
					}
					else { // INTERIOR_POLYGON_RING
						highlightingPolygon.getInnerBoundaryIs().add(highlightingBoundary);
					}

					int startNextRing = ((i+3) < highlightingSurfaceWGS84.getElemInfo().length) ? 
							highlightingSurfaceWGS84.getElemInfo()[i+3] - 1: // still holes to come
								highlightingOrdinatesArrayWGS84.length; // default

							// order points clockwise
							for (int j = highlightingSurfaceWGS84.getElemInfo()[i] - 1; j < startNextRing; j = j+3) {
								highlightingLinearRing.getCoordinates().add(String.valueOf(Building.reducePrecisionForXorY(highlightingOrdinatesArrayWGS84[j]) + "," 
										+ Building.reducePrecisionForXorY(highlightingOrdinatesArrayWGS84[j+1]) + ","
										+ Building.reducePrecisionForZ(highlightingOrdinatesArrayWGS84[j+2] + zOffset)));
							}

				}
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn("Exception when generating highlighting geometry of building " + gmlId);
			e.printStackTrace();
		}
		finally {
			if (rs != null) rs.close();
			if (getGeometriesStmt != null) getGeometriesStmt.close();
		}

		return placemarkList;
	}

}
