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

package org.citydb.core.query.builder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLTokenParser {
    private final Matcher matcher;

    private final String[] invalidTokens = {
            "del_.*?\\(.*?\\)",
            "delete_.*?\\(.*?\\)",
            "cleanup_.*?\\(.*?\\)"
    };

    public SQLTokenParser() {
        matcher = Pattern.compile("((?:" + String.join(")|(?:", invalidTokens) + "))",
                Pattern.UNICODE_CHARACTER_CLASS | Pattern.MULTILINE).matcher("");
    }

    public List<String> getInvalidTokens(String select) {
        List<String> invalidTokens = new ArrayList<>();

        if (select != null) {
            matcher.reset(select);
            while (matcher.find())
                invalidTokens.add(matcher.group(1));
        }

        return invalidTokens;
    }

}