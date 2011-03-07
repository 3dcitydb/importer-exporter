package de.tub.citydb.db.cache.model;


public class CacheTableBasic extends CacheTableModel {
	public static CacheTableBasic instance = null;

	private CacheTableBasic() {		
	}

	public synchronized static CacheTableBasic getInstance() {
		if (instance == null)
			instance = new CacheTableBasic();

		return instance;
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.BASIC;
	}

	@Override
	protected String getColumns() {
		return "(ID NUMBER, FROM_TABLE NUMBER(3), " +
				"GMLID VARCHAR2(256), " +
				"TO_TABLE NUMBER(3), " +
				"ATTRNAME VARCHAR(50))";
	}

}
