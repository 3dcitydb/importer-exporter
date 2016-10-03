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
package org.citydb.modules.kml.database;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.common.balloon.BalloonTemplateHandlerImpl;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.modules.kml.util.CityObject4JSON;
import org.citydb.modules.kml.util.ExportTracker;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;

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

public class KmlExporterManager {
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final ExportTracker tracker;
	private final ObjectFactory kmlFactory; 
	private final BlobExportAdapter textureExportAdapter;
	private final EventDispatcher eventDispatcher;
	private final Config config;
	
	private boolean isBBoxActive;
	private String mainFilename;
	private HashMap<CityGMLClass, Long> featureCounterMap;
	
	private final String ENCODING = "UTF-8";
	private final Charset CHARSET = Charset.forName(ENCODING);
	private final String TEMP_FOLDER = "__temp";

	public KmlExporterManager(JAXBContext jaxbKmlContext,
							  JAXBContext jaxbColladaContext,
							  WorkerPool<SAXEventBuffer> ioWriterPool,
							  ExportTracker tracker,
							  ObjectFactory kmlFactory,
							  BlobExportAdapter textureExportAdapter,
							  EventDispatcher eventDispatcher,
							  Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.ioWriterPool = ioWriterPool;
		this.tracker = tracker;
		this.kmlFactory = kmlFactory;
		this.textureExportAdapter = textureExportAdapter;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		isBBoxActive = config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().getActive().booleanValue();
		mainFilename = config.getInternal().getExportFileName().trim();
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
		
		featureCounterMap = new HashMap<CityGMLClass, Long>();
	}
	
	public void updateFeatureTracker(KmlSplittingResult work) {
		Long counter = featureCounterMap.get(work.getCityObjectType());
		if (counter == null)
			featureCounterMap.put(work.getCityObjectType(), new Long(1));
		else
			featureCounterMap.put(work.getCityObjectType(), counter + 1);		
		
		tracker.put(work.getId(), work.getJson());
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, 1, this));
	}
	
	public HashMap<CityGMLClass, Long> getFeatureCounter() {
		return featureCounterMap;
	}
	
	public ExportTracker getExportTracker() {
		return this.tracker;
	}

	public void print(List<PlacemarkType> placemarkList,
					  KmlSplittingResult work,
					  boolean balloonInSeparateFile) throws JAXBException {
		SAXEventBuffer buffer = new SAXEventBuffer();
		Marshaller kmlMarshaller = jaxbKmlContext.createMarshaller();
		if (isBBoxActive && config.getProject().getKmlExporter().isOneFilePerObject()) {
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
						StringBuilder parentFrame = new StringBuilder(BalloonTemplateHandlerImpl.parentFrameStart);

	        			parentFrame.append('.'); // same folder
        				parentFrame.append('/').append(BalloonTemplateHandlerImpl.balloonDirectoryName);
        				parentFrame.append('/').append(work.getGmlId()).append('-').append(work.getId());
        				parentFrame.append(BalloonTemplateHandlerImpl.parentFrameEnd);

        				if (!balloonExtracted) {
        					placemarkDescription = placemark.getDescription();

        					// --------------- create subfolder ---------------
        					
							if (config.getProject().getKmlExporter().isExportAsKmz()) {
								if (!isBBoxActive || !config.getProject().getKmlExporter().isOneFilePerObject()) {
        							// export temporarily as kml, it will be later added to kmz if needed
    								directory = new File(path, TEMP_FOLDER);
    								if (!directory.exists()) {
    									Logger.getInstance().info("Creating temporary folder...");
    									directory.mkdir();
    								}
    							}
							}
							else { // export as kml
								if (config.getProject().getKmlExporter().isOneFilePerObject()) {
									directory = new File(path, String.valueOf(work.getId()));
		        					if (!directory.exists()) {
		        						directory.mkdir();
		        					}
    							}
    						}

    						if (!isBBoxActive || !config.getProject().getKmlExporter().isOneFilePerObject() || !config.getProject().getKmlExporter().isExportAsKmz()) {
	       						try {
	       							File balloonsDirectory = new File(directory, BalloonTemplateHandlerImpl.balloonDirectoryName);
	       							if (!balloonsDirectory.exists()) {
	       								balloonsDirectory.mkdir();
	       							}
	       							File htmlFile = new File(balloonsDirectory, work.getGmlId() + '-' + work.getId() + ".html");
	       							FileOutputStream outputStream = new FileOutputStream(htmlFile);
	       							outputStream.write(placemarkDescription.getBytes(CHARSET));
	       							outputStream.close();
	       						}
	       						catch (IOException ioe) {
	       							ioe.printStackTrace();
	       						}
    						}

        					balloonExtracted = true;
        				}
        				placemark.setDescription(parentFrame.toString());
        			}

        			if (isBBoxActive && config.getProject().getKmlExporter().isOneFilePerObject()) {
        				if (gmlId == null) {
        					gmlId = work.getGmlId();

							boolean isHighlighting = false;

							String filename = gmlId + "_" + displayFormName;
							if (placemark.getId().startsWith(config.getProject().getKmlExporter().getIdPrefixes().getPlacemarkHighlight())) {
								filename = filename + "_" + DisplayForm.HIGHLIGTHTED_STR;
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
							if (config.getProject().getKmlExporter().isExportAsKmz()) {
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
								networkLinkType.setName(gmlId + " " + displayFormName + " " + DisplayForm.HIGHLIGTHTED_STR);
								linkType.setHref(work.getId() + "/" + gmlId + "_" + displayFormName + "_" + DisplayForm.HIGHLIGTHTED_STR + fileExtension);
							}
							else { // actual placemark, non-highlighting
								networkLinkType.setName(gmlId + " " + displayFormName);
								linkType.setHref(work.getId() + "/" + gmlId + "_" + displayFormName + fileExtension);
							}

							linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getProject().getKmlExporter().getViewRefreshMode()));
							linkType.setViewFormat("");
							if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP) {
								linkType.setViewRefreshTime(config.getProject().getKmlExporter().getViewRefreshTime());
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
							lodType.setMinLodPixels(config.getProject().getKmlExporter().getSingleObjectRegionSize());
							if (work.getDisplayForm().getVisibleUpTo() == -1)
								lodType.setMaxLodPixels(-1.0);
							else
								lodType.setMaxLodPixels((double)work.getDisplayForm().getVisibleUpTo() * (lodType.getMinLodPixels()/work.getDisplayForm().getVisibleFrom()));
							
							RegionType regionType = kmlFactory.createRegionType();
							regionType.setLatLonAltBox(latLonAltBoxType);
							regionType.setLod(lodType);

							// confusion between atom:link and kml:Link in ogckml22.xsd
							networkLinkType.getRest().add(kmlFactory.createLink(linkType));
							networkLinkType.setRegion(regionType);

							kmlMarshaller.marshal(kmlFactory.createNetworkLink(networkLinkType), buffer);
        				}
       					placemark.setStyleUrl(".." + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + mainFilename + placemark.getStyleUrl());
        				document.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemark));
        			}
        			else {
        				kmlMarshaller.marshal(kmlFactory.createPlacemark(placemark), buffer);
        			}

        		}
        	}

        	if (isBBoxActive && config.getProject().getKmlExporter().isOneFilePerObject() && kmlType != null) { // some Placemarks ARE null
				if (config.getProject().getKmlExporter().isExportAsKmz()) {
    				kmlMarshaller.marshal(kmlFactory.createKml(kmlType), fileWriter);
					zipOut.closeEntry();

					if (balloonInSeparateFile) {
						for (PlacemarkType placemark: placemarkList) {
							if (placemark != null) {
								ZipEntry zipEntry = new ZipEntry(BalloonTemplateHandlerImpl.balloonDirectoryName + "/" + work.getGmlId() + '-' + work.getId() + ".html");
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
        		ioWriterPool.addWork(buffer); // placemark or region depending on isOneFilePerObject()
        	}      		       		
        }
        catch (IOException ioe) {
        	ioe.printStackTrace();
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
		if (isBBoxActive && config.getProject().getKmlExporter().isOneFilePerObject()) {
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

				StringBuilder parentFrame = new StringBuilder(BalloonTemplateHandlerImpl.parentFrameStart);
    			if (isBBoxActive && 
       				config.getProject().getKmlExporter().isOneFilePerObject() &&
       				!config.getProject().getKmlExporter().isExportAsKmz())
    				parentFrame.append(".."); // one up
    			else
    				parentFrame.append("."); // same folder
				parentFrame.append('/').append(BalloonTemplateHandlerImpl.balloonDirectoryName);
   				parentFrame.append('/').append(colladaBundle.getGmlId()).append('-').append(colladaBundle.getId());
				parentFrame.append(BalloonTemplateHandlerImpl.parentFrameEnd);
				placemark.setDescription(parentFrame.toString());
				colladaBundle.setExternalBalloonFileContent(placemarkDescription);
			}
			if (isBBoxActive && config.getProject().getKmlExporter().isOneFilePerObject()) {
				
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
					if (config.getProject().getKmlExporter().isExportAsKmz()) {
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
					ioe.printStackTrace();
				}

				// the network link pointing to the file
				NetworkLinkType networkLinkType = kmlFactory.createNetworkLinkType();
				networkLinkType.setName(colladaBundle.getGmlId() + " " + DisplayForm.COLLADA_STR);

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
				lodType.setMinLodPixels(config.getProject().getKmlExporter().getSingleObjectRegionSize());

				regionType.setLatLonAltBox(latLonAltBoxType);
				regionType.setLod(lodType);

				LinkType linkType = kmlFactory.createLinkType();
				linkType.setHref(colladaBundle.getId() + "/" + colladaBundle.getGmlId() + "_" + DisplayForm.COLLADA_STR + fileExtension);
				linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getProject().getKmlExporter().getViewRefreshMode()));
				linkType.setViewFormat("");
				if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP) {
					linkType.setViewRefreshTime(config.getProject().getKmlExporter().getViewRefreshTime());
				}

				// confusion between atom:link and kml:Link in ogckml22.xsd
				networkLinkType.getRest().add(kmlFactory.createLink(linkType));
				networkLinkType.setRegion(regionType);

				kmlMarshaller.marshal(kmlFactory.createNetworkLink(networkLinkType), buffer);
			}
			else { // !config.getProject().getKmlExporter().isOneFilePerObject()
				kmlMarshaller.marshal(kmlFactory.createPlacemark(placemark), buffer);
			}

			ioWriterPool.addWork(buffer); // placemark or region depending on isOneFilePerObject()
	        colladaBundle.setPlacemark(null); // free heap space
		}

		// so much for the placemark, now model, images and balloon...

		if (config.getProject().getKmlExporter().isExportAsKmz() &&	isBBoxActive
				&& config.getProject().getKmlExporter().isOneFilePerObject()) {
			
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
	        		byte[] ordImageBytes = textureExportAdapter.getInByteArray(colladaBundle.getUnsupportedTexImageIds().get(imageFilename), imageFilename);
	        		zipEntry = imageFilename.startsWith("..") ?
	        				   new ZipEntry(imageFilename.substring(3)): // skip .. and File.separator
	        					   new ZipEntry(colladaBundle.getId() + "/" + imageFilename);
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
	        		BufferedImage texImage = colladaBundle.getTexImages().get(imageFilename).getBufferedImage();
	        		String imageType = imageFilename.substring(imageFilename.lastIndexOf('.') + 1);

					zipEntry = imageFilename.startsWith("..") ?
							   new ZipEntry(imageFilename.substring(3)): // skip .. and File.separator
								   new ZipEntry(colladaBundle.getId() + "/" + imageFilename);
					zipOut.putNextEntry(zipEntry);
					ImageIO.write(texImage, imageType, zipOut);
					zipOut.closeEntry();
	        	}
			}

			// ----------------- balloon saving -----------------
			if (colladaBundle.getExternalBalloonFileContent() != null) {
	        	zipEntry = new ZipEntry(BalloonTemplateHandlerImpl.balloonDirectoryName + "/" + colladaBundle.getGmlId() + '-' + colladaBundle.getId() + ".html");
				zipOut.putNextEntry(zipEntry);
				zipOut.write(colladaBundle.getExternalBalloonFileContent().getBytes(CHARSET));
				zipOut.closeEntry();
			}

			zipOut.close();
		}
		else {			
			if (config.getProject().getKmlExporter().isExportAsKmz()) {
				
				// export temporarily as kml, it will be later added to kmz if needed
				File tempFolder = new File(path, TEMP_FOLDER);
				if (!tempFolder.exists()) {
					Logger.getInstance().info("Creating temporary folder...");
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
	        
	        // ----------------- create glTF without embedded textures-----------------
			if (config.getProject().getKmlExporter().isCreateGltfModel() && !config.getProject().getKmlExporter().isEmbedTexturesInGltfFiles()) {
				convertColladaToglTF(colladaBundle, buildingDirectory, colladaModelFile, gltfModelFile);
			}	        
	        
			// ----------------- image saving -----------------
			if (colladaBundle.getUnsupportedTexImageIds() != null) {
				Set<String> keySet = colladaBundle.getUnsupportedTexImageIds().keySet();
				Iterator<String> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String imageFilename = iterator.next();
					String fileName = buildingDirectory + File.separator + imageFilename;
					textureExportAdapter.getInFile(colladaBundle.getUnsupportedTexImageIds().get(imageFilename), imageFilename, fileName);					
				}
			}

			if (colladaBundle.getTexImages() != null) {
				Set<String> keySet = colladaBundle.getTexImages().keySet();
				Iterator<String> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String imageFilename = iterator.next();
					BufferedImage texImage = colladaBundle.getTexImages().get(imageFilename).getBufferedImage();
					String imageType = imageFilename.substring(imageFilename.lastIndexOf('.') + 1);

					File imageFile = new File(buildingDirectory, imageFilename);
					if (!imageFile.exists()) // avoid overwriting and access conflicts
						ImageIO.write(texImage, imageType, imageFile);					
				}
			}
	
			// ----------------- create glTF with embedded textures-----------------
			if (config.getProject().getKmlExporter().isCreateGltfModel() && config.getProject().getKmlExporter().isEmbedTexturesInGltfFiles()) {
				convertColladaToglTF(colladaBundle, buildingDirectory, colladaModelFile, gltfModelFile);
				if (config.getProject().getKmlExporter().isNotCreateColladaFiles() && gltfModelFile.exists()) {
					Set<String> keySet = colladaBundle.getTexImages().keySet();
					Iterator<String> iterator = keySet.iterator();
					while (iterator.hasNext()) {
						String imageFilename = iterator.next();
						File imageFile = new File(buildingDirectory, imageFilename);
						if (imageFile.exists()) 
							imageFile.delete();					
					}
				}
			}			
			
			// ----------------- balloon saving -----------------
			if (colladaBundle.getExternalBalloonFileContent() != null) {
				try {
					File balloonsDirectory = new File(buildingDirectory + File.separator + BalloonTemplateHandlerImpl.balloonDirectoryName);
					if (!balloonsDirectory.exists()) {
						balloonsDirectory.mkdir();
					}
					File htmlFile = new File(balloonsDirectory, colladaBundle.getGmlId() + '-' + colladaBundle.getId() + ".html");
					FileOutputStream outputStream = new FileOutputStream(htmlFile);
					outputStream.write(colladaBundle.getExternalBalloonFileContent().getBytes(CHARSET));
					outputStream.close();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	private void convertColladaToglTF(ColladaBundle colladaBundle, File buildingDirectory, File colladaModelFile, File gltfModelFile) {
		String collada2gltfPath = config.getProject().getKmlExporter().getPathOfGltfConverter();
		File collada2gltfFile = new File(collada2gltfPath);
		if (collada2gltfFile.exists()) {
			ProcessBuilder pb = new ProcessBuilder(collada2gltfFile.getAbsolutePath(), "-f", colladaBundle.getGmlId() + ".dae", "-e", "true");
			pb.directory(buildingDirectory);
			try {
				Process process = pb.start();
				process.waitFor();
			} catch (IOException|InterruptedException e) {
				Logger.getInstance().debug("Unexpected errors occured while converting collada to glTF for city object '" + colladaBundle.getGmlId() + "' with output path: '" + gltfModelFile.getAbsolutePath() + "'");
			}
			finally {
				if (config.getProject().getKmlExporter().isNotCreateColladaFiles() && gltfModelFile.exists()) {
					colladaModelFile.delete();
				}
			}
		}		
	}
}

