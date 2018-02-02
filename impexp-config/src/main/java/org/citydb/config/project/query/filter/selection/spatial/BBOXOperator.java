package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.geometry.BoundingBox;

@XmlRootElement(name="bbox")
@XmlType(name="BBOXOperatorType", propOrder={
		"operand"
})
public class BBOXOperator extends AbstractSpatialOperator {	
	@XmlElementRefs({
		@XmlElementRef(type=FileReference.class),
		@XmlElementRef(type=BoundingBox.class)
	})
	private Object operand;
	
	public boolean isSetFileReference() {
		return operand instanceof FileReference;
	}

	public String getFileReference() {
		return isSetFileReference() ? ((FileReference)operand).getValue() : null;
	}

	public void setFileReference(String file) {
		this.operand = file;
	}

	public boolean isSetEnvelope() {
		return operand instanceof BoundingBox;
	}

	public BoundingBox getEnvelope() {
		return isSetEnvelope() ? (BoundingBox)operand : null;
	}

	public void setEnvelope(BoundingBox operand) {
		this.operand = operand;
	}
	
	@Override
	public void reset() {
		operand = null;
		super.reset();
	}
	
	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.BBOX;
	}
	
}
