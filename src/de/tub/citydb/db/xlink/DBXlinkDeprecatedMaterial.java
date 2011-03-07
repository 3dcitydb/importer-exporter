package de.tub.citydb.db.xlink;


public class DBXlinkDeprecatedMaterial implements DBXlink {
	private long id;
	private String gmlId;
	private long surfaceGeometryId;

	public DBXlinkDeprecatedMaterial(long id, String gmlId, long surfaceGeometryId) {
		this.id = id;
		this.gmlId = gmlId;
		this.surfaceGeometryId = surfaceGeometryId;
	}

	public long getId() {
		return id;
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

	public long getSurfaceGeometryId() {
		return surfaceGeometryId;
	}

	public void setSurfaceGeometryId(long surfaceGeometryId) {
		this.surfaceGeometryId = surfaceGeometryId;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.DEPRECATED_MATERIAL;
	}

}
