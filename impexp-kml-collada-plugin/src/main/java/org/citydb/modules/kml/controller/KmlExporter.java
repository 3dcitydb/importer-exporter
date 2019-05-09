/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
import org.citydb.ade.ADEExtensionManager;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.SingleWorkerPool;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.kmlExporter.AltitudeOffsetMode;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.BalloonContentMode;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.KmlTilingOptions;
import org.citydb.config.project.kmlExporter.PointAndCurve;
import org.citydb.config.project.kmlExporter.PointDisplayMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogTitle;
import org.citydb.log.Logger;
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
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.config.ConfigQueryBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.query.filter.tiling.Tile;
import org.citydb.query.filter.tiling.Tiling;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.util.ClientConstants;
import org.citydb.util.Util;
import org.citydb.writer.XMLWriterWorkerFactory;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KmlExporter implements EventHandler {
	private final Logger log = Logger.getInstance();

	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private ObjectFactory kmlFactory; 
	private WorkerPool<KmlSplittingResult> kmlWorkerPool;
	private SingleWorkerPool<SAXEventBuffer> writerPool;
	private KmlSplitter kmlSplitter;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private final String ENCODING = "UTF-8";
	private final Charset CHARSET = Charset.forName(ENCODING);
	private final String TEMP_FOLDER = "__temp";
	private File lastTempFolder = null;

	private Map<Integer, Long> objectCounter = new HashMap<>();
	private Map<Integer, Long> totalObjectCounter = new HashMap<>();
	private long geometryCounter;

	public KmlExporter (JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			SchemaMapping schemaMapping,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.schemaMapping = schemaMapping;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		kmlFactory = new ObjectFactory();
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() throws KmlExportException {
		// adding listener
		eventDispatcher.addEventHandler(EventType.OBJECT_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// checking workspace
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace();
		if (shouldRun && databaseAdapter.hasVersioningSupport() && 
				!databaseAdapter.getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName()) &&
				!databaseAdapter.getWorkspaceManager().existsWorkspace(workspace, true))
			return false;

		// check API key when using the elevation API
		if (config.getProject().getKmlExporter().getAltitudeOffsetMode() == AltitudeOffsetMode.GENERIC_ATTRIBUTE
			&& config.getProject().getKmlExporter().isCallGElevationService()
			&& !config.getProject().getGlobal().getApiKeys().isSetGoogleElevation()) {
			log.error("The Google Elevation API cannot be used due to a missing API key.");
			log.error("Please enter an API key or change the export preferences.");
			return false;
		}

		// check whether spatial indexes are enabled
		log.info("Checking for spatial indexes on geometry columns of involved tables...");
		try {
			if (!databaseAdapter.getUtil().isIndexEnabled("CITYOBJECT", "ENVELOPE") ||
					!databaseAdapter.getUtil().isIndexEnabled("SURFACE_GEOMETRY", "GEOMETRY")) {
				log.error("Spatial indexes are not activated.");
				log.error("Please use the database tab to activate the spatial indexes.");
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
						if (!databaseAdapter.getUtil().getAppearanceThemeList(workspace).contains(selectedTheme)) {
							log.error("Database does not contain appearance theme \"" + selectedTheme + "\"");
							return false;
						}
					}
				}
			} catch (SQLException e) {
				throw new KmlExportException("Generic database error.", e);
			}
		}

		// check collada2gltf tool
		if (config.getProject().getKmlExporter().isCreateGltfModel()) {
			Path collada2gltf = Paths.get(config.getProject().getKmlExporter().getPathOfGltfConverter());
			if (!collada2gltf.isAbsolute())
				collada2gltf = ClientConstants.IMPEXP_HOME.resolve(collada2gltf);

			if (!Files.exists(collada2gltf))
				throw new KmlExportException("Failed to find the COLLADA2glTF tool at the provided path " + collada2gltf + ".");
			else if (!Files.isExecutable(collada2gltf))
				throw new KmlExportException("Failed to execute the COLLADA2glTF tool at " + collada2gltf + ".");
		}

		// build query from filter settings
		Query query = null;
		try {
			ConfigQueryBuilder queryBuilder = new ConfigQueryBuilder(schemaMapping, databaseAdapter);
			query = queryBuilder.buildQuery(config.getProject().getKmlExporter().getQuery(), config.getProject().getNamespaceFilter());
		} catch (QueryBuildException e) {
			throw new KmlExportException("Failed to build the export filter expression.", e);
		}

		// tiling
		Tiling tiling = query.getTiling();
		KmlTilingOptions tilingOptions = null;
		Predicate predicate = null;
		boolean useTiling = query.isSetTiling();
		int remainingTiles = 1;
		int rows = useTiling ? tiling.getRows() : 1;  
		int columns = useTiling ? tiling.getColumns() : 1;

		if (useTiling) {
			try {
				// transform tiling extent to WGS84
				tiling.transformExtent(Database.PREDEFINED_SRS.get(Database.PredefinedSrsName.WGS84_2D), databaseAdapter);
				tilingOptions = tiling.getTilingOptions() instanceof KmlTilingOptions ? (KmlTilingOptions)tiling.getTilingOptions() : new KmlTilingOptions();
				predicate = query.isSetSelection() ? query.getSelection().getPredicate() : null;

				// calculate and display number of tiles to be exported
				int displayFormats = config.getProject().getKmlExporter().getActiveDisplayFormsAmount(config.getProject().getKmlExporter().getBuildingDisplayForms());
				remainingTiles = rows * columns * displayFormats;
				log.info(remainingTiles + " (" + rows + "x" + columns + "x" + displayFormats + ") tiles will be generated.");	
			} catch (FilterException e) {
				throw new KmlExportException("Failed to transform tiling extent.", e);
			}
		}

		// check whether the Balloon template files existed, if not, error message will be printed out: file not found! 
		boolean balloonCheck = checkBalloonSettings(CityGMLClass.BUILDING, query);
		balloonCheck = checkBalloonSettings(CityGMLClass.WATER_BODY, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.LAND_USE, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.SOLITARY_VEGETATION_OBJECT, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.TRANSPORTATION_COMPLEX, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.RELIEF_FEATURE, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.CITY_FURNITURE, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.GENERIC_CITY_OBJECT, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.CITY_OBJECT_GROUP, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.BRIDGE, query) && balloonCheck;
		balloonCheck = checkBalloonSettings(CityGMLClass.TUNNEL, query) && balloonCheck;
		if (!balloonCheck) 
			return false;	

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
		String path = config.getInternal().getExportFile().toAbsolutePath().normalize().toString();
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
		if (useTiling) {
			try {
				masterFileWriter = writeMasterFileHeader(fileName, path, query);
			} catch (JAXBException | IOException | SAXException e) {
				throw new KmlExportException("Failed to write KML master file header.", e);
			}
		}

		// start writing cityobject JSON file if required
		FileOutputStream jsonFileWriter = null;
		boolean jsonHasContent = false;
		if (config.getProject().getKmlExporter().isWriteJSONFile() && useTiling) {
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
		
		if (!ADEExtensionManager.getInstance().getEnabledExtensions().isEmpty())
			log.warn("NOTE: This operation does not work on ADE features.");

		long start = System.currentTimeMillis();

		// iterate over tiles
		for (int i = 0; shouldRun && i < rows; i++) {
			for (int j = 0; shouldRun && j < columns; j++) {

				// track exported objects
				ExportTracker tracker = new ExportTracker();

				// set active tile and get tile extent in WGS84
				Tile tile = null;
				if (useTiling) {
					try {
						tile = tiling.getTileAt(i, j);
						tiling.setActiveTile(tile);

						Predicate bboxFilter = tile.getFilterPredicate(databaseAdapter);
						if (predicate != null)
							query.setSelection(new SelectionFilter(LogicalOperationFactory.AND(predicate, bboxFilter)));
						else
							query.setSelection(new SelectionFilter(bboxFilter));
					} catch (FilterException e) {
						if (jsonFileWriter != null) try { jsonFileWriter.close(); } catch (IOException ioe) { }
						throw new KmlExportException("Failed to get tile at [" + i + "," + j + "].", e);
					}
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
						if (useTiling) {
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
						writerPool = new SingleWorkerPool<SAXEventBuffer>(
								"kml_writer_pool",
								new XMLWriterWorkerFactory(saxWriter, eventDispatcher),
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
										schemaMapping,
										writerPool,
										tracker,
										query,
										kmlFactory,
										config,
										eventDispatcher),
								300,
								false);

						// prestart pool workers
						writerPool.prestartCoreWorkers();
						kmlWorkerPool.prestartCoreWorkers();

						// fail if we could not start a single import worker
						if (kmlWorkerPool.getPoolSize() == 0)
							throw new KmlExportException("Failed to start database export worker pool. Check the database connection pool settings.");

						// create file header writer
						SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(kmlFactory.createDocument(null).getName(), saxWriter);

						// ok, preparations done. inform user...
						log.info("Exporting to file: " + file.getAbsolutePath());

						// create kml root element
						KmlType kmlType = kmlFactory.createKmlType();
						JAXBElement<KmlType> kml = kmlFactory.createKml(kmlType);

						DocumentType document = kmlFactory.createDocumentType();
						if (useTiling)
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

							if (useTiling && config.getProject().getKmlExporter().isShowTileBorders())
								addBorder(tile.getExtent(), null, saxWriter);

						} catch (JAXBException e) {
							throw new KmlExportException("Failed to write output file.", e);
						}

						// get database splitter and start query
						try {
							kmlSplitter = new KmlSplitter(
									schemaMapping,
									kmlWorkerPool,
									query,
									displayForm,
									config);

							if (shouldRun)
								kmlSplitter.startQuery();
						} catch (SQLException | QueryBuildException | FilterException e) {
							throw new KmlExportException("Failed to query the database.", e);
						}

						// shutdown worker pools
						try {
							kmlWorkerPool.shutdownAndWait();
							writerPool.shutdownAndWait();
						} catch (InterruptedException e) {
							throw new KmlExportException("Failed to shutdown worker pools.", e);
						}

						try {
							// add styles
							if (!objectCounter.isEmpty() &&
									(!config.getProject().getKmlExporter().isOneFilePerObject() || !useTiling)) {
								for (int objectClassId : objectCounter.keySet()) {
									if (objectCounter.get(objectClassId) > 0)
										addStyle(displayForm, objectClassId, saxWriter);
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
							if (!objectCounter.isEmpty()) {
								saxWriter.flush();
								if (config.getProject().getKmlExporter().isExportAsKmz()) {
									zipOut.closeEntry();

									List<File> filesToZip = new ArrayList<File>();
									File tempFolder = new File(currentWorkingDirectoryPath, TEMP_FOLDER);
									lastTempFolder = tempFolder;
									int indexOfZipFilePath = tempFolder.getCanonicalPath().length() + 1;

									if (tempFolder.exists()) { // !config.getProject().getKmlExporter().isOneFilePerObject()
										log.info("Zipping to kmz archive from temporary folder...");
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
										log.info("Removing temporary folder...");
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
						if (useTiling && objectCounter.isEmpty() && !config.getProject().getKmlExporter().isExportEmptyTiles()) {
							log.debug("Tile_" + i + "_" + j + " is empty. Deleting file " + file.getName() + ".");
							file.delete();
						}

						eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.finish.msg"), this));
					} finally {
						// clean up
						if (writerPool != null && !writerPool.isTerminated())
							writerPool.shutdownNow();

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
				if (masterFileWriter != null && !objectCounter.isEmpty()) {
					try {
						writeMasterFileTileReference(fileName, tile, tilingOptions, masterFileWriter);
					} catch (JAXBException e) {
						if (jsonFileWriter != null) try { jsonFileWriter.close(); } catch (IOException ioe) { }
						throw new KmlExportException("Failed to write tile reference to master file.", e);
					}
				}

				// fill cityobject JSON file after tile has been processed
				if (jsonFileWriter != null && !objectCounter.isEmpty()) {
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

				objectCounter.clear();
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
		if (useTiling) {
			try {
				writeMasterJsonFileTileReference(path, fileName, fileExtension, tiling);
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
		if (!totalObjectCounter.isEmpty()) {
			log.info("Exported city objects:");
			Map<String, Long> typeNames = Util.mapObjectCounter(totalObjectCounter, schemaMapping);					
			typeNames.keySet().stream().sorted().forEach(object -> log.info(object + ": " + typeNames.get(object)));
		}

		log.info("Processed geometry objects: " + geometryCounter);

		if (lastTempFolder != null && lastTempFolder.exists()) 
			deleteFolder(lastTempFolder); // just in case

		if (shouldRun)
			log.info("Total export time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	private SAXWriter writeMasterFileHeader(String fileName, String path, Query query) throws JAXBException, IOException, SAXException {
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

		BoundingBox extent = query.getTiling().getExtent();
		lookAtType.setLongitude(extent.getLowerCorner().getX() + Math.abs((extent.getUpperCorner().getX() - extent.getLowerCorner().getX())/2));
		lookAtType.setLatitude(extent.getLowerCorner().getY() + Math.abs((extent.getUpperCorner().getY() - extent.getLowerCorner().getY())/2));
		lookAtType.setAltitude(0.0);
		lookAtType.setHeading(0.0);
		lookAtType.setTilt(60.0);
		lookAtType.setRange(970.0);
		document.setAbstractViewGroup(kmlFactory.createLookAt(lookAtType));
		kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

		fragmentWriter.setWriteMode(WriteMode.HEAD);
		marshaller.marshal(kml, fragmentWriter);				

		if (config.getProject().getKmlExporter().isOneFilePerObject()) {
			for (FeatureType featureType : query.getFeatureTypeFilter().getFeatureTypes()) {
				int objectClassId = featureType.getObjectClassId();
				
				switch (Util.getCityGMLClass(objectClassId)) {
				case BUILDING:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case CITY_FURNITURE:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getCityFurnitureDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case CITY_OBJECT_GROUP:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getCityObjectGroupDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case GENERIC_CITY_OBJECT:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getGenericCityObjectDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case LAND_USE:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getLandUseDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case RELIEF_FEATURE:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getReliefDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case TRANSPORTATION_COMPLEX:
				case TRACK:
				case RAILWAY:
				case ROAD:
				case SQUARE:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getTransportationDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case SOLITARY_VEGETATION_OBJECT:
				case PLANT_COVER:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getVegetationDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case WATER_BODY:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getWaterBodyDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case BRIDGE:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getBridgeDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				case TUNNEL:
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getTunnelDisplayForms())
						addStyle(displayForm, objectClassId, saxWriter);
					break;
				default:
					break;
				}
			}
		}

		if (config.getProject().getKmlExporter().isShowBoundingBox()) {
			StyleType style = kmlFactory.createStyleType();
			style.setId("frameStyle");
			LineStyleType frameLineStyleType = kmlFactory.createLineStyleType();
			frameLineStyleType.setWidth(4.0);
			style.setLineStyle(frameLineStyleType);

			addBorder(extent, style, saxWriter);
		}

		return saxWriter;
	}

	private void writeMasterFileTileReference(String tileName, Tile tile, KmlTilingOptions tilingOptions, SAXWriter saxWriter) throws JAXBException {
		if (tile == null)
			return;

		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		// tileName should not contain special characters,
		// since it will be used as filename for all displayForm files
		tileName = tileName + "_Tile_" + tile.getX() + "_" + tile.getY();

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

			BoundingBox extent = tile.getExtent();	
			LatLonAltBoxType latLonAltBoxType = kmlFactory.createLatLonAltBoxType();			
			latLonAltBoxType.setNorth(extent.getUpperCorner().getY());
			latLonAltBoxType.setSouth(extent.getLowerCorner().getY());
			latLonAltBoxType.setEast(extent.getUpperCorner().getX());
			latLonAltBoxType.setWest(extent.getLowerCorner().getX());

			LodType lodType = kmlFactory.createLodType();
			lodType.setMinLodPixels((double)displayForm.getVisibleFrom());
			lodType.setMaxLodPixels((double)displayForm.getVisibleUpTo());

			regionType.setLatLonAltBox(latLonAltBoxType);
			regionType.setLod(lodType);

			LinkType linkType = kmlFactory.createLinkType();
			linkType.setHref("Tiles/" + tile.getX() + "/" + tile.getY() + "/" + tilenameForDisplayForm);
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

	private void writeMasterJsonFileTileReference(String path, String fileName, String fileExtension, Tiling tiling) throws IOException {
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
				jsonFileWriterForMasterFile.write(("\n\t\"" + "colnum" + "\": " + tiling.getColumns() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "rownum" + "\": " + tiling.getRows() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\"" + "bbox" + "\":{ ").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "xmin" + "\": " + tiling.getExtent().getLowerCorner().getX() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "xmax" + "\": " + tiling.getExtent().getUpperCorner().getX() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "ymin" + "\": " + tiling.getExtent().getLowerCorner().getY() + ",").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t\t\"" + "ymax" + "\": " + tiling.getExtent().getUpperCorner().getY()).getBytes(CHARSET));
				jsonFileWriterForMasterFile.write(("\n\t}").getBytes(CHARSET));
				jsonFileWriterForMasterFile.write("\n}\n".getBytes(CHARSET));				
				jsonFileWriterForMasterFile.close();
			}							
		}		
	}

	private void addStyle(DisplayForm currentDisplayForm, int objectClassId, SAXWriter saxWriter) throws JAXBException {
		if (!currentDisplayForm.isActive()) return;
		switch (Util.getCityGMLClass(objectClassId)) {
		case SOLITARY_VEGETATION_OBJECT:
		case PLANT_COVER:
			addStyle(currentDisplayForm,
					config.getProject().getKmlExporter().getVegetationDisplayForms(),
					SolitaryVegetationObject.STYLE_BASIS_NAME,
					saxWriter);
			break;

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

	private void addBorder(BoundingBox tile, StyleType style, SAXWriter saxWriter) throws JAXBException {
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		PlacemarkType placemark = kmlFactory.createPlacemarkType();
		placemark.setName("Tile border");
		LineStringType lineString = kmlFactory.createLineStringType();
		lineString.setTessellate(true);
		lineString.getCoordinates().add(tile.getLowerCorner().getX() + "," + tile.getLowerCorner().getY());
		lineString.getCoordinates().add(tile.getUpperCorner().getX() + "," + tile.getLowerCorner().getY());
		lineString.getCoordinates().add(tile.getUpperCorner().getX() + "," + tile.getUpperCorner().getY());
		lineString.getCoordinates().add(tile.getLowerCorner().getX() + "," + tile.getUpperCorner().getY());
		lineString.getCoordinates().add(tile.getLowerCorner().getX() + "," + tile.getLowerCorner().getY());
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
			log.logStackTrace(e);
			return null;
		}
		return bytes;
	}

	private boolean checkBalloonSettings(CityGMLClass cityObjectType, Query query) {
		FeatureTypeFilter typeFilter = query.getFeatureTypeFilter();
		Balloon[] balloonSettings = null;
		switch (cityObjectType) {
		case BUILDING:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getBuildingBalloon()};
			break;
		case WATER_BODY:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getWaterBodyBalloon()};
			break;
		case LAND_USE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getLandUseBalloon()};
			break;
		case SOLITARY_VEGETATION_OBJECT:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getVegetationBalloon()};
			break;
		case TRANSPORTATION_COMPLEX:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getTransportationBalloon()};
			break;
		case RELIEF_FEATURE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getReliefBalloon()};
			break;
		case CITY_FURNITURE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getCityFurnitureBalloon()};
			break;
		case GENERIC_CITY_OBJECT:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getGenericCityObject3DBalloon(),
					config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getPointBalloon(),
					config.getProject().getKmlExporter().getGenericCityObjectPointAndCurve().getCurveBalloon()};
			break;
		case CITY_OBJECT_GROUP:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getCityObjectGroupBalloon()};
			break;
		case BRIDGE:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getBridgeBalloon()};
			break;
		case TUNNEL:
			balloonSettings = new Balloon[]{config.getProject().getKmlExporter().getTunnelBalloon()};
			break;
		default:
			return false;
		}

		Class<? extends CityGML> typeClass = cityObjectType.getModelClass();
		FeatureType featureType = AbstractFeature.class.isAssignableFrom(typeClass) ? 
				schemaMapping.getFeatureType(Util.getObjectClassId(typeClass.asSubclass(AbstractFeature.class))) : null;
		
		boolean settingsMustBeChecked = featureType != null ? typeFilter.containsFeatureType(featureType) : false;

		boolean success = true;
		for (Balloon balloon: balloonSettings) {
			if (settingsMustBeChecked &&
					balloon.isIncludeDescription() &&
					balloon.getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
				String balloonTemplateFilename = balloon.getBalloonContentTemplateFile();
				if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
					File ballonTemplateFile = new File(balloonTemplateFilename);
					if (!ballonTemplateFile.exists()) {
						log.error("Balloon template file \"" + balloonTemplateFilename + "\" not found.");
						success = false;
					}
				}
			}
		}
		return success;
	}

	private void getAllFiles(File startFolder, List<File> fileList) {
		File[] files = startFolder.listFiles();
		for (File file : files) {
			fileList.add(file);
			if (file.isDirectory())
				getAllFiles(file, fileList);
		}
	}

	private void deleteFolder(File folder) {
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
		if (e.getEventType() == EventType.OBJECT_COUNTER) {
			Map<Integer, Long> counter = ((ObjectCounterEvent)e).getCounter();
			
			for (Entry<Integer, Long> entry : counter.entrySet()) {
				Long tmp = objectCounter.get(entry.getKey());
				objectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
				
				tmp = totalObjectCounter.get(entry.getKey());
				totalObjectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
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
						log.error("A SQL error occurred: " + iter.next().getMessage());
						while (iter.hasNext())
							log.error("Cause: " + iter.next().getMessage());
					} else {
						log.error("An error occurred: " + cause.getMessage());
						while ((cause = cause.getCause()) != null)
							log.error(cause.getClass().getTypeName() + ": " + cause.getMessage());
					}
				}

				String msg = interruptEvent.getLogMessage();
				if (msg != null)
					log.log(interruptEvent.getLogLevelType(), msg);

				log.info("Waiting for objects being currently processed to end...");

				if (kmlSplitter != null)
					kmlSplitter.shutdown();

				if (kmlWorkerPool != null)
					kmlWorkerPool.drainWorkQueue();

				if (lastTempFolder != null && lastTempFolder.exists()) deleteFolder(lastTempFolder); // just in case
			}
		}
	}
}
