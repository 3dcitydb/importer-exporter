package de.tub.citydb.db.xlink;

public class DBXlinkLibraryObject implements DBXlink {
	long id;
	String fileURI;

	public DBXlinkLibraryObject(long id, String fileURI) {
		this.id = id;
		this.fileURI = fileURI;
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
		return DBXlinkEnum.LIBRARY_OBJECT;
	}

}
