package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractBinaryComparisonOperatorType", propOrder={
		"literal"
})
@XmlSeeAlso({
	EqualToOperator.class,
	NotEqualToOperator.class,
	LessThanOperator.class,
	LessThanOrEqualToOperator.class,
	GreaterThanOperator.class,
	GreaterThanOrEqualToOperator.class
})
public abstract class AbstractBinaryComparisonOperator extends AbstractComparisonOperator {
	@XmlAttribute(required = false)
	private Boolean matchCase = true;
	@XmlElement(required = true)
	private String literal;

	public boolean isSetLiteral() {
		return literal != null;
	}
	
	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}
	
	public boolean isMatchCase() {
		return matchCase;
	}

	public void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
	}
	
	@Override
	public void reset() {
		matchCase = true;
		literal = null;
		super.reset();
	}
	
}
