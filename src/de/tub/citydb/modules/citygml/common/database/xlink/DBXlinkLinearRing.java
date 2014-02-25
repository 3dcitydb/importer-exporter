package de.tub.citydb.modules.citygml.common.database.xlink;

public class DBXlinkLinearRing implements DBXlink {
	private String gmlId;
	private long parentId;
	
	public DBXlinkLinearRing(String gmlId, long parentId) {
		this.gmlId = gmlId;
		this.parentId = parentId;
	}
	
	@Override
	public String getGmlId() {
		return gmlId;
	}

	@Override
	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}
	
	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.LINEAR_RING;
	}

}
