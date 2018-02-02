package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsLike")
@XmlType(name="LikeOperatorType", propOrder={
		"literal"
})
public class LikeOperator extends AbstractComparisonOperator {
	@XmlAttribute
	private String wildCard = "*";
	@XmlAttribute
	private String singleCharacter = ".";
	@XmlAttribute
	private String escapeCharacter = "\\";
	@XmlElement(required = true)
	private String literal;
	
	public boolean isSetWildCard() {
		return wildCard != null;
	}
	
	public String getWildCard() {
		return wildCard;
	}

	public void setWildCard(String wildCard) {
		this.wildCard = wildCard;
	}
	
	public boolean isSetSingleCharacter() {
		return singleCharacter != null;
	}

	public String getSingleCharacter() {
		return singleCharacter;
	}

	public void setSingleCharacter(String singleCharacter) {
		this.singleCharacter = singleCharacter;
	}

	public boolean isSetEscapeCharacter() {
		return escapeCharacter != null;
	}
	
	public String getEscapeCharacter() {
		return escapeCharacter;
	}

	public void setEscapeCharacter(String escapeCharacter) {
		this.escapeCharacter = escapeCharacter;
	}

	public boolean isSetLiteral() {
		return literal != null;
	}
	
	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}
	
	@Override
	public void reset() {
		wildCard = "*";
		singleCharacter = ".";
		escapeCharacter = "\\";
		literal = null;
		super.reset();
	}

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.LIKE;
	}

}
