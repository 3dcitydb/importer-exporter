package de.tub.citydb.config.project.general;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AffineTransformationType", propOrder={
		"transformationMatrix"
})
public class AffineTransformation {
	@XmlAttribute(required=true)
	private Boolean useAffineTransformation = false;
	private TransformationMatrix transformationMatrix;

	public AffineTransformation() {
		transformationMatrix = new TransformationMatrix();
	}

	public boolean isSetUseAffineTransformation() {
		if (useAffineTransformation != null)
			return useAffineTransformation.booleanValue();

		return false;
	}

	public Boolean getUseAffineTransformation() {
		return useAffineTransformation;
	}

	public void setUseAffineTransformation(Boolean useAffineTransformation) {
		this.useAffineTransformation = useAffineTransformation;
	}

	public TransformationMatrix getTransformationMatrix() {
		return transformationMatrix;
	}

	public void setTransformationMatrix(TransformationMatrix transformationMatrix) {
		if (transformationMatrix != null)
			this.transformationMatrix = transformationMatrix;
	}
	
	public boolean isSetTransformationMatrix() {
		return transformationMatrix != null;
	}
	
}
