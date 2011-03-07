package de.tub.citydb.filter.feature;

import java.sql.SQLException;
import java.util.List;

import org.citygml4j.geometry.BoundingVolume;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.gml.DirectPosition;
import org.citygml4j.model.gml.Envelope;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.FilterBoundingBox;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.filter.FilterSRSType;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;
import de.tub.citydb.util.DBUtil;

public class BoundingBoxFilter implements Filter<Envelope> {
	private final FilterConfig filter;
	private final DBUtil dbUtil;
	
	private boolean isActive;
	private FilterBoundingBox boundingBoxFilter;
	private BoundingVolume boundingBox;
	private int dbSrid;
	
	public BoundingBoxFilter(Config config, FilterMode mode, DBUtil dbUtil) {	
		this.dbUtil = dbUtil;

		if (mode == FilterMode.EXPORT)
			filter = config.getProject().getExporter().getFilter();
		else
			filter = config.getProject().getImporter().getFilter();
			
		dbSrid = config.getInternal().getDbSrid();		
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
				
				// check whether we have to transform coordinate values
				if (boundingBoxFilter.getSRS() != FilterSRSType.SAME_AS_IN_DB) {
					try {
						// todo: if dbSrid == 0, then we could not read SRID from database
						// in that case, transformation will certainly fail...
						
						boundingBox = dbUtil.transformBBox(boundingBox, boundingBoxFilter.getSRS().getSrid(), dbSrid);
					} catch (SQLException e) {
						//
					}					
				}
				
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
			if (!envelope.isSetLowerCorner() || !envelope.isSetUpperCorner())
				return true;
			
			DirectPosition lowerCorner = envelope.getLowerCorner();
			DirectPosition upperCorner = envelope.getUpperCorner();

			if (!lowerCorner.isSetValue() || !upperCorner.isSetValue())
				return true;
			
			List<Double> lowerCornerValue = lowerCorner.getValue();
			List<Double> upperCornerValue = upperCorner.getValue();
			
			if (lowerCornerValue.size() < 2 || upperCornerValue.size() < 2)
				return true;
			
			Double minX = lowerCornerValue.get(0);
			Double minY = lowerCornerValue.get(1);

			Double maxX = upperCornerValue.get(0);
			Double maxY = upperCornerValue.get(1);
						
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
				if (minX >= boundingBox.getUpperCorner().getX() ||
						maxX <= boundingBox.getLowerCorner().getX() ||
						minY >= boundingBox.getUpperCorner().getY() ||
						maxY <= boundingBox.getLowerCorner().getY())
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
