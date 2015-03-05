/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.kml.database;

import java.util.HashMap;

import net.opengis.kml._2.PlacemarkType;

import org.citydb.textureAtlas.model.TextureImage;
import org.collada._2005._11.colladaschema.COLLADA;

public class ColladaBundle {
	
	// wrapped textures or images in unknown formats (like .rgb)
	// they cannot be "atlased", this is why they must be stored separately
	private HashMap<String, Long> unsupportedTexImageIds;

	// images or atlases in usual formats (like .jpg)
	private HashMap<String, TextureImage> texImages;

	private COLLADA collada;
	private String colladaAsString;
	private PlacemarkType placemark;
	private String gmlId;
	private long id;
	private String externalBalloonFileContent;

	public void setTexImages(HashMap<String, TextureImage> texImages) {
		this.texImages = texImages;
	}

	public HashMap<String, TextureImage> getTexImages() {
		return texImages;
	}

	public 	void setUnsupportedTexImageIds(HashMap<String, Long> unsupportedTexImageIds) {
		this.unsupportedTexImageIds = unsupportedTexImageIds;
	}

	public 	HashMap<String, Long> getUnsupportedTexImageIds() {
		return unsupportedTexImageIds;
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

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getGmlId() {
		return gmlId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
