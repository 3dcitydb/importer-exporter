package de.tub.citydb.db.xlink;

public class DBXlinkTextureAssociation implements DBXlink {
	private long surfaceDataId;
	private long surfaceGeometryId;
	private String gmlId;

	public DBXlinkTextureAssociation(long surfaceDataId, long surfaceGeometryId, String gmlId) {
		this.surfaceDataId = surfaceDataId;
		this.surfaceGeometryId = surfaceGeometryId;
		this.gmlId = gmlId;
	}

	public long getSurfaceDataId() {
		return surfaceDataId;
	}

	public void setSurfaceDataId(long surfaceDataId) {
		this.surfaceDataId = surfaceDataId;
	}

	public long getSurfaceGeometryId() {
		return surfaceGeometryId;
	}

	public void setSurfaceGeometryId(long surfaceGeometryId) {
		this.surfaceGeometryId = surfaceGeometryId;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTUREASSOCIATION;
	}

}
