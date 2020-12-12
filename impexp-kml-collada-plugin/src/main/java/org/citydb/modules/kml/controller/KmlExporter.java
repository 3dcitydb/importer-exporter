/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

import net.opengis.kml._2.*;
import org.citydb.ade.kmlExporter.ADEKmlExportExtensionManager;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.SingleWorkerPool;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.exception.ErrorCode;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.kmlExporter.*;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.*;
import org.citydb.log.Logger;
import org.citydb.modules.kml.concurrent.KmlExportWorkerFactory;
import org.citydb.modules.kml.database.*;
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
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
import org.citydb.util.Util;
import org.citydb.writer.XMLWriterWorkerFactory;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;
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
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KmlExporter implements EventHandler {
	private final Logger log = Logger.getInstance();
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private final AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private final ObjectFactory kmlFactory;
	private final Map<Integer, Long> objectCounter = new HashMap<>();
	private final Map<Integer, Long> totalObjectCounter = new HashMap<>();

	private JAXBContext jaxbKmlContext;
	private WorkerPool<KmlSplittingResult> kmlWorkerPool;
	private SingleWorkerPool<SAXEventBuffer> writerPool;
	private KmlSplitter kmlSplitter;

	private final String ENCODING = "UTF-8";
	private final Charset CHARSET = Charset.forName(ENCODING);
	private File lastTempFolder = null;
	private long geometryCounter;

	private volatile boolean shouldRun = true;
	private KmlExportException exception;

	public KmlExporter() {
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		config = ObjectRegistry.getInstance().getConfig();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		kmlFactory = new ObjectFactory();
	}

	public boolean doExport(Path outputFile) throws KmlExportException {
		if (outputFile == null || outputFile.getFileName() == null) {
			throw new KmlExportException("The output file '" + outputFile + "' is invalid.");
		}

		eventDispatcher.addEventHandler(EventType.OBJECT_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		try {
			return process(outputFile);
		} finally {
			eventDispatcher.removeEventHandler(this);
		}
	}

	private boolean process(Path outputFile) throws KmlExportException {
		// get JAXB contexts for KML and COLLADA
		JAXBContext jaxbColladaContext;
		try {
			log.debug("Initializing KML/COLLADA context.");
			jaxbKmlContext = getKmlContext();
			jaxbColladaContext = getColladaContext();
		} catch (JAXBException e) {
			throw new KmlExportException("Failed to initialize KML/COLLADA context.", e);
		}

		// checking workspace
		Workspace workspace = config.getDatabaseConfig().getWorkspaces().getKmlExportWorkspace();
		if (shouldRun && databaseAdapter.hasVersioningSupport()
				&& !databaseAdapter.getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName())) {
			try {
				log.info("Switching to database workspace " + workspace + ".");
				databaseAdapter.getWorkspaceManager().checkWorkspace(workspace);
			} catch (SQLException e) {
				throw new KmlExportException("Failed to switch to database workspace.", e);
			}
		}

		// check API key when using the elevation API
		if (config.getKmlExportConfig().getElevation().getAltitudeOffsetMode() == AltitudeOffsetMode.GENERIC_ATTRIBUTE
				&& config.getKmlExportConfig().getElevation().isCallGElevationService()
				&& !config.getGlobalConfig().getApiKeys().isSetGoogleElevation()) {
			throw new KmlExportException(ErrorCode.MISSING_GOOGLE_API_KEY, "The Google Elevation API cannot be used due to a missing API key.");
		}

		// check whether spatial indexes are enabled
		log.info("Checking for spatial indexes on geometry columns of involved tables...");
		try {
			if (!databaseAdapter.getUtil().isIndexEnabled("CITYOBJECT", "ENVELOPE")
					|| !databaseAdapter.getUtil().isIndexEnabled("SURFACE_GEOMETRY", "GEOMETRY")) {
				throw new KmlExportException(ErrorCode.SPATIAL_INDEXES_NOT_ACTIVATED, "Spatial indexes are not activated.");
			}
		} catch (SQLException e) {
			throw new KmlExportException("Failed to retrieve status of spatial indexes.", e);
		}

		// check whether the selected theme existed in the database, just for Building Class...
		String selectedTheme = config.getKmlExportConfig().getAppearanceTheme();
		if (!selectedTheme.equals(KmlExportConfig.THEME_NONE)) {
			try {
				DisplayForm displayForm = config.getKmlExportConfig().getDisplayForms().get(DisplayFormType.COLLADA);
				if (displayForm != null
						&& displayForm.isActive()
						&& !databaseAdapter.getUtil().getAppearanceThemeList(workspace).contains(selectedTheme)) {
					throw new KmlExportException("The database does not contain the appearance theme '" + selectedTheme + "'.");
				}
			} catch (SQLException e) {
				throw new KmlExportException("Failed to check the appearance theme.", e);
			}
		}

		// check gltf options
		if (config.getKmlExportConfig().getGltfOptions().isCreateGltfModel()) {
			// check collada2gltf converter tool
			Path collada2gltf = Paths.get(config.getKmlExportConfig().getGltfOptions().getPathToConverter());
			if (!collada2gltf.isAbsolute())
				collada2gltf = ClientConstants.IMPEXP_HOME.resolve(collada2gltf);

			if (!Files.exists(collada2gltf))
				throw new KmlExportException("Failed to find the COLLADA2glTF tool at the provided path " + collada2gltf + ".");
			else if (!Files.isExecutable(collada2gltf))
				throw new KmlExportException("Failed to execute the COLLADA2glTF tool at " + collada2gltf + ".");

			// check whether we have to deactivate KMZ
			if (config.getKmlExportConfig().isExportAsKmz()) {
				log.warn("glTF export cannot be used with KMZ compression. Deactivating KMZ.");
				config.getKmlExportConfig().setExportAsKmz(false);
			}
		}

		// build query from filter settings
		Query query;
		try {
			ConfigQueryBuilder queryBuilder = new ConfigQueryBuilder(schemaMapping, databaseAdapter);
			SimpleKmlQuery queryConfig = config.getKmlExportConfig().getQuery();
			query = queryBuilder.buildQuery(queryConfig, config.getNamespaceFilter());

			// calculate extent if the bbox filter is disabled
			KmlTiling spatialFilter = queryConfig.getSpatialFilter();
			if (!spatialFilter.isSetExtent()) {
				try {
					spatialFilter.setExtent(databaseAdapter.getUtil().calcBoundingBox(workspace, query, schemaMapping));
					query = queryBuilder.buildQuery(queryConfig, config.getNamespaceFilter());
				} catch (SQLException | FilterException e) {
					throw new QueryBuildException("Failed to calculate bounding box based on the non-spatial filter settings.", e);
				}
			}
		} catch (QueryBuildException e) {
			throw new KmlExportException("Failed to build the export filter expression.", e);
		}
		Predicate predicate = query.isSetSelection() ? query.getSelection().getPredicate() : null;

		// check whether CityGML features can be exported from LoD 0
		if (config.getKmlExportConfig().getLodToExportFrom() == 0) {
			FeatureTypeFilter featureTypeFilter = query.getFeatureTypeFilter();
			for (FeatureType featureType : featureTypeFilter.getFeatureTypes()) {
				String namespace = featureType.getSchema().getNamespace(CityGMLVersion.v2_0_0).getURI();
				if (Modules.isCityGMLModuleNamespace(namespace) && !featureType.isAvailableForLod(0)) {
					log.warn(featureType + " cannot be exported from LoD 0 and will be skipped.");
					featureTypeFilter.removeFeatureType(featureType);
				}
			}

			if (featureTypeFilter.isEmpty()) {
				throw new KmlExportException("No valid feature types provided for LoD 0.");
			}
		}

		// tiling
		Tiling tiling = query.getTiling();
		boolean useTiling = query.isSetTiling();
		int rows = useTiling ? tiling.getRows() : 1;
		int columns = useTiling ? tiling.getColumns() : 1;
		if (!useTiling) {
			try {
				tiling = new Tiling(config.getKmlExportConfig().getQuery().getSpatialFilter().getExtent(), rows, columns);
			} catch (FilterException e) {
				throw new KmlExportException("Failed to build the internal fallback tiling.", e);
			}
		}

		// transform tiling extent to WGS84
		try {
			tiling.transformExtent(DatabaseConfig.PREDEFINED_SRS.get(DatabaseConfig.PredefinedSrsName.WGS84_2D), databaseAdapter);
		} catch (FilterException e) {
			throw new KmlExportException("Failed to transform tiling extent.", e);
		}

		// calculate and display number of tiles to be exported
		int displayFormats = config.getKmlExportConfig().getDisplayForms().getActiveDisplayFormsAmount();
		int remainingTiles = rows * columns * displayFormats;
		log.info(remainingTiles + " (" + rows + "x" + columns + "x" + displayFormats + ") tiles will be generated.");

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
		String path = outputFile.toAbsolutePath().normalize().toString();
		String fileExtension = config.getKmlExportConfig().isExportAsKmz() ? ".kmz" : ".kml";
		String fileName;

		if (path.lastIndexOf(File.separator) == -1) {
			fileName = path.lastIndexOf(".") == -1 ? path : path.substring(0, path.lastIndexOf("."));			
			path = ".";
		} else {
			fileName = path.lastIndexOf(".") == -1 ? path.substring(path.lastIndexOf(File.separator) + 1) : path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
			path = path.substring(0, path.lastIndexOf(File.separator));
		}

		// start writing KML master file
		SAXWriter masterFileWriter;
		try {
			masterFileWriter = writeMasterFileHeader(fileName, path, tiling, query.getFeatureTypeFilter().getFeatureTypes());
		} catch (JAXBException | IOException | SAXException e) {
			throw new KmlExportException("Failed to write KML master file header.", e);
		}

		// start writing cityobject JSON file if required
		FileOutputStream jsonFileWriter = null;
		boolean jsonHasContent = false;
		if (config.getKmlExportConfig().isWriteJSONFile()) {
			try {
				File jsonFile = new File(path + File.separator + fileName + ".json");
				jsonFileWriter = new FileOutputStream(jsonFile);
				jsonFileWriter.write("{\n".getBytes(CHARSET));
			} catch (IOException e) {
				throw new KmlExportException("Failed to write JSON file header.", e);
			}			
		}

		long start = System.currentTimeMillis();

		// iterate over tiles
		for (int row = 0; shouldRun && row < rows; row++) {
			for (int column = 0; shouldRun && column < columns; column++) {

				// track exported objects
				ExportTracker tracker = new ExportTracker();

				// set active tile and get tile extent in WGS84
				Tile tile;
				try {
					tile = tiling.getTileAt(row, column);
					tiling.setActiveTile(tile);

					Predicate bboxFilter = tile.getFilterPredicate(databaseAdapter);
					if (predicate != null)
						query.setSelection(new SelectionFilter(LogicalOperationFactory.AND(predicate, bboxFilter)));
					else
						query.setSelection(new SelectionFilter(bboxFilter));
				} catch (FilterException e) {
					if (jsonFileWriter != null) try { jsonFileWriter.close(); } catch (IOException ioe) { }
					throw new KmlExportException("Failed to get tile at [" + row + "," + column + "].", e);
				}

				// iterate over display forms
				for (DisplayForm displayForm : config.getKmlExportConfig().getDisplayForms().values()) {
					if (!displayForm.isActive()) 
						continue;

					if (lastTempFolder != null && lastTempFolder.exists()) 
						deleteFolder(lastTempFolder); // just in case

					File file;
					ZipOutputStream zipOut = null;
					String currentWorkingDirectoryPath;
					try {
						File tilesRootDirectory = new File(path, "Tiles");
						tilesRootDirectory.mkdir();
						File rowTilesDirectory = new File(tilesRootDirectory.getPath(),  String.valueOf(row));
						rowTilesDirectory.mkdir();
						File columnTilesDirectory = new File(rowTilesDirectory.getPath(),  String.valueOf(column));
						columnTilesDirectory.mkdir();
						file = new File(columnTilesDirectory.getPath() + File.separator + fileName + "_Tile_" + row + "_" + column + "_" + displayForm.getName() + fileExtension);
						currentWorkingDirectoryPath = columnTilesDirectory.getPath();
						tracker.setCurrentWorkingDirectoryPath(currentWorkingDirectoryPath);

						eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("kmlExport.dialog.writingToFile"), this));
						eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName(), this));
						eventDispatcher.triggerEvent(new CounterEvent(CounterType.REMAINING_TILES, --remainingTiles, this));

						// open file for writing
						try {
							OutputStreamWriter fileWriter = null;
							if (config.getKmlExportConfig().isExportAsKmz()) {
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
						writerPool = new SingleWorkerPool<>(
								"kml_writer_pool",
								new XMLWriterWorkerFactory(saxWriter, eventDispatcher),
								100,
								true);

						kmlWorkerPool = new WorkerPool<>(
								"db_exporter_pool",
								config.getKmlExportConfig().getResources().getThreadPool().getMinThreads(),
								config.getKmlExportConfig().getResources().getThreadPool().getMaxThreads(),
								PoolSizeAdaptationStrategy.AGGRESSIVE,
								new KmlExportWorkerFactory(outputFile,
										jaxbKmlContext,
										jaxbColladaContext,
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
						document.setName(fileName + "_Tile_" + row + "_" + column + "_" + displayForm.getName());

						document.setOpen(false);
						kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

						// write file header
						Marshaller marshaller = null;
						try {
							marshaller = jaxbKmlContext.createMarshaller();
							fragmentWriter.setWriteMode(WriteMode.HEAD);
							marshaller.marshal(kml, fragmentWriter);

							if (config.getKmlExportConfig().isShowTileBorders())
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
							if (!objectCounter.isEmpty() && !config.getKmlExportConfig().isOneFilePerObject()) {
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
								if (config.getKmlExportConfig().isExportAsKmz()) {
									zipOut.closeEntry();

									List<File> filesToZip = new ArrayList<File>();
									String TEMP_FOLDER = "__temp";
									File tempFolder = new File(currentWorkingDirectoryPath, TEMP_FOLDER);
									lastTempFolder = tempFolder;
									int indexOfZipFilePath = tempFolder.getCanonicalPath().length() + 1;

									if (tempFolder.exists()) { // !config.getKmlExporter().isOneFilePerObject()
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
						if (objectCounter.isEmpty() && !config.getKmlExportConfig().isExportEmptyTiles()) {
							log.debug("Tile_" + row + "_" + column + " is empty. Deleting file " + file.getName() + ".");
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
						writeMasterFileTileReference(fileName, tile, masterFileWriter);
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
		try {
			writeMasterJsonFileTileReference(path, fileName, fileExtension, tiling);
		} catch (IOException e) {
			throw new KmlExportException("Failed to write master JSON file.", e);
		}

		// close cityobject JSON file
		if (jsonFileWriter != null) {
			try {
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

		if (shouldRun) {
			log.info("Total export time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");
		} else if (exception != null) {
			throw exception;
		}

		return shouldRun;
	}

	private SAXWriter writeMasterFileHeader(String fileName, String path, Tiling tiling, List<FeatureType> featureTypes) throws JAXBException, IOException, SAXException {
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

		BoundingBox extent = tiling.getExtent();
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

		if (config.getKmlExportConfig().isOneFilePerObject()) {
			for (DisplayForm displayForm : config.getKmlExportConfig().getDisplayForms().values()) {
				for (FeatureType featureType : featureTypes) {
					addStyle(displayForm, featureType.getObjectClassId(), saxWriter);
				}
			}
		}

		if (config.getKmlExportConfig().isShowBoundingBox()) {
			StyleType style = kmlFactory.createStyleType();
			style.setId("frameStyle");
			LineStyleType frameLineStyleType = kmlFactory.createLineStyleType();
			frameLineStyleType.setWidth(4.0);
			style.setLineStyle(frameLineStyleType);

			addBorder(extent, style, saxWriter);
		}

		return saxWriter;
	}

	private void writeMasterFileTileReference(String tileName, Tile tile, SAXWriter saxWriter) throws JAXBException {
		if (tile == null)
			return;

		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		// tileName should not contain special characters,
		// since it will be used as filename for all displayForm files
		tileName = tileName + "_Tile_" + tile.getRow() + "_" + tile.getColumn();

		FolderType folderType = kmlFactory.createFolderType();
		folderType.setName(tileName);

		for (DisplayForm displayForm : config.getKmlExportConfig().getDisplayForms().values()) {
			if (!displayForm.isActive()) 
				continue;

			String fileExtension = config.getKmlExportConfig().isExportAsKmz() ? ".kmz" : ".kml";
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
			lodType.setMaxLodPixels((double)displayForm.getVisibleTo());

			regionType.setLatLonAltBox(latLonAltBoxType);
			regionType.setLod(lodType);

			LinkType linkType = kmlFactory.createLinkType();
			linkType.setHref("Tiles/" + tile.getRow() + "/" + tile.getColumn() + "/" + tilenameForDisplayForm);
			linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getKmlExportConfig().getViewRefreshMode()));
			linkType.setViewFormat("");
			if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP)
				linkType.setViewRefreshTime(config.getKmlExportConfig().getViewRefreshTime());

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
		for (DisplayForm displayForm : config.getKmlExportConfig().getDisplayForms().values()) {
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
				jsonFileWriterForMasterFile.write(("\n\t\"" + "maxLodPixels" + "\": " + displayForm.getVisibleTo() + ",").getBytes(CHARSET));
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

	private void addStyle(DisplayForm displayForm, int objectClassId, SAXWriter saxWriter) throws JAXBException {
		if (!displayForm.isActive()) {
			return;
		}

		DisplayFormType type = displayForm.getType();
		Style style;
		String styleBasisName;

		switch (Util.getCityGMLClass(objectClassId)) {
			case SOLITARY_VEGETATION_OBJECT:
			case PLANT_COVER:
				style = config.getKmlExportConfig().getVegetationStyles().getOrDefault(type);
				styleBasisName = SolitaryVegetationObject.STYLE_BASIS_NAME;
				break;
			case TRANSPORTATION_COMPLEX:
			case TRACK:
			case RAILWAY:
			case ROAD:
			case SQUARE:
				style = config.getKmlExportConfig().getTransportationStyles().getOrDefault(type);
				styleBasisName = Transportation.STYLE_BASIS_NAME;
				break;
			case RELIEF_FEATURE:
				style = config.getKmlExportConfig().getReliefStyles().getOrDefault(type);
				styleBasisName = Relief.STYLE_BASIS_NAME;
				break;
			case CITY_OBJECT_GROUP:
				style = Style.of(DisplayFormType.FOOTPRINT); // hard-coded for groups
				styleBasisName = CityObjectGroup.STYLE_BASIS_NAME;
				break;
			case CITY_FURNITURE:
				style = config.getKmlExportConfig().getCityFurnitureStyles().getOrDefault(type);
				styleBasisName = CityFurniture.STYLE_BASIS_NAME;
				break;
			case GENERIC_CITY_OBJECT:
				addGenericCityObjectPointAndCurveStyle(saxWriter);
				style = config.getKmlExportConfig().getGenericCityObjectStyles().getOrDefault(type);
				styleBasisName = GenericCityObject.STYLE_BASIS_NAME;
				break;
			case LAND_USE:
				style = config.getKmlExportConfig().getLandUseStyles().getOrDefault(type);
				styleBasisName = LandUse.STYLE_BASIS_NAME;
				break;
			case WATER_BODY:
				style = config.getKmlExportConfig().getWaterBodyStyles().getOrDefault(type);
				styleBasisName = WaterBody.STYLE_BASIS_NAME;
				break;
			case BRIDGE:
				style = config.getKmlExportConfig().getBridgeStyles().getOrDefault(type);
				styleBasisName = Bridge.STYLE_BASIS_NAME;
				break;
			case TUNNEL:
				style = config.getKmlExportConfig().getTunnelStyles().getOrDefault(type);
				styleBasisName = Tunnel.STYLE_BASIS_NAME;
				break;
			case BUILDING:
				style = config.getKmlExportConfig().getBuildingStyles().getOrDefault(type);
				styleBasisName = Building.STYLE_BASIS_NAME;
				break;
			case ADE_COMPONENT:
				ADEPreference preference = ADEKmlExportExtensionManager.getInstance().getPreference(config, objectClassId);
				addADEPointAndCurveStyle(saxWriter, preference);
				style = preference.getStyles().getOrDefault(type);
				styleBasisName = preference.getTarget();
				break;
			default:
				return;
		}

		addStyle(style, styleBasisName, saxWriter);
	}

	private void addGenericCityObjectPointAndCurveStyle(SAXWriter saxWriter) throws JAXBException {
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		BalloonStyleType balloonStyle = new BalloonStyleType();
		balloonStyle.setText("$[description]");

		PointAndCurve pacSettings = config.getKmlExportConfig().getGenericCityObjectPointAndCurve();

		if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
			StyleType pointStyleNormal = kmlFactory.createStyleType();
			LabelStyleType labelStyleType = kmlFactory.createLabelStyleType();
			labelStyleType.setScale(0.0);
			pointStyleNormal.setLabelStyle(labelStyleType);

			IconStyleType iconStyleType = kmlFactory.createIconStyleType();
			iconStyleType.setScale(pacSettings.getPointIconScale());
			iconStyleType.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointIconColor()))));
			BasicLinkType icon = kmlFactory.createBasicLinkType();
			icon.setHref(PointAndCurve.DefaultIconHref);
			iconStyleType.setIcon(icon);
			pointStyleNormal.setIconStyle(iconStyleType);			
			pointStyleNormal.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Normal");
			pointStyleNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(pointStyleNormal), saxWriter);
		}
		else if (pacSettings.getPointDisplayMode() == PointDisplayMode.CUBE) {
			String fillColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeFillColor()));
			String lineColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeFillColor()));
			String hlFillColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeHighlightedColor()));
			String hlLineColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeHighlightedColor()));

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
			pointLineStyleNormal.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointNormalColor()))));
			pointLineStyleNormal.setWidth(pacSettings.getPointThickness());
			StyleType pointStyleNormal = kmlFactory.createStyleType();
			pointStyleNormal.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.POINT + "Normal");
			pointStyleNormal.setLineStyle(pointLineStyleNormal);
			pointStyleNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(pointStyleNormal), saxWriter);

			if (pacSettings.isPointHighlightingEnabled()) {
				LineStyleType pointLineStyleHighlight = kmlFactory.createLineStyleType();
				pointLineStyleHighlight.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointHighlightedColor()))));
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
		lineStyleNormal.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getCurveNormalColor()))));
		lineStyleNormal.setWidth(pacSettings.getCurveThickness());
		StyleType curveStyleNormal = kmlFactory.createStyleType();
		curveStyleNormal.setId(GenericCityObject.STYLE_BASIS_NAME + GenericCityObject.CURVE + "Normal");
		curveStyleNormal.setLineStyle(lineStyleNormal);
		curveStyleNormal.setBalloonStyle(balloonStyle);

		marshaller.marshal(kmlFactory.createStyle(curveStyleNormal), saxWriter);

		if (pacSettings.isCurveHighlightingEnabled()) {
			LineStyleType lineStyleHighlight = kmlFactory.createLineStyleType();
			lineStyleHighlight.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getCurveHighlightedColor()))));
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

	private void addADEPointAndCurveStyle(SAXWriter saxWriter, ADEPreference preference) throws JAXBException {
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		BalloonStyleType balloonStyle = new BalloonStyleType();
		balloonStyle.setText("$[description]");

		PointAndCurve pacSettings = preference.getPointAndCurve();

		if (pacSettings.getPointDisplayMode() == PointDisplayMode.ICON) {
			StyleType pointStyleNormal = kmlFactory.createStyleType();
			LabelStyleType labelStyleType = kmlFactory.createLabelStyleType();
			labelStyleType.setScale(0.0);
			pointStyleNormal.setLabelStyle(labelStyleType);

			IconStyleType iconStyleType = kmlFactory.createIconStyleType();
			iconStyleType.setScale(pacSettings.getPointIconScale());
			iconStyleType.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointIconColor()))));
			BasicLinkType icon = kmlFactory.createBasicLinkType();
			icon.setHref(PointAndCurve.DefaultIconHref);
			iconStyleType.setIcon(icon);
			pointStyleNormal.setIconStyle(iconStyleType);
			pointStyleNormal.setId(preference.getTarget() + ADEObject.POINT + "Normal");
			pointStyleNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(pointStyleNormal), saxWriter);
		}
		else if (pacSettings.getPointDisplayMode() == PointDisplayMode.CUBE) {
			String fillColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeFillColor()));
			String lineColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeFillColor()));
			String hlFillColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeHighlightedColor()));
			String hlLineColor = Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointCubeHighlightedColor()));

			LineStyleType lineStyleCubeNormal = kmlFactory.createLineStyleType();
			lineStyleCubeNormal.setColor(hexStringToByteArray(lineColor));
			lineStyleCubeNormal.setWidth(1.5);
			PolyStyleType polyStyleCubeNormal = kmlFactory.createPolyStyleType();
			polyStyleCubeNormal.setColor(hexStringToByteArray(fillColor));
			StyleType styleCubeNormal = kmlFactory.createStyleType();
			styleCubeNormal.setId(preference.getTarget() + ADEObject.POINT + "Normal");
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
				styleCubeHighlight.setId(preference.getTarget() + ADEObject.POINT + "Highlight");
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
				styleMapCube.setId(preference.getTarget() + ADEObject.POINT + "Style");
				styleMapCube.getPair().add(pairCubeNormal);
				styleMapCube.getPair().add(pairCubeHighlight);

				marshaller.marshal(kmlFactory.createStyle(styleCubeHighlight), saxWriter);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapCube), saxWriter);
			}
		}
		else {
			LineStyleType pointLineStyleNormal = kmlFactory.createLineStyleType();
			pointLineStyleNormal.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointNormalColor()))));
			pointLineStyleNormal.setWidth(pacSettings.getPointThickness());
			StyleType pointStyleNormal = kmlFactory.createStyleType();
			pointStyleNormal.setId(preference.getTarget() + ADEObject.POINT + "Normal");
			pointStyleNormal.setLineStyle(pointLineStyleNormal);
			pointStyleNormal.setBalloonStyle(balloonStyle);

			marshaller.marshal(kmlFactory.createStyle(pointStyleNormal), saxWriter);

			if (pacSettings.isPointHighlightingEnabled()) {
				LineStyleType pointLineStyleHighlight = kmlFactory.createLineStyleType();
				pointLineStyleHighlight.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getPointHighlightedColor()))));
				pointLineStyleHighlight.setWidth(pacSettings.getPointHighlightedThickness());
				StyleType pointStyleHighlight = kmlFactory.createStyleType();
				pointStyleHighlight.setId(preference.getTarget() + ADEObject.POINT + "Highlight");
				pointStyleHighlight.setLineStyle(pointLineStyleHighlight);
				pointStyleHighlight.setBalloonStyle(balloonStyle);

				PairType pairPointNormal = kmlFactory.createPairType();
				pairPointNormal.setKey(StyleStateEnumType.NORMAL);
				pairPointNormal.setStyleUrl("#" + pointStyleNormal.getId());
				PairType pairPointHighlight = kmlFactory.createPairType();
				pairPointHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
				pairPointHighlight.setStyleUrl("#" + pointStyleHighlight.getId());
				StyleMapType styleMapPoint = kmlFactory.createStyleMapType();
				styleMapPoint.setId(preference.getTarget() + ADEObject.POINT + "Style");
				styleMapPoint.getPair().add(pairPointNormal);
				styleMapPoint.getPair().add(pairPointHighlight);

				marshaller.marshal(kmlFactory.createStyle(pointStyleHighlight), saxWriter);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapPoint), saxWriter);
			}
		}

		LineStyleType lineStyleNormal = kmlFactory.createLineStyleType();
		lineStyleNormal.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getCurveNormalColor()))));
		lineStyleNormal.setWidth(pacSettings.getCurveThickness());
		StyleType curveStyleNormal = kmlFactory.createStyleType();
		curveStyleNormal.setId(preference.getTarget() + ADEObject.CURVE + "Normal");
		curveStyleNormal.setLineStyle(lineStyleNormal);
		curveStyleNormal.setBalloonStyle(balloonStyle);

		marshaller.marshal(kmlFactory.createStyle(curveStyleNormal), saxWriter);

		if (pacSettings.isCurveHighlightingEnabled()) {
			LineStyleType lineStyleHighlight = kmlFactory.createLineStyleType();
			lineStyleHighlight.setColor(hexStringToByteArray(Style.formatColorStringForKML(Integer.toHexString(pacSettings.getCurveHighlightedColor()))));
			lineStyleHighlight.setWidth(pacSettings.getCurveHighlightedThickness());
			StyleType curveStyleHighlight = kmlFactory.createStyleType();
			curveStyleHighlight.setId(preference.getTarget() + ADEObject.CURVE + "Highlight");
			curveStyleHighlight.setLineStyle(lineStyleHighlight);
			curveStyleHighlight.setBalloonStyle(balloonStyle);

			PairType pairCurveNormal = kmlFactory.createPairType();
			pairCurveNormal.setKey(StyleStateEnumType.NORMAL);
			pairCurveNormal.setStyleUrl("#" + curveStyleNormal.getId());
			PairType pairCurveHighlight = kmlFactory.createPairType();
			pairCurveHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
			pairCurveHighlight.setStyleUrl("#" + curveStyleHighlight.getId());
			StyleMapType styleMapCurve = kmlFactory.createStyleMapType();
			styleMapCurve.setId(preference.getTarget() + ADEObject.CURVE + "Style");
			styleMapCurve.getPair().add(pairCurveNormal);
			styleMapCurve.getPair().add(pairCurveHighlight);

			marshaller.marshal(kmlFactory.createStyle(curveStyleHighlight), saxWriter);
			marshaller.marshal(kmlFactory.createStyleMap(styleMapCurve), saxWriter);
		}
	}

	private void addStyle(Style style, String styleBasisName, SAXWriter saxWriter) throws JAXBException {
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		BalloonStyleType balloonStyle = new BalloonStyleType();
		balloonStyle.setText("$[description]");

		String fillColor = Style.formatColorStringForKML(Integer.toHexString(style.getRgba0()));
		String lineColor = Style.formatColorStringForKML(Integer.toHexString(style.getRgba1()));
		String roofFillColor = Style.formatColorStringForKML(Integer.toHexString(style.getRgba2()));
		String roofLineColor = Style.formatColorStringForKML(Integer.toHexString(style.getRgba3()));
		String highlightFillColor = Style.formatColorStringForKML(Integer.toHexString(style.getRgba4()));
		String highlightLineColor = Style.formatColorStringForKML(Integer.toHexString(style.getRgba5()));

		switch (style.getType()) {
			case FOOTPRINT:
			case EXTRUDED:
				LineStyleType lineStyleFootprintNormal = kmlFactory.createLineStyleType();
				lineStyleFootprintNormal.setColor(hexStringToByteArray(lineColor));
				lineStyleFootprintNormal.setWidth(1.5);
				PolyStyleType polyStyleFootprintNormal = kmlFactory.createPolyStyleType();
				polyStyleFootprintNormal.setColor(hexStringToByteArray(fillColor));
				StyleType styleFootprintNormal = kmlFactory.createStyleType();
				styleFootprintNormal.setId(styleBasisName + style.getType().getName() + "Normal");
				styleFootprintNormal.setLineStyle(lineStyleFootprintNormal);
				styleFootprintNormal.setPolyStyle(polyStyleFootprintNormal);
				styleFootprintNormal.setBalloonStyle(balloonStyle);

				marshaller.marshal(kmlFactory.createStyle(styleFootprintNormal), saxWriter);

				if (style.isHighlightingEnabled()) {
					LineStyleType lineStyleFootprintHighlight = kmlFactory.createLineStyleType();
					lineStyleFootprintHighlight.setColor(hexStringToByteArray(highlightLineColor));
					lineStyleFootprintHighlight.setWidth(1.5);
					PolyStyleType polyStyleFootprintHighlight = kmlFactory.createPolyStyleType();
					polyStyleFootprintHighlight.setColor(hexStringToByteArray(highlightFillColor));
					StyleType styleFootprintHighlight = kmlFactory.createStyleType();
					styleFootprintHighlight.setId(styleBasisName + style.getType().getName() + "Highlight");
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
					styleMapFootprint.setId(styleBasisName + style.getType().getName() + "Style");
					styleMapFootprint.getPair().add(pairFootprintNormal);
					styleMapFootprint.getPair().add(pairFootprintHighlight);

					marshaller.marshal(kmlFactory.createStyle(styleFootprintHighlight), saxWriter);
					marshaller.marshal(kmlFactory.createStyleMap(styleMapFootprint), saxWriter);
				}

				break;

			case GEOMETRY:
				boolean isBuilding = Building.STYLE_BASIS_NAME.equals(styleBasisName);
				boolean isBridge = Bridge.STYLE_BASIS_NAME.equals(styleBasisName);
				boolean isTunnel = Tunnel.STYLE_BASIS_NAME.equals(styleBasisName);

				LineStyleType lineStyleWallNormal = kmlFactory.createLineStyleType();
				lineStyleWallNormal.setColor(hexStringToByteArray(lineColor));
				PolyStyleType polyStyleWallNormal = kmlFactory.createPolyStyleType();
				polyStyleWallNormal.setColor(hexStringToByteArray(fillColor));
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
					styleWallNormal.setId(styleBasisName + style.getType().getName() + "Normal");

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

				if (style.isHighlightingEnabled()) {
					LineStyleType lineStyleGeometryInvisible = kmlFactory.createLineStyleType();
					lineStyleGeometryInvisible.setColor(hexStringToByteArray("01" + highlightLineColor.substring(2)));
					PolyStyleType polyStyleGeometryInvisible = kmlFactory.createPolyStyleType();
					polyStyleGeometryInvisible.setColor(hexStringToByteArray("00" + highlightFillColor.substring(2)));
					StyleType styleGeometryInvisible = kmlFactory.createStyleType();
					styleGeometryInvisible.setId(styleBasisName + style.getType().getName() + "StyleInvisible");
					styleGeometryInvisible.setLineStyle(lineStyleGeometryInvisible);
					styleGeometryInvisible.setPolyStyle(polyStyleGeometryInvisible);
					styleGeometryInvisible.setBalloonStyle(balloonStyle);

					LineStyleType lineStyleGeometryHighlight = kmlFactory.createLineStyleType();
					lineStyleGeometryHighlight.setColor(hexStringToByteArray(highlightLineColor));
					PolyStyleType polyStyleGeometryHighlight = kmlFactory.createPolyStyleType();
					polyStyleGeometryHighlight.setColor(hexStringToByteArray(highlightFillColor));
					StyleType styleGeometryHighlight = kmlFactory.createStyleType();
					styleGeometryHighlight.setId(styleBasisName + style.getType().getName() + "StyleHighlight");
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
					styleMapGeometry.setId(styleBasisName + style.getType().getName() + "Style");
					styleMapGeometry.getPair().add(pairGeometryNormal);
					styleMapGeometry.getPair().add(pairGeometryHighlight);

					marshaller.marshal(kmlFactory.createStyle(styleGeometryInvisible), saxWriter);
					marshaller.marshal(kmlFactory.createStyle(styleGeometryHighlight), saxWriter);
					marshaller.marshal(kmlFactory.createStyleMap(styleMapGeometry), saxWriter);
				}

				break;

			case COLLADA:
				if (style.isHighlightingEnabled()) {
					LineStyleType lineStyleColladaInvisible = kmlFactory.createLineStyleType();
					lineStyleColladaInvisible.setColor(hexStringToByteArray("01" + highlightLineColor.substring(2)));
					PolyStyleType polyStyleColladaInvisible = kmlFactory.createPolyStyleType();
					polyStyleColladaInvisible.setColor(hexStringToByteArray("00" + highlightFillColor.substring(2)));
					StyleType styleColladaInvisible = kmlFactory.createStyleType();
					styleColladaInvisible.setId(styleBasisName + style.getType().getName() + "StyleInvisible");
					styleColladaInvisible.setLineStyle(lineStyleColladaInvisible);
					styleColladaInvisible.setPolyStyle(polyStyleColladaInvisible);
					styleColladaInvisible.setBalloonStyle(balloonStyle);

					LineStyleType lineStyleColladaHighlight = kmlFactory.createLineStyleType();
					lineStyleColladaHighlight.setColor(hexStringToByteArray(highlightLineColor));
					PolyStyleType polyStyleColladaHighlight = kmlFactory.createPolyStyleType();
					polyStyleColladaHighlight.setColor(hexStringToByteArray(highlightFillColor));
					StyleType styleColladaHighlight = kmlFactory.createStyleType();
					styleColladaHighlight.setId(styleBasisName + style.getType().getName() + "StyleHighlight");
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
					styleMapCollada.setId(styleBasisName + style.getType().getName() + "Style");
					styleMapCollada.getPair().add(pairColladaNormal);
					styleMapCollada.getPair().add(pairColladaHighlight);

					marshaller.marshal(kmlFactory.createStyle(styleColladaInvisible), saxWriter);
					marshaller.marshal(kmlFactory.createStyle(styleColladaHighlight), saxWriter);
					marshaller.marshal(kmlFactory.createStyleMap(styleMapCollada), saxWriter);
				}
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
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getBuildingBalloon()};
			break;
		case WATER_BODY:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getWaterBodyBalloon()};
			break;
		case LAND_USE:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getLandUseBalloon()};
			break;
		case SOLITARY_VEGETATION_OBJECT:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getVegetationBalloon()};
			break;
		case TRANSPORTATION_COMPLEX:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getTransportationBalloon()};
			break;
		case RELIEF_FEATURE:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getReliefBalloon()};
			break;
		case CITY_FURNITURE:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getCityFurnitureBalloon()};
			break;
		case GENERIC_CITY_OBJECT:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getGenericCityObject3DBalloon(),
					config.getKmlExportConfig().getGenericCityObjectPointAndCurve().getPointBalloon(),
					config.getKmlExportConfig().getGenericCityObjectPointAndCurve().getCurveBalloon()};
			break;
		case CITY_OBJECT_GROUP:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getCityObjectGroupBalloon()};
			break;
		case BRIDGE:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getBridgeBalloon()};
			break;
		case TUNNEL:
			balloonSettings = new Balloon[]{config.getKmlExportConfig().getTunnelBalloon()};
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
			Map<Integer, Long> counter = ((ObjectCounterEvent) e).getCounter();
			for (Entry<Integer, Long> entry : counter.entrySet()) {
				Long tmp = objectCounter.get(entry.getKey());
				objectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
				tmp = totalObjectCounter.get(entry.getKey());
				totalObjectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
			}
		} else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			geometryCounter++;
		} else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;
				InterruptEvent event = (InterruptEvent) e;

				log.log(event.getLogLevelType(), event.getLogMessage());
				if (event.getCause() != null) {
					exception = new KmlExportException("Aborting export due to errors.", event.getCause());
				}

				log.info("Waiting for objects being currently processed to end...");

				if (kmlSplitter != null) {
					kmlSplitter.shutdown();
				}

				if (kmlWorkerPool != null) {
					kmlWorkerPool.drainWorkQueue();
				}

				if (lastTempFolder != null && lastTempFolder.exists()) {
					deleteFolder(lastTempFolder); // just in case
				}
			}
		}
	}

	private JAXBContext getKmlContext() throws JAXBException {
		JAXBContext kmlContext = (JAXBContext) ObjectRegistry.getInstance().lookup("net.opengis.kml._2.JAXBContext");
		if (kmlContext == null) {
			kmlContext = JAXBContext.newInstance("net.opengis.kml._2", getClass().getClassLoader());
			ObjectRegistry.getInstance().register("net.opengis.kml._2.JAXBContext", kmlContext);
		}

		return kmlContext;
	}

	private JAXBContext getColladaContext() throws JAXBException {
		JAXBContext colladaContext = (JAXBContext) ObjectRegistry.getInstance().lookup("org.collada._2005._11.colladaschema.JAXBContext");
		if (colladaContext == null) {
			colladaContext = JAXBContext.newInstance("org.collada._2005._11.colladaschema", getClass().getClassLoader());
			ObjectRegistry.getInstance().register("org.collada._2005._11.colladaschema.JAXBContext", colladaContext);
		}

		return colladaContext;
	}
}
