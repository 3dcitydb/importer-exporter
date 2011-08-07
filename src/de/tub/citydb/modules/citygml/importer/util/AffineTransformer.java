package de.tub.citydb.modules.citygml.importer.util;

import java.util.List;

import org.citygml4j.geometry.Matrix;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.general.AffineTransformation;

public class AffineTransformer {
	private final Matrix matrix4x4;
	private final Matrix matrix3x4;
	private final Matrix inverse4x4;
	private final Matrix inverse2x2;
	
	public AffineTransformer(Config config) throws Exception {
		AffineTransformation pref = config.getProject().getImporter().getAffineTransformation();
		matrix4x4 = pref.getTransformationMatrix().toMatrix4x4();
		matrix3x4 = matrix4x4.getMatrix(3, 4);

		inverse4x4 = matrix4x4.inverse();
		inverse2x2 = inverse4x4.getMatrix(2, 2);
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
		Matrix tmp = new Matrix(4, 4);
		tmp.setMatrix(0, 2, 0, 3, worldToTexture);
		return tmp.times(inverse4x4).getMatrix(3, 4);
	}
	
	public Matrix transformImplicitGeometryTransformationMatrix(Matrix transformationMatrix) {
		return transformationMatrix.times(matrix4x4);
	}
	
}
