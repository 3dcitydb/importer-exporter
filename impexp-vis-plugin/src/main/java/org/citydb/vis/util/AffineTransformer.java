/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.vis.util;

import org.citydb.config.geometry.GeometryObject;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.geometry.Point;

import java.sql.SQLException;

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
