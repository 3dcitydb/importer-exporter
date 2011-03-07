package de.tub.citydb.db.xlink;

public interface DBXlink {
	public DBXlinkEnum getXlinkType();
	public String getGmlId();
	public void setGmlId(String gmlId);
}
