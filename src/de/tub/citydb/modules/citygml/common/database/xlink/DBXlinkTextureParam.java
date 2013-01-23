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

public class DBXlinkTextureParam implements DBXlink {
	private long id;
	private String gmlId;
	private DBXlinkTextureParamEnum type;

	private boolean isTextureParameterization;
	private String texParamGmlId;
	private String worldToTexture;
	private String textureCoord;
	private String targetURI;
	private String texCoordListId;

	public DBXlinkTextureParam(long id, String gmlId, DBXlinkTextureParamEnum type) {
		this.id = id;
		this.gmlId = gmlId;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public DBXlinkTextureParamEnum getType() {
		return type;
	}

	public void setType(DBXlinkTextureParamEnum type) {
		this.type = type;
	}

	public boolean isTextureParameterization() {
		return isTextureParameterization;
	}

	public void setTextureParameterization(boolean isTextureParameterization) {
		this.isTextureParameterization = isTextureParameterization;
	}

	public String getTexParamGmlId() {
		return texParamGmlId;
	}

	public void setTexParamGmlId(String texParamGmlId) {
		this.texParamGmlId = texParamGmlId;
	}

	public String getWorldToTexture() {
		return worldToTexture;
	}

	public void setWorldToTexture(String worldToTexture) {
		this.worldToTexture = worldToTexture;
	}

	public String getTextureCoord() {
		return textureCoord;
	}

	public void setTextureCoord(String textureCoord) {
		this.textureCoord = textureCoord;
	}

	public String getTargetURI() {
		return targetURI;
	}

	public void setTargetURI(String targetURI) {
		this.targetURI = targetURI;
	}

	public String getTexCoordListId() {
		return texCoordListId;
	}

	public void setTexCoordListId(String texCoordListId) {
		this.texCoordListId = texCoordListId;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTUREPARAM;
	}
}
