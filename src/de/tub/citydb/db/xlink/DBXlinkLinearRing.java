package de.tub.citydb.db.xlink;

public class DBXlinkLinearRing implements DBXlink {
	private String gmlId;
	private String parentGmlId;
	private int ringId;

	public DBXlinkLinearRing(String gmlId, String parentGmlId, int ringId) {
		this.gmlId = gmlId;
		this.parentGmlId = parentGmlId;
		this.ringId = ringId;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getParentGmlId() {
		return parentGmlId;
	}

	public void setParentGmlId(String parentGmlId) {
		this.parentGmlId = parentGmlId;
	}

	public int getRingId() {
		return ringId;
	}

	public void setRingId(int ringId) {
		this.ringId = ringId;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.LINEAR_RING;
	}

}
