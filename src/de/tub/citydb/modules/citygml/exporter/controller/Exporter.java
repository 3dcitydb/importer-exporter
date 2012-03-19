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
package de.tub.citydb.modules.citygml.exporter.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.transform.stream.StreamResult;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.xml.io.writer.JAXBModelWriter;
import org.citygml4j.builder.jaxb.xml.io.writer.JAXBOutputFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.AppearanceModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityModelInfo;
import org.xml.sax.SAXException;

import de.tub.citydb.api.concurrent.SingleWorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.exporter.ExportAppearance;
import de.tub.citydb.config.project.filter.TileNameSuffixMode;
import de.tub.citydb.config.project.filter.TileSuffixMode;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerEnum;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.exporter.concurrent.DBExportWorkerFactory;
import de.tub.citydb.modules.citygml.exporter.concurrent.DBExportXlinkWorkerFactory;
import de.tub.citydb.modules.citygml.exporter.database.content.DBSplitter;
import de.tub.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import de.tub.citydb.modules.citygml.exporter.database.gmlid.DBExportCache;
import de.tub.citydb.modules.common.concurrent.IOWriterWorkerFactory;
import de.tub.citydb.modules.common.event.EventType;
import de.tub.citydb.modules.common.event.FeatureCounterEvent;
import de.tub.citydb.modules.common.event.GeometryCounterEvent;
import de.tub.citydb.modules.common.event.InterruptEvent;
import de.tub.citydb.modules.common.event.StatusDialogMessage;
import de.tub.citydb.modules.common.event.StatusDialogTitle;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.util.Util;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.database.IndexStatusInfo.IndexType;

public class Exporter implements EventHandler {
	private final Logger LOG = Logger.getInstance();

	private final JAXBBuilder jaxbBuilder;
	private final DatabaseConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private DBSplitter dbSplitter;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private WorkerPool<DBSplittingResult> dbWorkerPool;
	private SingleWorkerPool<SAXEventBuffer> ioWriterPool;
	private WorkerPool<DBXlink> xlinkExporterPool;
	private CacheManager cacheManager;
	private DBGmlIdLookupServerManager lookupServerManager;
	private ExportFilter exportFilter;
	private boolean useTiling;

	private EnumMap<CityGMLClass, Long> totalFeatureCounterMap;
	private EnumMap<GMLClass, Long> totalGeometryCounterMap;
	private EnumMap<CityGMLClass, Long> featureCounterMap;
	private EnumMap<GMLClass, Long> geometryCounterMap;

	public Exporter(JAXBBuilder jaxbBuilder, DatabaseConnectionPool dbPool, Config config, EventDispatcher eventDispatcher) {
		this.jaxbBuilder = jaxbBuilder;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		featureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		geometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
		totalFeatureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		totalGeometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
	}
	
	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() {
		// get config shortcuts
		de.tub.citydb.config.project.system.System system = config.getProject().getExporter().getSystem();
		Database database = config.getProject().getDatabase();

		// worker pool settings
		int minThreads = system.getThreadPool().getDefaultPool().getMinThreads();
		int maxThreads = system.getThreadPool().getDefaultPool().getMaxThreads();

		// calc queueSize
		// how to properly calculate?
		//int dbQueueSize = maxThreads * 20;
		//int writerQueueSize = maxThreads * 100;

		// gml:id lookup cache update
		int lookupCacheBatchSize = database.getUpdateBatching().getGmlIdLookupServerBatchValue();		

		// adding listeners
		eventDispatcher.addEventHandler(EventType.FEATURE_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// checking workspace... this should be improved in future...
		Workspace workspace = database.getWorkspaces().getExportWorkspace();
		if (shouldRun && !workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.existsWorkspace(workspace);

			String name = "'" + workspace.getName().trim() + "'";
			String timestamp = workspace.getTimestamp().trim();
			if (timestamp.trim().length() > 0)
				name += " at timestamp " + timestamp;

			if (!workspaceExists) {
				LOG.error("Database workspace " + name + " is not available.");
				return false;
			} else 
				LOG.info("Switching to database workspace " + name + '.');
		}

		// set module context according to CityGML version and create SAX writer
		CityGMLVersion version = config.getProject().getExporter().getCityGMLVersion().toCityGMLVersion();
		ModuleContext moduleContext = new ModuleContext(version);
		CityModelInfo cityModel = new CityModelInfo();

		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setWriteEncoding(true);
		saxWriter.setIndentString("  ");
		saxWriter.setHeaderComment("Written by " + this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + '"', 
				this.getClass().getPackage().getImplementationVendor());
		saxWriter.setDefaultNamespace(moduleContext.getModule(CityGMLModuleType.CORE).getNamespaceURI());

		for (Module module : moduleContext.getModules()) {
			if (module instanceof CoreModule)
				continue;

			if (version != CityGMLVersion.v0_4_0 &&
					!config.getProject().getExporter().getAppearances().isSetExportAppearance() &&
					module instanceof AppearanceModule)
				continue;

			saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
			if (module instanceof CityGMLModule)
				saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
		}

		// checking file
		Internal internalConfig = config.getInternal();
		File exportFile = new File(internalConfig.getExportFileName());
		String fileName = exportFile.getName();
		String folderName = new File(exportFile.getAbsolutePath()).getParent();

		String fileExtension = Util.getFileExtension(fileName);		
		if (fileExtension == null)
			fileExtension = "gml";
		else
			fileName = Util.stripFileExtension(fileName);

		// create export folder
		File folder = new File(folderName);
		if (!folder.exists() && !folder.mkdirs()) {
			LOG.error("Failed to create folder '" + folderName + "'.");
			return false;
		}

		// set target reference system for export
		DatabaseSrs targetSRS = config.getProject().getExporter().getTargetSRS();
		internalConfig.setExportTargetSRS(targetSRS);
		internalConfig.setTransformCoordinates(targetSRS.isSupported() && 
				targetSRS.getSrid() != dbPool.getActiveConnectionMetaData().getReferenceSystem().getSrid());

		if (internalConfig.isTransformCoordinates()) {
			if (targetSRS.is3D() == dbPool.getActiveConnectionMetaData().getReferenceSystem().is3D()) {
				LOG.info("Transforming geometry representation to reference system '" + targetSRS.getDescription() + "' (SRID: " + targetSRS.getSrid() + ").");
				LOG.warn("Transformation is NOT applied to height reference system.");
			} else {
				LOG.error("Dimensionality of reference system for geometry transformation does not match.");
				return false;
			}
		}

		// log index status
		try {
			for (IndexType type : IndexType.values())
				DBUtil.getIndexStatus(type).printStatusToConsole();
		} catch (SQLException e) {
			LOG.error("Database error while querying index status: " + e.getMessage());
			return false;
		}

		// check whether database contains global appearances and set internal flag
		try {
			internalConfig.setExportGlobalAppearances(config.getProject().getExporter().getAppearances().isSetExportAppearance() && 
					DBUtil.getNumGlobalAppearances() > 0);
		} catch (SQLException e) {
			LOG.error("Database error while querying the number of global appearances: " + e.getMessage());
			return false;
		}

		// getting export filter
		exportFilter = new ExportFilter(config);

		// bounding box config
		Tiling tiling = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();
		useTiling = exportFilter.getBoundingBoxFilter().isActive() && tiling.getMode() != TilingMode.NO_TILING;

		int rows = useTiling ? tiling.getRows() : 1;  
		int columns = useTiling ? tiling.getColumns() : 1;

		for (int i = 0; shouldRun && i < rows; i++) {
			for (int j = 0; shouldRun && j < columns; j++) {

				try {
					File file = null;

					if (useTiling) {
						exportFilter.getBoundingBoxFilter().setActiveTile(i, j);

						// create suffix for folderName and fileName
						TileSuffixMode suffixMode = tiling.getTilePathSuffix();
						String suffix = "";

						BoundingBox bbox = exportFilter.getBoundingBoxFilter().getFilterState();
						double minX = bbox.getLowerLeftCorner().getX();
						double minY = bbox.getLowerLeftCorner().getY();
						double maxX = bbox.getUpperRightCorner().getX();
						double maxY = bbox.getUpperRightCorner().getY();

						switch (suffixMode) {
						case XMIN_YMIN:
							suffix = String.valueOf(minX) + '_' + String.valueOf(minY);
							break;
						case XMAX_YMIN:
							suffix = String.valueOf(maxX) + '_' + String.valueOf(minY);
							break;
						case XMIN_YMAX:
							suffix = String.valueOf(minX) + '_' + String.valueOf(maxY);
							break;
						case XMAX_YMAX:
							suffix = String.valueOf(maxX) + '_' + String.valueOf(maxY);
							break;
						case XMIN_YMIN_XMAX_YMAX:
							suffix = String.valueOf(minX) + '_' + String.valueOf(minY) + '_' + String.valueOf(maxX) + '_' + String.valueOf(maxY);
							break;
						default:
							suffix = String.valueOf(i) + '_' + String.valueOf(j);
						}

						String subfolderName = folderName + File.separator + tiling.getTilePath() + '_'  + suffix;
						File subfolder = new File(subfolderName);
						if (!subfolder.exists() && !subfolder.mkdirs()) {
							LOG.error("Failed to create tiling subfolder '" + subfolderName + "'.");
							return false;
						}

						if (tiling.getTileNameSuffix() == TileNameSuffixMode.SAME_AS_PATH)
							file = new File(subfolderName + File.separator + fileName + '_'  + suffix + '.' + fileExtension);
						else // no suffix for filename
							file = new File(subfolderName + File.separator + fileName + '.' + fileExtension);
					}

					else // no tiling
						file = new File(folderName + File.separator + fileName + '.' + fileExtension);

					config.getInternal().setExportFileName(file.getAbsolutePath());
					File path = new File(file.getAbsolutePath());
					internalConfig.setExportPath(path.getParent());

					eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.cityObj.msg"), this));
					eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName(), this));

					// checking export path for texture images
					ExportAppearance appearances = config.getProject().getExporter().getAppearances();
					if (appearances.isSetExportAppearance()) {
						// read user input
						String textureExportPath = null;
						boolean isRelative = appearances.isTexturePathRealtive();

						if (isRelative)
							textureExportPath = appearances.getRelativeTexturePath();
						else
							textureExportPath = appearances.getAbsoluteTexturePath();

						if (textureExportPath != null && textureExportPath.length() > 0) {
							// convert into system readable path name
							File tmp = new File(textureExportPath);
							textureExportPath = tmp.getPath();

							if (isRelative) {
								File exportPath = new File(path.getParent() + File.separator + textureExportPath);

								if (exportPath.isFile() || (exportPath.isDirectory() && !exportPath.canWrite())) {
									LOG.error("Failed to open texture files subfolder '" + exportPath.toString() + "' for writing.");
									return false;
								} else if (!exportPath.isDirectory()) {
									boolean success = exportPath.mkdirs();

									if (!success) {
										LOG.error("Failed to create texture files subfolder '" + exportPath.toString() + "'.");
										return false;
									} else
										LOG.info("Created texture files subfolder '" + textureExportPath + "'.");
								}

								internalConfig.setExportTextureFilePath(textureExportPath);
							} else {
								File exportPath = new File(tmp.getAbsolutePath());

								if (!exportPath.exists() || !exportPath.isDirectory() || !exportPath.canWrite()) {
									LOG.error("Failed to open texture files folder '" + exportPath.toString() + "' for writing.");
									return false;
								}

								internalConfig.setExportTextureFilePath(exportPath.toString());
							}
						}
					}

					// open file for writing
					try {
						OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
						saxWriter.setOutput(new StreamResult(fileWriter));
					} catch (IOException ioE) {
						LOG.error("Failed to open file '" + fileName + "' for writing: " + ioE.getMessage());
						return false;
					}					

					// create instance of temp table manager
					cacheManager = new CacheManager(dbPool, maxThreads);

					// create instance of gml:id lookup server manager...
					lookupServerManager = new DBGmlIdLookupServerManager();

					// ...and start servers
					try {		
						lookupServerManager.initServer(
								DBGmlIdLookupServerEnum.GEOMETRY,
								new DBExportCache(cacheManager, 
										CacheTableModelEnum.GMLID_GEOMETRY, 
										system.getGmlIdLookupServer().getGeometry().getPartitions(),
										lookupCacheBatchSize),
										system.getGmlIdLookupServer().getGeometry().getCacheSize(),
										system.getGmlIdLookupServer().getGeometry().getPageFactor(),
										maxThreads);

						lookupServerManager.initServer(
								DBGmlIdLookupServerEnum.FEATURE,
								new DBExportCache(cacheManager, 
										CacheTableModelEnum.GMLID_FEATURE, 
										system.getGmlIdLookupServer().getFeature().getPartitions(), 
										lookupCacheBatchSize),
										system.getGmlIdLookupServer().getFeature().getCacheSize(),
										system.getGmlIdLookupServer().getFeature().getPageFactor(),
										maxThreads);
					} catch (SQLException sqlEx) {
						LOG.error("SQL error while initializing database export: " + sqlEx.getMessage());
						return false;
					}

					// create worker pools
					// here we have an open issue: queue sizes are fix...
					xlinkExporterPool = new WorkerPool<DBXlink>(
							minThreads,
							maxThreads,
							new DBExportXlinkWorkerFactory(dbPool, config, eventDispatcher),
							300,
							false);

					ioWriterPool = new SingleWorkerPool<SAXEventBuffer>(
							new IOWriterWorkerFactory(saxWriter),
							100,
							false);

					dbWorkerPool = new WorkerPool<DBSplittingResult>(
							minThreads,
							maxThreads,
							new DBExportWorkerFactory(
									dbPool,
									jaxbBuilder,
									ioWriterPool,
									xlinkExporterPool,
									lookupServerManager,
									cacheManager,
									exportFilter,
									config,
									eventDispatcher),
									300,
									false);

					// prestart pool workers
					xlinkExporterPool.prestartCoreWorkers();
					ioWriterPool.prestartCoreWorkers();
					dbWorkerPool.prestartCoreWorkers();

					// ok, preparations done. inform user...
					LOG.info("Exporting to file: " + file.getAbsolutePath());

					// write CityModel header element
					JAXBModelWriter writer = null;
					try {
						writer = new JAXBModelWriter(
								saxWriter, 
								(JAXBOutputFactory)jaxbBuilder.createCityGMLOutputFactory(moduleContext), 
								moduleContext, 
								cityModel);
						writer.writeStartDocument();
					} catch (CityGMLWriteException e) {
						LOG.error("I/O error: " + e.getCause().getMessage());
						return false;
					}

					// flush writer to make sure header has been written
					try {
						saxWriter.flush();
					} catch (SAXException e) {
						LOG.error("I/O error: " + e.getMessage());
						return false;
					}

					// get database splitter and start query
					dbSplitter = null;
					try {
						dbSplitter = new DBSplitter(
								dbPool,
								dbWorkerPool,
								exportFilter,
								eventDispatcher,
								config);

						if (shouldRun)
							dbSplitter.startQuery();
					} catch (SQLException sqlE) {
						LOG.error("SQL error: " + sqlE.getMessage());
						return false;
					}

					try {
						dbWorkerPool.shutdownAndWait();
						if (shouldRun)
							xlinkExporterPool.shutdownAndWait();
						
						ioWriterPool.shutdownAndWait();
					} catch (InterruptedException e) {
						LOG.error("Internal error: " + e.getMessage());
					}

					// write footer element
					try {
						writer.writeEndDocument();
					} catch (CityGMLWriteException e) {
						LOG.error("I/O error: " + e.getCause().getMessage());
						return false;
					}

					// flush sax writer
					try {
						saxWriter.flush();
						saxWriter.getOutputWriter().close();
					} catch (SAXException e) {
						LOG.error("I/O error: " + e.getMessage());
						return false;
					} catch (IOException e) {
						LOG.error("I/O error: " + e.getMessage());
						return false;
					}

					eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.finish.msg"), this));

					// cleaning up...
					try {
						lookupServerManager.shutdownAll();
					} catch (SQLException e) {
						LOG.error("SQL error: " + e.getMessage());
					}
					
					try {
						LOG.info("Cleaning temporary cache.");
						cacheManager.dropAll();
					} catch (SQLException sqlE) {
						LOG.error("SQL error: " + sqlE.getMessage());
						return false;
					}

					// finally join eventDispatcher
					try {
						eventDispatcher.flushEvents();
					} catch (InterruptedException iE) {
						LOG.error("Internal error: " + iE.getMessage());
						return false;
					}

					// set null
					cacheManager = null;
					lookupServerManager = null;
					xlinkExporterPool = null;
					ioWriterPool = null;
					dbWorkerPool = null;
					dbSplitter = null;

					// show exported features
					if (!featureCounterMap.isEmpty()) {
						LOG.info("Exported CityGML features:");
						for (CityGMLClass type : featureCounterMap.keySet())
							LOG.info(type + ": " + featureCounterMap.get(type));
					}

					long geometryObjects = 0;
					for (GMLClass type : geometryCounterMap.keySet())
						geometryObjects += geometryCounterMap.get(type);

					if (geometryObjects != 0)
						LOG.info("Processed geometry objects: " + geometryObjects);

					featureCounterMap.clear();
					geometryCounterMap.clear();

				} finally {
					if (cacheManager != null) {
						try {
							LOG.info("Cleaning temporary cache.");
							cacheManager.dropAll();
							cacheManager = null;
						} catch (SQLException sqlEx) {
							LOG.error("SQL error while finishing database export: " + sqlEx.getMessage());
						}
					}
				}
			}
		}

		// show totally exported features
		if (useTiling && (rows > 1 || columns > 1)) {
			if (!totalFeatureCounterMap.isEmpty()) {
				LOG.info("Totally exported CityGML features:");
				for (CityGMLClass type : totalFeatureCounterMap.keySet())
					LOG.info(type + ": " + totalFeatureCounterMap.get(type));
			}

			long geometryObjects = 0;
			for (GMLClass type : totalGeometryCounterMap.keySet())
				geometryObjects += totalGeometryCounterMap.get(type);

			if (geometryObjects != 0)
				LOG.info("Total processed geometry objects: " + geometryObjects);
		}

		return shouldRun;
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

				if (useTiling) {
					counter = totalFeatureCounterMap.get(type);

					if (counter == null)
						totalFeatureCounterMap.put(type, update);
					else
						totalFeatureCounterMap.put(type, counter + update);
				}
			}
		}

		else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			HashMap<GMLClass, Long> counterMap = ((GeometryCounterEvent)e).getCounter();

			for (GMLClass type : counterMap.keySet()) {
				Long counter = geometryCounterMap.get(type);
				Long update = counterMap.get(type);

				if (counter == null)
					geometryCounterMap.put(type, update);
				else
					geometryCounterMap.put(type, counter + update);

				if (useTiling) {
					counter = totalGeometryCounterMap.get(type);

					if (counter == null)
						totalGeometryCounterMap.put(type, update);
					else
						totalGeometryCounterMap.put(type, counter + update);
				}
			}
		}

		else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					LOG.log(((InterruptEvent)e).getLogLevelType(), log);

				if (dbSplitter != null)
					dbSplitter.shutdown();
				
				if (xlinkExporterPool != null)
					xlinkExporterPool.shutdownNow();
				
				if (dbWorkerPool != null)
					dbWorkerPool.drainWorkQueue();
			}
		}
	}
}
