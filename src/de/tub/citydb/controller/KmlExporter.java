/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import net.opengis.kml._2.DocumentType;
import net.opengis.kml._2.KmlType;
import net.opengis.kml._2.LineStringType;
import net.opengis.kml._2.LineStyleType;
import net.opengis.kml._2.ObjectFactory;
import net.opengis.kml._2.PairType;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PolyStyleType;
import net.opengis.kml._2.StyleMapType;
import net.opengis.kml._2.StyleStateEnumType;
import net.opengis.kml._2.StyleType;
import oracle.ord.im.OrdImage;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.BoundingVolume;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.CityGMLClass;
import org.xml.sax.SAXException;

import de.tub.citydb.concurrent.IOWriterWorkerFactory;
import de.tub.citydb.concurrent.KmlExportWorkerFactory;
import de.tub.citydb.concurrent.SingleWorkerPool;
import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.filter.TiledBoundingBox;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.kmlExporter.BalloonTemplateHandler;
import de.tub.citydb.db.kmlExporter.ColladaBundle;
import de.tub.citydb.db.kmlExporter.KmlSplitter;
import de.tub.citydb.db.kmlExporter.KmlSplittingResult;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.StatusDialogMessage;
import de.tub.citydb.event.statistic.StatusDialogTitle;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.FilterMode;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.SAXWriter;
import de.tub.citydb.sax.KMLHeaderWriter;
import de.tub.citydb.util.DBUtil;

public class KmlExporter implements EventListener {
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final DBConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private CityGMLFactory cityGMLFactory; 
	private ObjectFactory kmlFactory; 
	private WorkerPool<KmlSplittingResult> kmlWorkerPool;
	private SingleWorkerPool<SAXBuffer> ioWriterPool;
	private KmlSplitter kmlSplitter;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private static final double BORDER_GAP = 0.000001;

	private static final String ENCODING = "UTF-8";

	private final int WGS84_SRID = 4326;

	private BoundingVolume tileMatrix;
	private BoundingVolume wgs84TileMatrix;

	private double wgs84DeltaLongitude;
	private double wgs84DeltaLatitude;

	private static int rows;
	private static int columns;

	private String path;
	private String filename;

	private HashSet<String> alreadyExported;

	public KmlExporter (JAXBContext jaxbKmlContext,
						JAXBContext jaxbColladaContext,
						DBConnectionPool dbPool,
						Config config,
						EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	public boolean doProcess() {
		// get config shortcuts
		de.tub.citydb.config.project.system.System system = config.getProject().getKmlExporter().getSystem();

		// worker pool settings
		int minThreads = system.getThreadPool().getDefaultPool().getMinThreads();
		int maxThreads = system.getThreadPool().getDefaultPool().getMaxThreads();

		// adding listener
		eventDispatcher.addListener(EventType.Interrupt, this);

		// checking workspace...
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getKmlExportWorkspace();
		if (!workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.checkWorkspace(workspace);

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
		DBUtil dbUtil = DBUtil.getInstance(dbPool);
		try {
			if (!dbUtil.isIndexed("CITYOBJECT", "ENVELOPE") || 
					!dbUtil.isIndexed("SURFACE_GEOMETRY", "GEOMETRY")) {
				Logger.getInstance().error("Spatial indexes are not activated.");
				Logger.getInstance().error("Please use the preferences tab to activate the spatial indexes.");
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
				for (DisplayLevel displayLevel : config.getProject().getKmlExporter().getDisplayLevels()) {
					if (displayLevel.getLevel() == DisplayLevel.COLLADA && displayLevel.isActive()) {
						if (!DBUtil.getInstance(dbPool).getAppearanceThemeList(workspace).contains(selectedTheme)) {
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
		
		if (config.getProject().getKmlExporter().isIncludeDescription()) {
			String balloonTemplateFilename = config.getProject().getKmlExporter().getBalloonContentTemplateFile();
			if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
				File ballonTemplateFile = new File(balloonTemplateFilename);
				if (!ballonTemplateFile.exists()) {
					Logger.getInstance().error("Balloon template file \"" + balloonTemplateFilename + "\" not found.");
					return false;
				}
			}
		}


		// create a saxWriter instance 
		// define indent for xml output and namespace mappings
		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setIndentString("  ");
		saxWriter.forceNSDecl("http://www.opengis.net/kml/2.2", ""); // default namespace
		saxWriter.forceNSDecl("http://www.google.com/kml/ext/2.2", "gx");
		saxWriter.forceNSDecl("http://www.w3.org/2005/Atom", "atom");
		saxWriter.forceNSDecl("urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", "xal");

		kmlFactory = new ObjectFactory();		
		cityGMLFactory = new CityGMLFactory();		

		Properties props = new Properties();
		props.put(Marshaller.JAXB_FRAGMENT, new Boolean(true));

		path = config.getInternal().getExportFileName().trim();
		if (path.lastIndexOf(File.separator) == -1) {
			filename = path;
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

		// getting export filter
		ExportFilter exportFilter = new ExportFilter(config, DBUtil.getInstance(dbPool), FilterMode.KML_EXPORT);

		// bounding box config
		Tiling tiling = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();
		boolean useTiling = exportFilter.getBoundingBoxFilter().isActive() && tiling.getMode() != TilingMode.NO_TILING;


		if (useTiling) {
			try {
				int activeDisplayLevelAmount = config.getProject().getKmlExporter().getActiveDisplayLevelAmount();
				Logger.getInstance().info(String.valueOf(rows * columns * activeDisplayLevelAmount) +
					 	" (" + rows + "x" + columns + "x" + activeDisplayLevelAmount +
					 	") tiles will be generated."); 
				generateMasterFile();
			}
			catch (FileNotFoundException fnfe) {
				return false;
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

		for (DisplayLevel displayLevel : config.getProject().getKmlExporter().getDisplayLevels()) {
			if (!displayLevel.isActive()) continue;

			alreadyExported = new HashSet<String>();

			for (int i = 0; shouldRun && i < rows; i++) {
				for (int j = 0; shouldRun && j < columns; j++) {

					ConcurrentLinkedQueue<ColladaBundle> buildingQueue = null;
					if (displayLevel.getLevel() >= DisplayLevel.COLLADA ||
							config.getProject().getKmlExporter().isBalloonContentInSeparateFile()) {
						buildingQueue = new ConcurrentLinkedQueue<ColladaBundle>(); 
					}
					
					try {
						String fileExtension = config.getProject().getKmlExporter().isExportAsKmz() ? ".kmz" : ".kml";
						File file = null;
						if (useTiling) {
							exportFilter.getBoundingBoxFilter().setActiveTile(i, j);
							file = new File(path + File.separator + filename + "_Tile_"
									 	 	+ i + "_" + j + "_" + displayLevel.getName() + fileExtension);
						}
						else {
							file = new File(path + File.separator + filename + "_" + displayLevel.getName() + fileExtension);
						}

						eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName()));

						// open file for writing
						OutputStreamWriter fileWriter = null;
						ZipOutputStream zipOut = null;

						try {
							if (config.getProject().getKmlExporter().isExportAsKmz()) { 
								zipOut = new ZipOutputStream(new FileOutputStream(file));
								ZipEntry zipEntry = new ZipEntry("doc.kml");
								zipOut.putNextEntry(zipEntry);
								fileWriter = new OutputStreamWriter(zipOut);
							}
							else {
								Charset charset = Charset.forName(ENCODING);
								fileWriter = new OutputStreamWriter(new FileOutputStream(file), charset);
							}
						} catch (IOException ioE) {
							Logger.getInstance().error("Failed to open file '" + file.getName() + "' for writing: " + ioE.getMessage());
							return false;
						}

						// reset SAXWriter
						saxWriter.reset();
						saxWriter.setWriter(fileWriter);				

						// create worker pools
						// here we have an open issue: queue sizes are fix...
						ioWriterPool = new SingleWorkerPool<SAXBuffer>(
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

						// ok, preparations done. inform user...
						Logger.getInstance().info("Exporting to file: " + file.getAbsolutePath());

						// create file header
						KMLHeaderWriter xmlHeader = new KMLHeaderWriter(saxWriter);

						// create kml root element
						KmlType kmlType = kmlFactory.createKmlType();
						JAXBElement<KmlType> kml = kmlFactory.createKml(kmlType);

						DocumentType document = kmlFactory.createDocumentType();
						if (useTiling) {
							document.setName(filename + "_Tile_" + i + "_" + j + "_" + displayLevel.getName());
						}
						else {
							document.setName(filename + "_" + displayLevel.getName());
						}
						document.setOpen(true);
						kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

						try {
							xmlHeader.setRootElement(kml, jaxbKmlContext, props);
							xmlHeader.startRootElement();
							addStyleAndBorder(displayLevel, i, j);
						} catch (JAXBException jaxBE) {
							Logger.getInstance().error("I/O error: " + jaxBE.getMessage());
							return false;
						} catch (SAXException saxE) {
							Logger.getInstance().error("I/O error: " + saxE.getMessage());
							return false;
						}

						// flush writer to make sure header has been written
						try {
							saxWriter.flush();
						} catch (IOException ioE) {
							Logger.getInstance().error("I/O error: " + ioE.getMessage());
							return false;
						}

						// get database splitter and start query
						kmlSplitter = null;
						try {
							kmlSplitter = new KmlSplitter(
									dbPool,
									kmlWorkerPool,
									exportFilter,
									displayLevel,
									alreadyExported,
									eventDispatcher,
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
							xmlHeader.endRootElement();
						} catch (SAXException saxE) {
							Logger.getInstance().error("XML error: " + saxE.getMessage());
							return false;
						}

						eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("kmlExport.dialog.writingToFile")));

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
											zipOut.write(colladaBundle.getColladaAsString().getBytes());
											zipOut.closeEntry();
										}

										// ----------------- balloon saving -----------------
										if (colladaBundle.getExternalBalloonFileContent() != null) {
											ZipEntry zipEntry = new ZipEntry(BalloonTemplateHandler.balloonDirectoryName + "/" + colladaBundle.getBuildingId() + ".html");
											zipOut.putNextEntry(zipEntry);
											zipOut.write(colladaBundle.getExternalBalloonFileContent().getBytes());
											zipOut.closeEntry();
										}
										
										// ----------------- image saving -----------------
										if (colladaBundle.getTexOrdImages() != null) {
											Set<String> keySet = colladaBundle.getTexOrdImages().keySet();
											Iterator<String> iterator = keySet.iterator();
											while (iterator.hasNext()) {
												String imageFilename = iterator.next();
												OrdImage texOrdImage = colladaBundle.getTexOrdImages().get(imageFilename);
												byte[] ordImageBytes = texOrdImage.getDataInByteArray();

												ZipEntry zipEntry = new ZipEntry(colladaBundle.getBuildingId() + "/" + imageFilename);
												zipOut.putNextEntry(zipEntry);
												zipOut.write(ordImageBytes, 0, ordImageBytes.length);
												zipOut.closeEntry();
											}
										}

										if (colladaBundle.getTexImages() != null) {
											Set<String> keySet = colladaBundle.getTexImages().keySet();
											Iterator<String> iterator = keySet.iterator();
											while (iterator.hasNext()) {
												String imageFilename = iterator.next();
												BufferedImage texImage = colladaBundle.getTexImages().get(imageFilename);
												String imageType = imageFilename.substring(imageFilename.lastIndexOf('.') + 1);

												ZipEntry zipEntry = new ZipEntry(colladaBundle.getBuildingId() + "/" + imageFilename);
												zipOut.putNextEntry(zipEntry);
												ImageIO.write(texImage, imageType, zipOut);
												zipOut.closeEntry();
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

						eventDispatcher.triggerEvent(new StatusDialogMessage(" "));

						// cleaning up...
						try {
							dbPool.refresh();
						} catch (SQLException e) {
							//
						}

						// finally join eventDispatcher
						try {
							eventDispatcher.join();
						} catch (InterruptedException iE) {
							Logger.getInstance().error("Internal error: " + iE.getMessage());
							return false;
						}

						// set null
						ioWriterPool = null;
						kmlWorkerPool = null;
						kmlSplitter = null;

					}
					finally {}
				}
			}
		}

		eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.finish.msg")));

		return shouldRun;
	}

	public int calculateRowsColumnsAndDelta() throws SQLException {
		DBUtil dbUtil = DBUtil.getInstance(dbPool);
		TiledBoundingBox bbox = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox();
		TilingMode tilingMode = bbox.getTiling().getMode();
		double autoTileSideLength = config.getProject().getKmlExporter().getAutoTileSideLength();

		tileMatrix = new BoundingVolume(new Point(bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(), 0),
										new Point(bbox.getUpperRightCorner().getX(), bbox.getUpperRightCorner().getY(), 0));

		int dbSrid = config.getInternal().getOpenConnection().getMetaData().getSrid();
		if (bbox.getSRS().getSrid() != 0 && bbox.getSRS().getSrid() != dbSrid) {
			wgs84TileMatrix = dbUtil.transformBBox(tileMatrix, bbox.getSRS().getSrid(), WGS84_SRID);
			tileMatrix = dbUtil.transformBBox(tileMatrix, bbox.getSRS().getSrid(), dbSrid);
		}
		else {
			wgs84TileMatrix = dbUtil.transformBBox(tileMatrix, dbSrid, WGS84_SRID);
		}
		
		if (tilingMode.equals(TilingMode.NO_TILING)) {
			rows = 1;
			columns = 1;
		}
		else if (tilingMode.equals(TilingMode.AUTOMATIC)) {
			// approximate
			rows = (int)((tileMatrix.getUpperCorner().getY() - tileMatrix.getLowerCorner().getY()) / autoTileSideLength) + 1;
			columns = (int)((tileMatrix.getUpperCorner().getX() - tileMatrix.getLowerCorner().getX()) / autoTileSideLength) + 1;
			bbox.getTiling().setRows(rows);
			bbox.getTiling().setColumns(columns);
		}
		else {
			rows = bbox.getTiling().getRows();
			columns = bbox.getTiling().getColumns();
		}

		// must be done like this to avoid non-matching tile limits
		wgs84DeltaLatitude = (wgs84TileMatrix.getUpperCorner().getY() - wgs84TileMatrix.getLowerCorner().getY()) / rows;
		wgs84DeltaLongitude = (wgs84TileMatrix.getUpperCorner().getX() - wgs84TileMatrix.getLowerCorner().getX()) / columns;
		
		return rows*columns;
	}


	private void generateMasterFile() throws FileNotFoundException,
											  SQLException,
											  DatatypeConfigurationException { 

		try {
			File mainFile = new File(path + File.separator + filename + ".kml");
			FileOutputStream outputStream = new FileOutputStream(mainFile);
			
			StringBuffer kmlTree = new StringBuffer();

			kmlTree.append("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\" standalone=\"yes\"?>\n"); 
			kmlTree.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\""); 
			kmlTree.append(" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\">\n");

			kmlTree.append("\t<Document>\n");
			kmlTree.append("\t\t<name>" + filename + "</name>\n");
			kmlTree.append("\t\t<open>1</open>\n");

			kmlTree.append("\t\t<LookAt>\n");
			kmlTree.append("\t\t\t<longitude>" + (wgs84TileMatrix.getUpperCorner().getX() + wgs84TileMatrix.getLowerCorner().getX())/2 + "</longitude>\n");
			kmlTree.append("\t\t\t<latitude>" + (wgs84TileMatrix.getLowerCorner().getY() + (wgs84TileMatrix.getUpperCorner().getY() - wgs84TileMatrix.getLowerCorner().getY())/3) + "</latitude>\n");
			kmlTree.append("\t\t\t<altitude>0.0</altitude>\n");
			kmlTree.append("\t\t\t<heading>0.0</heading>\n");
			kmlTree.append("\t\t\t<tilt>60.0</tilt>\n");
			kmlTree.append("\t\t\t<range>970.0</range>\n");
			kmlTree.append("\t\t</LookAt>\n");

			if (config.getProject().getKmlExporter().isShowBoundingBox()) {

				kmlTree.append("\t\t<Style id=\"frameStyle\">\n");
				kmlTree.append("\t\t\t<LineStyle>\n");
				kmlTree.append("\t\t\t\t<width>4</width>\n");
				kmlTree.append("\t\t\t</LineStyle>\n");
				kmlTree.append("\t\t</Style>\n");

				kmlTree.append("\t\t<Placemark>\n");
				kmlTree.append("\t\t\t<name>Bounding box border</name>\n");
				kmlTree.append("\t\t\t<styleUrl>#frameStyle</styleUrl>\n");
				kmlTree.append("\t\t\t<LineString>\n");
				kmlTree.append("\t\t\t\t<tessellate>1</tessellate>\n");
				kmlTree.append("\t\t\t\t<coordinates>");
				kmlTree.append((wgs84TileMatrix.getLowerCorner().getX() - BORDER_GAP) + "," + (wgs84TileMatrix.getLowerCorner().getY() - BORDER_GAP * .5) + " ");
				kmlTree.append((wgs84TileMatrix.getLowerCorner().getX() - BORDER_GAP) + "," + (wgs84TileMatrix.getUpperCorner().getY() + BORDER_GAP * .5) + " ");
				kmlTree.append((wgs84TileMatrix.getUpperCorner().getX() + BORDER_GAP) + "," + (wgs84TileMatrix.getUpperCorner().getY() + BORDER_GAP * .5) + " ");
				kmlTree.append((wgs84TileMatrix.getUpperCorner().getX() + BORDER_GAP) + "," + (wgs84TileMatrix.getLowerCorner().getY() - BORDER_GAP * .5) + " ");
				kmlTree.append((wgs84TileMatrix.getLowerCorner().getX() - BORDER_GAP) + "," + (wgs84TileMatrix.getLowerCorner().getY() - BORDER_GAP * .5));
				kmlTree.append("\t\t\t\t</coordinates>\n");
				kmlTree.append("\t\t\t</LineString>\n");
				kmlTree.append("\t\t</Placemark>\n");

			}

			outputStream.write(kmlTree.toString().getBytes());

			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {

					// must be done like this to avoid non-matching tile limits
					double wgs84TileSouthLimit = wgs84TileMatrix.getLowerCorner().getY() + (i * wgs84DeltaLatitude); 
					double wgs84TileNorthLimit = wgs84TileMatrix.getLowerCorner().getY() + ((i+1) * wgs84DeltaLatitude); 
					double wgs84TileWestLimit = wgs84TileMatrix.getLowerCorner().getX() + (j * wgs84DeltaLongitude); 
					double wgs84TileEastLimit = wgs84TileMatrix.getLowerCorner().getX() + ((j+1) * wgs84DeltaLongitude); 

					kmlTree = new StringBuffer();
					// tileName should not contain special characters,
					// since it will be used as filename for all displayLevel files
					String tileName = filename + "_Tile_" + i + "_" + j;
					kmlTree.append("\t\t<Folder>\n");
					kmlTree.append("\t\t\t<name>" + tileName + "</name>\n");

					for (DisplayLevel displayLevel : config.getProject().getKmlExporter().getDisplayLevels()) {

						if (!displayLevel.isActive()) continue;

						String fileExtension = config.getProject().getKmlExporter().isExportAsKmz() ? ".kmz" : ".kml";
						String tilenameForDisplayLevel = tileName + "_" + displayLevel.getName() + fileExtension; 

						kmlTree.append("\t\t\t<NetworkLink>\n");
						kmlTree.append("\t\t\t\t<name>Display as " + displayLevel.getName() + "</name>\n");
						kmlTree.append("\t\t\t\t<Region>\n");

						kmlTree.append("\t\t\t\t\t<LatLonAltBox>\n");
						kmlTree.append("\t\t\t\t\t\t<north>" + wgs84TileNorthLimit + "</north>\n");
						kmlTree.append("\t\t\t\t\t\t<south>" + wgs84TileSouthLimit + "</south>\n");
						kmlTree.append("\t\t\t\t\t\t<east>" + wgs84TileEastLimit + "</east>\n");
						kmlTree.append("\t\t\t\t\t\t<west>" + wgs84TileWestLimit + "</west>\n");
						kmlTree.append("\t\t\t\t\t</LatLonAltBox>\n");

						kmlTree.append("\t\t\t\t\t<Lod>\n");
						kmlTree.append("\t\t\t\t\t\t<minLodPixels>" + displayLevel.getVisibleFrom() + "</minLodPixels>\n");
						kmlTree.append("\t\t\t\t\t\t<maxLodPixels>" + displayLevel.getVisibleUpTo() + "</maxLodPixels>\n");
						kmlTree.append("\t\t\t\t\t</Lod>\n");

						kmlTree.append("\t\t\t\t</Region>\n");

						kmlTree.append("\t\t\t\t<Link>\n");
						kmlTree.append("\t\t\t\t\t<href>" + tilenameForDisplayLevel + "</href>\n");
						kmlTree.append("\t\t\t\t\t<viewRefreshMode>onRequest</viewRefreshMode>\n");
						kmlTree.append("\t\t\t\t</Link>\n");

						kmlTree.append("\t\t\t</NetworkLink>\n");
					}
					kmlTree.append("\t\t</Folder>\n");
					outputStream.write(kmlTree.toString().getBytes());
				}
			}

			kmlTree = new StringBuffer();
			kmlTree.append("\t</Document>\n");
			kmlTree.append("</kml>\n");
			outputStream.write(kmlTree.toString().getBytes());
			outputStream.close();
		}
		catch (FileNotFoundException fnfe) {
			Logger.getInstance().error("Path \"" + path + "\" not found.");
			throw fnfe;
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}
	
	public void addStyleAndBorder(DisplayLevel displayLevel, int i, int j) throws JAXBException {
		SAXBuffer saxBuffer = new SAXBuffer();
		Marshaller marshaller = jaxbKmlContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		switch (displayLevel.getLevel()) {
		case DisplayLevel.FOOTPRINT:
		case DisplayLevel.EXTRUDED:
			int indexOfDl = config.getProject().getKmlExporter().getDisplayLevels().indexOf(displayLevel);
			String fillColor = Integer.toHexString(DisplayLevel.DEFAULT_FILL_COLOR);
			String lineColor = Integer.toHexString(DisplayLevel.DEFAULT_LINE_COLOR);
			String hlFillColor = Integer.toHexString(DisplayLevel.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			String hlLineColor = Integer.toHexString(DisplayLevel.DEFAULT_LINE_HIGHLIGHTED_COLOR);
			if (indexOfDl != -1) {
				displayLevel = config.getProject().getKmlExporter().getDisplayLevels().get(indexOfDl);
				if (displayLevel.isSetRgba0()) {
					fillColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba0()));
				}
				if (displayLevel.isSetRgba1()) {
					lineColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba1()));
				}
				if (displayLevel.isSetRgba4()) {
					hlFillColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba4()));
				}
				if (displayLevel.isSetRgba5()) {
					hlLineColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba5()));
				}
			}

			LineStyleType lineStyleFootprintNormal = kmlFactory.createLineStyleType();
			lineStyleFootprintNormal.setColor(hexStringToByteArray(lineColor));
			lineStyleFootprintNormal.setWidth(1.5);
			PolyStyleType polyStyleFootprintNormal = kmlFactory.createPolyStyleType();
			polyStyleFootprintNormal.setColor(hexStringToByteArray(fillColor));
			StyleType styleFootprintNormal = kmlFactory.createStyleType();
			styleFootprintNormal.setId(displayLevel.getName() + "Normal");
			styleFootprintNormal.setLineStyle(lineStyleFootprintNormal);
			styleFootprintNormal.setPolyStyle(polyStyleFootprintNormal);

			marshaller.marshal(kmlFactory.createStyle(styleFootprintNormal), saxBuffer);

			if (config.getProject().getKmlExporter().isFootprintHighlighting()) {
				LineStyleType lineStyleFootprintHighlight = kmlFactory.createLineStyleType();
				lineStyleFootprintHighlight.setColor(hexStringToByteArray(hlLineColor));
				lineStyleFootprintHighlight.setWidth(1.5);
				PolyStyleType polyStyleFootprintHighlight = kmlFactory.createPolyStyleType();
				polyStyleFootprintHighlight.setColor(hexStringToByteArray(hlFillColor));
				StyleType styleFootprintHighlight = kmlFactory.createStyleType();
				styleFootprintHighlight.setId(displayLevel.getName() + "Highlight");
				styleFootprintHighlight.setLineStyle(lineStyleFootprintHighlight);
				styleFootprintHighlight.setPolyStyle(polyStyleFootprintHighlight);

				PairType pairFootprintNormal = kmlFactory.createPairType();
				pairFootprintNormal.setKey(StyleStateEnumType.NORMAL);
				pairFootprintNormal.setStyleUrl("#" + styleFootprintNormal.getId());
				PairType pairFootprintHighlight = kmlFactory.createPairType();
				pairFootprintHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
				pairFootprintHighlight.setStyleUrl("#" + styleFootprintHighlight.getId());
				StyleMapType styleMapFootprint = kmlFactory.createStyleMapType();
				styleMapFootprint.setId(displayLevel.getName() + "Style");
				styleMapFootprint.getPair().add(pairFootprintNormal);
				styleMapFootprint.getPair().add(pairFootprintHighlight);

				marshaller.marshal(kmlFactory.createStyle(styleFootprintHighlight), saxBuffer);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapFootprint), saxBuffer);
			}

			ioWriterPool.addWork(saxBuffer);
			break;

		case DisplayLevel.GEOMETRY:

			PolyStyleType polyStyleGroundSurface = kmlFactory.createPolyStyleType();
			polyStyleGroundSurface.setColor(hexStringToByteArray("ff00aa00"));
			StyleType styleGroundSurface = kmlFactory.createStyleType();
			styleGroundSurface.setId(CityGMLClass.GROUNDSURFACE.toString() + "Style");
			styleGroundSurface.setPolyStyle(polyStyleGroundSurface);

			indexOfDl = config.getProject().getKmlExporter().getDisplayLevels().indexOf(displayLevel);
			String wallFillColor = Integer.toHexString(DisplayLevel.DEFAULT_WALL_FILL_COLOR);
			String wallLineColor = Integer.toHexString(DisplayLevel.DEFAULT_WALL_LINE_COLOR);
			String roofFillColor = Integer.toHexString(DisplayLevel.DEFAULT_ROOF_FILL_COLOR);
			String roofLineColor = Integer.toHexString(DisplayLevel.DEFAULT_ROOF_LINE_COLOR);
			if (indexOfDl != -1) {
				displayLevel = config.getProject().getKmlExporter().getDisplayLevels().get(indexOfDl);
				if (displayLevel.isSetRgba0()) {
					wallFillColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba0()));
				}
				if (displayLevel.isSetRgba1()) {
					wallLineColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba1()));
				}
				if (displayLevel.isSetRgba2()) {
					roofFillColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba2()));
				}
				if (displayLevel.isSetRgba3()) {
					roofLineColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba3()));
				}
			}

			LineStyleType lineStyleWallNormal = kmlFactory.createLineStyleType();
			lineStyleWallNormal.setColor(hexStringToByteArray(wallLineColor));
			PolyStyleType polyStyleWallNormal = kmlFactory.createPolyStyleType();
			polyStyleWallNormal.setColor(hexStringToByteArray(wallFillColor));
			StyleType styleWallNormal = kmlFactory.createStyleType();
			styleWallNormal.setId(CityGMLClass.WALLSURFACE.toString() + "Normal");
			styleWallNormal.setLineStyle(lineStyleWallNormal);
			styleWallNormal.setPolyStyle(polyStyleWallNormal);

			LineStyleType lineStyleRoofNormal = kmlFactory.createLineStyleType();
			lineStyleRoofNormal.setColor(hexStringToByteArray(roofLineColor));
			PolyStyleType polyStyleRoofNormal = kmlFactory.createPolyStyleType();
			polyStyleRoofNormal.setColor(hexStringToByteArray(roofFillColor));
			StyleType styleRoofNormal = kmlFactory.createStyleType();
			styleRoofNormal.setId(CityGMLClass.ROOFSURFACE.toString() + "Normal");
			styleRoofNormal.setLineStyle(lineStyleRoofNormal);
			styleRoofNormal.setPolyStyle(polyStyleRoofNormal);

			marshaller.marshal(kmlFactory.createStyle(styleGroundSurface), saxBuffer);
			marshaller.marshal(kmlFactory.createStyle(styleWallNormal), saxBuffer);
			marshaller.marshal(kmlFactory.createStyle(styleRoofNormal), saxBuffer);

			if (config.getProject().getKmlExporter().isGeometryHighlighting()) {
				String invisibleColor = Integer.toHexString(DisplayLevel.INVISIBLE_COLOR);
				String highlightFillColor = Integer.toHexString(DisplayLevel.EXPLOSION_HIGHLIGHTED_FILL_COLOR);
				String highlightLineColor = Integer.toHexString(DisplayLevel.EXPLOSION_HIGHLIGHTED_LINE_COLOR);
				if (indexOfDl != -1) {
					displayLevel = config.getProject().getKmlExporter().getDisplayLevels().get(indexOfDl);
					if (displayLevel.isSetRgba4()) {
						highlightFillColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba4()));
						invisibleColor = "01" + highlightFillColor.substring(2);
					}
					if (displayLevel.isSetRgba5()) {
						highlightLineColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba5()));
					}
				}

				LineStyleType lineStyleGeometryInvisible = kmlFactory.createLineStyleType();
				lineStyleGeometryInvisible.setColor(hexStringToByteArray(invisibleColor));
				PolyStyleType polyStyleGeometryInvisible = kmlFactory.createPolyStyleType();
				polyStyleGeometryInvisible.setColor(hexStringToByteArray(invisibleColor));
				StyleType styleGeometryInvisible = kmlFactory.createStyleType();
				styleGeometryInvisible.setId(displayLevel.getName() + "StyleInvisible");
				styleGeometryInvisible.setLineStyle(lineStyleGeometryInvisible);
				styleGeometryInvisible.setPolyStyle(polyStyleGeometryInvisible);
	
				LineStyleType lineStyleGeometryHighlight = kmlFactory.createLineStyleType();
				lineStyleGeometryHighlight.setColor(hexStringToByteArray(highlightLineColor));
				PolyStyleType polyStyleGeometryHighlight = kmlFactory.createPolyStyleType();
				polyStyleGeometryHighlight.setColor(hexStringToByteArray(highlightFillColor));
				StyleType styleGeometryHighlight = kmlFactory.createStyleType();
				styleGeometryHighlight.setId(displayLevel.getName() + "StyleHighlight");
				styleGeometryHighlight.setLineStyle(lineStyleGeometryHighlight);
				styleGeometryHighlight.setPolyStyle(polyStyleGeometryHighlight);
	
				PairType pairGeometryNormal = kmlFactory.createPairType();
				pairGeometryNormal.setKey(StyleStateEnumType.NORMAL);
				pairGeometryNormal.setStyleUrl("#" + styleGeometryInvisible.getId());
				PairType pairGeometryHighlight = kmlFactory.createPairType();
				pairGeometryHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
				pairGeometryHighlight.setStyleUrl("#" + styleGeometryHighlight.getId());
				StyleMapType styleMapGeometry = kmlFactory.createStyleMapType();
				styleMapGeometry.setId(displayLevel.getName() +"Style");
				styleMapGeometry.getPair().add(pairGeometryNormal);
				styleMapGeometry.getPair().add(pairGeometryHighlight);
	
				marshaller.marshal(kmlFactory.createStyle(styleGeometryInvisible), saxBuffer);
				marshaller.marshal(kmlFactory.createStyle(styleGeometryHighlight), saxBuffer);
				marshaller.marshal(kmlFactory.createStyleMap(styleMapGeometry), saxBuffer);
			}

			ioWriterPool.addWork(saxBuffer);
			break;

		case DisplayLevel.COLLADA:
			
			if (config.getProject().getKmlExporter().isColladaHighlighting()) {
				indexOfDl = config.getProject().getKmlExporter().getDisplayLevels().indexOf(displayLevel);
				String invisibleColor = Integer.toHexString(DisplayLevel.INVISIBLE_COLOR);
				String highlightFillColor = Integer.toHexString(DisplayLevel.EXPLOSION_HIGHLIGHTED_FILL_COLOR);
				String highlightLineColor = Integer.toHexString(DisplayLevel.EXPLOSION_HIGHLIGHTED_LINE_COLOR);
				if (indexOfDl != -1) {
					displayLevel = config.getProject().getKmlExporter().getDisplayLevels().get(indexOfDl);
					if (displayLevel.isSetRgba4()) {
						highlightFillColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba4()));
						invisibleColor = "01" + highlightFillColor.substring(2);
					}
					if (displayLevel.isSetRgba5()) {
						highlightLineColor = DisplayLevel.formatColorStringForKML(Integer.toHexString(displayLevel.getRgba5()));
					}
				}

				LineStyleType lineStyleColladaInvisible = kmlFactory.createLineStyleType();
				lineStyleColladaInvisible.setColor(hexStringToByteArray(invisibleColor));
				PolyStyleType polyStyleColladaInvisible = kmlFactory.createPolyStyleType();
				polyStyleColladaInvisible.setColor(hexStringToByteArray(invisibleColor));
				StyleType styleColladaInvisible = kmlFactory.createStyleType();
				styleColladaInvisible.setId(displayLevel.getName() + "StyleInvisible");
				styleColladaInvisible.setLineStyle(lineStyleColladaInvisible);
				styleColladaInvisible.setPolyStyle(polyStyleColladaInvisible);
	
				LineStyleType lineStyleColladaHighlight = kmlFactory.createLineStyleType();
				lineStyleColladaHighlight.setColor(hexStringToByteArray(highlightLineColor));
				PolyStyleType polyStyleColladaHighlight = kmlFactory.createPolyStyleType();
				polyStyleColladaHighlight.setColor(hexStringToByteArray(highlightFillColor));
				StyleType styleColladaHighlight = kmlFactory.createStyleType();
				styleColladaHighlight.setId(displayLevel.getName() + "StyleHighlight");
				styleColladaHighlight.setLineStyle(lineStyleColladaHighlight);
				styleColladaHighlight.setPolyStyle(polyStyleColladaHighlight);
	
				PairType pairColladaNormal = kmlFactory.createPairType();
				pairColladaNormal.setKey(StyleStateEnumType.NORMAL);
				pairColladaNormal.setStyleUrl("#" + styleColladaInvisible.getId());
				PairType pairColladaHighlight = kmlFactory.createPairType();
				pairColladaHighlight.setKey(StyleStateEnumType.HIGHLIGHT);
				pairColladaHighlight.setStyleUrl("#" + styleColladaHighlight.getId());
				StyleMapType styleMapCollada = kmlFactory.createStyleMapType();
				styleMapCollada.setId(displayLevel.getName() +"Style");
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

		if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter() &&
			config.getProject().getKmlExporter().isShowTileBorders()) {
			saxBuffer = new SAXBuffer();
			
			// must be done like this to avoid non-matching tile limits
			double wgs84TileSouthLimit = wgs84TileMatrix.getLowerCorner().getY() + (i * wgs84DeltaLatitude); 
			double wgs84TileNorthLimit = wgs84TileMatrix.getLowerCorner().getY() + ((i+1) * wgs84DeltaLatitude); 
			double wgs84TileWestLimit = wgs84TileMatrix.getLowerCorner().getX() + (j * wgs84DeltaLongitude); 
			double wgs84TileEastLimit = wgs84TileMatrix.getLowerCorner().getX() + ((j+1) * wgs84DeltaLongitude); 
			
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
	
	@Override
	public void handleEvent(Event e) throws Exception {

		if (e.getEventType() == EventType.Interrupt) {
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


}
