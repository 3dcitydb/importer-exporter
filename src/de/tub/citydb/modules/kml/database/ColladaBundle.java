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
