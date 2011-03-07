package de.tub.citydb.db.kmlExporter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import java.math.BigInteger;

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
/*
	protected VertexInfo (BigInteger vertexId, long surfaceId, double s, double t) {
		setVertexId(vertexId);
		if (texCoords == null) {
			texCoords = new HashMap<Long, TexCoords>();
		}
		texCoords.put(new Long(surfaceId), new TexCoords(s, t));
	}
*/
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
	

	private void setVertexId(BigInteger vertexId) {
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
