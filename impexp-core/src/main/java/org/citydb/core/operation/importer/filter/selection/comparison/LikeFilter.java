/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.importer.filter.selection.comparison;

import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.core.query.filter.FilterException;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LikeFilter {
    private final String literal;
    private final Matcher matcher;

    private char wildCard = '*';
    private char singleCharacter = '.';
    private char escapeCharacter = '\\';

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

    public boolean isSatisfiedBy(AbstractFeature feature) {
        for (Code code : feature.getName()) {
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
