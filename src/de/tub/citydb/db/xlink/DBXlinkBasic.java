package de.tub.citydb.db.xlink;

import de.tub.citydb.db.DBTableEnum;

public class DBXlinkBasic implements DBXlink {
	private long id;
	private DBTableEnum fromTable;
	private String gmlId;
	private DBTableEnum toTable;
	private String attrName;

	public DBXlinkBasic(long id, DBTableEnum fromTable, String gmlId, DBTableEnum toTable) {
		this.id = id;
		this.fromTable = fromTable;
		this.gmlId = gmlId;
		this.toTable = toTable;
	}

	public long getId() {
		return id;
	}

	public DBTableEnum getFromTable() {
		return fromTable;
	}

	public void setFromTable(DBTableEnum fromTable) {
		this.fromTable = fromTable;
	}

	public DBTableEnum getToTable() {
		return toTable;
	}

	public void setToTable(DBTableEnum toTable) {
		this.toTable = toTable;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.BASIC;
	}
}
