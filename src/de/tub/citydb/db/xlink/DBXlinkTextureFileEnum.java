package de.tub.citydb.db.xlink;

public enum DBXlinkTextureFileEnum {
	UNDEFINED,
	TEXTURE_IMAGE,
	WORLD_FILE;

	public static DBXlinkTextureFileEnum fromInt(int i) {
		for (DBXlinkTextureFileEnum c : DBXlinkTextureFileEnum.values()) {
			if (c.ordinal() == i) {
				return c;
			}
		}

		return UNDEFINED;
	}
}
