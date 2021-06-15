/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.vis.database;

import net.opengis.kml._2.DocumentType;
import net.opengis.kml._2.KmlType;
import net.opengis.kml._2.LatLonAltBoxType;
import net.opengis.kml._2.LinkType;
import net.opengis.kml._2.LodType;
import net.opengis.kml._2.NetworkLinkType;
import net.opengis.kml._2.ObjectFactory;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.RegionType;
import net.opengis.kml._2.ViewRefreshModeEnumType;
import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.core.ade.visExporter.ADEVisExportException;
import org.citydb.core.ade.visExporter.ADEVisExportExtension;
import org.citydb.core.ade.visExporter.ADEVisExportExtensionManager;
import org.citydb.core.ade.visExporter.ADEVisExportHelper;
import org.citydb.core.ade.visExporter.ADEVisExportManager;
import org.citydb.core.ade.visExporter.ADEVisExportQueryHelper;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.visExporter.DisplayFormType;
import org.citydb.config.project.visExporter.GltfOptions;
import org.citydb.config.project.visExporter.GltfVersion;
import org.citydb.config.project.visExporter.Style;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.BlobExportAdapter;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.global.CounterEvent;
import org.citydb.util.event.global.CounterType;
import org.citydb.util.log.Logger;
import org.citydb.core.query.Query;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.vis.util.BalloonTemplateHandler;
import org.citydb.vis.util.CityObject4JSON;
import org.citydb.vis.util.ExportTracker;
import org.citygml4j.util.xml.SAXEventBuffer;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class VisExporterManager implements ADEVisExportHelper {
	private final Logger log = Logger.getInstance();
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final WorkerPool<SAXEventBuffer> writerPool;
	private final ExportTracker tracker;
	private final ObjectFactory kmlFactory; 
	private final BlobExportAdapter textureExportAdapter;
	private final EventDispatcher eventDispatcher;
	private final Config config;
	private final ADEVisExportQueryHelper sqlQueries;

	private final boolean useTiling;
	private final HashMap<Integer, Long> objectCounter;
	private final IdentityHashMap<ADEVisExportExtension, ADEVisExportManager> adeVisExportManagers;

	private String mainFilename;
	private final String ENCODING = "UTF-8";
	private final Charset CHARSET = Charset.forName(ENCODING);
	private final String TEMP_FOLDER = "__temp";
	private long implicitId;

	public VisExporterManager(Path outputFile,
                              JAXBContext jaxbKmlContext,
                              JAXBContext jaxbColladaContext,
                              AbstractDatabaseAdapter databaseAdapter,
                              WorkerPool<SAXEventBuffer> writerPool,
                              ExportTracker tracker,
                              Query query,
                              ObjectFactory kmlFactory,
                              BlobExportAdapter textureExportAdapter,
                              EventDispatcher eventDispatcher,
                              Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.databaseAdapter = databaseAdapter;
		this.writerPool = writerPool;
		this.tracker = tracker;
		this.kmlFactory = kmlFactory;
		this.textureExportAdapter = textureExportAdapter;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		sqlQueries = new Queries(databaseAdapter, databaseAdapter.getConnectionDetails().getSchema(), this);
		adeVisExportManagers = new IdentityHashMap<>();

		useTiling = query.isSetTiling();
		mainFilename = outputFile.toAbsolutePath().normalize().toString();
		if (mainFilename.lastIndexOf(File.separator) != -1) {
			if (mainFilename.lastIndexOf(".") == -1) {
				mainFilename = mainFilename.substring(mainFilename.lastIndexOf(File.separator) + 1);
			}
			else {
				mainFilename = mainFilename.substring(mainFilename.lastIndexOf(File.separator) + 1, mainFilename.lastIndexOf("."));
			}
		}
		else {
			if (mainFilename.lastIndexOf(".") != -1) {
				mainFilename = mainFilename.substring(0, mainFilename.lastIndexOf("."));
			}
		}
		mainFilename = mainFilename + ".kml";

		objectCounter = new HashMap<>();
	}
	
	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return databaseAdapter;
	}

	@Override
	public ADEVisExportQueryHelper getSQLQueryHelper() {
		return sqlQueries;
	}

	public void updateFeatureTracker(DBSplittingResult work) {
		Long counter = objectCounter.get(work.getObjectClassId());
		if (counter == null)
			objectCounter.put(work.getObjectClassId(), 1L);
		else
			objectCounter.put(work.getObjectClassId(), counter + 1);		

		tracker.put(work.getId(), work.getJson());
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));
	}

	public HashMap<Integer, Long> getObjectCounter() {
		return objectCounter;
	}

	public long nextImplicitId() {
		// we generate unique ids for implicit geometries for grouping objects.
		// we use negative values to not affect the ids of regular surfaces.
		return --implicitId;
	}

	public ExportTracker getExportTracker() {
		return this.tracker;
	}

	public void print(List<PlacemarkType> placemarkList,
			DBSplittingResult work,
			boolean balloonInSeparateFile) throws JAXBException {
		SAXEventBuffer buffer = new SAXEventBuffer();
		Marshaller kmlMarshaller = jaxbKmlContext.createMarshaller();
		if (useTiling && config.getVisExportConfig().isOneFilePerObject()) {
			kmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
		else {
			kmlMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		}

		// all placemarks in this list belong together (same gmlid),
		// so the balloon must be extracted only once.
		boolean balloonExtracted = false;

		String gmlId = null;
		String placemarkDescription = null;
		KmlType kmlType = null;
		DocumentType document = null;
		ZipOutputStream zipOut = null;
		OutputStreamWriter fileWriter = null;

		String path = tracker.getCurrentWorkingDirectoryPath();
		File directory = new File(path);

		try {
			for (PlacemarkType placemark: placemarkList) {
				if (placemark != null) {
					String displayFormName = work.getDisplayForm().getName();

					if (placemark.getDescription() != null && balloonInSeparateFile) {
						StringBuilder parentFrame = new StringBuilder(BalloonTemplateHandler.parentFrameStart);

						parentFrame.append('.'); // same folder
						parentFrame.append('/').append(BalloonTemplateHandler.balloonDirectoryName);
						parentFrame.append('/').append(work.getGmlId()).append('-').append(work.getId());
						parentFrame.append(BalloonTemplateHandler.parentFrameEnd);

						if (!balloonExtracted) {
							placemarkDescription = placemark.getDescription();

							// --------------- create subfolder ---------------

							if (config.getVisExportConfig().isExportAsKmz()) {
								if (!useTiling || !config.getVisExportConfig().isOneFilePerObject()) {
									// export temporarily as kml, it will be later added to kmz if needed
									directory = new File(path, TEMP_FOLDER);
									if (!directory.exists()) {
										log.info("Creating temporary folder...");
										directory.mkdir();
									}
								}
							}
							else { // export as kml
								if (config.getVisExportConfig().isOneFilePerObject()) {
									directory = new File(path, String.valueOf(work.getId()));
									if (!directory.exists()) {
										directory.mkdir();
									}
								}
							}

							if (!useTiling || !config.getVisExportConfig().isOneFilePerObject() || !config.getVisExportConfig().isExportAsKmz()) {
								try {
									File balloonsDirectory = new File(directory, BalloonTemplateHandler.balloonDirectoryName);
									if (!balloonsDirectory.exists()) {
										balloonsDirectory.mkdir();
									}
									File htmlFile = new File(balloonsDirectory, work.getGmlId() + '-' + work.getId() + ".html");
									FileOutputStream outputStream = new FileOutputStream(htmlFile);
									outputStream.write(placemarkDescription.getBytes(CHARSET));
									outputStream.close();
								}
								catch (IOException ioe) {
									log.logStackTrace(ioe);
								}
							}

							balloonExtracted = true;
						}
						placemark.setDescription(parentFrame.toString());
					}

					if (useTiling && config.getVisExportConfig().isOneFilePerObject()) {
						if (gmlId == null) {
							gmlId = work.getGmlId();

							boolean isHighlighting = false;

							String filename = gmlId + "_" + displayFormName;
							if (placemark.getId().startsWith(config.getVisExportConfig().getIdPrefixes().getPlacemarkHighlight())) {
								filename = filename + "_" + Style.HIGHLIGTHTED_STR;
								isHighlighting = true;
							}

							File placemarkDirectory = new File(path + File.separator + work.getId());
							if (!placemarkDirectory.exists()) {
								placemarkDirectory.mkdir();
							}

							// create kml root element
							kmlType = kmlFactory.createKmlType();
							document = kmlFactory.createDocumentType();
							document.setOpen(true);
							document.setName(filename);
							kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));

							String fileExtension = ".kml";
							if (config.getVisExportConfig().isExportAsKmz()) {
								fileExtension = ".kmz";
								File placemarkFile = new File(placemarkDirectory, filename + ".kmz");
								zipOut = new ZipOutputStream(new FileOutputStream(placemarkFile));
								ZipEntry zipEntry = new ZipEntry("doc.kml");
								zipOut.putNextEntry(zipEntry);
								fileWriter = new OutputStreamWriter(zipOut, CHARSET);
							}
							else {
								File placemarkFile = new File(placemarkDirectory, filename + ".kml");
								fileWriter = new OutputStreamWriter(new FileOutputStream(placemarkFile), CHARSET);
							}

							// the network link pointing to the file
							NetworkLinkType networkLinkType = kmlFactory.createNetworkLinkType();
							LinkType linkType = kmlFactory.createLinkType();

							if (isHighlighting) {
								networkLinkType.setName(gmlId + " " + displayFormName + " " + Style.HIGHLIGTHTED_STR);
								linkType.setHref(work.getId() + "/" + gmlId + "_" + displayFormName + "_" + Style.HIGHLIGTHTED_STR + fileExtension);
							}
							else { // actual placemark, non-highlighting
								networkLinkType.setName(gmlId + " " + displayFormName);
								linkType.setHref(work.getId() + "/" + gmlId + "_" + displayFormName + fileExtension);
							}

							linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getVisExportConfig().getViewRefreshMode()));
							linkType.setViewFormat("");
							if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP) {
								linkType.setViewRefreshTime(config.getVisExportConfig().getViewRefreshTime());
							}

							LatLonAltBoxType latLonAltBoxType = kmlFactory.createLatLonAltBoxType();
							CityObject4JSON cityObject4JSON = tracker.get(work.getId());
							if (cityObject4JSON != null) { // avoid NPE when aborting large KML/COLLADA exports
								latLonAltBoxType.setNorth(cityObject4JSON.getEnvelopeYmax());
								latLonAltBoxType.setSouth(cityObject4JSON.getEnvelopeYmin());
								latLonAltBoxType.setEast(cityObject4JSON.getEnvelopeXmax());
								latLonAltBoxType.setWest(cityObject4JSON.getEnvelopeXmin());
							}

							LodType lodType = kmlFactory.createLodType();
							lodType.setMinLodPixels(config.getVisExportConfig().getSingleObjectRegionSize());
							if (work.getDisplayForm().getVisibleTo() == -1)
								lodType.setMaxLodPixels(-1.0);
							else
								lodType.setMaxLodPixels((double)work.getDisplayForm().getVisibleTo() * (lodType.getMinLodPixels()/work.getDisplayForm().getVisibleFrom()));

							RegionType regionType = kmlFactory.createRegionType();
							regionType.setLatLonAltBox(latLonAltBoxType);
							regionType.setLod(lodType);

							// confusion between atom:link and kml:Link in ogckml22.xsd
							networkLinkType.getRest().add(kmlFactory.createLink(linkType));
							networkLinkType.setRegion(regionType);

							kmlMarshaller.marshal(kmlFactory.createNetworkLink(networkLinkType), buffer);
						}
						placemark.setStyleUrl("../../../../" + mainFilename + placemark.getStyleUrl());
						document.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemark));
					}
					else {
						kmlMarshaller.marshal(kmlFactory.createPlacemark(placemark), buffer);
					}

				}
			}

			if (useTiling && config.getVisExportConfig().isOneFilePerObject() && kmlType != null) { // some Placemarks ARE null
				if (config.getVisExportConfig().isExportAsKmz()) {
					kmlMarshaller.marshal(kmlFactory.createKml(kmlType), fileWriter);
					zipOut.closeEntry();

					if (balloonInSeparateFile) {
						for (PlacemarkType placemark: placemarkList) {
							if (placemark != null) {
								ZipEntry zipEntry = new ZipEntry(BalloonTemplateHandler.balloonDirectoryName + "/" + work.getGmlId() + '-' + work.getId() + ".html");
								if (placemarkDescription != null) {
									zipOut.putNextEntry(zipEntry);
									zipOut.write(placemarkDescription.getBytes(CHARSET));
									zipOut.closeEntry();
									break; // only once since gmlId is the same for all placemarks
								}
							}
						}
					}

					zipOut.close();
				}
				else {
					kmlMarshaller.marshal(kmlFactory.createKml(kmlType), fileWriter);
					fileWriter.close();
				}
			}

			// buffer should not be empty, otherwise cause an error exception in IO Worker
			if (!buffer.isEmpty()) {
				writerPool.addWork(buffer); // placemark or region depending on isOneFilePerObject()
			}      		       		
		}
		catch (IOException ioe) {
			log.logStackTrace(ioe);
		}
	}

	public void print(ColladaBundle colladaBundle, long id, boolean balloonInSeparateFile) throws JAXBException, 
	FileNotFoundException,
	IOException,
	SQLException {
		ZipOutputStream zipOut = null;
		OutputStreamWriter fileWriter = null;
		SAXEventBuffer buffer = new SAXEventBuffer();

		Marshaller kmlMarshaller = jaxbKmlContext.createMarshaller();
		if (useTiling && config.getVisExportConfig().isOneFilePerObject()) {
			kmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
		else {
			kmlMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		}

		Marshaller colladaMarshaller = jaxbColladaContext.createMarshaller();
		colladaMarshaller.setProperty(Marshaller.JAXB_ENCODING, ENCODING);
		colladaMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		PlacemarkType placemark = colladaBundle.getPlacemark();
		String path = tracker.getCurrentWorkingDirectoryPath();

		if (placemark != null) {
			String placemarkDescription = placemark.getDescription();

			if (placemarkDescription != null && balloonInSeparateFile) {

				StringBuilder parentFrame = new StringBuilder(BalloonTemplateHandler.parentFrameStart);
				if (useTiling && 
						config.getVisExportConfig().isOneFilePerObject() &&
						!config.getVisExportConfig().isExportAsKmz())
					parentFrame.append(".."); // one up
				else
					parentFrame.append("."); // same folder
				parentFrame.append('/').append(BalloonTemplateHandler.balloonDirectoryName);
				parentFrame.append('/').append(colladaBundle.getGmlId()).append('-').append(colladaBundle.getId());
				parentFrame.append(BalloonTemplateHandler.parentFrameEnd);
				placemark.setDescription(parentFrame.toString());
				colladaBundle.setExternalBalloonFileContent(placemarkDescription);
			}
			if (useTiling && config.getVisExportConfig().isOneFilePerObject()) {

				// the file per object
				KmlType kmlType = kmlFactory.createKmlType();
				DocumentType document = kmlFactory.createDocumentType();
				document.setOpen(true);
				document.setName(colladaBundle.getGmlId());
				kmlType.setAbstractFeatureGroup(kmlFactory.createDocument(document));
				document.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemark));

				File placemarkDirectory = new File(path + File.separator + colladaBundle.getId());
				if (!placemarkDirectory.exists()) {
					placemarkDirectory.mkdir();
				}

				String fileExtension = ".kml";
				try {
					if (config.getVisExportConfig().isExportAsKmz()) {
						fileExtension = ".kmz";
						File placemarkFile = new File(placemarkDirectory, colladaBundle.getGmlId() + "_collada.kmz");
						zipOut = new ZipOutputStream(new FileOutputStream(placemarkFile));
						ZipEntry zipEntry = new ZipEntry("doc.kml");
						zipOut.putNextEntry(zipEntry);
						fileWriter = new OutputStreamWriter(zipOut, CHARSET);
						kmlMarshaller.marshal(kmlFactory.createKml(kmlType), fileWriter);
						zipOut.closeEntry();
					}
					else {
						File placemarkFile = new File(placemarkDirectory, colladaBundle.getGmlId() + "_collada.kml");
						fileWriter = new OutputStreamWriter(new FileOutputStream(placemarkFile), CHARSET);
						kmlMarshaller.marshal(kmlFactory.createKml(kmlType), fileWriter);
						fileWriter.close();
					}
				}
				catch (IOException ioe) {
					log.logStackTrace(ioe);
				}

				// the network link pointing to the file
				NetworkLinkType networkLinkType = kmlFactory.createNetworkLinkType();
				networkLinkType.setName(colladaBundle.getGmlId() + " " + DisplayFormType.COLLADA.getName());

				RegionType regionType = kmlFactory.createRegionType();

				LatLonAltBoxType latLonAltBoxType = kmlFactory.createLatLonAltBoxType();
				CityObject4JSON cityObject4JSON = tracker.get(id);
				if (cityObject4JSON != null) { // avoid NPE when aborting large KML/COLLADA exports
					latLonAltBoxType.setNorth(cityObject4JSON.getEnvelopeYmax());
					latLonAltBoxType.setSouth(cityObject4JSON.getEnvelopeYmin());
					latLonAltBoxType.setEast(cityObject4JSON.getEnvelopeXmax());
					latLonAltBoxType.setWest(cityObject4JSON.getEnvelopeXmin());
				}

				LodType lodType = kmlFactory.createLodType();
				lodType.setMinLodPixels(config.getVisExportConfig().getSingleObjectRegionSize());

				regionType.setLatLonAltBox(latLonAltBoxType);
				regionType.setLod(lodType);

				LinkType linkType = kmlFactory.createLinkType();
				linkType.setHref(colladaBundle.getId() + "/" + colladaBundle.getGmlId() + "_" + DisplayFormType.COLLADA.getName() + fileExtension);
				linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getVisExportConfig().getViewRefreshMode()));
				linkType.setViewFormat("");
				if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP) {
					linkType.setViewRefreshTime(config.getVisExportConfig().getViewRefreshTime());
				}

				// confusion between atom:link and kml:Link in ogckml22.xsd
				networkLinkType.getRest().add(kmlFactory.createLink(linkType));
				networkLinkType.setRegion(regionType);

				kmlMarshaller.marshal(kmlFactory.createNetworkLink(networkLinkType), buffer);
			}
			else { // !config.getVisExportConfig().isOneFilePerObject()
				kmlMarshaller.marshal(kmlFactory.createPlacemark(placemark), buffer);
			}

			writerPool.addWork(buffer); // placemark or region depending on isOneFilePerObject()
			colladaBundle.setPlacemark(null); // free heap space
		}

		// so much for the placemark, now model, images and balloon...

		if (config.getVisExportConfig().isExportAsKmz() &&	useTiling
				&& config.getVisExportConfig().isOneFilePerObject()) {

			// marshalling in parallel threads should save some time
			StringWriter sw = new StringWriter();
			colladaMarshaller.marshal(colladaBundle.getCollada(), sw);
			colladaBundle.setColladaAsString(sw.toString());
			colladaBundle.setCollada(null); // free heap space

			// ----------------- model saving -----------------
			ZipEntry zipEntry = new ZipEntry(colladaBundle.getId() + "/" + colladaBundle.getGmlId() + ".dae");
			zipOut.putNextEntry(zipEntry);
			zipOut.write(colladaBundle.getColladaAsString().getBytes(CHARSET));
			zipOut.closeEntry();

			// ----------------- image saving -----------------
			if (colladaBundle.getUnsupportedTexImageIds() != null) {
				Set<String> keySet = colladaBundle.getUnsupportedTexImageIds().keySet();
				Iterator<String> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String imageFilename = iterator.next();
					byte[] ordImageBytes = textureExportAdapter.getInByteArray(colladaBundle.getUnsupportedTexImageIds().get(imageFilename));
					if (ordImageBytes != null) {
						zipEntry = imageFilename.startsWith("..") ?
								new ZipEntry(imageFilename.substring(3)) : // skip .. and File.separator
								new ZipEntry(colladaBundle.getId() + "/" + imageFilename);
						zipOut.putNextEntry(zipEntry);
						zipOut.write(ordImageBytes, 0, ordImageBytes.length);
						zipOut.closeEntry();
					}
				}
			}

			if (colladaBundle.getTexImages() != null) {
				Set<String> keySet = colladaBundle.getTexImages().keySet();
				Iterator<String> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String imageFilename = iterator.next();
					BufferedImage texImage = colladaBundle.getTexImages().get(imageFilename).getBufferedImage();
					String imageType = imageFilename.substring(imageFilename.lastIndexOf('.') + 1);

					zipEntry = imageFilename.startsWith("..") ?
							new ZipEntry(imageFilename.substring(3)) : // skip .. and File.separator
							new ZipEntry(colladaBundle.getId() + "/" + imageFilename);
					zipOut.putNextEntry(zipEntry);
					ImageIO.write(texImage, imageType, zipOut);
					zipOut.closeEntry();
				}
			}

			// ----------------- balloon saving -----------------
			if (colladaBundle.getExternalBalloonFileContent() != null) {
				zipEntry = new ZipEntry(BalloonTemplateHandler.balloonDirectoryName + "/" + colladaBundle.getGmlId() + '-' + colladaBundle.getId() + ".html");
				zipOut.putNextEntry(zipEntry);
				zipOut.write(colladaBundle.getExternalBalloonFileContent().getBytes(CHARSET));
				zipOut.closeEntry();
			}

			zipOut.close();
		}
		else {
			if (config.getVisExportConfig().isExportAsKmz()) {

				// export temporarily as kml, it will be later added to kmz if needed
				File tempFolder = new File(path, TEMP_FOLDER);
				if (!tempFolder.exists()) {
					log.info("Creating temporary folder...");
					tempFolder.mkdir();
				}
				path = path + File.separator + TEMP_FOLDER;
			}

			// --------------- create subfolder ---------------
			File buildingDirectory = new File(path, String.valueOf(colladaBundle.getId()));
			if (!buildingDirectory.exists()) {
				buildingDirectory.mkdir();
			}

			// ----------------- model saving -----------------
			File colladaModelFile = new File(buildingDirectory, colladaBundle.getGmlId() + ".dae");
			File gltfModelFile = new File(buildingDirectory, colladaBundle.getGmlId() + ".gltf");
			FileOutputStream fos = new FileOutputStream(colladaModelFile);
			colladaMarshaller.marshal(colladaBundle.getCollada(), fos);
			fos.close();

			// ----------------- image saving -----------------
			if (colladaBundle.getUnsupportedTexImageIds() != null) {
				for (String imageFilename : colladaBundle.getUnsupportedTexImageIds().keySet()) {
					String fileName = buildingDirectory + File.separator + imageFilename;
					textureExportAdapter.writeToFile(colladaBundle.getUnsupportedTexImageIds().get(imageFilename), fileName);
				}
			}

			if (colladaBundle.getTexImages() != null) {
				for (String imageFilename : colladaBundle.getTexImages().keySet()) {
					BufferedImage texImage = colladaBundle.getTexImages().get(imageFilename).getBufferedImage();
					String imageType = imageFilename.substring(imageFilename.lastIndexOf('.') + 1);

					File imageFile = new File(buildingDirectory, imageFilename);
					if (!imageFile.exists()) // avoid overwriting and access conflicts
						ImageIO.write(texImage, imageType, imageFile);
				}
			}

			// ----------------- create glTF -----------------
			if (config.getVisExportConfig().getGltfOptions().isCreateGltfModel()) {
				convertColladaToglTF(buildingDirectory, colladaModelFile, gltfModelFile);

				if (config.getVisExportConfig().getGltfOptions().isEmbedTextures()
						&& config.getVisExportConfig().getGltfOptions().isRemoveColladaFiles()
						&& gltfModelFile.exists()) {
					for (String imageFilename : colladaBundle.getTexImages().keySet()) {
						File imageFile = new File(buildingDirectory, imageFilename);
						if (imageFile.exists())
							imageFile.delete();
					}
				}
			}

			// ----------------- balloon saving -----------------
			if (colladaBundle.getExternalBalloonFileContent() != null) {
				try {
					File balloonsDirectory = new File(buildingDirectory + File.separator + BalloonTemplateHandler.balloonDirectoryName);
					if (!balloonsDirectory.exists()) {
						balloonsDirectory.mkdir();
					}
					File htmlFile = new File(balloonsDirectory, colladaBundle.getGmlId() + '-' + colladaBundle.getId() + ".html");
					FileOutputStream outputStream = new FileOutputStream(htmlFile);
					outputStream.write(colladaBundle.getExternalBalloonFileContent().getBytes(CHARSET));
					outputStream.close();
				}
				catch (IOException ioe) {
					log.logStackTrace(ioe);
				}
			}
		}
	}

	private void convertColladaToglTF(File buildingDirectory, File colladaModelFile, File gltfModelFile) {
		GltfOptions gltfOptions = config.getVisExportConfig().getGltfOptions();

		String collada2gltfPath = gltfOptions.getPathToConverter();
		File collada2gltfFile = new File(CoreConstants.IMPEXP_HOME.resolve(collada2gltfPath).toString());
		if (collada2gltfFile.exists()) {
			List<String> commands = new ArrayList<>();
			commands.add(collada2gltfFile.getAbsolutePath());
			commands.add("-i");
			commands.add(colladaModelFile.getAbsolutePath());
			commands.add("-o");
			commands.add(gltfModelFile.getAbsolutePath());
			commands.add("-v");
			commands.add(gltfOptions.getGltfVersion() == GltfVersion.v1_0 ? "1.0" : "2.0");
			if (!gltfOptions.isEmbedTextures()) {
				commands.add("-t");
			}
			if (gltfOptions.isUseBinaryGltf()) {
				commands.add("-b");
			}
			// only use Draco compressions with gltF 2.0
			if (gltfOptions.getGltfVersion() == GltfVersion.v2_0 && gltfOptions.isUseDracoCompression()) {
				commands.add("-d");
			}

			try {
				Process process = new ProcessBuilder(commands)
						.directory(buildingDirectory)
						.start();

				int exitCode = process.waitFor();
				if (exitCode != 0) {
					throw new IOException("Exit code: " + exitCode);
				}
			} catch (Exception e) {
				log.error("COLLADA2GLTF failed to convert '" + colladaModelFile.getAbsolutePath() + "'.", e);
			} finally {
				if (gltfOptions.isRemoveColladaFiles()
						&& (gltfModelFile.exists()
						|| (new File(gltfModelFile.getAbsolutePath().replace(".gltf", ".glb"))).exists())) {
					colladaModelFile.delete();
				}
			}
		}
	}

	public ADEVisExportManager getADEVisExportManager(ADEExtension adeExtension) {
		ADEVisExportManager adeVisExportManager = null;

		ADEVisExportExtension adeVisExportExtension = ADEVisExportExtensionManager.getInstance().getADEVisExportExtension(adeExtension);
		if (adeVisExportExtension != null) {
			adeVisExportManager = adeVisExportManagers.get(adeVisExportExtension);
			if (adeVisExportManager == null) {
				adeVisExportManager = adeVisExportExtension.createADEVisExportManager();
				adeVisExportManager.init(this);
				adeVisExportManagers.put(adeVisExportExtension, adeVisExportManager);
			}
		}

		return adeVisExportManager;
	}

	public ADEVisExportManager getADEVisExportManager(int objectClassId) throws ADEVisExportException {
		ADEExtension adeExtension = ADEExtensionManager.getInstance().getExtensionByObjectClassId(objectClassId);
		ADEVisExportManager adeVisExportManager = getADEVisExportManager(adeExtension);

		if (adeVisExportManager == null) {
			throw new ADEVisExportException("The VIS export extension is not enabled " +
					"for the ADE class '" + ObjectRegistry.getInstance().getSchemaMapping().getFeatureType(objectClassId).getPath() + "'.");
		}

		return adeVisExportManager;
	}

	public List<ADEVisExportManager> getADEVisExportManagers() {
		List<ADEVisExportManager> result = new ArrayList<>();

		for (ADEExtension adeExtension : ADEExtensionManager.getInstance().getExtensions()) {
			ADEVisExportManager adeVisExportManager = getADEVisExportManager(adeExtension);
			if (adeVisExportManager != null) {
				result.add(adeVisExportManager);
			}
		}

		return result;
	}

}

