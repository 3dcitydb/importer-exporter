/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
