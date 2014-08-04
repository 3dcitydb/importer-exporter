package org.citydb.modules.citygml.common.database.xlink;


public class DBXlinkLinearRing implements DBXlink {
	private String gmlId;
	private long parentId;
	private long ringNo;
	private boolean isReverse;

	public DBXlinkLinearRing(String gmlId, long parentId, long ringNo, boolean isReverse) {
		this.gmlId = gmlId;
		this.parentId = parentId;
		this.ringNo = ringNo;
		this.isReverse = isReverse;
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

	public long getRingNo() {
		return ringNo;
	}

	public void setRingNo(long ringNo) {
		this.ringNo = ringNo;
	}

	public boolean isReverse() {
		return isReverse;
	}

	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.LINEAR_RING;
	}

}
