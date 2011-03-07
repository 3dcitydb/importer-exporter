package de.tub.citydb.db.cache.model;

public enum CacheTableModelEnum {
	// provide a unique id for the tmp table
	// that does not extend 6 chars!
	GMLID_FEATURE("IDF"),
	GMLID_GEOMETRY("IDG"),
	SURFACE_GEOMETRY("SG"),
	LINEAR_RING("LR"),
	BASIC("BA"),
	TEXTUREPARAM("TP"),
	TEXTUREASSOCIATION("TA"),
	TEXTURE_FILE("TF"),
	LIBRARY_OBJECT("LO"),
	DEPRECATED_MATERIAL("DP"),
	GROUP_TO_CITYOBJECT("GTC");

	private final String value;

	CacheTableModelEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}
