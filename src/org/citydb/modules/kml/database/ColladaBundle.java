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

import java.util.HashMap;

import org.citydb.textureAtlas.model.TextureImage;
import org.collada._2005._11.colladaschema.COLLADA;

import net.opengis.kml._2.PlacemarkType;

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
