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
