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
package org.citydb.modules.citygml.common.database.xlink;

import org.citydb.api.geometry.GeometryObject;

public class DBXlinkTextureCoordList implements DBXlink {
	private long id;
	private String gmlId;
	private String texParamGmlId;
	private GeometryObject textureCoord;
	private long targetId;
	
	private long surfaceGeometryId;
	private boolean isReverse;

	public DBXlinkTextureCoordList(long id, 
			String gmlId, 
			String texParamGmlId, 
			long targetId) {
		this.id = id;
		this.gmlId = gmlId;
		this.texParamGmlId = texParamGmlId;
		this.targetId = targetId;
	}
	
	public DBXlinkTextureCoordList(long id, 
			String gmlId, 
			String texParamGmlId, 
			GeometryObject textureCoord,
			long targetId) {
		this.id = id;
		this.gmlId = gmlId;
		this.texParamGmlId = texParamGmlId;
		this.textureCoord = textureCoord;
		this.targetId = targetId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String getGmlId() {
		return gmlId;
	}

	@Override
	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getTexParamGmlId() {
		return texParamGmlId;
	}

	public void setTexParamGmlId(String texParamGmlId) {
		this.texParamGmlId = texParamGmlId;
	}

	public GeometryObject getTextureCoord() {
		return textureCoord;
	}

	public void setTextureCoord(GeometryObject textureCoord) {
		this.textureCoord = textureCoord;
	}

	public long getTargetId() {
		return targetId;
	}

	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}

	public long getSurfaceGeometryId() {
		return surfaceGeometryId;
	}

	public void setSurfaceGeometryId(long surfaceGeometryId) {
		this.surfaceGeometryId = surfaceGeometryId;
	}

	public boolean isReverse() {
		return isReverse;
	}

	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTURE_COORD_LIST;
	}
}
