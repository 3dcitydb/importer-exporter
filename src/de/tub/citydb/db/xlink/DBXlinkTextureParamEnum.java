package de.tub.citydb.db.xlink;

public enum DBXlinkTextureParamEnum {
	UNDEFINED,
	X3DMATERIAL,
	GEOREFERENCEDTEXTURE,
	TEXCOORDGEN,
	TEXCOORDLIST,
	XLINK_TEXTUREASSOCIATION;

	public static DBXlinkTextureParamEnum fromInt(int i) {
		for (DBXlinkTextureParamEnum c : DBXlinkTextureParamEnum.values()) {
			if (c.ordinal() == i) {
				return c;
			}
		}

		return UNDEFINED;
	}
}
