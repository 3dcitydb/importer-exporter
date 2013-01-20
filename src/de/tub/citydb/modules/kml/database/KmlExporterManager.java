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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import oracle.ord.im.OrdImage;

import org.citygml4j.util.xml.SAXEventBuffer;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.modules.kml.database.BalloonTemplateHandlerImpl;
import de.tub.citydb.modules.kml.controller.KmlExporter;
import de.tub.citydb.modules.kml.util.CityObject4JSON;

public class KmlExporterManager {
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final ObjectFactory kmlFactory; 
	private final ConcurrentLinkedQueue<ColladaBundle> buildingQueue;
	private final Config config;
	
	private boolean isBBoxActive;
	private String mainFilename;
	
	private static final String ENCODING = "UTF-8";
	private static final Charset CHARSET = Charset.forName(ENCODING);

	public KmlExporterManager(JAXBContext jaxbKmlContext,
							  JAXBContext jaxbColladaContext,
							  WorkerPool<SAXEventBuffer> ioWriterPool,
							  ObjectFactory kmlFactory,
							  ConcurrentLinkedQueue<ColladaBundle> buildingQueue,
							  Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.ioWriterPool = ioWriterPool;
		this.kmlFactory = kmlFactory;
		this.buildingQueue = buildingQueue;
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

        try {
        	for (PlacemarkType placemark: placemarkList) {
        		if (placemark != null) {
					String displayFormName = work.getDisplayForm().getName();

					if (placemark.getDescription() != null && balloonInSeparateFile) {
						StringBuffer parentFrame = new StringBuffer(BalloonTemplateHandlerImpl.parentFrameStart);
	        			if (isBBoxActive && 
	        				config.getProject().getKmlExporter().isOneFilePerObject() &&
	        				!config.getProject().getKmlExporter().isExportAsKmz())
	        				parentFrame.append(".."); // one up
	        			else
	        				parentFrame.append('.'); // same folder

        				parentFrame.append('/').append(BalloonTemplateHandlerImpl.balloonDirectoryName);
        				parentFrame.append('/').append(work.getGmlId());
        				parentFrame.append(BalloonTemplateHandlerImpl.parentFrameEnd);

        				if (!balloonExtracted) {
        					placemarkDescription = placemark.getDescription();
        					if (config.getProject().getKmlExporter().isExportAsKmz()) {
        						if (!isBBoxActive || !config.getProject().getKmlExporter().isOneFilePerObject()) {
            						ColladaBundle colladaBundle = new ColladaBundle();
            						colladaBundle.setGmlId(work.getGmlId());
            						colladaBundle.setExternalBalloonFileContent(placemarkDescription);
            						buildingQueue.add(colladaBundle);
        						}
        					}	
        					else { // kml
        						String path = config.getInternal().getExportFileName().trim();
        						path = path.substring(0, path.lastIndexOf(File.separator));
        						try {
        							File balloonsDirectory = new File(path + File.separator + BalloonTemplateHandlerImpl.balloonDirectoryName);
        							if (!balloonsDirectory.exists()) {
        								balloonsDirectory.mkdir();
        							}
        							File htmlFile = new File(balloonsDirectory, work.getGmlId() + ".html");
        							FileOutputStream outputStream = new FileOutputStream(htmlFile);
        							outputStream.write(placemarkDescription.getBytes());
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
							String path = config.getInternal().getExportFileName().trim();
							path = path.substring(0, path.lastIndexOf(File.separator));
							boolean isHighlighting = false;

							String filename = gmlId + "_" + displayFormName;
							if (placemark.getId().startsWith(DisplayForm.GEOMETRY_HIGHLIGHTED_PLACEMARK_ID)) {
								filename = filename + "_" + DisplayForm.HIGHLIGTHTED_STR;
								isHighlighting = true;
							}

							File placemarkDirectory = new File(path + File.separator + gmlId);
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
								linkType.setHref(gmlId + "/" + gmlId + "_" + displayFormName + "_" + DisplayForm.HIGHLIGTHTED_STR + fileExtension);
							}
							else { // actual placemark, non-highlighting
								networkLinkType.setName(gmlId + " " + displayFormName);
								linkType.setHref(gmlId + "/" + gmlId + "_" + displayFormName + fileExtension);
							}

							linkType.setViewRefreshMode(ViewRefreshModeEnumType.fromValue(config.getProject().getKmlExporter().getViewRefreshMode()));
							linkType.setViewFormat("");
							if (linkType.getViewRefreshMode() == ViewRefreshModeEnumType.ON_STOP) {
								linkType.setViewRefreshTime(config.getProject().getKmlExporter().getViewRefreshTime());
							}

							LatLonAltBoxType latLonAltBoxType = kmlFactory.createLatLonAltBoxType();
							CityObject4JSON cityObject4JSON = KmlExporter.getAlreadyExported().get(work.getId());
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
       					placemark.setStyleUrl(".." + File.separator + mainFilename + placemark.getStyleUrl());
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
								ZipEntry zipEntry = new ZipEntry(BalloonTemplateHandlerImpl.balloonDirectoryName + "/" + work.getGmlId() + ".html");
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

			ioWriterPool.addWork(buffer); // placemark or region depending on isOneFilePerObject()
        }
        catch (IOException ioe) {
        	ioe.printStackTrace();
        }
	}

	public void print(ColladaBundle colladaBundle,
					  long id,
					  boolean balloonInSeparateFile) throws JAXBException, 
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
		
		if (placemark != null) {
			String placemarkDescription = placemark.getDescription();
			if (placemarkDescription != null && balloonInSeparateFile) {

				StringBuffer parentFrame = new StringBuffer(BalloonTemplateHandlerImpl.parentFrameStart);
    			if (isBBoxActive && 
       				config.getProject().getKmlExporter().isOneFilePerObject() &&
       				!config.getProject().getKmlExporter().isExportAsKmz())
    				parentFrame.append(".."); // one up
    			else
    				parentFrame.append("."); // same folder
				parentFrame.append('/').append(BalloonTemplateHandlerImpl.balloonDirectoryName);
				parentFrame.append('/').append(colladaBundle.getGmlId());
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
//				placemark.setStyleUrl(".." + File.separator + mainFilename + placemark.getStyleUrl());
				document.getAbstractFeatureGroup().add(kmlFactory.createPlacemark(placemark));

				String path = config.getInternal().getExportFileName().trim();
				path = path.substring(0, path.lastIndexOf(File.separator));
				File placemarkDirectory = new File(path + File.separator + colladaBundle.getGmlId());
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
				CityObject4JSON cityObject4JSON = KmlExporter.getAlreadyExported().get(id);
				if (cityObject4JSON != null) { // avoid NPE when aborting large KML/COLLADA exports
					latLonAltBoxType.setNorth(cityObject4JSON.getEnvelopeYmax());
					latLonAltBoxType.setSouth(cityObject4JSON.getEnvelopeYmin());
					latLonAltBoxType.setEast(cityObject4JSON.getEnvelopeXmax());
					latLonAltBoxType.setWest(cityObject4JSON.getEnvelopeXmin());
				}

				LodType lodType = kmlFactory.createLodType();
				lodType.setMinLodPixels(config.getProject().getKmlExporter().getSingleObjectRegionSize());
/*
				if (work.getDisplayForm().getVisibleUpTo() == -1)
					lodType.setMaxLodPixels(-1.0);
				else
					lodType.setMaxLodPixels((double)work.getDisplayForm().getVisibleUpTo() * (lodType.getMinLodPixels()/work.getDisplayForm().getVisibleFrom()));
*/
				regionType.setLatLonAltBox(latLonAltBoxType);
				regionType.setLod(lodType);

				LinkType linkType = kmlFactory.createLinkType();
				linkType.setHref(colladaBundle.getGmlId() + "/" + colladaBundle.getGmlId() + "_" + DisplayForm.COLLADA_STR + fileExtension);
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

		if (config.getProject().getKmlExporter().isExportAsKmz()) {
			// marshalling in parallel threads should save some time
	        StringWriter sw = new StringWriter();
	        colladaMarshaller.marshal(colladaBundle.getCollada(), sw);
	        colladaBundle.setColladaAsString(sw.toString());
	        colladaBundle.setCollada(null); // free heap space

	        // list will be used at KmlExporter since ZipOutputStream
	        // must be accessed sequentially and is not thread-safe
			if (!isBBoxActive || !config.getProject().getKmlExporter().isOneFilePerObject()) {
				buildingQueue.add(colladaBundle);
			}
			else {
				// ----------------- model saving -----------------
				ZipEntry zipEntry = new ZipEntry(colladaBundle.getGmlId() + "/" + colladaBundle.getGmlId() + ".dae");
				zipOut.putNextEntry(zipEntry);
				zipOut.write(colladaBundle.getColladaAsString().getBytes(CHARSET));
				zipOut.closeEntry();

				// ----------------- image saving -----------------
				if (colladaBundle.getTexOrdImages() != null) {
					Set<String> keySet = colladaBundle.getTexOrdImages().keySet();
					Iterator<String> iterator = keySet.iterator();
					while (iterator.hasNext()) {
						String imageFilename = iterator.next();
						OrdImage texOrdImage = colladaBundle.getTexOrdImages().get(imageFilename);
//						byte[] ordImageBytes = texOrdImage.getDataInByteArray();
						byte[] ordImageBytes = texOrdImage.getBlobContent().getBytes(1, (int)texOrdImage.getBlobContent().length());

//						zipEntry = new ZipEntry(imageFilename);
						zipEntry = imageFilename.startsWith("..") ?
								   new ZipEntry(imageFilename.substring(3)): // skip .. and File.separator
								   new ZipEntry(colladaBundle.getGmlId() + "/" + imageFilename);
						zipOut.putNextEntry(zipEntry);
						zipOut.write(ordImageBytes, 0, ordImageBytes.length);
//						zipOut.write(ordImageBytes, 0, bytes_read);
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

//						zipEntry = new ZipEntry(imageFilename);
						zipEntry = imageFilename.startsWith("..") ?
								   new ZipEntry(imageFilename.substring(3)): // skip .. and File.separator
								   new ZipEntry(colladaBundle.getGmlId() + "/" + imageFilename);
						zipOut.putNextEntry(zipEntry);
						ImageIO.write(texImage, imageType, zipOut);
						zipOut.closeEntry();
					}
				}

				// ----------------- balloon saving -----------------
				if (colladaBundle.getExternalBalloonFileContent() != null) {
					zipEntry = new ZipEntry(BalloonTemplateHandlerImpl.balloonDirectoryName + "/" + colladaBundle.getGmlId() + ".html");
					zipOut.putNextEntry(zipEntry);
					zipOut.write(colladaBundle.getExternalBalloonFileContent().getBytes(CHARSET));
					zipOut.closeEntry();
				}

				zipOut.close();
			}
		}
		else { // export as kml
			// --------------- create subfolder ---------------
			String path = config.getInternal().getExportFileName().trim();
			path = path.substring(0, path.lastIndexOf(File.separator));
			File buildingDirectory = new File(path + File.separator + colladaBundle.getGmlId());
			if (!buildingDirectory.exists()) {
				buildingDirectory.mkdir();
			}

			// ----------------- model saving -----------------
			File buildingModelFile = new File(buildingDirectory, colladaBundle.getGmlId() + ".dae");
			FileOutputStream fos = new FileOutputStream(buildingModelFile);
	        colladaMarshaller.marshal(colladaBundle.getCollada(), fos);
	        fos.close();

			// ----------------- image saving -----------------

			// first those wrapped textures or images in unknown formats (like .rgb)
			if (colladaBundle.getTexOrdImages() != null) {
				Set<String> keySet = colladaBundle.getTexOrdImages().keySet();
				Iterator<String> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String imageFilename = iterator.next();
					OrdImage texOrdImage = colladaBundle.getTexOrdImages().get(imageFilename);
					if (texOrdImage.getContentLength() < 1) continue;
					try {
						texOrdImage.getDataInFile(buildingDirectory + File.separator + imageFilename);
					}
					catch (IOException ioEx) {}
					finally {
						texOrdImage.close();
					}
				}
			}

			if (colladaBundle.getTexImages() != null) {
				Set<String> keySet = colladaBundle.getTexImages().keySet();
				Iterator<String> iterator = keySet.iterator();
				while (iterator.hasNext()) {
					String imageFilename = iterator.next();
					BufferedImage texImage = colladaBundle.getTexImages().get(imageFilename);
					String imageType = imageFilename.substring(imageFilename.lastIndexOf('.') + 1);

					File imageFile = new File(buildingDirectory, imageFilename);
					if (!imageFile.exists()) // avoid overwriting and access conflicts
						ImageIO.write(texImage, imageType, imageFile);
				}
			}
			
			// ----------------- balloon saving -----------------
			if (colladaBundle.getExternalBalloonFileContent() != null) {
				try {
					File balloonsDirectory = new File(path + File.separator + BalloonTemplateHandlerImpl.balloonDirectoryName);
					if (!balloonsDirectory.exists()) {
						balloonsDirectory.mkdir();
					}
					File htmlFile = new File(balloonsDirectory, placemark.getName() + ".html");
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

/*
	public void print(StyleType styleType) throws JAXBException {
		SAXEventBuffer buffer = new SAXEventBuffer();
		Marshaller kmlMarshaller = jaxbKmlContext.createMarshaller();
		kmlMarshaller.marshal(kmlFactory.createStyle(styleType), buffer);
		ioWriterPool.addWork(buffer);
	}

	public void print(StyleMapType styleMapType) throws JAXBException {
		SAXEventBuffer buffer = new SAXEventBuffer();
		Marshaller kmlMarshaller = jaxbKmlContext.createMarshaller();
		kmlMarshaller.marshal(kmlFactory.createStyleMap(styleMapType), buffer);
		ioWriterPool.addWork(buffer);
	}
*/

}

