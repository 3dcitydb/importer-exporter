package de.tub.citydb.modules.citygml.importer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import de.tub.citydb.util.Util;

public class LocalTextureCoordinatesResolver {
	private final HashMap<Long, ParameterizedTextureTarget> textureObjects;
	private final HashMap<String, LinearRingTarget> rings;
	private HashSet<ParameterizedTextureTarget> targets;
	private boolean isActive;

	public LocalTextureCoordinatesResolver() {
		textureObjects = new HashMap<Long, ParameterizedTextureTarget>();
		rings = new HashMap<String, LinearRingTarget>();
		targets = new HashSet<ParameterizedTextureTarget>();
	}
	
	public void reset() {
		textureObjects.clear();
		rings.clear();
		targets.clear();
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void registerLinearRing(String ringId, long surfaceGeometryId, boolean isReverse) {
		if (!isActive || rings.containsKey(ringId))
			return;

		ParameterizedTextureTarget textureObject = textureObjects.get(surfaceGeometryId);
		if (textureObject == null) {
			textureObject = new ParameterizedTextureTarget(surfaceGeometryId, isReverse);
			textureObjects.put(surfaceGeometryId, textureObject);
		}

		LinearRingTarget ringTarget = new LinearRingTarget(textureObject);
		textureObject.addLinearRingTarget(ringTarget);
		rings.put(ringId, ringTarget);
	}

	public boolean setTextureCoordinates(String ringId, String textureCoordinates) {
		LinearRingTarget ring = rings.get(ringId);
		if (ring != null) {
			ring.setTextureCoordinates(textureCoordinates);
			targets.add(ring.getParameterizedTextureTarget());
			return true;
		}

		return false;
	}
	
	public long getSurfaceGeometryId(String ringId) {
		LinearRingTarget ring = rings.get(ringId);
		return (ring != null) ? ring.getParameterizedTextureTarget().getSurfaceGeometryId() : 0;
	}

	public void clearLocalContext() {
		for (ParameterizedTextureTarget textureObject : targets)
			textureObject.clearTextureCoordinates();

		targets.clear();
	}

	public HashSet<ParameterizedTextureTarget> getTargets(){
		return targets;
	}

	public class ParameterizedTextureTarget {
		private final long surfaceGeometryId;
		private final boolean isReverse;
		private List<LinearRingTarget> targetRings;

		private ParameterizedTextureTarget(long surfaceGeometryId, boolean isReverse) {
			this.surfaceGeometryId = surfaceGeometryId;
			this.isReverse = isReverse;
			targetRings = new ArrayList<LinearRingTarget>();
		}

		private void addLinearRingTarget(LinearRingTarget ring) {
			// we rely that the linear rings are registered in the order they
			// appear in the parent surface geometry
			targetRings.add(ring);
		}

		private void clearTextureCoordinates() {
			for (LinearRingTarget ring : targetRings)
				ring.clearTextureCoordinates();
		}

		public String compileTextureCoordinates() {
			StringBuilder builder = new StringBuilder();
			
			for (int i = 0; i < targetRings.size(); i++) {
				LinearRingTarget ring = targetRings.get(i);
				String textureCoordinates = ring.getTextureCoordinates();
				
				if (textureCoordinates != null && textureCoordinates.length() > 0) {
					if (isReverse)
						textureCoordinates = reverseTextureCoordinates(textureCoordinates);
					
					builder.append(textureCoordinates);
				}
				
				if (i < targetRings.size() - 1)
					builder.append(";");
			}
			
			return builder.toString();
		}
		
		public long getSurfaceGeometryId() {
			return surfaceGeometryId;
		}
		
		public String getRingId(int i) {
			if (i < targetRings.size()) {
				LinearRingTarget ring = targetRings.get(i);
				for (Entry<String, LinearRingTarget> entry : rings.entrySet()) {
					if (entry.getValue() == ring)
						return entry.getKey();
				}
			}
			
			return null;
		}
		
		private String reverseTextureCoordinates(String textureCoordinates) {
			String[] coords = textureCoordinates.split("\\s+");
			
			for (int lower = 0, upper = coords.length - 2; lower < upper; lower += 2, upper -= 2) {
				String x = coords[lower];
				String y = coords[lower + 1];

				coords[lower] = coords[upper];
				coords[lower + 1] = coords[upper + 1];
				
				coords[upper] = x;
				coords[upper + 1] = y;
			}
			
			return Util.collection2string(Arrays.asList(coords), " ");
		}
	}

	private class LinearRingTarget {
		private final ParameterizedTextureTarget textureObject;
		private String textureCoordinates;

		private LinearRingTarget(ParameterizedTextureTarget textureObject) {
			this.textureObject = textureObject;
		}

		private ParameterizedTextureTarget getParameterizedTextureTarget() {
			return textureObject;
		}

		private String getTextureCoordinates() {
			return textureCoordinates;
		}

		private void setTextureCoordinates(String textureCoordinates) {
			if (textureCoordinates != null)
				this.textureCoordinates = textureCoordinates.trim();
		}

		private void clearTextureCoordinates() {
			textureCoordinates = null;
		}
	}

}