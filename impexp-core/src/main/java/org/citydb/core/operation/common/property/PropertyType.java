package org.citydb.core.operation.common.property;

public enum PropertyType {
    COMPLEX(-1),
    INTEGER(0),
    DOUBLE(1),
    STRING(2),
    DATE(3),
    URI(4),
    GEOMETRY(5),
    SURFACE_GEOMETRY(6),
    IMPLICIT_GEOMETRY(7),
    GRID_COVERAGE(8),
    APPEARANCE(9),
    DYNAMIZER(10),
    FEATURE(11),
    CODE_LIST(12),
    MEASURE(13),
    JSON(14),
    XML_CONTENT(15);


    private final int value;

    private PropertyType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
