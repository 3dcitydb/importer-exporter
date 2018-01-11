package org.citydb.modules.kml.util;

import java.sql.SQLException;

import org.citydb.config.geometry.GeometryObject;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.geometry.Point;

public class AffineTransformer {
	private final Matrix transformationMatrix;
	private final Point referencePoint;
	private final int targetSrid;

	public AffineTransformer(Matrix transformationMatrix, Point referencePoint, int targetSrid) {
		this.transformationMatrix = transformationMatrix;
		this.referencePoint = referencePoint;
		this.targetSrid = targetSrid;
	}

	public GeometryObject applyTransformation(GeometryObject geomObj) throws SQLException {
		if (geomObj.getSrid() == 0) {
			for (int i = 0; i < geomObj.getNumElements(); i++) {
				double[] originalCoords = geomObj.getCoordinates(i);
				for (int j = 0; j < originalCoords.length; j += 3) {
					double[] vals = new double[]{originalCoords[j], originalCoords[j+1], originalCoords[j+2], 1};
					Matrix v = new Matrix(vals, 4);
					v = transformationMatrix.times(v);
					originalCoords[j] = v.get(0, 0) + referencePoint.getX();
					originalCoords[j+1] = v.get(1, 0) + referencePoint.getY();
					originalCoords[j+2] = v.get(2, 0) + referencePoint.getZ();
				}
			}

			// implicit geometries are not associated with a crs (srid = 0)
			// after transformation into world coordinates, we therefore have to assign the database crs
			geomObj.setSrid(targetSrid);
		}

		return geomObj;
	}

}
