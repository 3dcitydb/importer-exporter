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
package de.tub.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ColladaOptions", propOrder={
		"ignoreSurfaceOrientation",
		"generateTextureAtlases",
		"packingAlgorithm",
		"textureAtlasPots",
		"scaleImages",
		"imageScaleFactor",
		"groupObjects",
		"groupSize",
})
public class ColladaOptions {
	private boolean ignoreSurfaceOrientation;
	private boolean generateTextureAtlases;
	private int packingAlgorithm;
	private boolean textureAtlasPots;
	private boolean scaleImages;
	private double imageScaleFactor;
	private boolean groupObjects;
	private int groupSize;

	public ColladaOptions() {
		ignoreSurfaceOrientation = false;
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
