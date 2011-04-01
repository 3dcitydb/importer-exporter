package de.tub.citydb.config.project.general;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.citygml4j.geometry.Matrix;

@XmlType(name="TransformationMatrixType", propOrder={
		"value"
})
public class TransformationMatrix {
	@XmlValue
	private List<Double> value;

	public TransformationMatrix() {
		value = Matrix.identity(3, 4).toRowPackedList();
	}

	public List<Double> getValue() {
		if (value == null)
			value = new ArrayList<Double>();

		return value;
	}

	public void setValue(List<Double> value) {
		this.value = value;
	}

	public boolean isSetValue() {
		return value != null && !value.isEmpty();
	}

	public Matrix toMatrix3x4() {
		Matrix matrix = null;

		if (isSetValue() && value.size() == 12)
			matrix = new Matrix(value, 3);
		else {
			matrix = Matrix.identity(3, 4);
			value = matrix.toRowPackedList();
		}

		return matrix;
	}
	
	public Matrix toMatrix4x4() {
		Matrix matrix = new Matrix(4, 4);
		matrix.setMatrix(0, 2, 0, 3, toMatrix3x4());
		
		matrix.set(3, 0, 0);
		matrix.set(3, 1, 0);
		matrix.set(3, 2, 0);
		matrix.set(3, 3, 1);
		
		return matrix;
	}

}
