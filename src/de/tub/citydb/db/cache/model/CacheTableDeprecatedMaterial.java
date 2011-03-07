package de.tub.citydb.db.cache.model;


public class CacheTableDeprecatedMaterial extends CacheTableModel {
	public static CacheTableDeprecatedMaterial instance = null;
	
	private CacheTableDeprecatedMaterial() {		
	}
	
	public synchronized static CacheTableDeprecatedMaterial getInstance() {
		if (instance == null)
			instance = new CacheTableDeprecatedMaterial();
		
		return instance;
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.DEPRECATED_MATERIAL;
	}
	
	@Override
	protected String getColumns() {
		return "(ID NUMBER," +
		"GMLID VARCHAR2(256), " +
		"SURFACE_GEOMETRY_ID NUMBER)";
	}
}
