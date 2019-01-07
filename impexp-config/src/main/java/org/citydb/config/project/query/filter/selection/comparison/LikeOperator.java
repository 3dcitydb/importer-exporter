/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
	@XmlAttribute
	private Boolean matchCase = true;
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

	public boolean isMatchCase() {
		return matchCase;
	}

	public void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
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
