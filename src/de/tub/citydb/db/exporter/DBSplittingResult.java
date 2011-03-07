package de.tub.citydb.db.exporter;

import org.citygml4j.model.citygml.CityGMLClass;

public class DBSplittingResult {
	private long primaryKey;
	private CityGMLClass cityObjectType;
	private boolean checkIfAlreadyExported;

	public DBSplittingResult(long primaryKey, CityGMLClass cityObjectType) {
		this.primaryKey = primaryKey;
		this.cityObjectType = cityObjectType;
		checkIfAlreadyExported = false;
	}

	public long getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(long primaryKey) {
		this.primaryKey = primaryKey;
	}

	public CityGMLClass getCityObjectType() {
		return cityObjectType;
	}

	public void setCityObjectType(CityGMLClass cityObjectType) {
		this.cityObjectType = cityObjectType;
	}

	public boolean isCheckIfAlreadyExported() {
		return checkIfAlreadyExported;
	}

	public void setCheckIfAlreadyExported(boolean checkIfAlreadyExported) {
		this.checkIfAlreadyExported = checkIfAlreadyExported;
	}
		
}
