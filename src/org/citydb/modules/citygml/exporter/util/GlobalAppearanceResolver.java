package org.citydb.modules.citygml.exporter.util;

import java.util.HashMap;
import java.util.Set;

public class GlobalAppearanceResolver {
	private final long id;
	private final String gmlId;
	
	private HashMap<Long, SurfaceDataTarget> targets;
	
	public GlobalAppearanceResolver(long id, String gmlId) {
		this.id = id;
		this.gmlId = gmlId;
		targets = new HashMap<Long, SurfaceDataTarget>();
	}
	
	public long getId() {
		return id;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void registerSurfaceData(long surfaceDataId, long surfaceGeometryId, String gmlId, boolean isReverse) {
		SurfaceDataTarget target = targets.get(surfaceDataId);
		if (target == null) {
			target = new SurfaceDataTarget();
			targets.put(surfaceDataId, target);
		}
		
		if (!target.geometries.containsKey(surfaceGeometryId))
			target.geometries.put(surfaceGeometryId, new SurfaceGeometryTarget(gmlId, isReverse));
	}
	
	public Set<Long> getSurfaceDataIds() {
		return targets.keySet();
	}
	
	public SurfaceDataTarget getSurfaceDataTarget(long surfaceDataId) {
		return targets.get(surfaceDataId);
	}
	
	public class SurfaceDataTarget {
		private HashMap<Long, SurfaceGeometryTarget> geometries;
		
		private SurfaceDataTarget() {
			geometries = new HashMap<Long, SurfaceGeometryTarget>();
		}
		
		public Set<Long> getSurfaceGeometryIds() {
			return geometries.keySet();
		}
		
		public SurfaceGeometryTarget getSurfaceGeometryTarget(long surfaceGeometryId) {
			return geometries.get(surfaceGeometryId);
		}
	}
	
	public class SurfaceGeometryTarget {
		private final String gmlId;
		private final boolean isReverse;
		
		private SurfaceGeometryTarget(String gmlId, boolean isReverse) {
			this.gmlId = gmlId;
			this.isReverse = isReverse;
		}

		public String getGmlId() {
			return gmlId;
		}

		public boolean isReverse() {
			return isReverse;
		}
	}
}
