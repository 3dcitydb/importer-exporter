package de.tub.citydb.filter.feature;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.FilterBoundingBox;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;
import de.tub.citygml4j.geometry.BoundingVolume;
import de.tub.citygml4j.geometry.Point;
import de.tub.citygml4j.model.gml.DirectPosition;
import de.tub.citygml4j.model.gml.Envelope;

public class BoundingBoxFilter implements Filter<Envelope> {
	private final FilterConfig filter;
	private boolean isActive;
	private FilterBoundingBox boundingBoxFilter;
	private BoundingVolume boundingBox;
	
	public BoundingBoxFilter(Config config, FilterMode mode) {	
		if (mode == FilterMode.EXPORT)
			filter = config.getProject().getExporter().getFilter();
		else
			filter = config.getProject().getImporter().getFilter();
			
		init();
	}
	
	private void init() {
		isActive = filter.isSetComplex() &&
			filter.getComplexFilter().getBoundingBoxFilter().isSet();

		if (isActive) {
			boundingBoxFilter = filter.getComplexFilter().getBoundingBoxFilter();
			
			Double minX = boundingBoxFilter.getLowerLeftCorner().getX();
			Double minY = boundingBoxFilter.getLowerLeftCorner().getY();
			Double maxX = boundingBoxFilter.getUpperRightCorner().getX();
			Double maxY = boundingBoxFilter.getUpperRightCorner().getY();

			if (minX != null && minY != null && maxX != null && maxY != null) {
				boundingBox = new BoundingVolume(
						new Point(minX, minY, 0),
						new Point(maxX, maxY, 0)
				);
			} else
				isActive = false;
		}
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}
	
	public boolean filter(Envelope envelope) {
		if (isActive) {
			DirectPosition lowerCorner = envelope.getLowerCorner();
			DirectPosition upperCorner = envelope.getUpperCorner();
			
			// if the envelope information is not sufficient we filter 
			if (lowerCorner == null || upperCorner == null)
				return true;
			
			Double minX = lowerCorner.getValue().get(0);
			Double minY = lowerCorner.getValue().get(1);

			Double maxX = upperCorner.getValue().get(0);
			Double maxY = upperCorner.getValue().get(1);
						
			if (boundingBoxFilter.isSetContainMode()) {
				if (minX >= boundingBox.getLowerCorner().getX() &&
						minY >= boundingBox.getLowerCorner().getY() &&
						maxX <= boundingBox.getUpperCorner().getX() &&
						maxY <= boundingBox.getUpperCorner().getY())
					return false;
				else
					return true;
			}
			
			else if (boundingBoxFilter.isSetOverlapMode()) {
				if (minX > boundingBox.getUpperCorner().getX() ||
						maxX < boundingBox.getLowerCorner().getX() ||
						minY > boundingBox.getUpperCorner().getY() ||
						maxY < boundingBox.getLowerCorner().getY())
					return true;
				else 
					return false;
			}
		}
		
		return false;
	}
	
	public BoundingVolume getFilterState() {
		return boundingBox;
	}
}
