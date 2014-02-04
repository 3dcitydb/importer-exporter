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
package de.tub.citydb.modules.citygml.common.database.xlink;

public class DBXlinkTextureFile implements DBXlink {
	private long surfaceDataId;
	private String fileURI;
	private String mimeType;
	private String mimeTypeCodeSpace;
	private boolean hasWorldFile;
	private boolean isTextureAtlas;

	public DBXlinkTextureFile(long surfaceDataId, String fileURI, String mimeType, String mimeTypeCodeSpace) {
		this.surfaceDataId = surfaceDataId;
		this.fileURI = fileURI;
		this.mimeType = mimeType;
		this.mimeTypeCodeSpace = mimeTypeCodeSpace;
	}

	public long getSurfaceDataId() {
		return surfaceDataId;
	}

	public void setSurfaceDataId(long surfaceDataId) {
		this.surfaceDataId = surfaceDataId;
	}

	public String getFileURI() {
		return fileURI;
	}

	public void setFileURI(String fileURI) {
		this.fileURI = fileURI;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeTypeCodeSpace() {
		return mimeTypeCodeSpace;
	}

	public void setMimeTypeCodeSpace(String mimeTypeCodeSpace) {
		this.mimeTypeCodeSpace = mimeTypeCodeSpace;
	}

	public boolean hasWorldFile() {
		return hasWorldFile;
	}

	public void setHasWorldFile(boolean hasWorldFile) {
		this.hasWorldFile = hasWorldFile;
	}
	
	public boolean isTextureAtlas() {
		return isTextureAtlas;
	}

	public void setTextureAtlas(boolean isTextureAtlas) {
		this.isTextureAtlas = isTextureAtlas;
	}

	@Override
	public String getGmlId() {
		// we do not have a gml:id, but fileURI is our identifier
		return fileURI;
	}

	@Override
	public void setGmlId(String gmlid) {
		// we do not need this here since we are not relying on gml:ids
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTURE_FILE;
	}

}
