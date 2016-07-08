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
package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ColladaOptions", propOrder={
		"ignoreSurfaceOrientation",
		"generateSurfaceNormals",
		"cropImages",
		"generateTextureAtlases",
		"packingAlgorithm",
		"textureAtlasPots",
		"scaleImages",
		"imageScaleFactor",
		"groupObjects",
		"groupSize"
})
public class ColladaOptions {
	private boolean ignoreSurfaceOrientation;
	private boolean generateSurfaceNormals;
	private boolean cropImages;
	private boolean generateTextureAtlases;
	private int packingAlgorithm;
	private boolean textureAtlasPots;
	private boolean scaleImages;
	private double imageScaleFactor;
	private boolean groupObjects;
	private int groupSize;

	public ColladaOptions() {
		ignoreSurfaceOrientation = false;
		generateSurfaceNormals = false;
		cropImages = false;
		generateTextureAtlases = true;
		packingAlgorithm = 5; // TextureAtlasGenerator.TPIM
		textureAtlasPots = true;
		scaleImages = false;
		imageScaleFactor = 1.0;
		groupObjects = false;
		groupSize = 1;
	}

	public void setIgnoreSurfaceOrientation(boolean ignoreSurfaceOrientation) {
		this.ignoreSurfaceOrientation = ignoreSurfaceOrientation;
	}

	public boolean isIgnoreSurfaceOrientation() {
		return ignoreSurfaceOrientation;
	}

	public void setGenerateSurfaceNormals(boolean generateSurfaceNormals) {
		this.generateSurfaceNormals = generateSurfaceNormals;
	}

	public boolean isGenerateSurfaceNormals() {
		return generateSurfaceNormals;
	}
	
	public void setCropImages(boolean cropImages) {
		this.cropImages = cropImages;
	}

	public boolean isCropImages() {
		return cropImages;
	}
	
	public void setGenerateTextureAtlases(boolean generateTextureAtlases) {
		this.generateTextureAtlases = generateTextureAtlases;
	}

	public boolean isGenerateTextureAtlases() {
		return generateTextureAtlases;
	}

	public void setPackingAlgorithm(int packingAlgorithm) {
		this.packingAlgorithm = packingAlgorithm;
	}

	public int getPackingAlgorithm() {
		return packingAlgorithm;
	}

	public void setTextureAtlasPots(boolean textureAtlasPots) {
		this.textureAtlasPots = textureAtlasPots;
	}

	public boolean isTextureAtlasPots() {
		return textureAtlasPots;
	}

	public void setScaleImages(boolean scaleImages) {
		this.scaleImages = scaleImages;
	}

	public boolean isScaleImages() {
		return scaleImages;
	}

	public void setImageScaleFactor(double imageScaleFactor) {
		this.imageScaleFactor = imageScaleFactor;
	}

	public double getImageScaleFactor() {
		return imageScaleFactor;
	}

	public void setGroupObjects(boolean groupObjects) {
		this.groupObjects = groupObjects;
	}

	public boolean isGroupObjects() {
		return groupObjects;
	}

	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	public int getGroupSize() {
		return groupSize;
	}

	
}
