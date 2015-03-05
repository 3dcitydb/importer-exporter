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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class VertexInfo {
	private BigInteger vertexId;
	private double x;
	private double y;
	private double z;
	private HashMap<Long, TexCoords> texCoords;
	private VertexInfo nextVertexInfo = null;

	protected VertexInfo (BigInteger vertexId, double x, double y, double z) {
		setVertexId(vertexId);
		setX(x);
		setY(y);
		setZ(z);
	}

	protected VertexInfo (BigInteger vertexId) {
		setVertexId(vertexId);
	}

	protected void addTexCoords (long surfaceId, TexCoords texCoordsForThisSurface) {
		if (texCoordsForThisSurface == null) {
			return;
		}
		if (texCoords == null) {
			texCoords = new HashMap<Long, TexCoords>();
		}
		texCoords.put(new Long(surfaceId), texCoordsForThisSurface);
	}

	protected TexCoords getTexCoords (long surfaceId) {
		TexCoords value = null;
		if (texCoords != null) {
			value = texCoords.get(new Long(surfaceId));
		}
		return value;
	}

	protected HashMap<Long, TexCoords> getAllTexCoords () {
		return texCoords;
	}

	protected void addTexCoordsFrom (VertexInfo anotherVertexInfo) {
		if (anotherVertexInfo.texCoords == null) {
			return;
		}
		if (texCoords == null) {
			texCoords = new HashMap<Long, TexCoords>();
		}
		Set<Long> keySet = anotherVertexInfo.texCoords.keySet();
		Iterator<Long> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			Long surfaceId = iterator.next();
			texCoords.put(surfaceId, anotherVertexInfo.getTexCoords(surfaceId));
		}
	}
	

	protected void setVertexId(BigInteger vertexId) {
		this.vertexId = vertexId;
	}

	protected BigInteger getVertexId() {
		return vertexId;
	}

	protected void setX(double x) {
		this.x = x;
	}

	protected double getX() {
		return x;
	}

	protected void setY(double y) {
		this.y = y;
	}

	protected double getY() {
		return y;
	}

	protected void setZ(double z) {
		this.z = z;
	}

	protected double getZ() {
		return z;
	}

	protected void setNextVertexInfo(VertexInfo nextVertexInfo) {
		this.nextVertexInfo = nextVertexInfo;
	}

	protected VertexInfo getNextVertexInfo() {
		return nextVertexInfo;
	}

}
