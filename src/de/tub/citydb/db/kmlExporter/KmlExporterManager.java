package de.tub.citydb.db.kmlExporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.opengis.kml._2.ObjectFactory;
import net.opengis.kml._2.PlacemarkType;
import oracle.ord.im.OrdImage;
import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.sax.SAXBuffer;

public class KmlExporterManager {
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final WorkerPool<SAXBuffer> ioWriterPool;
	private final ObjectFactory kmlFactory; 
	private final ConcurrentLinkedQueue<ColladaBundle> buildingQueue;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	
	public KmlExporterManager(JAXBContext jaxbKmlContext,
							  JAXBContext jaxbColladaContext,
							  WorkerPool<SAXBuffer> ioWriterPool,
							  ObjectFactory kmlFactory,
							  ConcurrentLinkedQueue<ColladaBundle> buildingQueue,
							  Config config,
							  EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.ioWriterPool = ioWriterPool;
		this.kmlFactory = kmlFactory;
		this.buildingQueue = buildingQueue;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}


	public void print(List<PlacemarkType> placemarkList) throws JAXBException {
		SAXBuffer buffer = new SAXBuffer();
		Marshaller kmlMarshaller = jaxbKmlContext.createMarshaller();
		kmlMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		// all placemarks in this list have the same gmlid,
		// so the balloon must be extracted only once.
		boolean balloonExtracted = false;
		
		for (PlacemarkType placemark: placemarkList) {
			if (placemark != null) {
				String placemarkDescription = placemark.getDescription();
				if (placemarkDescription != null && config.getProject().getKmlExporter().isBalloonContentInSeparateFile()) {

					StringBuffer parentFrame = new StringBuffer(BalloonTemplateHandler.parentFrameStart);
					parentFrame.append(placemark.getName());
					parentFrame.append(BalloonTemplateHandler.parentFrameEnd);
					placemark.setDescription(parentFrame.toString());
					
					if (!balloonExtracted) {
						if (config.getProject().getKmlExporter().isExportAsKmz()) {
							ColladaBundle colladaBundle = new ColladaBundle();
							colladaBundle.setBuildingId(placemark.getName());
							colladaBundle.setExternalBalloonFileContent(placemarkDescription);
					        buildingQueue.add(colladaBundle);
						}
						else {
							String path = config.getInternal().getExportFileName().trim();
							path = path.substring(0, path.lastIndexOf(File.separator));
							try {
								File balloonsDirectory = new File(path + File.separator + BalloonTemplateHandler.balloonDirectoryName);
								if (!balloonsDirectory.exists()) {
									balloonsDirectory.mkdir();
								}
								File htmlFile = new File(balloonsDirectory, placemark.getName() + ".html");
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
				}
				kmlMarshaller.marshal(kmlFactory.createPlacemark(placemark), buffer);
			}
		}
		ioWriterPool.addWork(buffer);
	}

	public void print(ColladaBundle colladaBundle) throws JAXBException, 
														  FileNotFoundException,
														  IOException,
														  SQLException {
		SAXBuffer buffer = new SAXBuffer();
		Marshaller kmlMarshaller = jaxbKmlContext.createMarshaller();
		kmlMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		Marshaller colladaMarshaller = jaxbColladaContext.createMarshaller();
        colladaMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        colladaMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		PlacemarkType placemark = colladaBundle.getPlacemark();
		
		if (placemark != null) {
			String placemarkDescription = placemark.getDescription();
			if (placemarkDescription != null && config.getProject().getKmlExporter().isBalloonContentInSeparateFile()) {

				StringBuffer parentFrame = new StringBuffer(BalloonTemplateHandler.parentFrameStart);
				parentFrame.append(colladaBundle.getBuildingId());
				parentFrame.append(BalloonTemplateHandler.parentFrameEnd);
				placemark.setDescription(parentFrame.toString());
				colladaBundle.setExternalBalloonFileContent(placemarkDescription);
			}
			kmlMarshaller.marshal(kmlFactory.createPlacemark(placemark), buffer);
		}
		ioWriterPool.addWork(buffer);
        colladaBundle.setPlacemark(null); // free heap space


		if (config.getProject().getKmlExporter().isExportAsKmz()) {
			// marshalling in parallel threads should save some time
	        StringWriter sw = new StringWriter();
	        colladaMarshaller.marshal(colladaBundle.getCollada(), sw);
	        colladaBundle.setColladaAsString(sw.toString());
	        colladaBundle.setCollada(null); // free heap space

	        // list will be used at KmlExporter since ZipOutputStream
	        // must be accessed sequentially and is not thread-safe
	        buildingQueue.add(colladaBundle);
		}
		else {
			// --------------- create subfolder ---------------
			String path = config.getInternal().getExportFileName().trim();
			path = path.substring(0, path.lastIndexOf(File.separator));
			File buildingDirectory = new File(path + File.separator + colladaBundle.getBuildingId());
			buildingDirectory.mkdir();

			// ----------------- model saving -----------------
			File buildingModelFile = new File(buildingDirectory, colladaBundle.getBuildingId() + ".dae");
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
					byte[] ordImageBytes = texOrdImage.getDataInByteArray();

					File imageFile = new File(buildingDirectory, imageFilename);
					FileOutputStream outputStream = new FileOutputStream(imageFile);
					outputStream.write(ordImageBytes, 0, ordImageBytes.length);
					outputStream.close();
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
					ImageIO.write(texImage, imageType, imageFile);
				}
			}
			
			// ----------------- balloon saving -----------------
			if (colladaBundle.getExternalBalloonFileContent() != null) {
				try {
					File balloonsDirectory = new File(path + File.separator + BalloonTemplateHandler.balloonDirectoryName);
					if (!balloonsDirectory.exists()) {
						balloonsDirectory.mkdir();
					}
					File htmlFile = new File(balloonsDirectory, placemark.getName() + ".html");
					FileOutputStream outputStream = new FileOutputStream(htmlFile);
					outputStream.write(colladaBundle.getExternalBalloonFileContent().getBytes());
					outputStream.close();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
}
