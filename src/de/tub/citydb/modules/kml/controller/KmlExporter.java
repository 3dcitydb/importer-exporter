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
package de.tub.citydb.modules.kml.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.stream.StreamResult;

import net.opengis.kml._2.BalloonStyleType;
import net.opengis.kml._2.DocumentType;
import net.opengis.kml._2.FolderType;
import net.opengis.kml._2.KmlType;
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
import oracle.ord.im.OrdImage;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import de.tub.citydb.api.concurrent.SingleWorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.api.gui.BoundingBoxCorner;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Database.PredefinedSrsName;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.filter.FeatureClass;
import de.tub.citydb.config.project.filter.TiledBoundingBox;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.kmlExporter.Balloon;
import de.tub.citydb.config.project.kmlExporter.BalloonContentMode;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.concurrent.IOWriterWorkerFactory;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.EventType;
import de.tub.citydb.modules.common.event.InterruptEvent;
import de.tub.citydb.modules.common.event.StatusDialogMessage;
import de.tub.citydb.modules.common.event.StatusDialogTitle;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.FilterMode;
import de.tub.citydb.modules.kml.concurrent.KmlExportWorkerFactory;
import de.tub.citydb.modules.kml.database.BalloonTemplateHandlerImpl;
import de.tub.citydb.modules.kml.database.Building;
import de.tub.citydb.modules.kml.database.CityObjectGroup;
import de.tub.citydb.modules.kml.database.ColladaBundle;
import de.tub.citydb.modules.kml.database.KmlSplitter;
import de.tub.citydb.modules.kml.database.KmlSplittingResult;
import de.tub.citydb.modules.kml.database.SolitaryVegetationObject;
import de.tub.citydb.modules.kml.util.CityObject4JSON;
import de.tub.citydb.modules.kml.util.KMLHeaderWriter;
import de.tub.citydb.util.database.DBUtil;

public class KmlExporter implements EventHandler {
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final DatabaseConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private CityGMLFactory cityGMLFactory; 
	private ObjectFactory kmlFactory; 
	private WorkerPool<KmlSplittingResult> kmlWorkerPool;
	private SingleWorkerPool<SAXEventBuffer> ioWriterPool;
	private KmlSplitter kmlSplitter;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private static final double BORDER_GAP = 0.000001;

	private static final String ENCODING = "UTF-8";
	private static final Charset CHARSET = Charset.forName(ENCODING);

	private final DatabaseSrs WGS84_2D = Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D);

	private BoundingBox tileMatrix;
	private BoundingBox wgs84TileMatrix;

	private double wgs84DeltaLongitude;
	private double wgs84DeltaLatitude;

	private static int rows;
	private static int columns;

	private String path;
	private String filename;

	private EnumMap<CityGMLClass, Long>featureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
	private long geometryCounter;

	private static HashMap<String, CityObject4JSON> alreadyExported;

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
		cityGMLFactory = new CityGMLFactory();		
	}
	
	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() {
		geometryCounter = 0;
		
		// get config shortcuts
		de.tub.citydb.config.project.system.System system = config.getProject().getKmlExporter().getSystem();

		// worker pool settings
		int minThreads = system.getThreadPool().getDefaultPool().getMinThreads();
		int maxThreads = system.getThreadPool().getDefaultPool().getMaxThreads();

		// adding listener
		eventDispatcher.addEventHandler(EventType.COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// checking workspace...
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace();
		if (!workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.existsWorkspace(workspace);

			String name = "'" + workspace.getName().trim() + "'";
			String timestamp = workspace.getTimestamp().trim();
			if (timestamp.trim().length() > 0)
				name += " at timestamp " + timestamp;
			
			if (!workspaceExists) {
				Logger.getInstance().error("Database workspace " + name + " is not available.");
				return false;
			} else 
				Logger.getInstance().info("Switching to database workspace " + name + '.');
		}
		
		// check whether spatial indexes are enabled
		Logger.getInstance().info("Checking for spatial indexes on geometry columns of involved tables...");
		try {
			if (!DBUtil.isIndexed("CITYOBJECT", "ENVELOPE") || 
					!DBUtil.isIndexed("SURFACE_GEOMETRY", "GEOMETRY")) {
				Logger.getInstance().error("Spatial indexes are not activated.");
				Logger.getInstance().error("Please use the database tab to activate the spatial indexes.");
				return false;
			}
		}
		catch (SQLException e) {
			Logger.getInstance().error("Failed to retrieve status of spatial indexes: " + e.getMessage());
			return false;
		}

		String selectedTheme = config.getProject().getKmlExporter().getAppearanceTheme();
		if (!selectedTheme.equals(de.tub.citydb.config.project.kmlExporter.KmlExporter.THEME_NONE)) {
			try {
				for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
					if (displayForm.getForm() == DisplayForm.COLLADA && displayForm.isActive()) {
						if (!DBUtil.getAppearanceThemeList(workspace).contains(selectedTheme)) {
							Logger.getInstance().error("Database does not contain appearance theme \"" + selectedTheme + "\"");
							return false;
						}
					}
				}
			}
			catch (SQLException e) {
				Logger.getInstance().error("Generic DB error: " + e.getMessage());
				return false;
			}
		}

		if (!checkBalloonSettings(config.getProject().getKmlExporter().getBuildingBalloon())) return false;
		if (!checkBalloonSettings(config.getProject().getKmlExporter().getCityObjectGroupBalloon())) return false;
		if (!checkBalloonSettings(config.getProject().getKmlExporter().getVegetationBalloon())) return false;

		// getting export filter
		ExportFilter exportFilter = new ExportFilter(config, FilterMode.KML_EXPORT);
		boolean isBBoxActive = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getActive().booleanValue();
		// bounding box config
		Tiling tiling = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();

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
		
		Properties props = new Properties();
		props.put(Marshaller.JAXB_FRAGMENT, new Boolean(true));

		path = config.getInternal().getExportFileName().trim();
		if (path.lastIndexOf(File.separator) == -1) {
			if (path.lastIndexOf(".") == -1) {
				filename = path;
			}
			else {
				filename = path.substring(0, path.lastIndexOf("."));
			}
			path = ".";
		}
		else {
			if (path.lastIndexOf(".") == -1) {
				filename = path.substring(path.lastIndexOf(File.separator) + 1);
			}
			else {
				filename = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf("."));
			}
			path = path.substring(0, path.lastIndexOf(File.separator));
		}

		if (isBBoxActive && tiling.getMode() != TilingMode.NO_TILING) {
			try {
				int activeDisplayFormsAmount = 
					de.tub.citydb.config.project.kmlExporter.KmlExporter.getActiveDisplayFormsAmount(config.getProject().getKmlExporter().getBuildingDisplayForms());
				Logger.getInstance().info(String.valueOf(rows * columns * activeDisplayFormsAmount) +
					 	" (" + rows + "x" + columns + "x" + activeDisplayFormsAmount +
					 	") tiles will be generated."); 
			}
			catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		else {
			rows = 1;
			columns = 1;
		}

		for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
			if (!displayForm.isActive()) continue;

			alreadyExported = new HashMap<String, CityObject4JSON>();

			for (int i = 0; shouldRun && i < rows; i++) {
				for (int j = 0; shouldRun && j < columns; j++) {

					ConcurrentLinkedQueue<ColladaBundle> buildingQueue = null;
					if (displayForm.getForm() >= DisplayForm.COLLADA ||
							config.getProject().getKmlExporter().getBuildingBalloon().isBalloonContentInSeparateFile()) {
						buildingQueue = new ConcurrentLinkedQueue<ColladaBundle>(); 
					}
					
					File file = null;
					OutputStreamWriter fileWriter = null;
					ZipOutputStream zipOut = null;

					try {
						String fileExtension = config.getProject().getKmlExporter().isExportAsKmz() ? ".kmz" : ".kml";
						if (isBBoxActive && tiling.getMode() != TilingMode.NO_TILING) {
							exportFilter.getBoundingBoxFilter().setActiveTile(i, j);
							file = new File(path + File.separator + filename + "_Tile_"
									 	 	+ i + "_" + j + "_" + displayForm.getName() + fileExtension);
						}
						else {
							file = new File(path + File.separator + filename + "_" + displayForm.getName() + fileExtension);
						}

						eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName(), this));

						// open file for writing
						try {
							if (config.getProject().getKmlExporter().isExportAsKmz()) { 
								zipOut = new ZipOutputStream(new FileOutputStream(file));
								ZipEntry zipEntry = new ZipEntry("doc.kml");
								zipOut.putNextEntry(zipEntry);
								fileWriter = new OutputStreamWriter(zipOut, CHARSET);
							}
							else {
								fileWriter = new OutputStreamWriter(new FileOutputStream(file), CHARSET);
							}
								
							// set output for SAXWriter
							saxWriter.setOutput(new StreamResult(fileWriter), ENCODING);	
						} catch (IOException ioE) {
							Logger.getInstance().error("Failed to open file '" + file.getName() + "' for writing: " + ioE.getMessage());
							return false;
						}

						// create worker pools
						// here we have an open issue: queue sizes are fix...
						ioWriterPool = new SingleWorkerPool<SAXEventBuffer>(
								new IOWriterWorkerFactory(saxWriter),
								100,
								true);

						kmlWorkerPool = new WorkerPool<KmlSplittingResult>(
								minThreads,
								maxThreads,
								new KmlExportWorkerFactory(
										jaxbKmlContext,
										jaxbColladaContext,
										dbPool,
										ioWriterPool,
										kmlFactory,
										cityGMLFactory,
										buildingQueue,
										config,
										eventDispatcher),
										300,
										false);

						// prestart pool workers
						ioWriterPool.prestartCoreWorkers();
						kmlWorkerPool.prestartCoreWorkers();

						// create file header
						KMLHeaderWriter kmlHeader = new KMLHeaderWriter(saxWriter);

						// ok, preparations done. inform user...
						Logger.getInstance().info("Exporting to file: " + file.getAbsolutePath());

						// create kml root element
						KmlType kmlType = kmlFactory.createKmlType();
						JAXBElement<KmlType> kml = kmlFactory.createKml(kmlType);

						DocumentType document = kmlFactory.createDocumentType();
						if (isBBoxActive &&	tiling.getMode() != TilingMode.NO_TILING) {
							document.setName(filename + "_Tile_" + i + "_" + j + "_" + displayForm.getName());
						}
						else {
							document.setName(filename + "_" + displayForm.getName());
						}
						document.setOpen(true);
						kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

						try {
							kmlHeader.setRootElement(kml, jaxbKmlContext, props);
							kmlHeader.startRootElement();

							// make sure header has been written
							saxWriter.flush();

							if (!config.getProject().getKmlExporter().isOneFilePerObject() ||
								config.getProject().getKmlExporter().getFilter().isSetSimpleFilter()) {
								addStyle(displayForm);
							}
							if (isBBoxActive &&	tiling.getMode() != TilingMode.NO_TILING) {
								addBorder(i, j);
							}
						} catch (JAXBException jaxBE) {
							Logger.getInstance().error("I/O error: " + jaxBE.getMessage());
							return false;
						} catch (SAXException saxE) {
							Logger.getInstance().error("I/O error: " + saxE.getMessage());
							return false;
						}

						// get database splitter and start query
						kmlSplitter = null;
						try {
							kmlSplitter = new KmlSplitter(
									dbPool,
									kmlWorkerPool,
									exportFilter,
									displayForm,
									config);

							if (shouldRun)
								kmlSplitter.startQuery();
						} catch (SQLException sqlE) {
							Logger.getInstance().error("SQL error: " + sqlE.getMessage());
							return false;
						}

						try {
							if (shouldRun)
								kmlWorkerPool.shutdownAndWait();

							ioWriterPool.shutdownAndWait();
						} catch (InterruptedException e) {
							System.out.println(e.getMessage());
						}

						// write footer element
						try {
							kmlHeader.endRootElement();
						} catch (SAXException saxE) {
							Logger.getInstance().error("XML error: " + saxE.getMessage());
							return false;
						}

						eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("kmlExport.dialog.writingToFile"), this));

						// flush sax writer and close file
						try {
							saxWriter.flush();
							if (config.getProject().getKmlExporter().isExportAsKmz()) { 
								zipOut.closeEntry();

								// ZipOutputStream must be accessed sequentially and is not thread-safe
								if (buildingQueue != null) {
									ColladaBundle colladaBundle = buildingQueue.poll();
									while (colladaBundle != null) {
										// ----------------- model saving -----------------
										if (colladaBundle.getColladaAsString() != null) {
											// File.separator would be wrong here, it MUST be "/"
											ZipEntry zipEntry = new ZipEntry(colladaBundle.getBuildingId() + "/" 
													+ colladaBundle.getBuildingId() + ".dae");
											zipOut.putNextEntry(zipEntry);
											zipOut.write(colladaBundle.getColladaAsString().getBytes(CHARSET));
											zipOut.closeEntry();
										}

										// ----------------- balloon saving -----------------
										if (colladaBundle.getExternalBalloonFileContent() != null) {
											ZipEntry zipEntry = new ZipEntry(BalloonTemplateHandlerImpl.balloonDirectoryName + "/" + colladaBundle.getBuildingId() + ".html");
											zipOut.putNextEntry(zipEntry);
											zipOut.write(colladaBundle.getExternalBalloonFileContent().getBytes(CHARSET));
											zipOut.closeEntry();
										}

										// ----------------- image saving -----------------
										if (colladaBundle.getTexOrdImages() != null) {
											Set<String> keySet = colladaBundle.getTexOrdImages().keySet();
											Iterator<String> iterator = keySet.iterator();
											while (iterator.hasNext()) {
												String imageFilename = iterator.next();
												OrdImage texOrdImage = colladaBundle.getTexOrdImages().get(imageFilename);
												if (texOrdImage.getContentLength() < 1) continue;
//												byte[] ordImageBytes = texOrdImage.getDataInByteArray();
												byte[] ordImageBytes = texOrdImage.getBlobContent().getBytes(1, (int)texOrdImage.getBlobContent().length());

												ZipEntry zipEntry = imageFilename.startsWith("..") ?
																	new ZipEntry(imageFilename.substring(3)): // skip .. and File.separator
																	new ZipEntry(colladaBundle.getBuildingId() + "/" + imageFilename);
												try {
													zipOut.putNextEntry(zipEntry);
//													zipOut.write(ordImageBytes, 0, bytes_read);
													zipOut.write(ordImageBytes, 0, ordImageBytes.length);
													zipOut.closeEntry();
												}
												catch (ZipException ze) {} // ignore repeated images
											}
										}

										if (colladaBundle.getTexImages() != null) {
											Set<String> keySet = colladaBundle.getTexImages().keySet();
											Iterator<String> iterator = keySet.iterator();
											while (iterator.hasNext()) {
												String imageFilename = iterator.next();
												BufferedImage texImage = colladaBundle.getTexImages().get(imageFilename);
												String imageType = imageFilename.substring(imageFilename.lastIndexOf('.') + 1);

												ZipEntry zipEntry = imageFilename.startsWith("..") ?
																    new ZipEntry(imageFilename.substring(3)): // skip .. and File.separator
																    new ZipEntry(colladaBundle.getBuildingId() + "/" + imageFilename);
												try {
													zipOut.putNextEntry(zipEntry);
													ImageIO.write(texImage, imageType, zipOut);
													zipOut.closeEntry();
												}
												catch (ZipException ze) {} // ignore repeated images
											}
										}
										colladaBundle = buildingQueue.poll();
									}
								}

								zipOut.close();
							}
							fileWriter.close();
						}
						catch (Exception ioe) {
							Logger.getInstance().error("I/O error: " + ioe.getMessage());
							try {
								fileWriter.close();
							}
							catch (Exception e) {}
							return false;
						}

						eventDispatcher.triggerEvent(new StatusDialogMessage(" ", this));

						// finally join eventDispatcher
						try {
							eventDispatcher.flushEvents();
						} catch (InterruptedException iE) {
							Logger.getInstance().error("Internal error: " + iE.getMessage());
							return false;
						}

						// set null
						ioWriterPool = null;
						kmlWorkerPool = null;
						kmlSplitter = null;

					}
/*
					catch (FileNotFoundException fnfe) {
						Logger.getInstance().error("Path \"" + path + "\" not found.");
						return false;
					}
*/
					finally {}
				}
			}
		}

		if (isBBoxActive) {
			try {
				eventDispatcher.triggerEvent(new StatusDialogTitle(filename + ".kml", this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("kmlExport.dialog.writingMainFile"), this));
				generateMasterFile();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}

		if (config.getProject().getKmlExporter().isWriteJSONFile()) {
			try {
				Logger.getInstance().info("Writing file: " + filename + ".json");
				File jsonFile = new File(path + File.separator + filename + ".json");
				FileOutputStream outputStream = new FileOutputStream(jsonFile);
				outputStream.write("{\n".getBytes(CHARSET));

				Iterator<String> iterator = alreadyExported.keySet().iterator();
				while (iterator.hasNext()) {
					String gmlId = iterator.next();
					outputStream.write(("\t\"" + gmlId + "\": {").toString().getBytes(CHARSET));
					outputStream.write(alreadyExported.get(gmlId).toString().getBytes(CHARSET));
					if (iterator.hasNext()) {
						outputStream.write(",\n".getBytes(CHARSET));
					}
				}

				outputStream.write("\n}\n".getBytes(CHARSET));
				outputStream.close();
			}
			catch (IOException ioe) {
				Logger.getInstance().error("I/O error: " + ioe.getMessage());
//				ioe.printStackTrace();
			}
		}
		
		eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.finish.msg"), this));
/*
		Logger.getInstance().info("Exported CityGML features:");
		int appearances = 0;
		for (DisplayForm displayForm : config.getProject().getKmlExporter().getDisplayLevels()) {
			if (displayForm.isActive() && DisplayForm.COLLADA == displayForm.getLevel()) {
				appearances = de.tub.citydb.config.project.kmlExporter.KmlExporter.THEME_NONE.equals(selectedTheme)? 0 : 1;
				break;
			}
		}
*/
		// show exported features
		if (!featureCounterMap.isEmpty()) {
			Logger.getInstance().info("Exported CityGML features:");
			for (CityGMLClass type : featureCounterMap.keySet())
				Logger.getInstance().info(type + ": " + featureCounterMap.get(type));
		}
		Logger.getInstance().info("Processed geometry objects: " + geometryCounter);

		return shouldRun;
	}

	public int calculateRowsColumnsAndDelta() throws SQLException {
		TiledBoundingBox bbox = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox();
		TilingMode tilingMode = bbox.getTiling().getMode();
		double autoTileSideLength = config.getProject().getKmlExporter().getAutoTileSideLength();

		tileMatrix = new BoundingBox(new BoundingBoxCorner(bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY()),
										new BoundingBoxCorner(bbox.getUpperRightCorner().getX(), bbox.getUpperRightCorner().getY()));

		DatabaseSrs dbSrs = dbPool.getActiveConnectionMetaData().getReferenceSystem();

		if (bbox.getSrs() == null) {
			throw new SQLException("Unknown BoundingBox srs");
		}

		if (bbox.getSrs().getSrid() != 0 && bbox.getSrs().getSrid() != dbSrs.getSrid()) {
			wgs84TileMatrix = DBUtil.transformBBox(tileMatrix, bbox.getSrs(), WGS84_2D);
			tileMatrix = DBUtil.transformBBox(tileMatrix, bbox.getSrs(), dbSrs);
		}
		else {
			wgs84TileMatrix = DBUtil.transformBBox(tileMatrix, dbSrs, WGS84_2D);
		}
		
		if (tilingMode == TilingMode.NO_TILING) {
			rows = 1;
			columns = 1;
		}
		else if (tilingMode == TilingMode.AUTOMATIC) {
			// approximate
			rows = (int)((tileMatrix.getUpperRightCorner().getY() - tileMatrix.getLowerLeftCorner().getY()) / autoTileSideLength) + 1;
			columns = (int)((tileMatrix.getUpperRightCorner().getX() - tileMatrix.getLowerLeftCorner().getX()) / autoTileSideLength) + 1;
			bbox.getTiling().setRows(rows);
			bbox.getTiling().setColumns(columns);
		}
		else {
			rows = bbox.getTiling().getRows();
			columns = bbox.getTiling().getColumns();
		}

		// must be done like this to avoid non-matching tile limits
		wgs84DeltaLatitude = (wgs84TileMatrix.getUpperRightCorner().getY() - wgs84TileMatrix.getLowerLeftCorner().getY()) / rows;
		wgs84DeltaLongitude = (wgs84TileMatrix.getUpperRightCorner().getX() - wgs84TileMatrix.getLowerLeftCorner().getX()) / columns;
		
		return rows*columns;
	}


	private void generateMasterFile() throws FileNotFoundException,
											 SQLException,
											 JAXBException,
											 DatatypeConfigurationException { 

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

		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		Properties props = new Properties();
		props.put(Marshaller.JAXB_FRAGMENT, new Boolean(true));
//		props.put(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
//		props.put(Marshaller.JAXB_ENCODING, ENCODING);

		TilingMode tilingMode = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling().getMode();

		try {
			File mainFile = new File(path + File.separator + filename + ".kml");
			FileOutputStream outputStream = new FileOutputStream(mainFile);
			saxWriter.setOutput(new StreamResult(outputStream), ENCODING);	

			ioWriterPool = new SingleWorkerPool<SAXEventBuffer>(
					new IOWriterWorkerFactory(saxWriter),
					100,
					true);
			ioWriterPool.prestartCoreWorkers();

			// create file header
			KMLHeaderWriter kmlHeader = new KMLHeaderWriter(saxWriter);

			// create kml root element
			KmlType kmlType = kmlFactory.createKmlType();
			JAXBElement<KmlType> kml = kmlFactory.createKml(kmlType);
			DocumentType document = kmlFactory.createDocumentType();
			document.setOpen(true);
			document.setName(filename);
			LookAtType lookAtType = kmlFactory.createLookAtType();
			lookAtType.setLongitude((wgs84TileMatrix.getUpperRightCorner().getX() + wgs84TileMatrix.getLowerLeftCorner().getX())/2);
			lookAtType.setLatitude((wgs84TileMatrix.getLowerLeftCorner().getY() + (wgs84TileMatrix.getUpperRightCorner().getY() - wgs84TileMatrix.getLowerLeftCorner().getY())/3));
			lookAtType.setAltitude(0.0);
			lookAtType.setHeading(0.0);
			lookAtType.setTilt(60.0);
			lookAtType.setRange(970.0);
			document.setAbstractViewGroup(kmlFactory.createLookAt(lookAtType));
			kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

			try {
				kmlHeader.setRootElement(kml, jaxbKmlContext, props);
				kmlHeader.startRootElement();
				if (config.getProject().getKmlExporter().isOneFilePerObject()) {
					for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {
						addStyle(displayForm);
					}
				}
				// make sure header has been written
				saxWriter.flush();
			}
			catch (SAXException saxE) {
				Logger.getInstance().error("I/O error: " + saxE.getMessage());
			}

			if (config.getProject().getKmlExporter().isShowBoundingBox()) {
				SAXEventBuffer tmp = new SAXEventBuffer();
				
				StyleType frameStyleType = kmlFactory.createStyleType();
				frameStyleType.setId("frameStyle");
				LineStyleType frameLineStyleType = kmlFactory.createLineStyleType();
				frameLineStyleType.setWidth(4.0);
				frameStyleType.setLineStyle(frameLineStyleType);
				marshaller.marshal(kmlFactory.createStyle(frameStyleType), tmp);

				PlacemarkType placemarkType = kmlFactory.createPlacemarkType();
				placemarkType.setName("Bounding box border");
				placemarkType.setStyleUrl("#" + frameStyleType.getId());
				LineStringType lineStringType = kmlFactory.createLineStringType();
				lineStringType.setTessellate(true);
				lineStringType.getCoordinates().add("" + (wgs84TileMatrix.getLowerLeftCorner().getX() - BORDER_GAP) + "," + (wgs84TileMatrix.getLowerLeftCorner().getY() - BORDER_GAP * .5));
				lineStringType.getCoordinates().add(" " + (wgs84TileMatrix.getLowerLeftCorner().getX() - BORDER_GAP) + "," + (wgs84TileMatrix.getUpperRightCorner().getY() + BORDER_GAP * .5));
				lineStringType.getCoordinates().add(" " + (wgs84TileMatrix.getUpperRightCorner().getX() + BORDER_GAP) + "," + (wgs84TileMatrix.getUpperRightCorner().getY() + BORDER_GAP * .5));
				lineStringType.getCoordinates().add(" " + (wgs84TileMatrix.getUpperRightCorner().getX() + BORDER_GAP) + "," + (wgs84TileMatrix.getLowerLeftCorner().getY() - BORDER_GAP * .5));
				lineStringType.getCoordinates().add(" " + (wgs84TileMatrix.getLowerLeftCorner().getX() - BORDER_GAP) + "," + (wgs84TileMatrix.getLowerLeftCorner().getY() - BORDER_GAP * .5));
				placemarkType.setAbstractGeometryGroup(kmlFactory.createLineString(lineStringType));
				
				marshaller.marshal(kmlFactory.createPlacemark(placemarkType), tmp);
				ioWriterPool.addWork(tmp);
			}

			int upperLevelVisibility = -1; 
			for (int i = DisplayForm.COLLADA; i >= DisplayForm.FOOTPRINT; i--) {
				DisplayForm df = new DisplayForm(i, -1, -1);
				int indexOfDf = config.getProject().getKmlExporter().getBuildingDisplayForms().indexOf(df); 
				df = config.getProject().getKmlExporter().getBuildingDisplayForms().get(indexOfDf);

				if (df.isActive()) {
					df.setVisibleUpTo(upperLevelVisibility);
					upperLevelVisibility = df.getVisibleFrom();
				}
			}
			
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {

					// must be done like this to avoid non-matching tile limits
					double wgs84TileSouthLimit = wgs84TileMatrix.getLowerLeftCorner().getY() + (i * wgs84DeltaLatitude); 
					double wgs84TileNorthLimit = wgs84TileMatrix.getLowerLeftCorner().getY() + ((i+1) * wgs84DeltaLatitude); 
					double wgs84TileWestLimit = wgs84TileMatrix.getLowerLeftCorner().getX() + (j * wgs84DeltaLongitude); 
					double wgs84TileEastLimit = wgs84TileMatrix.getLowerLeftCorner().getX() + ((j+1) * wgs84DeltaLongitude); 

					// tileName should not contain special characters,
					// since it will be used as filename for all displayForm files
					String tileName = filename;
					if (tilingMode != TilingMode.NO_TILING) {
						tileName = tileName + "_Tile_" + i + "_" + j;
					}
					FolderType folderType = kmlFactory.createFolderType();
					folderType.setName(tileName);

					for (DisplayForm displayForm : config.getProject().getKmlExporter().getBuildingDisplayForms()) {

						if (!displayForm.isActive()) continue;

						String fileExtension = config.getProject().getKmlExporter().isExportAsKmz() ? ".kmz" : ".kml";
						String tilenameForDisplayForm = tileName + "_" + displayForm.getName() + fileExtension; 

						NetworkLinkType networkLinkType = kmlFactory.createNetworkLinkType();
						networkLinkType.setName("Display as " + displayForm.getName());

						RegionType regionType = kmlFactory.createRegionType();

						LatLonAltBoxType latLonAltBoxType = kmlFactory.createLatLonAltBoxType();
						latLonAltBoxType.setNorth(wgs84TileNorthLimit);
						latLonAltBoxType.setSouth(wgs84TileSouthLimit);
						latLonAltBoxType.setEast(wgs84TileEastLimit);
						latLonAltBoxType.setWest(wgs84TileWestLimit);

						LodType lodType = kmlFactory.createLodType();
						lodType.setMinLodPixels((double)displayForm.getVisibleFrom());
						lodType.setMaxLodPixels((double)displayForm.getVisibleUpTo());

						regionType.setLatLonAltBox(latLonAltBoxType);
						regionType.setLod(lodType);

						LinkType linkType = kmlFactory.createLinkType();
						linkType.setHref(tilenameForDisplayForm);
						linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getProject().getKmlExporter().getViewRefreshMode()));
						linkType.setViewFormat("");
						if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP) {
							linkType.setViewRefreshTime(config.getProject().getKmlExporter().getViewRefreshTime());
						}

						// confusion between atom:link and kml:Link in ogckml22.xsd
						networkLinkType.getRest().add(kmlFactory.createLink(linkType));
						networkLinkType.setRegion(regionType);
						folderType.getAbstractFeatureGroup().add(kmlFactory.createNetworkLink(networkLinkType));
					}
					SAXEventBuffer tmp = new SAXEventBuffer();
					marshaller.marshal(kmlFactory.createFolder(folderType), tmp);
					ioWriterPool.addWork(tmp);
				}
			}

			try {
				ioWriterPool.shutdownAndWait();
				kmlHeader.endRootElement();
				saxWriter.flush();
			} catch (SAXException saxE) {
				Logger.getInstance().error("I/O error: " + saxE.getMessage());
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
			outputStream.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
	}

	private void addStyle(DisplayForm currentDisplayForm) throws JAXBException {
		if (!currentDisplayForm.isActive()) return;
		FeatureClass featureFilter = config.getProject().getKmlExporter().getFilter().getComplexFilter().getFeatureClass();
		if (featureFilter.isSetVegetation()) {
			addStyle(currentDisplayForm,
					 config.getProject().getKmlExporter().getVegetationDisplayForms(),
					 SolitaryVegetationObject.STYLE_BASIS_NAME);
		}
		if (featureFilter.isSetCityObjectGroup()) {
			addStyle(config.getProject().getKmlExporter().getCityObjectGroupDisplayForms().get(0), // hard-coded for groups
					 config.getProject().getKmlExporter().getCityObjectGroupDisplayForms(),
					 CityObjectGroup.STYLE_BASIS_NAME);
		}
		if (featureFilter.isSetBuilding()) {
			addStyle(currentDisplayForm,
					 config.getProject().getKmlExporter().getBuildingDisplayForms(),
					 Building.STYLE_BASIS_NAME);
		}
	}

	private void addStyle(DisplayForm currentDisplayForm,
						  List<DisplayForm> displayFormsForObjectType,
						  String styleBasisName) throws JAXBException {

		SAXEventBuffer saxBuffer = new SAXEventBuffer();
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
			
			marshaller.marshal(kmlFactory.createStyle(styleFootprintNormal), saxBuffer);

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

				marshaller.marshal(kmlFactory.createStyle(styleFootprintHighlight), saxBuffer);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapFootprint), saxBuffer);
			}

			ioWriterPool.addWork(saxBuffer);
			break;

		case DisplayForm.GEOMETRY:

			boolean isBuilding = Building.STYLE_BASIS_NAME.equals(styleBasisName); // buildings are most complex

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
				styleWallNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.WALL_SURFACE).toString() + "Normal");
			else
				styleWallNormal.setId(styleBasisName + currentDisplayForm.getName() + "Normal");
			marshaller.marshal(kmlFactory.createStyle(styleWallNormal), saxBuffer);

			if (isBuilding) {
				PolyStyleType polyStyleGroundSurface = kmlFactory.createPolyStyleType();
				polyStyleGroundSurface.setColor(hexStringToByteArray("ff00aa00"));
				StyleType styleGroundSurface = kmlFactory.createStyleType();
				styleGroundSurface.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.GROUND_SURFACE).toString() + "Style");
				styleGroundSurface.setPolyStyle(polyStyleGroundSurface);
				styleGroundSurface.setBalloonStyle(balloonStyle);
				marshaller.marshal(kmlFactory.createStyle(styleGroundSurface), saxBuffer);

				LineStyleType lineStyleRoofNormal = kmlFactory.createLineStyleType();
				lineStyleRoofNormal.setColor(hexStringToByteArray(roofLineColor));
				PolyStyleType polyStyleRoofNormal = kmlFactory.createPolyStyleType();
				polyStyleRoofNormal.setColor(hexStringToByteArray(roofFillColor));
				StyleType styleRoofNormal = kmlFactory.createStyleType();
				styleRoofNormal.setId(TypeAttributeValueEnum.fromCityGMLClass(CityGMLClass.ROOF_SURFACE).toString() + "Normal");
				styleRoofNormal.setLineStyle(lineStyleRoofNormal);
				styleRoofNormal.setPolyStyle(polyStyleRoofNormal);
				styleRoofNormal.setBalloonStyle(balloonStyle);
				marshaller.marshal(kmlFactory.createStyle(styleRoofNormal), saxBuffer);
			}

			if (currentDisplayForm.isHighlightingEnabled()) {
				String invisibleColor = Integer.toHexString(DisplayForm.INVISIBLE_COLOR);
				String highlightFillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
				String highlightLineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
/*
				if (indexOfDf != -1) {
					currentDisplayForm = displayFormsForObjectType.get(indexOfDf);
*/
					if (currentDisplayForm.isSetRgba4()) {
						highlightFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba4()));
						invisibleColor = "01" + highlightFillColor.substring(2);
					}
					if (currentDisplayForm.isSetRgba5()) {
						highlightLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba5()));
					}
/*
				}
*/
				LineStyleType lineStyleGeometryInvisible = kmlFactory.createLineStyleType();
				lineStyleGeometryInvisible.setColor(hexStringToByteArray(invisibleColor));
				PolyStyleType polyStyleGeometryInvisible = kmlFactory.createPolyStyleType();
				polyStyleGeometryInvisible.setColor(hexStringToByteArray(invisibleColor));
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
	
				marshaller.marshal(kmlFactory.createStyle(styleGeometryInvisible), saxBuffer);
				marshaller.marshal(kmlFactory.createStyle(styleGeometryHighlight), saxBuffer);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapGeometry), saxBuffer);
			}

			ioWriterPool.addWork(saxBuffer);
			break;

		case DisplayForm.COLLADA:
			
			if (currentDisplayForm.isHighlightingEnabled()) {
				indexOfDf = displayFormsForObjectType.indexOf(currentDisplayForm);
				String invisibleColor = Integer.toHexString(DisplayForm.INVISIBLE_COLOR);
				String highlightFillColor = Integer.toHexString(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
				String highlightLineColor = Integer.toHexString(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
				if (indexOfDf != -1) {
					currentDisplayForm = displayFormsForObjectType.get(indexOfDf);
					if (currentDisplayForm.isSetRgba4()) {
						highlightFillColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba4()));
						invisibleColor = "01" + highlightFillColor.substring(2);
					}
					if (currentDisplayForm.isSetRgba5()) {
						highlightLineColor = DisplayForm.formatColorStringForKML(Integer.toHexString(currentDisplayForm.getRgba5()));
					}
				}

				LineStyleType lineStyleColladaInvisible = kmlFactory.createLineStyleType();
				lineStyleColladaInvisible.setColor(hexStringToByteArray(invisibleColor));
				PolyStyleType polyStyleColladaInvisible = kmlFactory.createPolyStyleType();
				polyStyleColladaInvisible.setColor(hexStringToByteArray(invisibleColor));
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
	
				marshaller.marshal(kmlFactory.createStyle(styleColladaInvisible), saxBuffer);
				marshaller.marshal(kmlFactory.createStyle(styleColladaHighlight), saxBuffer);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapCollada), saxBuffer);
				ioWriterPool.addWork(saxBuffer);
			}
			break;

			default:
			// no style
			break;
		}
	}

	private void addBorder(int i, int j) throws JAXBException {
		SAXEventBuffer saxBuffer = new SAXEventBuffer();
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter() &&
				config.getProject().getKmlExporter().isShowTileBorders()) {
			saxBuffer = new SAXEventBuffer();

			// must be done like this to avoid non-matching tile limits
			double wgs84TileSouthLimit = wgs84TileMatrix.getLowerLeftCorner().getY() + (i * wgs84DeltaLatitude); 
			double wgs84TileNorthLimit = wgs84TileMatrix.getLowerLeftCorner().getY() + ((i+1) * wgs84DeltaLatitude); 
			double wgs84TileWestLimit = wgs84TileMatrix.getLowerLeftCorner().getX() + (j * wgs84DeltaLongitude); 
			double wgs84TileEastLimit = wgs84TileMatrix.getLowerLeftCorner().getX() + ((j+1) * wgs84DeltaLongitude); 

			PlacemarkType placemark = kmlFactory.createPlacemarkType();
			placemark.setName("Tile border");
			LineStringType lineString = kmlFactory.createLineStringType();
			lineString.setTessellate(true);
			lineString.getCoordinates().add(String.valueOf(wgs84TileWestLimit) + "," + wgs84TileSouthLimit);
			lineString.getCoordinates().add(String.valueOf(wgs84TileWestLimit) + "," + wgs84TileNorthLimit);
			lineString.getCoordinates().add(String.valueOf(wgs84TileEastLimit) + "," + wgs84TileNorthLimit);
			lineString.getCoordinates().add(String.valueOf(wgs84TileEastLimit) + "," + wgs84TileSouthLimit);
			lineString.getCoordinates().add(String.valueOf(wgs84TileWestLimit) + "," + wgs84TileSouthLimit);
			placemark.setAbstractGeometryGroup(kmlFactory.createLineString(lineString));

			marshaller.marshal(kmlFactory.createPlacemark(placemark), saxBuffer);
			ioWriterPool.addWork(saxBuffer);
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

	private boolean checkBalloonSettings (Balloon balloonSettings) {
		
		if (balloonSettings.isIncludeDescription() &&
			balloonSettings.getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
			String balloonTemplateFilename = balloonSettings.getBalloonContentTemplateFile();
			if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
				File ballonTemplateFile = new File(balloonTemplateFilename);
				if (!ballonTemplateFile.exists()) {
					Logger.getInstance().error("Balloon template file \"" + balloonTemplateFilename + "\" not found.");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void handleEvent(Event e) throws Exception {

		if (e.getEventType() == EventType.COUNTER &&
				((CounterEvent)e).getType() == CounterType.TOPLEVEL_FEATURE) {

			CityGMLClass type = null;
			Object kmlExportObject = e.getSource();

			if (kmlExportObject instanceof Building) {
				type = CityGMLClass.BUILDING;
			}
			else if (kmlExportObject instanceof CityObjectGroup) {
				type = CityGMLClass.CITY_OBJECT_GROUP;
			}
			else if (kmlExportObject instanceof SolitaryVegetationObject) {
				type = CityGMLClass.SOLITARY_VEGETATION_OBJECT;
			}

			Long counter = featureCounterMap.get(type);
			Long update = ((CounterEvent)e).getCounter();

			if (counter == null)
				featureCounterMap.put(type, update);
			else
				featureCounterMap.put(type, counter + update);
		}
		else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			geometryCounter++;
		}
		else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					Logger.getInstance().log(((InterruptEvent)e).getLogLevelType(), log);

				if (kmlSplitter != null)
					kmlSplitter.shutdown();

				if (kmlWorkerPool != null) {
					kmlWorkerPool.shutdownNow();
				}
			}
		}
	}

	public static HashMap<String, CityObject4JSON> getAlreadyExported() {
		return alreadyExported;
	}

}
