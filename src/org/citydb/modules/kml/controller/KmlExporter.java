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
package org.citydb.modules.kml.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.citydb.api.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.api.concurrent.SingleWorkerPool;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.filter.FeatureClass;
import org.citydb.config.project.filter.TiledBoundingBox;
import org.citydb.config.project.filter.Tiling;
import org.citydb.config.project.filter.TilingMode;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.BalloonContentMode;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.PointAndCurve;
import org.citydb.config.project.kmlExporter.PointDisplayMode;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.common.concurrent.IOWriterWorkerFactory;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.FeatureCounterEvent;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.StatusDialogMessage;
import org.citydb.modules.common.event.StatusDialogTitle;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.FilterMode;
import org.citydb.modules.kml.concurrent.KmlExportWorkerFactory;
import org.citydb.modules.kml.database.Bridge;
import org.citydb.modules.kml.database.Building;
import org.citydb.modules.kml.database.CityFurniture;
import org.citydb.modules.kml.database.CityObjectGroup;
import org.citydb.modules.kml.database.GenericCityObject;
import org.citydb.modules.kml.database.KmlSplitter;
import org.citydb.modules.kml.database.KmlSplittingResult;
import org.citydb.modules.kml.database.LandUse;
import org.citydb.modules.kml.database.Relief;
import org.citydb.modules.kml.database.SolitaryVegetationObject;
import org.citydb.modules.kml.database.Transportation;
import org.citydb.modules.kml.database.Tunnel;
import org.citydb.modules.kml.database.WaterBody;
import org.citydb.modules.kml.datatype.TypeAttributeValueEnum;
import org.citydb.modules.kml.util.CityObject4JSON;
import org.citydb.modules.kml.util.ExportTracker;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import net.opengis.kml._2.BalloonStyleType;
import net.opengis.kml._2.BasicLinkType;
import net.opengis.kml._2.DocumentType;
import net.opengis.kml._2.FolderType;
import net.opengis.kml._2.IconStyleType;
import net.opengis.kml._2.KmlType;
import net.opengis.kml._2.LabelStyleType;
import net.opengis.kml._2.LatLonAltBoxType;
import net.opengis.kml._2.LineStringType;
import net.opengis.kml._2.LineStyleType;
import net.opengis.kml._2.LinkType;
import net.opengis.kml._2.LodType;
import net.opengis.kml._2.LookAtType;
import net.opengis.kml._2.NetworkLinkType;
import net.opengis.kml._2.ObjectFactory;
import net.opengis.kml._2.PairType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolyStyleType;
import net.opengis.kml._2.RegionType;
import net.opengis.kml._2.StyleMapType;
import net.opengis.kml._2.StyleStateEnumType;
import net.opengis.kml._2.StyleType;
import net.opengis.kml._2.ViewRefreshModeEnumType;

public class KmlExporter implements EventHandler {
	private final Logger LOG = Logger.getInstance();

	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final DatabaseConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private ObjectFactory kmlFactory; 
	private WorkerPool<KmlSplittingResult> kmlWorkerPool;
	private SingleWorkerPool<SAXEventBuffer> ioWriterPool;
	private KmlSplitter kmlSplitter;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private final String ENCODING = "UTF-8";
	private final Charset CHARSET = Charset.forName(ENCODING);
	private final String TEMP_FOLDER = "__temp";
	private File lastTempFolder = null;
	private GeometryObject globeWGS84BboxGeometry;
	private BoundingBox globeWGS84Bbox; 
	private int rows = 1;
	private int columns = 1;

	private EnumMap<CityGMLClass, Long> featureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
	private EnumMap<CityGMLClass, Long> totalFeatureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
	private long geometryCounter;

	public KmlExporter (JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DatabaseConnectionPool dbPool,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		kmlFactory = new ObjectFactory();
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() throws KmlExportException {
		// adding listener
		eventDispatcher.addEventHandler(EventType.FEATURE_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// checking workspace
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace();
		if (shouldRun && dbPool.getActiveDatabaseAdapter().hasVersioningSupport() && 
				!dbPool.getActiveDatabaseAdapter().getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName()) &&
				!dbPool.getActiveDatabaseAdapter().getWorkspaceManager().existsWorkspace(workspace, true))
			return false;

		// check whether spatial indexes are enabled
		LOG.info("Checking for spatial indexes on geometry columns of involved tables...");
		try {
			if (!dbPool.getActiveDatabaseAdapter().getUtil().isIndexEnabled("CITYOBJECT", "ENVELOPE") || 
					!dbPool.getActiveDatabaseAdapter().getUtil().isIndexEnabled("SURFACE_GEOMETRY", "GEOMETRY")) {
				LOG.error("Spatial indexes are not activated.");
				LOG.error("Please use the database tab to activate the spatial indexes.");
				return false;
			}
		} catch (SQLException e) {
			throw new KmlExportException("Failed to retrieve status of spatial indexes.", e);
		}

		// check whether the selected theme existed in the database,just for Building Class...
		String selectedTheme = config.getProject().getKmlExporter().getAppearanceTheme();
		if (!selectedTheme.equals(org.citydb.config.project.kmlExporter.KmlExporter.THEME_NONE)) {
			try {
				// displayForms Could be e.g. Footprint, Extruded, Geometry and COLLADA
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
					if (displayForm.getForm() == DisplayForm.COLLADA && displayForm.isActive()) {
						if (!dbPool.getActiveDatabaseAdapter().getUtil().getAppearanceThemeList(workspace).contains(selectedTheme)) {
							LOG.error("Database does not contain appearance theme \"" + selectedTheme + "\"");
							return false;
						}
					}
				}
			} catch (SQLException e) {
				throw new KmlExportException("Generic database error.", e);
			}
		}

		// check whether the Balloon template files existed, if not, error message will be printed out: file not found! 
		boolean balloonCheck = checkBalloonSettings(CityGMLClass.BUILDING);
		balloonCheck = checkBalloonSettings(CityGMLClass.WATER_BODY) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.LAND_USE) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.SOLITARY_VEGETATION_OBJECT) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.TRANSPORTATION_COMPLEX) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.RELIEF_FEATURE) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.CITY_FURNITURE) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.GENERIC_CITY_OBJECT) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.CITY_OBJECT_GROUP) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.BRIDGE) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.TUNNEL) && balloonCheck;
		if (!balloonCheck) 
			return false;	

		// check collada2gltf tool
		if (config.getProject().getKmlExporter().isCreateGltfModel()) {
			File file = new File(config.getProject().getKmlExporter().getPathOfGltfConverter());

			if (!file.exists())
				throw new KmlExportException("Failed to find the COLLADA2glTF tool at the provided path " + file.getAbsolutePath() + ".");
			else if (!file.canExecute())
				throw new KmlExportException("Failed to execute the COLLADA2glTF tool at " + file.getAbsolutePath() + ".");
		}

		boolean isBBoxActive = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getActive().booleanValue();
		Tiling tiling = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();

		// calculate and display number of tiles to be exported
		int remainingTiles = 1;
		if (isBBoxActive) {
			try {
				remainingTiles = calculateRowsColumns();
				int displayFormats = config.getProject().getKmlExporter().getActiveDisplayFormsAmount(config.getProject().getKmlExporter().getBuildingDisplayForms());
				remainingTiles *= displayFormats;
				LOG.info(String.valueOf(rows * columns * displayFormats) + " (" + rows + "x" + columns + "x" + displayFormats + ") tiles will be generated."); 
			} catch (SQLException e) {
				throw new KmlExportException("Failed to calculate the number of tiles to be exported.", e);
			}
		}

		// get export filter and bounding box config
		ExportFilter exportFilter = new ExportFilter(config, FilterMode.KML_EXPORT);
		if (isBBoxActive) {
			globeWGS84Bbox = exportFilter.getBoundingBoxFilter().getFilterState();
			globeWGS84BboxGeometry = GeometryObject.createPolygon(new double[]{
					globeWGS84Bbox.getLowerCorner().getX(), globeWGS84Bbox.getLowerCorner().getY(),
					globeWGS84Bbox.getUpperCorner().getX(), globeWGS84Bbox.getLowerCorner().getY(),
					globeWGS84Bbox.getUpperCorner().getX(), globeWGS84Bbox.getUpperCorner().getY(),
					globeWGS84Bbox.getLowerCorner().getX(), globeWGS84Bbox.getUpperCorner().getY(),
					globeWGS84Bbox.getLowerCorner().getX(), globeWGS84Bbox.getLowerCorner().getY(),
			}, 2, 4326);
		}

		// create a saxWriter instance 
		// define indent for xml output and namespace mappings
		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setIndentString("  ");
		saxWriter.setHeaderComment("Written by " + this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + '"', 
				this.getClass().getPackage().getImplementationVendor());
		saxWriter.setDefaultNamespace("http://www.opengis.net/kml/2.2"); // default namespace
		saxWriter.setPrefix("gx", "http://www.google.com/kml/ext/2.2");
		saxWriter.setPrefix("atom", "http://www.w3.org/2005/Atom");
		saxWriter.setPrefix("xal", "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0");

		// set export filename and path
		String path = config.getInternal().getExportFileName().trim();
		String fileExtension = config.getProject().getKmlExporter().isExportAsKmz() ? ".kmz" : ".kml";
		String fileName = null;

		if (path.lastIndexOf(File.separator) == -1) {
			fileName = path.lastIndexOf(".") == -1 ? path : path.substring(0, path.lastIndexOf("."));			
			path = ".";
		} else {
			fileName = path.lastIndexOf(".") == -1 ? path.substring(path.lastIndexOf(File.separator) + 1) : path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
			path = path.substring(0, path.lastIndexOf(File.separator));
		}

		// start writing KML master file if required
		SAXWriter masterFileWriter = null;
		if (isBBoxActive) {
			try {
				masterFileWriter = writeMasterFileHeader(fileName, path);
			} catch (JAXBException | IOException | SAXException e) {
				throw new KmlExportException("Failed to write KML master file header.", e);
			}
		}

		// start writing cityobject JSON file if required
		FileOutputStream jsonFileWriter = null;
		boolean jsonHasContent = false;
		if (config.getProject().getKmlExporter().isWriteJSONFile() && isBBoxActive) {
			try {
				File jsonFile = new File(path + File.separator + fileName + ".json");
				jsonFileWriter = new FileOutputStream(jsonFile);
				if (config.getProject().getKmlExporter().isWriteJSONPFile())
					jsonFileWriter.write((config.getProject().getKmlExporter().getCallbackNameJSONP() + "({\n").getBytes(CHARSET));
				else
					jsonFileWriter.write("{\n".getBytes(CHARSET));
			} catch (IOException e) {
				throw new KmlExportException("Failed to write JSON file header.", e);
			}			
		}

		long start = System.currentTimeMillis();

		// iterate over tiles
		for (int i = 0; shouldRun && i < rows; i++) {
			for (int j = 0; shouldRun && j < columns; j++) {

				// track exported objects
				ExportTracker tracker = new ExportTracker();

				// set active tile and get tile extent in WGS84
				GeometryObject wgs84Tile = null;
				if (isBBoxActive && tiling.getMode() != TilingMode.NO_TILING) {
					exportFilter.getBoundingBoxFilter().setActiveTile(i, j);
					BoundingBox wgs84Bbox = exportFilter.getBoundingBoxFilter().getFilterState();
					wgs84Tile = GeometryObject.createPolygon(new double[]{
							wgs84Bbox.getLowerCorner().getX(), wgs84Bbox.getLowerCorner().getY(),
							wgs84Bbox.getUpperCorner().getX(), wgs84Bbox.getLowerCorner().getY(),
							wgs84Bbox.getUpperCorner().getX(), wgs84Bbox.getUpperCorner().getY(),
							wgs84Bbox.getLowerCorner().getX(), wgs84Bbox.getUpperCorner().getY(),
							wgs84Bbox.getLowerCorner().getX(), wgs84Bbox.getLowerCorner().getY(),
					}, 2, 4326);
				}

				// iterate over display forms
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
					if (!displayForm.isActive()) 
						continue;

					if (lastTempFolder != null && lastTempFolder.exists()) 
						deleteFolder(lastTempFolder); // just in case

					File file = null;
					ZipOutputStream zipOut = null;
					String currentWorkingDirectoryPath = null;
					try {
						if (isBBoxActive && tiling.getMode() != TilingMode.NO_TILING) {
							File tilesRootDirectory = new File(path, "Tiles");
							tilesRootDirectory.mkdir();
							File rowTilesDirectory = new File(tilesRootDirectory.getPath(),  String.valueOf(i));
							rowTilesDirectory.mkdir();
							File columnTilesDirectory = new File(rowTilesDirectory.getPath(),  String.valueOf(j));
							columnTilesDirectory.mkdir();
							file = new File(columnTilesDirectory.getPath() + File.separator + fileName + "_Tile_" + i + "_" + j + "_" + displayForm.getName() + fileExtension);
							currentWorkingDirectoryPath = columnTilesDirectory.getPath();
						} else {
							file = new File(path + File.separator + fileName + "_" + displayForm.getName() + fileExtension);
							currentWorkingDirectoryPath = path;
						}
						tracker.setCurrentWorkingDirectoryPath(currentWorkingDirectoryPath);

						eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("kmlExport.dialog.writingToFile"), this));
						eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName(), this));
						eventDispatcher.triggerEvent(new CounterEvent(CounterType.REMAINING_TILES, --remainingTiles, this));
						
						// open file for writing
						try {
							OutputStreamWriter fileWriter = null;
							if (config.getProject().getKmlExporter().isExportAsKmz()) {
								zipOut = new ZipOutputStream(new FileOutputStream(file));
								ZipEntry zipEntry = new ZipEntry("doc.kml");
								zipOut.putNextEntry(zipEntry);
								fileWriter = new OutputStreamWriter(zipOut, CHARSET);
							} else
								fileWriter = new OutputStreamWriter(new FileOutputStream(file), CHARSET);

							// set output for SAXWriter
							saxWriter.setOutput(fileWriter);	
						} catch (IOException e) {
							throw new KmlExportException("Failed to open file '" + file.getName() + "' for writing.", e);
						}

						// create worker pools
						// here we have an open issue: queue sizes are fix...
						ioWriterPool = new SingleWorkerPool<SAXEventBuffer>(
								"kml_writer_pool",
								new IOWriterWorkerFactory(saxWriter, eventDispatcher),
								100,
								true);

						kmlWorkerPool = new WorkerPool<KmlSplittingResult>(
								"db_exporter_pool",
								config.getProject().getKmlExporter().getResources().getThreadPool().getDefaultPool().getMinThreads(),
								config.getProject().getKmlExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
								PoolSizeAdaptationStrategy.AGGRESSIVE,
								new KmlExportWorkerFactory(
										jaxbKmlContext,
										jaxbColladaContext,
										dbPool,
										ioWriterPool,
										tracker,
										kmlFactory,
										config,
										eventDispatcher),
								300,
								false);

						// prestart pool workers
						ioWriterPool.prestartCoreWorkers();
						kmlWorkerPool.prestartCoreWorkers();

						// fail if we could not start a single import worker
						if (kmlWorkerPool.getPoolSize() == 0)
							throw new KmlExportException("Failed to start database export worker pool. Check the database connection pool settings.");

						// create file header writer
						SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(kmlFactory.createDocument(null).getName(), saxWriter);

						// ok, preparations done. inform user...
						LOG.info("Exporting to file: " + file.getAbsolutePath());

						// create kml root element
						KmlType kmlType = kmlFactory.createKmlType();
						JAXBElement<KmlType> kml = kmlFactory.createKml(kmlType);

						DocumentType document = kmlFactory.createDocumentType();
						if (isBBoxActive &&	tiling.getMode() != TilingMode.NO_TILING)
							document.setName(fileName + "_Tile_" + i + "_" + j + "_" + displayForm.getName());
						else 
							document.setName(fileName + "_" + displayForm.getName());

						document.setOpen(false);
						kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

						// write file header
						Marshaller marshaller = null;
						try {
							marshaller = jaxbKmlContext.createMarshaller();
							fragmentWriter.setWriteMode(WriteMode.HEAD);
							marshaller.marshal(kml, fragmentWriter);

							if (isBBoxActive 
									&&	tiling.getMode() != TilingMode.NO_TILING 
									&& config.getProject().getKmlExporter().getFilter().isSetComplexFilter() 
									&& config.getProject().getKmlExporter().isShowTileBorders())
								addBorder(wgs84Tile, null, saxWriter);
							
						} catch (JAXBException e) {
							throw new KmlExportException("Failed to write output file.", e);
						}

						// get database splitter and start query
						try {
							kmlSplitter = new KmlSplitter(
									dbPool,
									kmlWorkerPool,
									exportFilter,
									displayForm,
									config);

							if (shouldRun)
								kmlSplitter.startQuery();
						} catch (SQLException e) {
							throw new KmlExportException("Failed to query the database.", e);
						}

						// shutdown worker pools
						try {
							kmlWorkerPool.shutdownAndWait();
							ioWriterPool.shutdownAndWait();
						} catch (InterruptedException e) {
							throw new KmlExportException("Failed to shutdown worker pools.", e);
						}

						try {
							// add styles
							if (!featureCounterMap.isEmpty() &&
									(!config.getProject().getKmlExporter().isOneFilePerObject() ||
											config.getProject().getKmlExporter().getFilter().isSetSimpleFilter())) {
								for (CityGMLClass type : featureCounterMap.keySet()) {
									if (featureCounterMap.get(type) > 0)
										addStyle(displayForm, type, saxWriter);
								}
							}
						} catch (JAXBException e) {
							throw new KmlExportException("Failed to write styles.", e);
						}

						// write footer element
						try {
							fragmentWriter.setWriteMode(WriteMode.TAIL);
							marshaller.marshal(kml, fragmentWriter);
						} catch (JAXBException e) {
							throw new KmlExportException("Failed to write output file.", e);
						}
						
						try {
							if (!featureCounterMap.isEmpty()) {
								saxWriter.flush();
								if (config.getProject().getKmlExporter().isExportAsKmz()) {
									zipOut.closeEntry();

									List<File> filesToZip = new ArrayList<File>();
									File tempFolder = new File(currentWorkingDirectoryPath, TEMP_FOLDER);
									lastTempFolder = tempFolder;
									int indexOfZipFilePath = tempFolder.getCanonicalPath().length() + 1;

									if (tempFolder.exists()) { // !config.getProject().getKmlExporter().isOneFilePerObject()
										LOG.info("Zipping to kmz archive from temporary folder...");
										getAllFiles(tempFolder, filesToZip);
										for (File fileToZip : filesToZip) {
											if (!fileToZip.isDirectory()) {
												FileInputStream inputStream = new FileInputStream(fileToZip);
												String zipEntryName = fileToZip.getCanonicalPath().substring(indexOfZipFilePath);
												zipEntryName = zipEntryName.replace(File.separator, "/"); // MUST
												ZipEntry zipEntry = new ZipEntry(zipEntryName);
												zipOut.putNextEntry(zipEntry);

												byte[] bytes = new byte[64*1024]; // 64K should be enough for most
												int length;
												while ((length = inputStream.read(bytes)) >= 0) {
													zipOut.write(bytes, 0, length);
												}
												inputStream.close();
												zipOut.closeEntry();
											}
										}
										LOG.info("Removing temporary folder...");
										deleteFolder(tempFolder);
									}
									zipOut.close();
								}
							}
						} catch (Exception e) {
							throw new KmlExportException("Failed to write output file.", e);
						}
						
						// flush sax writer and close file
						try {
							saxWriter.flush();
							saxWriter.getOutputWriter().close();
						} catch (Exception e) {
							throw new KmlExportException("Failed to close output file.", e);
						}

						// delete empty tile file if requested
						if (isBBoxActive && featureCounterMap.isEmpty() && !config.getProject().getKmlExporter().isExportEmptyTiles()) {
							LOG.debug("Tile_" + exportFilter.getBoundingBoxFilter().getTileRow()
									+ "_" + exportFilter.getBoundingBoxFilter().getTileColumn() + " is empty. Deleting file " + file.getName() + ".");
							file.delete();
						}

						eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.finish.msg"), this));
					} finally {
						// clean up
						if (ioWriterPool != null && !ioWriterPool.isTerminated())
							ioWriterPool.shutdownNow();

						if (kmlWorkerPool != null && !kmlWorkerPool.isTerminated())
							kmlWorkerPool.shutdownNow();

						try {
							eventDispatcher.flushEvents();
						} catch (InterruptedException e) {
							//
						}
					}
				}

				// create reference to tile file in master file
				if (masterFileWriter != null && !featureCounterMap.isEmpty()) {
					try {
						writeMasterFileTileReference(fileName, i, j, wgs84Tile, masterFileWriter);
					} catch (JAXBException e) {
						if (jsonFileWriter != null) try { jsonFileWriter.close(); } catch (IOException ioe) { }
						throw new KmlExportException("Failed to write tile reference to master file.", e);
					}
				}

				// fill cityobject JSON file after tile has been processed
				if (jsonFileWriter != null && !featureCounterMap.isEmpty()) {
					try {
						Iterator<CityObject4JSON> iter = tracker.values().iterator();
						if (iter.hasNext()) {
							if (jsonHasContent)
								jsonFileWriter.write(",\n".getBytes(CHARSET));
							else
								jsonHasContent = true;
						}

						while (iter.hasNext()) {
							jsonFileWriter.write(iter.next().toString().getBytes(CHARSET));
							if (iter.hasNext())
								jsonFileWriter.write(",\n".getBytes(CHARSET));
						}
					} catch (IOException e) {
						if (jsonFileWriter != null) try { jsonFileWriter.close(); } catch (IOException ioe) { }
						throw new KmlExportException("Failed to write JSON file.", e);
					}
				}

				featureCounterMap.clear();
			}
		}

		// complete KML master file
		if (masterFileWriter != null) {
			try {
				writeMasterFileFooter(masterFileWriter);
				masterFileWriter.close();
			} catch (JAXBException | SAXException e) {
				throw new KmlExportException("Failed to write KML master file footer.", e);
			}
		}

		// write master JSON file
		if (isBBoxActive) {
			try {
				writeMasterJsonFileTileReference(path, fileName, fileExtension);
			} catch (IOException e) {
				throw new KmlExportException("Failed to write master JSON file.", e);
			}
		}

		// close cityobject JSON file
		if (jsonFileWriter != null) {
			try {
				if (config.getProject().getKmlExporter().isWriteJSONPFile())
					jsonFileWriter.write("\n});\n".getBytes(CHARSET));
				else
					jsonFileWriter.write("\n}\n".getBytes(CHARSET));

				jsonFileWriter.close();
			} catch (IOException e) {
				throw new KmlExportException("Failed to close JSON file.", e);
			}
		}		

		// show exported features
		if (!totalFeatureCounterMap.isEmpty()) {
			LOG.info("Exported CityGML features:");
			for (CityGMLClass type : totalFeatureCounterMap.keySet())
				LOG.info(type + ": " + totalFeatureCounterMap.get(type));
		}

		LOG.info("Processed geometry objects: " + geometryCounter);

		if (lastTempFolder != null && lastTempFolder.exists()) 
			deleteFolder(lastTempFolder); // just in case

		if (shouldRun)
			LOG.info("Total export time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	private int calculateRowsColumns() throws SQLException {
		TiledBoundingBox bbox = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox();
		double autoTileSideLength = config.getProject().getKmlExporter().getAutoTileSideLength();
		BoundingBox extent = bbox;

		// transform bbox into the database srs if required
		DatabaseSrs dbSrs = dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
		if (bbox.isSetSrs() && bbox.getSrs().getSrid() != dbSrs.getSrid())
			extent = dbPool.getActiveDatabaseAdapter().getUtil().transformBoundingBox(extent, extent.getSrs(), dbSrs);

		// determine tile sizes and derive number of rows and columns
		switch (bbox.getTiling().getMode()) {
		case AUTOMATIC:
			// approximate
			rows = (int)((extent.getUpperCorner().getY() - extent.getLowerCorner().getY()) / autoTileSideLength) + 1;
			columns = (int)((extent.getUpperCorner().getX() - extent.getLowerCorner().getX()) / autoTileSideLength) + 1;
			bbox.getTiling().setRows(rows);
			bbox.getTiling().setColumns(columns);
			break;
		case NO_TILING:
			// no_tiling is internally mapped to manual tiling with one tile
			bbox.getTiling().setMode(TilingMode.MANUAL);
			bbox.getTiling().setRows(1);
			bbox.getTiling().setColumns(1);
		default:
			rows = bbox.getTiling().getRows();
			columns = bbox.getTiling().getColumns();
		}

		return rows * columns;
	}

	private SAXWriter writeMasterFileHeader(String fileName, String path) throws JAXBException, IOException, SAXException {
		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setIndentString("  ");
		saxWriter.setHeaderComment("Written by " + this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + '"', 
				this.getClass().getPackage().getImplementationVendor());
		saxWriter.setDefaultNamespace("http://www.opengis.net/kml/2.2"); // default namespace
		saxWriter.setPrefix("gx", "http://www.google.com/kml/ext/2.2");
		saxWriter.setPrefix("atom", "http://www.w3.org/2005/Atom");
		saxWriter.setPrefix("xal", "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0");

		Marshaller marshaller = jaxbKmlContext.createMarshaller();

		File mainFile = new File(path, fileName + ".kml");
		FileOutputStream outputStream = new FileOutputStream(mainFile);
		saxWriter.setOutput(outputStream, ENCODING);

		// create file header
		SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(new QName("http://www.opengis.net/kml/2.2", "Document"), saxWriter);						

		// create kml root element
		KmlType kmlType = kmlFactory.createKmlType();
		JAXBElement<KmlType> kml = kmlFactory.createKml(kmlType);
		DocumentType document = kmlFactory.createDocumentType();
		document.setOpen(true);
		document.setName(fileName);
		LookAtType lookAtType = kmlFactory.createLookAtType();

		lookAtType.setLongitude(globeWGS84Bbox.getLowerCorner().getX() + Math.abs((globeWGS84Bbox.getUpperCorner().getX() - globeWGS84Bbox.getLowerCorner().getX())/2));
		lookAtType.setLatitude(globeWGS84Bbox.getLowerCorner().getY() + Math.abs((globeWGS84Bbox.getUpperCorner().getY() - globeWGS84Bbox.getLowerCorner().getY())/2));
		lookAtType.setAltitude(0.0);
		lookAtType.setHeading(0.0);
		lookAtType.setTilt(60.0);
		lookAtType.setRange(970.0);
		document.setAbstractViewGroup(kmlFactory.createLookAt(lookAtType));
		kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

		fragmentWriter.setWriteMode(WriteMode.HEAD);
		marshaller.marshal(kml, fragmentWriter);				

		if (config.getProject().getKmlExporter().isOneFilePerObject()) {
			FeatureClass featureFilter = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass();
			if (featureFilter.isSetBuilding()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
					addStyle(displayForm, CityGMLClass.BUILDING, saxWriter);
				}
			}
			if (featureFilter.isSetCityFurniture()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getCityFurnitureDisplayForms()) {
					addStyle(displayForm, CityGMLClass.CITY_FURNITURE, saxWriter);
				}
			}
			if (featureFilter.isSetCityObjectGroup()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getCityObjectGroupDisplayForms()) {
					addStyle(displayForm, CityGMLClass.CITY_OBJECT_GROUP, saxWriter);
				}
			}
			if (featureFilter.isSetGenericCityObject()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getGenericCityObjectDisplayForms()) {
					addStyle(displayForm, CityGMLClass.GENERIC_CITY_OBJECT, saxWriter);
				}
			}
			if (featureFilter.isSetLandUse()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getLandUseDisplayForms()) {
					addStyle(displayForm, CityGMLClass.LAND_USE, saxWriter);
				}
			}
			if (featureFilter.isSetReliefFeature()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getReliefDisplayForms()) {
					addStyle(displayForm, CityGMLClass.RELIEF_FEATURE, saxWriter);
				}
			}
			if (featureFilter.isSetTransportation()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getTransportationDisplayForms()) {
					addStyle(displayForm, CityGMLClass.TRANSPORTATION_COMPLEX, saxWriter);
				}
			}
			if (featureFilter.isSetVegetation()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getVegetationDisplayForms()) {
					addStyle(displayForm, CityGMLClass.SOLITARY_VEGETATION_OBJECT, saxWriter);
				}
			}
			if (featureFilter.isSetWaterBody()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getWaterBodyDisplayForms()) {
					addStyle(displayForm, CityGMLClass.WATER_BODY, saxWriter);
				}
			}
			if (featureFilter.isSetBridge()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getBridgeDisplayForms()) {
					addStyle(displayForm, CityGMLClass.BRIDGE, saxWriter);
				}
			}
			if (featureFilter.isSetTunnel()) {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getTunnelDisplayForms()) {
					addStyle(displayForm, CityGMLClass.TUNNEL, saxWriter);
				}
			}
		}

		if (config.getProject().getKmlExporter().isShowBoundingBox()) {
			StyleType style = kmlFactory.createStyleType();
			style.setId("frameStyle");
			LineStyleType frameLineStyleType = kmlFactory.createLineStyleType();
			frameLineStyleType.setWidth(4.0);
			style.setLineStyle(frameLineStyleType);

			addBorder(globeWGS84BboxGeometry, style, saxWriter);
		}

		return saxWriter;
	}

	private void writeMasterFileTileReference(String tileName, int row, int column, GeometryObject wgs84Tile, SAXWriter saxWriter) throws JAXBException {
		if (wgs84Tile == null)
			return;

		TilingMode tilingMode = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling().getMode();
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		// tileName should not contain special characters,
		// since it will be used as filename for all displayForm files
		if (tilingMode != TilingMode.NO_TILING)
			tileName = tileName + "_Tile_" + row + "_" + column;

		FolderType folderType = kmlFactory.createFolderType();
		folderType.setName(tileName);

		for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
			if (!displayForm.isActive()) 
				continue;

			String fileExtension = config.getProject().getKmlExporter().isExportAsKmz() ? ".kmz" : ".kml";
			String tilenameForDisplayForm = tileName + "_" + displayForm.getName() + fileExtension; 

			NetworkLinkType networkLinkType = kmlFactory.createNetworkLinkType();
			networkLinkType.setName("Display as " + displayForm.getName());

			RegionType regionType = kmlFactory.createRegionType();

			double[] coordinates = wgs84Tile.getCoordinates(0);		
			double xmin = Math.min(coordinates[0], coordinates[6]);
			double ymin = Math.min(coordinates[1], coordinates[3]);
			double xmax = Math.max(coordinates[2], coordinates[4]);
			double ymax = Math.max(coordinates[5], coordinates[7]);

			LatLonAltBoxType latLonAltBoxType = kmlFactory.createLatLonAltBoxType();			
			latLonAltBoxType.setNorth(ymax);
			latLonAltBoxType.setSouth(ymin);
			latLonAltBoxType.setEast(xmax);
			latLonAltBoxType.setWest(xmin);

			LodType lodType = kmlFactory.createLodType();
			lodType.setMinLodPixels((double)displayForm.getVisibleFrom());
			lodType.setMaxLodPixels((double)displayForm.getVisibleUpTo());

			regionType.setLatLonAltBox(latLonAltBoxType);
			regionType.setLod(lodType);

			LinkType linkType = kmlFactory.createLinkType();
			linkType.setHref("Tiles" + File.separator + row + File.separator + column + File.separator + tilenameForDisplayForm);
			linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getProject().getKmlExporter().getViewRefreshMode()));
			linkType.setViewFormat("");
			if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP)
				linkType.setViewRefreshTime(config.getProject().getKmlExporter().getViewRefreshTime());

			// confusion between atom:link and kml:Link in ogckml22.xsd
			networkLinkType.getRest().add(kmlFactory.createLink(linkType));
			networkLinkType.setRegion(regionType);
			folderType.getAbstractFeatureGroup().add(kmlFactory.createNetworkLink(networkLinkType));
		}

		marshaller.marshal(kmlFactory.createFolder(folderType), saxWriter);
	}

	private void writeMasterFileFooter(SAXWriter saxWriter) throws JAXBException, SAXException {
		Marshaller marshaller = jaxbKmlContext.createMarshaller();

		// create file header
		SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(new QName("http://www.opengis.net/kml/2.2", "Document"), saxWriter);						
		fragmentWriter.setWriteMode(WriteMode.TAIL);

		// create kml root element
		KmlType kmlType = kmlFactory.createKmlType();
		JAXBElement<KmlType> kml = kmlFactory.createKml(kmlType);
		DocumentType document = kmlFactory.createDocumentType();
		kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

		marshaller.marshal(kml, fragmentWriter);
	}

	private void writeMasterJsonFileTileReference(String path, String fileName, String fileExtension) throws IOException {
		for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
			if (displayForm.isActive()) {
				File jsonFileForMasterFile = new File(path + File.separator + fileName + "_" + displayForm.getName() + "_MasterJSON" + ".json");
				FileOutputStream jsonFileWriterForMasterFile = new FileOutputStream(jsonFileForMasterFile);
				jsonFileWriterForMasterFile.write("{\n".getBytes(CHARSET));
				String versionNumber = "1.0.0";
				jsonFileWriterForMasterFile.write(("\t\"" + "version" + "\": \"" + versionNumber + "\",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "layername" + "\": \"" + fileName + "\",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "fileextension" + "\": \"" + fileExtension + "\",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "displayform" + "\": \"" + displayForm.getName() + "\",").getBytes(CHARSET));	
				jsonFileWriterForMasterFile.write(("\n\t\"" + "minLodPixels" + "\": " + displayForm.getVisibleFrom() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "maxLodPixels" + "\": " + displayForm.getVisibleUpTo() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "colnum" + "\": " + columns + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "rownum" + "\": " + rows + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "bbox" + "\":{ ").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "xmin" + "\": " + globeWGS84Bbox.getLowerCorner().getX() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "xmax" + "\": " + globeWGS84Bbox.getUpperCorner().getX() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "ymin" + "\": " + globeWGS84Bbox.getLowerCorner().getY() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "ymax" + "\": " + globeWGS84Bbox.getUpperCorner().getY()).getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t}").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write("\n}\n".getBytes(CHARSET));				
				jsonFileWriterForMasterFile.close();
			}							
		}		
	}

	private void addStyle(DisplayForm currentDisplayForm, CityGMLClass featureClass, SAXWriter saxWriter) throws JAXBException {
		if (!currentDisplayForm.isActive()) return;
		switch (featureClass) {
		case SOLITARY_VEGETATION_OBJECT:
		case PLANT_COVER:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getVegetationDisplayForms(),
					SolitaryVegetationObject.STYLE_BASIS_NAME,
					saxWriter);
			break;

		case TRAFFIC_AREA:
		case AUXILIARY_TRAFFIC_AREA:
		case TRANSPORTATION_COMPLEX:
		case TRACK:
		case RAILWAY:
		case ROAD:
		case SQUARE:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getTransportationDisplayForms(),
					Transportation.STYLE_BASIS_NAME,
					saxWriter);
			break;
		case RELIEF_FEATURE:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getReliefDisplayForms(),
					Relief.STYLE_BASIS_NAME,
					saxWriter);
			break;

		case CITY_OBJECT_GROUP:
			addStyle(new DisplayForm(DisplayForm.FOOTPRINT, -1, -1), // hard-coded for groups
					config.getProject().getKmlExporter().getCityObjectGroupDisplayForms(),
					CityObjectGroup.STYLE_BASIS_NAME,
					saxWriter);
			break;

		case CITY_FURNITURE:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getCityFurnitureDisplayForms(),
					CityFurniture.STYLE_BASIS_NAME,
					saxWriter);
			break;

		case GENERIC_CITY_OBJECT:
			addGenericCityObjectPointAndCurveStyle(saxWriter);
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getGenericCityObjectDisplayForms(),
					GenericCityObject.STYLE_BASIS_NAME,
					saxWriter);
			break;

		case LAND_USE:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getLandUseDisplayForms(),
					LandUse.STYLE_BASIS_NAME,
					saxWriter);
			break;

		case WATER_BODY:
		case WATER_CLOSURE_SURFACE:
		case WATER_GROUND_SURFACE:
		case WATER_SURFACE:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getWaterBodyDisplayForms(),
					WaterBody.STYLE_BASIS_NAME,
					saxWriter);
			break;
		case BRIDGE:
			addStyle(currentDisplayForm, 
					config.getProject().getKmlExporter().getBridgeDisplayForms(), 
					Bridge.STYLE_BASIS_NAME,
					saxWriter);
			break;
		case TUNNEL:
			addStyle(currentDisplayForm, 
					config.getProject().getKmlExporter().getTunnelDisplayForms(), 
					Tunnel.STYLE_BASIS_NAME,
					saxWriter);
			break;
		case BUILDING: // must be last, why?
		default:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getBuildingDisplayForms(),
					Building.STYLE_BASIS_NAME,
					saxWriter);
		}
	}

	private void addGenericCityObjectPointAndCurveStyle(SAXWriter saxWriter) throws JAXBException {
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		BalloonStyleType balloonStyle = new BalloonStyleType();
		balloonStyle.setText("$[description]");

		PointAndCurve pacSettings = config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve();

		if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
			StyleType pointStyleNormal = kmlFactory.createStyleType();
			LabelStyleType labelStyleType = kmlFactory.createLabelStyleType();
			labelStyleType.setScale(0.0);
			pointStyleNormal.setLabelStyle(labelStyleType);

			IconStyleType iconStyleType = kmlFactory.createIconStyleType();
			iconStyleType.setScale(pacSettings.getPointIconScale());
			iconStyleType.setColor(hexStringToByteArray(DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getPointIconColor()))));
			BasicLinkType icon = kmlFactory.createBasicLinkType();
			icon.setHref(PointAndCurve.DefaultIconHref);
			iconStyleType.setIcon(icon);
			pointStyleNormal.setIconStyle(iconStyleType);			
			pointStyleNormal.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Normal");
			pointStyleNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(pointStyleNormal), saxWriter);
		}
		else if (pacSettings.getPointDisplayMode() == PointDisplayMode.CUBE) {
			String fillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeFillColor()));
			String lineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeFillColor()));
			String hlFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeHighlightedColor()));
			String hlLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeHighlightedColor()));

			LineStyleType lineStyleCubeNormal = kmlFactory.createLineStyleType();
			lineStyleCubeNormal.setColor(hexStringToByteArray(lineColor));
			lineStyleCubeNormal.setWidth(1.5);
			PolyStyleType polyStyleCubeNormal = kmlFactory.createPolyStyleType();
			polyStyleCubeNormal.setColor(hexStringToByteArray(fillColor));
			StyleType styleCubeNormal = kmlFactory.createStyleType();
			styleCubeNormal.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Normal");
			styleCubeNormal.setLineStyle(lineStyleCubeNormal);
			styleCubeNormal.setPolyStyle(polyStyleCubeNormal);
			styleCubeNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(styleCubeNormal), saxWriter);

			if (pacSettings.isPointCubeHighlightingEnabled()) {
				LineStyleType lineStyleCubeHighlight = kmlFactory.createLineStyleType();
				lineStyleCubeHighlight.setColor(hexStringToByteArray(hlLineColor));
				lineStyleCubeHighlight.setWidth(1.5);
				PolyStyleType polyStyleCubeHighlight = kmlFactory.createPolyStyleType();
				polyStyleCubeHighlight.setColor(hexStringToByteArray(hlFillColor));
				StyleType styleCubeHighlight = kmlFactory.createStyleType();
				styleCubeHighlight.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Highlight");
				styleCubeHighlight.setLineStyle(lineStyleCubeHighlight);
				styleCubeHighlight.setPolyStyle(polyStyleCubeHighlight);
				styleCubeHighlight.setBalloonStyle(balloonStyle);

				PairType pairCubeNormal = kmlFactory.createPairType();
				pairCubeNormal.setKey(StyleStateEnumType.NORMAL);
				pairCubeNormal.setStyleUrl("#" + styleCubeNormal.getId());
				PairType pairCubeHighlight = kmlFactory.createPairType();
				pairCubeHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
				pairCubeHighlight.setStyleUrl("#" + styleCubeHighlight.getId());
				StyleMapType styleMapCube = kmlFactory.createStyleMapType();
				styleMapCube.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Style");
				styleMapCube.getPair().add(pairCubeNormal);
				styleMapCube.getPair().add(pairCubeHighlight);

				marshaller.marshal(kmlFactory.createStyle(styleCubeHighlight), saxWriter);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapCube), saxWriter);
			}
		}
		else {
			LineStyleType pointLineStyleNormal = kmlFactory.createLineStyleType();
			pointLineStyleNormal.setColor(hexStringToByteArray(DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getPointNormalColor()))));
			pointLineStyleNormal.setWidth(pacSettings.getPointThickness());
			StyleType pointStyleNormal = kmlFactory.createStyleType();
			pointStyleNormal.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Normal");
			pointStyleNormal.setLineStyle(pointLineStyleNormal);
			pointStyleNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(pointStyleNormal), saxWriter);

			if (pacSettings.isPointHighlightingEnabled()) {
				LineStyleType pointLineStyleHighlight = kmlFactory.createLineStyleType();
				pointLineStyleHighlight.setColor(hexStringToByteArray(DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getPointHighlightedColor()))));
				pointLineStyleHighlight.setWidth(pacSettings.getPointHighlightedThickness());
				StyleType pointStyleHighlight = kmlFactory.createStyleType();
				pointStyleHighlight.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Highlight");
				pointStyleHighlight.setLineStyle(pointLineStyleHighlight);
				pointStyleHighlight.setBalloonStyle(balloonStyle);

				PairType pairPointNormal = kmlFactory.createPairType();
				pairPointNormal.setKey(StyleStateEnumType.NORMAL);
				pairPointNormal.setStyleUrl("#" + pointStyleNormal.getId());
				PairType pairPointHighlight = kmlFactory.createPairType();
				pairPointHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
				pairPointHighlight.setStyleUrl("#" + pointStyleHighlight.getId());
				StyleMapType styleMapPoint = kmlFactory.createStyleMapType();
				styleMapPoint.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Style");
				styleMapPoint.getPair().add(pairPointNormal);
				styleMapPoint.getPair().add(pairPointHighlight);

				marshaller.marshal(kmlFactory.createStyle(pointStyleHighlight), saxWriter);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapPoint), saxWriter);
			}			
		}

		LineStyleType lineStyleNormal = kmlFactory.createLineStyleType();
		lineStyleNormal.setColor(hexStringToByteArray(DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getCurveNormalColor()))));
		lineStyleNormal.setWidth(pacSettings.getCurveThickness());
		StyleType curveStyleNormal = kmlFactory.createStyleType();
		curveStyleNormal.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.CURVE + "Normal");
		curveStyleNormal.setLineStyle(lineStyleNormal);
		curveStyleNormal.setBalloonStyle(balloonStyle);

		marshaller.marshal(kmlFactory.createStyle(curveStyleNormal), saxWriter);

		if (pacSettings.isCurveHighlightingEnabled()) {
			LineStyleType lineStyleHighlight = kmlFactory.createLineStyleType();
			lineStyleHighlight.setColor(hexStringToByteArray(DisplayForm.formatColorStringForKML(Integer.toHexString(pacSettings.getCurveHighlightedColor()))));
			lineStyleHighlight.setWidth(pacSettings.getCurveHighlightedThickness());
			StyleType curveStyleHighlight = kmlFactory.createStyleType();
			curveStyleHighlight.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.CURVE + "Highlight");
			curveStyleHighlight.setLineStyle(lineStyleHighlight);
			curveStyleHighlight.setBalloonStyle(balloonStyle);

			PairType pairCurveNormal = kmlFactory.createPairType();
			pairCurveNormal.setKey(StyleStateEnumType.NORMAL);
			pairCurveNormal.setStyleUrl("#" + curveStyleNormal.getId());
			PairType pairCurveHighlight = kmlFactory.createPairType();
			pairCurveHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
			pairCurveHighlight.setStyleUrl("#" + curveStyleHighlight.getId());
			StyleMapType styleMapCurve = kmlFactory.createStyleMapType();
			styleMapCurve.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.CURVE + "Style");
			styleMapCurve.getPair().add(pairCurveNormal);
			styleMapCurve.getPair().add(pairCurveHighlight);

			marshaller.marshal(kmlFactory.createStyle(curveStyleHighlight), saxWriter);
			marshaller.marshal(kmlFactory.createStyleMap(styleMapCurve), saxWriter);
		}
	}

	private void addStyle(DisplayForm currentDisplayForm,
			List<DisplayForm> displayFormsForObjectType,
			String styleBasisName,
			SAXWriter saxWriter) throws JAXBException {

		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		BalloonStyleType balloonStyle = new BalloonStyleType();
		balloonStyle.setText("$[description]");

		switch (currentDisplayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			int indexOfDf = displayFormsForObjectType.indexOf(currentDisplayForm);
			String fillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_COLOR);
			String lineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_COLOR);
			String hlFillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			String hlLineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
			if (indexOfDf != -1) {
				currentDisplayForm = displayFormsForObjectType.get(indexOfDf);
				if (currentDisplayForm.isSetRgba0()) {
					fillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba0()));
				}
				if (currentDisplayForm.isSetRgba1()) {
					lineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba1()));
				}
				if (currentDisplayForm.isSetRgba4()) {
					hlFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba4()));
				}
				if (currentDisplayForm.isSetRgba5()) {
					hlLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba5()));
				}
			}

			LineStyleType lineStyleFootprintNormal = kmlFactory.createLineStyleType();
			lineStyleFootprintNormal.setColor(hexStringToByteArray(lineColor));
			lineStyleFootprintNormal.setWidth(1.5);
			PolyStyleType polyStyleFootprintNormal = kmlFactory.createPolyStyleType();
			polyStyleFootprintNormal.setColor(hexStringToByteArray(fillColor));
			StyleType styleFootprintNormal = kmlFactory.createStyleType();
			styleFootprintNormal.setId(styleBasisName + currentDisplayForm.getName() + "Normal");
			styleFootprintNormal.setLineStyle(lineStyleFootprintNormal);
			styleFootprintNormal.setPolyStyle(polyStyleFootprintNormal);
			styleFootprintNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(styleFootprintNormal), saxWriter);

			if (currentDisplayForm.isHighlightingEnabled()) {
				LineStyleType lineStyleFootprintHighlight = kmlFactory.createLineStyleType();
				lineStyleFootprintHighlight.setColor(hexStringToByteArray(hlLineColor));
				lineStyleFootprintHighlight.setWidth(1.5);
				PolyStyleType polyStyleFootprintHighlight = kmlFactory.createPolyStyleType();
				polyStyleFootprintHighlight.setColor(hexStringToByteArray(hlFillColor));
				StyleType styleFootprintHighlight = kmlFactory.createStyleType();
				styleFootprintHighlight.setId(styleBasisName + currentDisplayForm.getName() + "Highlight");
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
				styleMapFootprint.setId(styleBasisName + currentDisplayForm.getName() + "Style");
				styleMapFootprint.getPair().add(pairFootprintNormal);
				styleMapFootprint.getPair().add(pairFootprintHighlight);

				marshaller.marshal(kmlFactory.createStyle(styleFootprintHighlight), saxWriter);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapFootprint), saxWriter);
			}

			break;

		case DisplayForm.GEOMETRY:

			boolean isBuilding = Building.STYLE_BASIS_NAME.equals(styleBasisName);
			boolean isBridge = Bridge.STYLE_BASIS_NAME.equals(styleBasisName); 
			boolean isTunnel = Tunnel.STYLE_BASIS_NAME.equals(styleBasisName); 

			indexOfDf = displayFormsForObjectType.indexOf(currentDisplayForm);
			String wallFillColor = Integer.toHexString(DisplayForm.DEFAULT_WALL_FILL_COLOR);
			String wallLineColor = Integer.toHexString(DisplayForm.DEFAULT_WALL_LINE_COLOR);
			String roofFillColor = Integer.toHexString(DisplayForm.DEFAULT_ROOF_FILL_COLOR);
			String roofLineColor = Integer.toHexString(DisplayForm.DEFAULT_ROOF_LINE_COLOR);
			if (indexOfDf != -1) {
				currentDisplayForm = displayFormsForObjectType.get(indexOfDf);
				if (currentDisplayForm.isSetRgba0()) {
					wallFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba0()));
				}
				if (currentDisplayForm.isSetRgba1()) {
					wallLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba1()));
				}
				if (currentDisplayForm.isSetRgba2()) {
					roofFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba2()));
				}
				if (currentDisplayForm.isSetRgba3()) {
					roofLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba3()));
				}
			}

			LineStyleType lineStyleWallNormal = kmlFactory.createLineStyleType();
			lineStyleWallNormal.setColor(hexStringToByteArray(wallLineColor));
			PolyStyleType polyStyleWallNormal = kmlFactory.createPolyStyleType();
			polyStyleWallNormal.setColor(hexStringToByteArray(wallFillColor));
			StyleType styleWallNormal = kmlFactory.createStyleType();

			styleWallNormal.setLineStyle(lineStyleWallNormal);
			styleWallNormal.setPolyStyle(polyStyleWallNormal);
			styleWallNormal.setBalloonStyle(balloonStyle);

			if (isBuilding)
				styleWallNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BUILDING_WALL_SURFACE).toString() + "Normal");
			else if (isBridge)
				styleWallNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BRIDGE_WALL_SURFACE).toString() + "Normal");
			else if (isTunnel)
				styleWallNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.TUNNEL_WALL_SURFACE).toString() + "Normal");
			else
				styleWallNormal.setId(styleBasisName + currentDisplayForm.getName() + "Normal");

			marshaller.marshal(kmlFactory.createStyle(styleWallNormal), saxWriter);

			if (isBuilding)
				styleWallNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BUILDING_GROUND_SURFACE).toString() + "Normal");
			else if (isBridge)
				styleWallNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BRIDGE_GROUND_SURFACE).toString() + "Normal");
			else if (isTunnel)
				styleWallNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.TUNNEL_GROUND_SURFACE).toString() + "Normal");

			marshaller.marshal(kmlFactory.createStyle(styleWallNormal), saxWriter);

			LineStyleType lineStyleRoofNormal = kmlFactory.createLineStyleType();
			lineStyleRoofNormal.setColor(hexStringToByteArray(roofLineColor));
			PolyStyleType polyStyleRoofNormal = kmlFactory.createPolyStyleType();
			polyStyleRoofNormal.setColor(hexStringToByteArray(roofFillColor));
			StyleType styleRoofNormal = kmlFactory.createStyleType();

			styleRoofNormal.setLineStyle(lineStyleRoofNormal);
			styleRoofNormal.setPolyStyle(polyStyleRoofNormal);
			styleRoofNormal.setBalloonStyle(balloonStyle);

			if (isBuilding)
				styleRoofNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BUILDING_ROOF_SURFACE).toString() + "Normal");
			else if (isBridge)
				styleRoofNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.BRIDGE_ROOF_SURFACE).toString() + "Normal");
			else if (isTunnel)
				styleRoofNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.TUNNEL_ROOF_SURFACE).toString() + "Normal");

			marshaller.marshal(kmlFactory.createStyle(styleRoofNormal), saxWriter);

			if (currentDisplayForm.isHighlightingEnabled()) {
				String highlightFillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
				String highlightLineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);

				if (currentDisplayForm.isSetRgba4()) {
					highlightFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba4()));
				}
				if (currentDisplayForm.isSetRgba5()) {
					highlightLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba5()));
				}

				LineStyleType lineStyleGeometryInvisible = kmlFactory.createLineStyleType();
				lineStyleGeometryInvisible.setColor(hexStringToByteArray("01" + highlightLineColor.substring(2)));
				PolyStyleType polyStyleGeometryInvisible = kmlFactory.createPolyStyleType();
				polyStyleGeometryInvisible.setColor(hexStringToByteArray("00" + highlightFillColor.substring(2)));
				StyleType styleGeometryInvisible = kmlFactory.createStyleType();
				styleGeometryInvisible.setId(styleBasisName + currentDisplayForm.getName() + "StyleInvisible");
				styleGeometryInvisible.setLineStyle(lineStyleGeometryInvisible);
				styleGeometryInvisible.setPolyStyle(polyStyleGeometryInvisible);
				styleGeometryInvisible.setBalloonStyle(balloonStyle);

				LineStyleType lineStyleGeometryHighlight = kmlFactory.createLineStyleType();
				lineStyleGeometryHighlight.setColor(hexStringToByteArray(highlightLineColor));
				PolyStyleType polyStyleGeometryHighlight = kmlFactory.createPolyStyleType();
				polyStyleGeometryHighlight.setColor(hexStringToByteArray(highlightFillColor));
				StyleType styleGeometryHighlight = kmlFactory.createStyleType();
				styleGeometryHighlight.setId(styleBasisName + currentDisplayForm.getName() + "StyleHighlight");
				styleGeometryHighlight.setLineStyle(lineStyleGeometryHighlight);
				styleGeometryHighlight.setPolyStyle(polyStyleGeometryHighlight);
				styleGeometryHighlight.setBalloonStyle(balloonStyle);

				PairType pairGeometryNormal = kmlFactory.createPairType();
				pairGeometryNormal.setKey(StyleStateEnumType.NORMAL);
				pairGeometryNormal.setStyleUrl("#" + styleGeometryInvisible.getId());
				PairType pairGeometryHighlight = kmlFactory.createPairType();
				pairGeometryHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
				pairGeometryHighlight.setStyleUrl("#" + styleGeometryHighlight.getId());
				StyleMapType styleMapGeometry = kmlFactory.createStyleMapType();
				styleMapGeometry.setId(styleBasisName + currentDisplayForm.getName() +"Style");
				styleMapGeometry.getPair().add(pairGeometryNormal);
				styleMapGeometry.getPair().add(pairGeometryHighlight);

				marshaller.marshal(kmlFactory.createStyle(styleGeometryInvisible), saxWriter);
				marshaller.marshal(kmlFactory.createStyle(styleGeometryHighlight), saxWriter);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapGeometry), saxWriter);
			}

			break;

		case DisplayForm.COLLADA:

			indexOfDf = displayFormsForObjectType.indexOf(currentDisplayForm);
			if (indexOfDf != -1) {
				currentDisplayForm = displayFormsForObjectType.get(indexOfDf);
				if (currentDisplayForm.isHighlightingEnabled()) {
					String highlightFillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
					String highlightLineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
					if (currentDisplayForm.isSetRgba4()) {
						highlightFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba4()));
					}
					if (currentDisplayForm.isSetRgba5()) {
						highlightLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba5()));
					}

					LineStyleType lineStyleColladaInvisible = kmlFactory.createLineStyleType();
					lineStyleColladaInvisible.setColor(hexStringToByteArray("01" + highlightLineColor.substring(2)));
					PolyStyleType polyStyleColladaInvisible = kmlFactory.createPolyStyleType();
					polyStyleColladaInvisible.setColor(hexStringToByteArray("00" + highlightFillColor.substring(2)));
					StyleType styleColladaInvisible = kmlFactory.createStyleType();
					styleColladaInvisible.setId(styleBasisName + currentDisplayForm.getName() + "StyleInvisible");
					styleColladaInvisible.setLineStyle(lineStyleColladaInvisible);
					styleColladaInvisible.setPolyStyle(polyStyleColladaInvisible);
					styleColladaInvisible.setBalloonStyle(balloonStyle);

					LineStyleType lineStyleColladaHighlight = kmlFactory.createLineStyleType();
					lineStyleColladaHighlight.setColor(hexStringToByteArray(highlightLineColor));
					PolyStyleType polyStyleColladaHighlight = kmlFactory.createPolyStyleType();
					polyStyleColladaHighlight.setColor(hexStringToByteArray(highlightFillColor));
					StyleType styleColladaHighlight = kmlFactory.createStyleType();
					styleColladaHighlight.setId(styleBasisName + currentDisplayForm.getName() + "StyleHighlight");
					styleColladaHighlight.setLineStyle(lineStyleColladaHighlight);
					styleColladaHighlight.setPolyStyle(polyStyleColladaHighlight);
					styleColladaHighlight.setBalloonStyle(balloonStyle);

					PairType pairColladaNormal = kmlFactory.createPairType();
					pairColladaNormal.setKey(StyleStateEnumType.NORMAL);
					pairColladaNormal.setStyleUrl("#" + styleColladaInvisible.getId());
					PairType pairColladaHighlight = kmlFactory.createPairType();
					pairColladaHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
					pairColladaHighlight.setStyleUrl("#" + styleColladaHighlight.getId());
					StyleMapType styleMapCollada = kmlFactory.createStyleMapType();
					styleMapCollada.setId(styleBasisName + currentDisplayForm.getName() +"Style");
					styleMapCollada.getPair().add(pairColladaNormal);
					styleMapCollada.getPair().add(pairColladaHighlight);

					marshaller.marshal(kmlFactory.createStyle(styleColladaInvisible), saxWriter);
					marshaller.marshal(kmlFactory.createStyle(styleColladaHighlight), saxWriter);
					marshaller.marshal(kmlFactory.createStyleMap(styleMapCollada), saxWriter);
				}
			}
			break;

		default:
			// no style
			break;
		}
	}

	private void addBorder(GeometryObject tile, StyleType style, SAXWriter saxWriter) throws JAXBException {
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		double[] coords = tile.getCoordinates(0);
		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName("Tile border");
		LineStringType lineString = kmlFactory.createLineStringType();
		lineString.setTessellate(true);
		lineString.getCoordinates().add(coords[0] + "," + coords[1]);
		lineString.getCoordinates().add(coords[2] + "," + coords[3]);
		lineString.getCoordinates().add(coords[4] + "," + coords[5]);
		lineString.getCoordinates().add(coords[6] + "," + coords[7]);
		lineString.getCoordinates().add(coords[8] + "," + coords[9]);
		placemark.setAbstractGeometryGroup(kmlFactory.createLineString(lineString));

		if (style != null) {
			placemark.setStyleUrl("#" + style.getId());
			marshaller.marshal(kmlFactory.createStyle(style), saxWriter);
		}

		marshaller.marshal(kmlFactory.createPlacemark(placemark), saxWriter);
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

	private boolean checkBalloonSettings(CityGMLClass cityObjectType) {
		Balloon[] balloonSettings = null;
		boolean settingsMustBeChecked = false;
		switch (cityObjectType) {
		case BUILDING:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getBuildingBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetBuilding();
			break;
		case WATER_BODY:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getWaterBodyBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetWaterBody();
			break;
		case LAND_USE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getLandUseBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetLandUse();
			break;
		case SOLITARY_VEGETATION_OBJECT:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getVegetationBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetVegetation();
			break;
		case TRANSPORTATION_COMPLEX:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getTransportationBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetTransportation();
			break;
		case RELIEF_FEATURE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getReliefBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetReliefFeature();
			break;
		case CITY_FURNITURE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getCityFurnitureBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetCityFurniture();
			break;
		case GENERIC_CITY_OBJECT:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getGenericCityObject3DBalloon(),
					config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getPointBalloon(),
					config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetGenericCityObject();
			break;
		case CITY_OBJECT_GROUP:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getCityObjectGroupBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetCityObjectGroup();
			break;
		case BRIDGE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getBridgeBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetBridge();
			break;
		case TUNNEL:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getTunnelBalloon()};
			settingsMustBeChecked = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass().isSetTunnel();
			break;
		default:
			return false;
		}

		boolean success = true;
		for (Balloon balloon: balloonSettings) {
			if (settingsMustBeChecked &&
					balloon.isIncludeDescription() &&
					balloon.getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
				String balloonTemplateFilename = balloon.getBalloonContentTemplateFile();
				if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
					File ballonTemplateFile = new File(balloonTemplateFilename);
					if (!ballonTemplateFile.exists()) {
						LOG.error("Balloon template file \"" + balloonTemplateFilename + "\" not found.");
						success = false;
					}
				}
			}
		}
		return success;
	}

	private static void getAllFiles(File startFolder, List<File> fileList) {
		File[] files = startFolder.listFiles();
		for (File file : files) {
			fileList.add(file);
			if (file.isDirectory())
				getAllFiles(file, fileList);
		}
	}

	private static void deleteFolder(File folder) {
		if (folder == null) return;
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f: files) {
				if (f.isDirectory())
					deleteFolder(f);
				else
					f.delete();
			}
		}
		folder.delete();
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.FEATURE_COUNTER) {
			HashMap<CityGMLClass, Long> counterMap = ((FeatureCounterEvent)e).getCounter();

			for (CityGMLClass type : counterMap.keySet()) {
				Long counter = featureCounterMap.get(type);
				Long update = counterMap.get(type);

				if (counter == null)
					featureCounterMap.put(type, update);
				else
					featureCounterMap.put(type, counter + update);

				counter = totalFeatureCounterMap.get(type);
				if (counter == null)
					totalFeatureCounterMap.put(type, update);
				else
					totalFeatureCounterMap.put(type, counter + update);
			}
		}
		else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			geometryCounter++;
		}
		else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;
				InterruptEvent interruptEvent = (InterruptEvent)e;

				if (interruptEvent.getCause() != null) {
					Throwable cause = interruptEvent.getCause();

					if (cause instanceof SQLException) {
						Iterator<Throwable> iter = ((SQLException)cause).iterator();
						LOG.error("A SQL error occured: " + iter.next().getMessage());
						while (iter.hasNext())
							LOG.error("Cause: " + iter.next().getMessage());
					} else {
						LOG.error("An error occured: " + cause.getMessage());
						while ((cause = cause.getCause()) != null)
							LOG.error("Cause: " + cause.getMessage());
					}
				}

				String log = interruptEvent.getLogMessage();
				if (log != null)
					LOG.log(interruptEvent.getLogLevelType(), log);

				LOG.info("Waiting for objects being currently processed to end...");

				if (kmlSplitter != null)
					kmlSplitter.shutdown();

				if (kmlWorkerPool != null)
					kmlWorkerPool.drainWorkQueue();

				if (lastTempFolder != null && lastTempFolder.exists()) deleteFolder(lastTempFolder); // just in case
			}
		}
	}
}
