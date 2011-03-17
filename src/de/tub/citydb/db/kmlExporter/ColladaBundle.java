package de.tub.citydb.db.kmlExporter;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import net.opengis.kml._2.PlacemarkType;
import oracle.ord.im.OrdImage;

import org.collada._2005._11.colladaschema.COLLADA;

public class ColladaBundle {
	
	// wrapped textures or images in unknown formats (like .rgb)
	// they cannot be "atlased", this is why they must be stored separately
	private HashMap<String, OrdImage> texOrdImages;

	// images or atlases in usual formats (like .jpg)
	private HashMap<String, BufferedImage> texImages;

	private COLLADA collada;
	private String colladaAsString;
	private PlacemarkType placemark;
	private String buildingId;
	private String externalBalloonFileContent;

	public void setTexImages(HashMap<String, BufferedImage> texImages) {
		this.texImages = texImages;
	}

	public HashMap<String, BufferedImage> getTexImages() {
		return texImages;
	}

	public 	void setTexOrdImages(HashMap<String, OrdImage> texOrdImages) {
		this.texOrdImages = texOrdImages;
	}

	public 	HashMap<String, OrdImage> getTexOrdImages() {
		return texOrdImages;
	}

	public void setCollada(COLLADA collada) {
		this.collada = collada;
	}

	public COLLADA getCollada() {
		return collada;
	}

	public void setPlacemark(PlacemarkType placemark) {
		this.placemark = placemark;
	}

	public PlacemarkType getPlacemark() {
		return placemark;
	}

	public void setBuildingId(String buildingId) {
		this.buildingId = buildingId;
	}

	public String getBuildingId() {
		return buildingId;
	}

	public void setColladaAsString(String colladaAsString) {
		this.colladaAsString = colladaAsString;
	}

	public String getColladaAsString() {
		return colladaAsString;
	}

	public void setExternalBalloonFileContent(String externalBalloonFileContent) {
		this.externalBalloonFileContent = externalBalloonFileContent;
	}

	public String getExternalBalloonFileContent() {
		return externalBalloonFileContent;
	}
}
