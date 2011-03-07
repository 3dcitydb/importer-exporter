package de.tub.citydb.db.xlink;

public enum DBXlinkExternalFileEnum {
	UNDEFINED,
	TEXTURE_IMAGE,
	WORLD_FILE,
	LIBRARY_OBJECT;

	public static DBXlinkExternalFileEnum fromInt(int i) {
		for (DBXlinkExternalFileEnum c : DBXlinkExternalFileEnum.values()) {
			if (c.ordinal() == i) {
				return c;
			}
		}

		return UNDEFINED;
	}
}
