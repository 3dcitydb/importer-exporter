package org.citydb.modules.kml.database;

import java.util.List;

public class SurfaceInfo {
	private final int ringCount;
	private final int[] vertexCount;
	private final List<VertexInfo> vertexInfos;

	public SurfaceInfo(int ringCount, int[] vertexCount, List<VertexInfo> vertexInfos) {
		this.ringCount = ringCount;
		this.vertexCount = vertexCount;
		this.vertexInfos = vertexInfos;
	}

	public int getRingCount() {
		return ringCount;
	}
	
	public int[] getRingCountAsArray() {
		int[] tmp = {ringCount};
		return tmp;
	}

	public int[] getVertexCount() {
		return vertexCount;
	}

	public List<VertexInfo> getVertexInfos() {
		return vertexInfos;
	}	
}
