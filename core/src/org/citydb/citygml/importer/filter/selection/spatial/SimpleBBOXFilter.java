package org.citydb.citygml.importer.filter.selection.spatial;

import java.sql.SQLException;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.Position;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.selection.spatial.SimpleBBOXMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.citygml.core.AbstractCityObject;

public class SimpleBBOXFilter {
	private BoundingBox bbox;
	private SimpleBBOXMode mode;

	public SimpleBBOXFilter(BBOXOperator bboxOperator, SimpleBBOXMode mode) throws FilterException {
		if (bboxOperator == null || !bboxOperator.isSetEnvelope())
			throw new FilterException("The bbox operator must not be null.");

		bbox = (BoundingBox)bboxOperator.getEnvelope();
		this.mode = mode;
	}

	public void transform(DatabaseSrs targetSrs, AbstractDatabaseAdapter databaseAdapter) throws FilterException {
		if (!targetSrs.isSupported())
			throw new FilterException("The reference system " + targetSrs.getDescription() + " is not supported.");

		DatabaseSrs bboxSrs = bbox.isSetSrs() ? bbox.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();
		if (targetSrs.getSrid() == bboxSrs.getSrid())
			return;

		// convert extent into polygon
		GeometryObject extentObj = GeometryObject.createPolygon(new double[]{
				bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(),
				bbox.getUpperCorner().getX(), bbox.getLowerCorner().getY(),
				bbox.getUpperCorner().getX(), bbox.getUpperCorner().getY(),
				bbox.getLowerCorner().getX(), bbox.getUpperCorner().getY(),
				bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(),
		}, 2, bboxSrs.getSrid());

		// transform polygon to new srs
		GeometryObject transformedBbox = null;
		try {
			transformedBbox = databaseAdapter.getUtil().transform(extentObj, targetSrs);
			if (transformedBbox == null)
				throw new FilterException("Failed to transform tiling extent to SRS " + targetSrs.getDescription() + ".");
		} catch (SQLException e) {
			throw new FilterException("Failed to transform tiling extent to SRS " + targetSrs.getDescription() + ".", e);
		}

		// create new extent from transformed polygon
		double[] coordinates = transformedBbox.getCoordinates(0);		
		bbox = new BoundingBox(
				new Position(Math.min(coordinates[0], coordinates[6]), Math.min(coordinates[1], coordinates[3])),
				new Position(Math.max(coordinates[2], coordinates[4]), Math.max(coordinates[5], coordinates[7])),
				targetSrs
				);	}

	public boolean isSatisfiedBy(AbstractCityObject cityObject) throws FilterException {
		if (!cityObject.isSetBoundedBy() || !cityObject.getBoundedBy().isSetEnvelope())
			return false;

		org.citygml4j.geometry.BoundingBox candidate = cityObject.getBoundedBy().getEnvelope().toBoundingBox();
		if (candidate == null)
			return false;

		if (mode == SimpleBBOXMode.WITHIN) {
			return (candidate.getLowerCorner().getX() >= bbox.getLowerCorner().getX() &&
					candidate.getLowerCorner().getY() >= bbox.getLowerCorner().getY() &&
					candidate.getUpperCorner().getX() <= bbox.getUpperCorner().getX() &&
					candidate.getUpperCorner().getY() <= bbox.getUpperCorner().getY());
		} else {
			return !(candidate.getLowerCorner().getX() >= bbox.getUpperCorner().getX() ||
					candidate.getLowerCorner().getY() >= bbox.getUpperCorner().getY() ||
					candidate.getUpperCorner().getX() <= bbox.getLowerCorner().getX() ||
					candidate.getUpperCorner().getY() <= bbox.getLowerCorner().getY());
		}
	}

}
