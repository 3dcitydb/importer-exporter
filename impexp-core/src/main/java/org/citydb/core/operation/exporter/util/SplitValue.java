package org.citydb.core.operation.exporter.util;

public class SplitValue {
    String[] values;

    SplitValue(int length) {
        values = new String[length];
    }

    public String result(int i) {
        if (i < 0 || i >= values.length) {
            throw new IndexOutOfBoundsException("No split result " + i);
        }

        return values[i];
    }

    public Double asDouble(int i) {
        try {
            return Double.parseDouble(result(i));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer asInteger(int i) {
        try {
            return Integer.parseInt(result(i));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
