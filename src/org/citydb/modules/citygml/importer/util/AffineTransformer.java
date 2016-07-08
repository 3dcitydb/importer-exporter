/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.modules.citygml.importer.util;

import java.util.List;

import org.citydb.config.Config;
import org.citydb.config.project.general.TransformationMatrix;
import org.citygml4j.geometry.Matrix;

public class AffineTransformer {
	private final Matrix matrix4x4;
	private final Matrix matrix3x4;
	private final Matrix inverse4x4;
	private final Matrix inverse2x2;
	
	public AffineTransformer(Config config) throws Exception {
		matrix4x4 = toMatrix4x4(config.getProject().getImporter().getAffineTransformation().getTransformationMatrix());
		matrix3x4 = matrix4x4.getMatrix(3, 4);
		inverse4x4 = matrix4x4.inverse();
		inverse2x2 = inverse4x4.getMatrix(2, 2);
		
		// remove translation from matrix4x4 since it is used only
		// in the context of implicit geometries for which translation
		// is denoted by their reference point
		matrix4x4.set(0, 3, 0);
		matrix4x4.set(1, 3, 0);
		matrix4x4.set(2, 3, 0);
	}
	
	public void transformCoordinates(List<Double> points) {
		for (int i = 0; i < points.size(); i += 3) {
			double[] vals = new double[]{ points.get(i), points.get(i+1), points.get(i+2), 1};
			Matrix v = new Matrix(vals, 4);

			v = matrix3x4.times(v);
			points.set(i, v.get(0, 0));
			points.set(i+1, v.get(1, 0));
			points.set(i+2, v.get(2, 0));
		}
	}
	
	public Matrix transformGeoreferencedTextureOrientation(Matrix orientation) {
		return orientation.times(inverse2x2);
	}
	
	public Matrix transformWorldToTexture(Matrix worldToTexture) {
		Matrix tmp = Matrix.identity(4, 4);
		tmp.setMatrix(0, 2, 0, 3, worldToTexture);
		return tmp.times(inverse4x4).getMatrix(3, 4);
	}
	
	public Matrix transformImplicitGeometryTransformationMatrix(Matrix transformationMatrix) {
		return matrix4x4.times(transformationMatrix);
	}
	
	public Matrix toMatrix4x4(TransformationMatrix transformationMatrix) {
		Matrix tmp = transformationMatrix.isSetValue() && transformationMatrix.getValue().size() == 12 ? 
				new Matrix(transformationMatrix.getValue(), 3) : Matrix.identity(3, 4);
		
		Matrix matrix = new Matrix(4, 4);
		matrix.setMatrix(0, 2, 0, 3, tmp);
		
		matrix.set(3, 0, 0);
		matrix.set(3, 1, 0);
		matrix.set(3, 2, 0);
		matrix.set(3, 3, 1);
		
		return matrix;
	}
	
}
