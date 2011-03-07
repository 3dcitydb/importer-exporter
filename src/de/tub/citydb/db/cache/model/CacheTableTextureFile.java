package de.tub.citydb.db.cache.model;


public class CacheTableTextureFile extends CacheTableModel {
	public static CacheTableTextureFile instance = null;
	
	private CacheTableTextureFile() {		
	}
	
	public synchronized static CacheTableTextureFile getInstance() {
		if (instance == null)
			instance = new CacheTableTextureFile();
		
		return instance;
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.TEXTURE_FILE;
	}
	
	@Override
	protected String getColumns() {
		return "(ID NUMBER, " +
		"FILE_URI VARCHAR2(1000), " +
		"TYPE NUMBER(3))";
	}
}
