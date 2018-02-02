package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.geometry.AbstractGeometry;

@XmlType(name="AbstractBinarySpatialOperatorType", propOrder={
		"operand"
})
@XmlSeeAlso({
	EqualsOperator.class,
	DisjointOperator.class,
	TouchesOperator.class,
	WithinOperator.class,
	OverlapsOperator.class,
	IntersectsOperator.class,
	ContainsOperator.class,
	AbstractDistanceOperator.class
})
public abstract class AbstractBinarySpatialOperator extends AbstractSpatialOperator {
	@XmlElementRefs({
		@XmlElementRef(type=FileReference.class),
		@XmlElementRef(type=AbstractGeometry.class)
	})
	private Object operand;
	
	public abstract SpatialOperatorName getOperatorName();

	public boolean isSetFileReference() {
		return operand instanceof FileReference;
	}

	public String getFileReference() {
		return isSetFileReference() ? ((FileReference)operand).getValue() : null;
	}

	public void setFileReference(String file) {
		this.operand = file;
	}

	public boolean isSetSpatialOperand() {
		return operand instanceof AbstractGeometry;
	}

	public AbstractGeometry getSpatialOperand() {
		return isSetSpatialOperand() ? (AbstractGeometry)operand : null;
	}

	public void setSpatialOperand(AbstractGeometry operand) {
		this.operand = operand;
	}
	
	@Override
	public void reset() {
		operand = null;
		super.reset();
	}
	
}
