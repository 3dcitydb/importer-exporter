package org.citydb.query.builder.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLTokenParser {
    private final Matcher matcher;

    private final String[] invalidTokens = {
            "del_.*?\\(.*?\\)",
            "delete_.*?\\(.*?\\)",
            "cleanup_.*?\\(.*?\\)",
            ";"
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