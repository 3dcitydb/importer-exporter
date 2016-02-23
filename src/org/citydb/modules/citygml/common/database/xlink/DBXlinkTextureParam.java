/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
package org.citydb.modules.citygml.common.database.xlink;



public class DBXlinkTextureParam implements DBXlink {
	private long id;
	private String gmlId;
	private DBXlinkTextureParamEnum type;

	private boolean isTextureParameterization;
	private String texParamGmlId;
	private String worldToTexture;

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
	
	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTUREPARAM;
	}
}
