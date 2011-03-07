package de.tub.citydb.db.xlink;


public class DBXlinkGroupToCityObject implements DBXlink {
	private long groupId;
	private String gmlId;
	private boolean isParent;
	private String role;

	public DBXlinkGroupToCityObject(long groupId, String gmlId, boolean isParent) {
		this.groupId = groupId;
		this.gmlId = gmlId;
		this.isParent = isParent;
	}
	
	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String getGmlId() {
		return gmlId;
	}

	@Override
	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.GROUP_TO_CITYOBJECT;
	}

}
