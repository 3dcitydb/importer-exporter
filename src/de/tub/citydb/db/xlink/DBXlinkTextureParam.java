package de.tub.citydb.db.xlink;

public class DBXlinkTextureParam implements DBXlink {
	private long id;
	private String gmlId;
	private DBXlinkTextureParamEnum type;

	private boolean isTextureParameterization;
	private String texParamGmlId;
	private String worldToTexture;
	private String textureCoord;
	private String targetURI;
	private String texCoordListId;

	public DBXlinkTextureParam(long id, String gmlId, DBXlinkTextureParamEnum type) {
		this.id = id;
		this.gmlId = gmlId;
		this.type = type;
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

	public DBXlinkTextureParamEnum getType() {
		return type;
	}

	public void setType(DBXlinkTextureParamEnum type) {
		this.type = type;
	}

	public boolean isTextureParameterization() {
		return isTextureParameterization;
	}

	public void setTextureParameterization(boolean isTextureParameterization) {
		this.isTextureParameterization = isTextureParameterization;
	}

	public String getTexParamGmlId() {
		return texParamGmlId;
	}

	public void setTexParamGmlId(String texParamGmlId) {
		this.texParamGmlId = texParamGmlId;
	}

	public String getWorldToTexture() {
		return worldToTexture;
	}

	public void setWorldToTexture(String worldToTexture) {
		this.worldToTexture = worldToTexture;
	}

	public String getTextureCoord() {
		return textureCoord;
	}

	public void setTextureCoord(String textureCoord) {
		this.textureCoord = textureCoord;
	}

	public String getTargetURI() {
		return targetURI;
	}

	public void setTargetURI(String targetURI) {
		this.targetURI = targetURI;
	}

	public String getTexCoordListId() {
		return texCoordListId;
	}

	public void setTexCoordListId(String texCoordListId) {
		this.texCoordListId = texCoordListId;
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTUREPARAM;
	}
}
