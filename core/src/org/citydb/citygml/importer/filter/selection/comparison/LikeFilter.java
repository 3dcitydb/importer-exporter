package org.citydb.citygml.importer.filter.selection.comparison;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.basicTypes.Code;

public class LikeFilter {
	private final String literal;
	
	private char wildCard = '*';
	private char singleCharacter = '.';
	private char escapeCharacter = '\\';
	
	private Matcher matcher;
	
	public LikeFilter(LikeOperator likeOperator) throws FilterException {
		if (likeOperator == null || !likeOperator.isSetLiteral())
			throw new FilterException("The like operator must not be null.");
		
		literal = likeOperator.getLiteral();
		
		if (likeOperator.isSetWildCard())
			wildCard = likeOperator.getWildCard().charAt(0);
		
		if (likeOperator.isSetSingleCharacter())
			singleCharacter = likeOperator.getSingleCharacter().charAt(0);
		
		if (likeOperator.isSetEscapeCharacter())
			escapeCharacter = likeOperator.getEscapeCharacter().charAt(0);
		
		matcher = Pattern.compile(replaceWildCards(), Pattern.UNICODE_CHARACTER_CLASS | Pattern.MULTILINE).matcher("");
	}
	
	public boolean isSatisfiedBy(AbstractCityObject cityObject) {
		// TODO: add support for wild cards
		for (Code code : cityObject.getName()) {
			if (code.isSetValue()) {
				if (matcher.reset(code.getValue()).matches())
					return true;
			}
		}
		
		return false;
	}
	
	private String replaceWildCards() {
		boolean escapeWildCard = wildCard != '*' && singleCharacter != '*';
		boolean espaceSingleChar = wildCard != '.' && singleCharacter != '.';
		
		StringBuilder tmp = new StringBuilder();
		
		for (int offset = 0; offset < literal.length(); offset++) {
			char ch = literal.charAt(offset);
			
			if (ch == escapeCharacter) {
				// keep escaped chars as is
				tmp.append(ch);
				if (++offset < literal.length())
					tmp.append(literal.charAt(offset));
			} else if ((ch == '*' && escapeWildCard) || (ch == '.' && espaceSingleChar)) {
				// escape Java wild cards
				tmp.append(escapeCharacter);
				tmp.append(ch);
			} else if (ch == wildCard) {
				// replace user-defined wild card
				tmp.append(".*?");
			} else if (ch == singleCharacter) {
				// replace user-defined single char
				tmp.append('.');
			} else
				tmp.append(ch);
		}
		
		return tmp.toString();
	}
	
}
