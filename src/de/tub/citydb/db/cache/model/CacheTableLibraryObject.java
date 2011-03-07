package de.tub.citydb.db.cache.model;


public class CacheTableLibraryObject extends CacheTableModel {
	public static CacheTableLibraryObject instance = null;
	
	private CacheTableLibraryObject() {		
	}
	
	public synchronized static CacheTableLibraryObject getInstance() {
		if (instance == null)
			instance = new CacheTableLibraryObject();
		
		return instance;
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.LIBRARY_OBJECT;
	}
	
	@Override
	protected String getColumns() {
		return "(ID NUMBER, " +
		"FILE_URI VARCHAR2(1000))";
	}
}
