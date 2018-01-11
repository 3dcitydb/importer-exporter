package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.PredicateName;

@XmlType(name="AbstractSpatialOperatorType", propOrder={
		"valueReference"
})
@XmlSeeAlso({
	BBOXOperator.class,
	AbstractBinarySpatialOperator.class
})
public abstract class AbstractSpatialOperator extends AbstractPredicate {
	@XmlElement
	private String valueReference;
	
	public abstract SpatialOperatorName getOperatorName();
	
	public boolean isSetValueReference() {
		return valueReference != null;
	}

	public String getValueReference() {
		return valueReference;
	}

	public void setValueReference(String valueReference) {
		this.valueReference = valueReference;
	}
		
	@Override
	public void reset() {
		valueReference = null;
	}
	
	@Override
	public PredicateName getPredicateName() {
		return PredicateName.SPATIAL_OPERATOR;
	}
}
