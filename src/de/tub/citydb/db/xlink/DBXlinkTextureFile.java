package de.tub.citydb.db.xlink;

public class DBXlinkTextureFile implements DBXlink {
	long id;
	String fileURI;
	DBXlinkTextureFileEnum type;

	public DBXlinkTextureFile(long id, String fileURI, DBXlinkTextureFileEnum type) {
		this.id = id;
		this.fileURI = fileURI;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFileURI() {
		return fileURI;
	}

	public void setFileURI(String fileURI) {
		this.fileURI = fileURI;
	}

	public DBXlinkTextureFileEnum getType() {
		return type;
	}

	public void setType(DBXlinkTextureFileEnum type) {
		this.type = type;
	}

	@Override
	public String getGmlId() {
		// we do not have a gml:id, but fileURI is our identifier
		return fileURI;
	}

	@Override
	public void setGmlId(String gmlid) {
		// we do not need this here since we are not relying on gml:ids
	}

	@Override
	public DBXlinkEnum getXlinkType() {
		return DBXlinkEnum.TEXTURE_FILE;
	}

}
